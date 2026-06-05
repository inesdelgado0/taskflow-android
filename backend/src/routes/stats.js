const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();

function buildSnapshot(title, tasks, now = unixTimestampMs()) {
  const rows = [{
    label: title,
    totalTasks: tasks.length,
    completedTasks: tasks.filter((task) => task.status === "COMPLETED").length,
    pendingTasks: tasks.filter((task) => task.status !== "COMPLETED" && task.status !== "CANCELLED").length,
    overdueTasks: tasks.filter((task) =>
      task.deadline &&
      task.deadline < now &&
      task.status !== "COMPLETED" &&
      task.status !== "CANCELLED"
    ).length,
    totalTimeMinutes: tasks.reduce((sum, task) => {
      const assignments = task.user_task || [];
      return sum + assignments.reduce((inner, assignment) => inner + (assignment.time_spent_minutes || 0), 0);
    }, 0)
  }];

  const totalTasks = rows.reduce((sum, row) => sum + row.totalTasks, 0);
  const completedTasks = rows.reduce((sum, row) => sum + row.completedTasks, 0);

  return {
    title,
    generatedAt: now,
    rows,
    totalTasks,
    completedTasks,
    pendingTasks: rows.reduce((sum, row) => sum + row.pendingTasks, 0),
    overdueTasks: rows.reduce((sum, row) => sum + row.overdueTasks, 0),
    completionRate: totalTasks === 0 ? 0 : Math.floor((completedTasks * 100) / totalTasks)
  };
}

router.get("/users/:id", asyncRoute(async (req, res) => {
  const userId = Number(req.params.id);
  const { data: assignments, error } = await supabase
    .from("user_task")
    .select("time_spent_minutes, tasks(*)")
    .eq("user_id", userId);

  if (error) return res.status(400).json({ message: error.message });

  const tasks = (assignments || []).map((row) => ({
    ...row.tasks,
    user_task: [{ time_spent_minutes: row.time_spent_minutes }]
  })).filter(Boolean);

  return res.json(buildSnapshot(`Estatisticas do utilizador: ${userId}`, tasks));
}));

router.get("/projects/:id", asyncRoute(async (req, res) => {
  const { data: tasks, error } = await supabase
    .from("tasks")
    .select("*, user_task(time_spent_minutes)")
    .eq("project_id", Number(req.params.id));

  if (error) return res.status(400).json({ message: error.message });

  return res.json(buildSnapshot(`Estatisticas do projeto: ${req.params.id}`, tasks || []));
}));

router.get("/tasks/:id", asyncRoute(async (req, res) => {
  const { data: task, error } = await supabase
    .from("tasks")
    .select("*, user_task(time_spent_minutes)")
    .eq("id", Number(req.params.id))
    .maybeSingle();

  if (error) return res.status(400).json({ message: error.message });
  if (!task) return res.status(404).json({ message: "Task not found." });

  return res.json(buildSnapshot(`Estatisticas da tarefa: ${task.title}`, [task]));
}));

router.get("/export", asyncRoute(async (req, res) => {
  const format = String(req.query.format || "csv").toLowerCase();
  const { data: tasks, error } = await supabase
    .from("tasks")
    .select("*, user_task(time_spent_minutes)")
    .order("project_id", { ascending: true });

  if (error) return res.status(400).json({ message: error.message });

  const snapshot = buildSnapshot("Estatisticas globais", tasks || []);
  if (format === "json") return res.json(snapshot);

  const csv = [
    "Item,Total,Concluidas,Pendentes,Atrasadas,Taxa de conclusao,Tempo total (min)",
    ...snapshot.rows.map((row) => [
      row.label,
      row.totalTasks,
      row.completedTasks,
      row.pendingTasks,
      row.overdueTasks,
      `${row.totalTasks === 0 ? 0 : Math.floor((row.completedTasks * 100) / row.totalTasks)}%`,
      row.totalTimeMinutes
    ].join(","))
  ].join("\n");

  res.setHeader("Content-Type", "text/csv; charset=utf-8");
  res.setHeader("Content-Disposition", `attachment; filename=taskflow-stats.${format === "pdf" ? "csv" : "csv"}`);
  return res.send(csv);
}));

module.exports = router;
