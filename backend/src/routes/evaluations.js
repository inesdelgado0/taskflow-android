const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute, handleSupabase } = require("../utils/http");
const { unixTimestamp } = require("../utils/time");

const router = express.Router();

router.get("/projects/:projectId/evaluations", asyncRoute(async (req, res) => {
  const result = await supabase
    .from("evaluations")
    .select("*")
    .eq("project_id", Number(req.params.projectId))
    .order("created_at", { ascending: false });

  return handleSupabase(res, result);
}));

router.put("/users/:userId/evaluate", asyncRoute(async (req, res) => {
  const rating = Number(req.body.rating);

  if (!req.body.project_id || !req.body.evaluator_id || !Number.isInteger(rating) || rating < 1 || rating > 5) {
    return res.status(400).json({ message: "project_id, evaluator_id and rating between 1 and 5 are required." });
  }

  const result = await supabase
    .from("evaluations")
    .upsert({
      project_id: req.body.project_id,
      evaluator_id: req.body.evaluator_id,
      evaluated_user_id: Number(req.params.userId),
      rating,
      comment: req.body.comment || null,
      created_at: unixTimestamp()
    }, {
      onConflict: "project_id,evaluated_user_id"
    })
    .select()
    .single();

  return handleSupabase(res, result);
}));

module.exports = router;
