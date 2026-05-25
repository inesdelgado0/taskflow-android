const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestamp } = require("../utils/time");

const router = express.Router();

function toTaskPayload(body, projectId) {
  return {
    project_id: projectId,
    title: body.title,
    description: body.description || null,
    priority: body.priority || "MEDIUM",
    deadline: body.deadline || null,
    status: body.status || "PENDING",
    created_by: body.created_by,
    updated_at: unixTimestamp()
  };
}

router.get("/projects/:projectId/tasks", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("tasks")
    .select("*")
    .eq("project_id", Number(req.params.projectId))
    .order("deadline", { ascending: true, nullsFirst: false });

  return handleSupabase(res, result);
}));

router.post("/projects/:projectId/tasks", asyncRoute(async (req, res) => {
  if (!req.body.title || !req.body.created_by) {
    return res.status(400).json({ message: "Title and created_by are required." });
  }

  const result = await supabase
    .from("tasks")
    .insert({
      ...toTaskPayload(req.body, Number(req.params.projectId)),
      created_at: unixTimestamp()
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.get("/tasks/:id", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("tasks")
    .select("*")
    .eq("id", Number(req.params.id))
    .maybeSingle();

  return handleSupabase(res, result, "Task not found.");
}));

router.put("/tasks/:id", asyncRoute(async (req, res) => {
  const existing = await supabase
    .from("tasks")
    .select("project_id")
    .eq("id", Number(req.params.id))
    .maybeSingle();

  if (existing.error) {
    return res.status(400).json({ message: existing.error.message });
  }

  if (!existing.data) {
    return res.status(404).json({ message: "Task not found." });
  }

  const result = await supabase
    .from("tasks")
    .update(toTaskPayload(req.body, existing.data.project_id))
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Task not found.");
}));

router.put("/tasks/:id/status", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("tasks")
    .update({
      status: req.body.status,
      updated_at: unixTimestamp()
    })
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Task not found.");
}));

router.delete("/tasks/:id", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("tasks")
    .delete()
    .eq("id", Number(req.params.id));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

module.exports = router;
