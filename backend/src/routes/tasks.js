const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");

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
    updated_at: unixTimestampMs()
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
      created_at: unixTimestampMs()
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
      updated_at: unixTimestampMs()
    })
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Task not found.");
}));

router.put("/tasks/:id/complete", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("tasks")
    .update({
      status: "COMPLETED",
      updated_at: unixTimestampMs()
    })
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Task not found.");
}));

router.put("/tasks/:id/progress", asyncRoute(async (req, res) => {
  const userId = Number(req.body.user_id || req.user?.sub);
  const taskId = Number(req.params.id);
  const completion = Number(req.body.completion_percentage);
  const timeSpent = Number(req.body.time_spent_minutes || 0);
  const now = unixTimestampMs();

  if (!userId || !Number.isInteger(completion) || completion < 0 || completion > 100 || timeSpent < 0) {
    return res.status(400).json({
      message: "user_id, completion_percentage between 0 and 100, and valid time_spent_minutes are required."
    });
  }

  const progressResult = await supabase
    .from("user_task")
    .upsert({
      user_id: userId,
      task_id: taskId,
      work_date: req.body.work_date || null,
      location: req.body.location || null,
      completion_percentage: completion,
      time_spent_minutes: timeSpent,
      is_completed: completion === 100,
      updated_at: now
    }, {
      onConflict: "user_id,task_id"
    })
    .select()
    .single();

  if (progressResult.error) {
    return res.status(400).json({ message: progressResult.error.message });
  }

  const taskResult = await supabase
    .from("tasks")
    .update({
      status: completion === 100 ? "COMPLETED" : "IN_PROGRESS",
      updated_at: now
    })
    .eq("id", taskId)
    .select()
    .single();

  if (taskResult.error) {
    return res.status(400).json({ message: taskResult.error.message });
  }

  return res.json({
    task: taskResult.data,
    progress: progressResult.data
  });
}));

router.post("/tasks/:id/users", asyncRoute(async (req, res) => {
  const userId = Number(req.body.user_id);

  if (!userId) {
    return res.status(400).json({ message: "user_id is required." });
  }

  const result = await supabase
    .from("user_task")
    .upsert({
      user_id: userId,
      task_id: Number(req.params.id),
      completion_percentage: 0,
      time_spent_minutes: 0,
      is_completed: false,
      updated_at: unixTimestampMs()
    }, {
      onConflict: "user_id,task_id"
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.delete("/tasks/:id/users/:userId", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("user_task")
    .delete()
    .eq("task_id", Number(req.params.id))
    .eq("user_id", Number(req.params.userId));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
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
