const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase } = require("../utils/http");

const router = express.Router();

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

  const users = (result.data || []).map((user) => ({
    id: user.id,
    name: user.name,
    username: user.username,
    email: user.email,
    role: user.role || "USER",
    roles: (user.user_roles || [])
      .map((entry) => entry.roles && entry.roles.code)
      .filter(Boolean),
    photo_url: user.photo_url,
    is_active: user.is_active,
    created_at: user.created_at,
    updated_at: user.updated_at
  }));

  return handleSupabase(res, { data: users, error: null });
}));

module.exports = router;
