const express = require("express");
const jwt = require("jsonwebtoken");
const { supabase } = require("../config/supabase");
const { asyncRoute, sendData, sendNoContent } = require("../utils/http");
const { unixTimestamp } = require("../utils/time");

const router = express.Router();

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
    user: {
      ...user,
      is_active: user.is_active === true || user.is_active === 1 || user.is_active === "1"
    }
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

  const normalizedRoles = roles.length > 0 ? roles : [user.role || "USER"];

  return {
    ...user,
    role: normalizedRoles[0],
    roles: normalizedRoles
  };
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

  const now = unixTimestamp();
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
    .eq("is_active", "1")
    .maybeSingle();

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  if (!user || user.password_hash !== password) {
    return res.status(401).json({ message: "Invalid credentials." });
  }

  return sendData(res, toAuthResponse(await attachRoles(user)));
}));

router.post("/register", asyncRoute(async (req, res) => {
  const { name, username, email, password } = req.body;
  const requestedRoles = Array.isArray(req.body.roles)
    ? req.body.roles
    : [req.body.role || "USER"];
  const primaryRole = requestedRoles[0] || "USER";

  if (!name || !username || !email || !password) {
    return res.status(400).json({ message: "Name, username, email and password are required." });
  }

  const now = unixTimestamp();
  const { data: user, error } = await supabase
    .from("users")
    .insert({
      name: name.trim(),
      username: username.trim(),
      email: email.trim(),
      password_hash: password,
      role: primaryRole,
      is_active: "1",
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
