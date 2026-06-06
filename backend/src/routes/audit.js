const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();

router.get("/", asyncRoute(async (req, res) => {
  let query = supabase
    .from("audit_log")
    .select("*")
    .order("timestamp", { ascending: false });

  if (req.query.user_id) {
    query = query.eq("user_id", Number(req.query.user_id));
  }

  if (req.query.action) {
    query = query.eq("action", String(req.query.action).toUpperCase());
  }

  if (req.query.entity_type) {
    query = query.eq("entity_type", String(req.query.entity_type).toUpperCase());
  }

  if (req.query.from) {
    query = query.gte("timestamp", Number(req.query.from));
  }

  if (req.query.to) {
    query = query.lte("timestamp", Number(req.query.to));
  }

  const result = await query.limit(Number(req.query.limit || 100));
  return handleSupabase(res, result);
}));

router.get("/:id", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("audit_log")
    .select("*")
    .eq("id", Number(req.params.id))
    .maybeSingle();

  return handleSupabase(res, result, "Audit log not found.");
}));

router.post("/", asyncRoute(async (req, res) => {
  if (!req.body.action) {
    return res.status(400).json({ message: "action is required." });
  }

  const result = await supabase
    .from("audit_log")
    .insert({
      user_id: req.body.user_id || null,
      action: String(req.body.action).toUpperCase(),
      entity_type: req.body.entity_type ? String(req.body.entity_type).toUpperCase() : null,
      entity_id: req.body.entity_id || null,
      details: req.body.details || null,
      timestamp: req.body.timestamp || unixTimestampMs()
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.delete("/:id", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("audit_log")
    .delete()
    .eq("id", Number(req.params.id));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

module.exports = router;
