const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestamp } = require("../utils/time");

const router = express.Router();

router.get("/tasks/:taskId/observations", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("observations")
    .select("*")
    .eq("task_id", Number(req.params.taskId))
    .order("created_at", { ascending: false });

  return handleSupabase(res, result);
}));

router.post("/tasks/:taskId/observations", asyncRoute(async (req, res) => {
  const text = req.body.text || null;
  const photoPath = req.body.photo_path || null;

  if (!req.body.user_id || (!text && !photoPath)) {
    return res.status(400).json({ message: "user_id and text or photo_path are required." });
  }

  const result = await supabase
    .from("observations")
    .insert({
      task_id: Number(req.params.taskId),
      user_id: req.body.user_id,
      text,
      photo_path: photoPath,
      created_at: unixTimestamp()
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.delete("/observations/:id", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("observations")
    .delete()
    .eq("id", Number(req.params.id));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

module.exports = router;
