const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();

function normalizePlatform(platform) {
  return String(platform || "ANDROID").trim().toUpperCase();
}

router.get("/", asyncRoute(async (req, res) => {
  let query = supabase
    .from("device_tokens")
    .select("*")
    .order("updated_at", { ascending: false });

  if (req.query.user_id) {
    query = query.eq("user_id", Number(req.query.user_id));
  }

  if (req.query.active !== undefined) {
    query = query.eq("is_active", String(req.query.active).toLowerCase() !== "false");
  }

  const result = await query.limit(Number(req.query.limit || 100));
  return handleSupabase(res, result);
}));

router.post("/", asyncRoute(async (req, res) => {
  const userId = Number(req.body.user_id);
  const token = String(req.body.token || "").trim();
  const platform = normalizePlatform(req.body.platform);

  if (!userId || !token) {
    return res.status(400).json({ message: "user_id and token are required." });
  }

  if (!["ANDROID", "IOS", "WEB"].includes(platform)) {
    return res.status(400).json({ message: "platform must be ANDROID, IOS or WEB." });
  }

  const now = unixTimestampMs();
  const result = await supabase
    .from("device_tokens")
    .upsert({
      user_id: userId,
      token,
      platform,
      device_name: req.body.device_name || null,
      is_active: true,
      created_at: req.body.created_at || now,
      updated_at: now,
      last_seen_at: now
    }, { onConflict: "token" })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.delete("/:token", asyncRoute(async (req, res) => {
  const token = decodeURIComponent(req.params.token || "").trim();

  if (!token) {
    return res.status(400).json({ message: "token is required." });
  }

  const { error } = await supabase
    .from("device_tokens")
    .update({
      is_active: false,
      updated_at: unixTimestampMs()
    })
    .eq("token", token);

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

module.exports = router;
