const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();

router.get("/", asyncRoute(async (req, res) => {
  let query = supabase
    .from("sync_queue")
    .select("*")
    .order("created_at", { ascending: true });

  if (req.query.endpoint) {
    query = query.eq("endpoint", req.query.endpoint);
  }

  const result = await query.limit(Number(req.query.limit || 100));
  return handleSupabase(res, result);
}));

router.post("/", asyncRoute(async (req, res) => {
  if (!req.body.endpoint || !req.body.http_method) {
    return res.status(400).json({ message: "endpoint and http_method are required." });
  }

  const result = await supabase
    .from("sync_queue")
    .insert({
      endpoint: req.body.endpoint,
      http_method: String(req.body.http_method).toUpperCase(),
      payload: typeof req.body.payload === "string"
        ? req.body.payload
        : JSON.stringify(req.body.payload || {}),
      created_at: req.body.created_at || unixTimestampMs(),
      retry_count: req.body.retry_count || 0,
      last_error: req.body.last_error || null
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.put("/:id/retry", asyncRoute(async (req, res) => {
  const existing = await supabase
    .from("sync_queue")
    .select("retry_count")
    .eq("id", Number(req.params.id))
    .maybeSingle();

  if (existing.error) {
    return res.status(400).json({ message: existing.error.message });
  }

  if (!existing.data) {
    return res.status(404).json({ message: "Sync queue item not found." });
  }

  const result = await supabase
    .from("sync_queue")
    .update({
      retry_count: (existing.data.retry_count || 0) + 1,
      last_error: req.body.last_error || null
    })
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.delete("/:id", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("sync_queue")
    .delete()
    .eq("id", Number(req.params.id));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

module.exports = router;
