const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase, sendNoContent } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");
const { USER_SELECT, toUserResponse } = require("../utils/users");

const router = express.Router();

function toProjectPayload(body) {
  const now = unixTimestampMs();

  return {
    name: body.name,
    description: body.description || null,
    start_date: body.start_date || null,
    end_date: body.end_date || null,
    status: body.status || "ACTIVE",
    manager_id: body.manager_id || null,
    created_by: body.created_by,
    updated_at: now
  };
}

router.get("/", asyncRoute(async (_req, res) => {
  const result = await supabase
    .from("projects")
    .select("*")
    .order("created_at", { ascending: false });

  return handleSupabase(res, result);
}));

router.get("/:id", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("projects")
    .select("*")
    .eq("id", Number(req.params.id))
    .maybeSingle();

  return handleSupabase(res, result, "Project not found.");
}));

router.post("/", asyncRoute(async (req, res) => {
  if (!req.body.name || !req.body.created_by) {
    return res.status(400).json({ message: "Name and created_by are required." });
  }

  const now = unixTimestampMs();
  const result = await supabase
    .from("projects")
    .insert({
      ...toProjectPayload(req.body),
      created_at: now
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.put("/:id", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("projects")
    .update(toProjectPayload(req.body))
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Project not found.");
}));

router.put("/:id/manager", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("projects")
    .update({
      manager_id: req.body.manager_id || null,
      updated_at: unixTimestampMs()
    })
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Project not found.");
}));

router.put("/:id/complete", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("projects")
    .update({
      status: "COMPLETED",
      updated_at: unixTimestampMs()
    })
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Project not found.");
}));

router.put("/:id/status", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("projects")
    .update({
      status: req.body.status,
      updated_at: unixTimestampMs()
    })
    .eq("id", Number(req.params.id))
    .select()
    .single();

  return handleSupabase(res, result, "Project not found.");
}));

router.get("/:id/users", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("user_project")
    .select(`
      joined_at,
      users (
        ${USER_SELECT}
      )
    `)
    .eq("project_id", Number(req.params.id))
    .order("joined_at", { ascending: true });

  if (result.error) {
    return res.status(400).json({ message: result.error.message });
  }

  return res.json((result.data || []).map((row) => ({
    ...toUserResponse(row.users),
    joined_at: row.joined_at
  })));
}));

router.post("/:id/users", asyncRoute(async (req, res) => {
  const userId = Number(req.body.user_id);

  if (!userId) {
    return res.status(400).json({ message: "user_id is required." });
  }

  const result = await supabase
    .from("user_project")
    .upsert({
      user_id: userId,
      project_id: Number(req.params.id),
      joined_at: unixTimestampMs()
    }, {
      onConflict: "user_id,project_id"
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

router.delete("/:id/users/:userId", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("user_project")
    .delete()
    .eq("project_id", Number(req.params.id))
    .eq("user_id", Number(req.params.userId));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

router.delete("/:id", asyncRoute(async (req, res) => {
  const { error } = await supabase
    .from("projects")
    .delete()
    .eq("id", Number(req.params.id));

  if (error) {
    return res.status(400).json({ message: error.message });
  }

  return sendNoContent(res);
}));

module.exports = router;
