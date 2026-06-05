const express = require("express");
const bcrypt = require("bcryptjs");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase } = require("../utils/http");
const { requireAuth } = require("../middleware/auth");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();
const BCRYPT_SALT_ROUNDS = Number(process.env.BCRYPT_SALT_ROUNDS || 12);
const USER_SELECT = `
  id,
  name,
  username,
  email,
  role,
  photo_url,
  is_active,
  created_at,
  updated_at,
  user_roles (
    roles (
      code
    )
  )
`;

async function replaceRoles(userId, roleCodes) {
  const codes = Array.from(new Set(roleCodes && roleCodes.length ? roleCodes : ["USER"]));
  const { data: roles, error: rolesError } = await supabase
    .from("roles")
    .select("id, code")
    .in("code", codes);

  if (rolesError) throw rolesError;
  if (!roles || roles.length !== codes.length) throw new Error("Invalid role.");

  const deleteResult = await supabase
    .from("user_roles")
    .delete()
    .eq("user_id", userId);

  if (deleteResult.error) throw deleteResult.error;

  const now = unixTimestampMs();
  const insertResult = await supabase
    .from("user_roles")
    .insert(roles.map((role) => ({
      user_id: userId,
      role_id: role.id,
      assigned_at: now
    })));

  if (insertResult.error) throw insertResult.error;
}

function toUserResponse(user) {
  const roles = (user.user_roles || [])
    .map((entry) => entry.roles && entry.roles.code)
    .filter(Boolean);

  return {
    id: user.id,
    name: user.name,
    username: user.username,
    email: user.email,
    role: user.role || "USER",
    roles,
    photo_url: user.photo_url,
    is_active: user.is_active,
    created_at: user.created_at,
    updated_at: user.updated_at
  };
}

async function getUserResponseById(userId) {
  const result = await supabase
    .from("users")
    .select(USER_SELECT)
    .eq("id", userId)
    .maybeSingle();

  if (result.error) {
    throw result.error;
  }

  if (!result.data) {
    return null;
  }

  return toUserResponse(result.data);
}

router.get("/", asyncRoute(async (_req, res) => {
  const result = await supabase
    .from("users")
    .select(USER_SELECT)
    .order("name", { ascending: true });

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  const users = (result.data || []).map(toUserResponse);

  return handleSupabase(res, { data: users, error: null });
}));

router.get("/:id(\\d+)", asyncRoute(async (req, res) => {
  const user = await getUserResponseById(Number(req.params.id));

  if (!user) {
    return res.status(404).json({ message: "User not found." });
  }

  return handleSupabase(res, { data: user, error: null });
}));

router.post("/", asyncRoute(async (req, res) => {
  const { name, username, email, password, photo_url: photoUrl } = req.body;
  const requestedRoles = Array.isArray(req.body.roles) ? req.body.roles : [req.body.role || "USER"];
  const primaryRole = requestedRoles[0] || "USER";

  if (!name || !username || !email || !password) {
    return res.status(400).json({ message: "Name, username, email and password are required." });
  }

  const now = unixTimestampMs();
  const passwordHash = await bcrypt.hash(password, BCRYPT_SALT_ROUNDS);
  const result = await supabase
    .from("users")
    .insert({
      name: name.trim(),
      username: username.trim(),
      email: email.trim(),
      password_hash: passwordHash,
      photo_url: photoUrl || null,
      role: primaryRole,
      is_active: req.body.is_active !== false,
      created_at: now,
      updated_at: now
    })
    .select()
    .single();

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  try {
    await replaceRoles(result.data.id, requestedRoles);
  } catch (error) {
    return res.status(400).json({ message: error.message });
  }

  return handleSupabase(res, { data: await getUserResponseById(result.data.id), error: null });
}));

router.put("/:id(\\d+)", asyncRoute(async (req, res) => {
  const userId = Number(req.params.id);
  const update = {
    name: req.body.name,
    username: req.body.username,
    email: req.body.email,
    photo_url: req.body.photo_url || null,
    role: req.body.role || (Array.isArray(req.body.roles) ? req.body.roles[0] : undefined),
    is_active: req.body.is_active !== undefined ? req.body.is_active : undefined,
    updated_at: unixTimestampMs()
  };

  Object.keys(update).forEach((key) => update[key] === undefined && delete update[key]);

  if (req.body.password) {
    update.password_hash = await bcrypt.hash(req.body.password, BCRYPT_SALT_ROUNDS);
  }

  const result = await supabase
    .from("users")
    .update(update)
    .eq("id", userId)
    .select()
    .single();

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  if (Array.isArray(req.body.roles) || req.body.role) {
    try {
      await replaceRoles(userId, Array.isArray(req.body.roles) ? req.body.roles : [req.body.role]);
    } catch (error) {
      return res.status(400).json({ message: error.message });
    }
  }

  return handleSupabase(res, { data: await getUserResponseById(userId), error: null });
}));

router.put("/:id(\\d+)/roles", asyncRoute(async (req, res) => {
  try {
    const userId = Number(req.params.id);
    await replaceRoles(userId, req.body.roles);
    return res.json(await getUserResponseById(userId));
  } catch (error) {
    return res.status(400).json({ message: error.message });
  }
}));

router.delete("/:id(\\d+)", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("users")
    .delete()
    .eq("id", Number(req.params.id));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return res.status(204).send();
}));

router.put("/me", requireAuth, asyncRoute(async (req, res) => {
  const userId = Number(req.user.sub);
  const { name, username, email, photo_url: photoUrl, password } = req.body;

  if (!name || !username || !email) {
    return res.status(400).json({ message: "Name, username and email are required." });
  }

  const cleanedEmail = email.trim();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cleanedEmail)) {
    return res.status(400).json({ message: "Invalid email." });
  }

  if (password && password.length < 8) {
    return res.status(400).json({ message: "Password must be at least 8 characters." });
  }

  const update = {
    name: name.trim(),
    username: username.trim(),
    email: cleanedEmail,
    photo_url: photoUrl || null,
    updated_at: unixTimestampMs()
  };

  if (password) {
    update.password_hash = await bcrypt.hash(password, BCRYPT_SALT_ROUNDS);
  }

  const result = await supabase
    .from("users")
    .update(update)
    .eq("id", userId)
    .select(`
      id,
      name,
      username,
      email,
      role,
      photo_url,
      is_active,
      created_at,
      updated_at,
      user_roles (
        roles (
          code
        )
      )
    `)
    .single();

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  return handleSupabase(res, { data: toUserResponse(result.data), error: null });
}));

module.exports = router;
