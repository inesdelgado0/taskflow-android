const express = require("express");
const bcrypt = require("bcryptjs");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase } = require("../utils/http");
const { requireAuth } = require("../middleware/auth");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();
const BCRYPT_SALT_ROUNDS = Number(process.env.BCRYPT_SALT_ROUNDS || 12);

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

router.get("/", asyncRoute(async (_req, res) => {
  const result = await supabase
    .from("users")
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
    .order("name", { ascending: true });

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  const users = (result.data || []).map(toUserResponse);

  return handleSupabase(res, { data: users, error: null });
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
