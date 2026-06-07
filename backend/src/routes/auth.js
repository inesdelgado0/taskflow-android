const express = require("express");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcryptjs");
const { supabase } = require("../config/supabase");
const { asyncRoute, sendData, sendNoContent } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");
const { extractRoleCodes } = require("../utils/users");

const router = express.Router();
const BCRYPT_SALT_ROUNDS = Number(process.env.BCRYPT_SALT_ROUNDS || 12);

function signToken(user) {
  return jwt.sign(
    {
      sub: String(user.id),
      email: user.email,
      role: user.role,
      roles: user.roles || [user.role]
    },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || "7d" }
  );
}

function toAuthResponse(user) {
  return {
    token: signToken(user),
    user
  };
}

async function attachRoles(user) {
  const { data, error } = await supabase
    .from("user_roles")
    .select("roles(code)")
    .eq("user_id", user.id);

  if (error) {
    throw error;
  }

  const roles = (data || [])
    .map((row) => row.roles && row.roles.code)
    .filter(Boolean);
  const fallbackRoles = extractRoleCodes(user);
  const normalizedRoles = roles.length > 0
    ? roles
    : (fallbackRoles.length > 0 ? fallbackRoles : ["USER"]);

  return {
    ...user,
    role: normalizedRoles[0],
    roles: normalizedRoles
  };
}

async function verifyPassword(password, passwordHash) {
  if (!passwordHash) {
    return false;
  }

  if (passwordHash.startsWith("$2a$") || passwordHash.startsWith("$2b$") || passwordHash.startsWith("$2y$")) {
    return bcrypt.compare(password, passwordHash);
  }

  return passwordHash === password;
}

async function assignRoles(userId, roleCodes) {
  const codes = Array.from(new Set(roleCodes && roleCodes.length ? roleCodes : ["USER"]));

  const { data: roles, error: rolesError } = await supabase
    .from("roles")
    .select("id, code")
    .in("code", codes);

  if (rolesError) {
    throw rolesError;
  }

  if (!roles || roles.length !== codes.length) {
    throw new Error("Invalid role.");
  }

  const now = unixTimestampMs();
  const { error } = await supabase
    .from("user_roles")
    .upsert(
      roles.map((role) => ({
        user_id: userId,
        role_id: role.id,
        assigned_at: now
      })),
      { onConflict: "user_id,role_id" }
    );

  if (error) {
    throw error;
  }
}

router.post("/login", asyncRoute(async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ message: "Email and password are required." });
  }

  const { data: user, error } = await supabase
    .from("users")
    .select("*")
    .eq("email", email.trim())
    .eq("is_active", true)
    .maybeSingle();

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  if (!user || !(await verifyPassword(password, user.password_hash))) {
    return res.status(401).json({ message: "Invalid credentials." });
  }

  return sendData(res, toAuthResponse(await attachRoles(user)));
}));

router.post("/register", asyncRoute(async (req, res) => {
  const { name, username, email, password } = req.body;
  const requestedRoles = Array.isArray(req.body.roles)
    ? req.body.roles
    : [req.body.role || "USER"];

  if (!name || !username || !email || !password) {
    return res.status(400).json({ message: "Name, username, email and password are required." });
  }

  const now = unixTimestampMs();
  const passwordHash = await bcrypt.hash(password, BCRYPT_SALT_ROUNDS);
  const { data: user, error } = await supabase
    .from("users")
    .insert({
      name: name.trim(),
      username: username.trim(),
      email: email.trim(),
      password_hash: passwordHash,
      is_active: true,
      created_at: now,
      updated_at: now
    })
    .select()
    .single();

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  try {
    await assignRoles(user.id, requestedRoles);
    return sendData(res, toAuthResponse(await attachRoles(user)), 201);
  } catch (assignError) {
    return res.status(400).json({ message: assignError.message });
  }
}));

router.post("/refresh", asyncRoute(async (req, res) => {
  const { refresh_token: token } = req.body;

  if (!token) {
    return res.status(400).json({ message: "Refresh token is required." });
  }

  try {
    const payload = jwt.verify(token, process.env.JWT_SECRET);
    const { data: user, error } = await supabase
      .from("users")
      .select("*")
      .eq("id", Number(payload.sub))
      .maybeSingle();

    if (error) {
      return res.status(400).json({ message: error.message });
    }

    if (!user) {
      return res.status(401).json({ message: "Invalid token." });
    }

    return sendData(res, toAuthResponse(await attachRoles(user)));
  } catch (_error) {
    return res.status(401).json({ message: "Invalid token." });
  }
}));

router.post("/logout", (_req, res) => {
  return sendNoContent(res);
});

module.exports = router;
