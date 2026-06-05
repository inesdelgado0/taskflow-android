const express = require("express");
const { supabase } = require("../config/supabase");
const { asyncRoute } = require("../utils/http");
const { unixTimestampMs } = require("../utils/time");

const router = express.Router();

function toRow(label, tasks, now = unixTimestampMs()) {
  return {
    label,
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
  };
}

function buildSnapshot(title, rows, now = unixTimestampMs()) {
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

function csvCell(value) {
  const text = String(value ?? "");
  const escaped = text.replace(/"/g, "\"\"");
  return /[",\n\r]/.test(escaped) ? `"${escaped}"` : escaped;
}

function snapshotToCsv(snapshot) {
  return [
    csvCell(snapshot.title),
    `Gerado em,${snapshot.generatedAt}`,
    "",
    "Item,Total,Concluidas,Pendentes,Atrasadas,Taxa de conclusao,Tempo total (min)",
    ...snapshot.rows.map((row) => [
      row.label,
      row.totalTasks,
      row.completedTasks,
      row.pendingTasks,
      row.overdueTasks,
      `${row.totalTasks === 0 ? 0 : Math.floor((row.completedTasks * 100) / row.totalTasks)}%`,
      row.totalTimeMinutes
    ].map(csvCell).join(",")),
    "",
    [
      "Total",
      snapshot.totalTasks,
      snapshot.completedTasks,
      snapshot.pendingTasks,
      snapshot.overdueTasks,
      `${snapshot.completionRate}%`,
      snapshot.rows.reduce((sum, row) => sum + row.totalTimeMinutes, 0)
    ].map(csvCell).join(",")
  ].join("\n");
}

function escapePdfText(value) {
  return String(value ?? "")
    .replace(/\\/g, "\\\\")
    .replace(/\(/g, "\\(")
    .replace(/\)/g, "\\)");
}

function snapshotToPdf(snapshot) {
  const lines = [
    snapshot.title,
    `Gerado em: ${snapshot.generatedAt}`,
    "",
    "Item | Total | Concluidas | Pendentes | Atrasadas | Taxa | Tempo",
    ...snapshot.rows.map((row) => {
      const rate = row.totalTasks === 0 ? 0 : Math.floor((row.completedTasks * 100) / row.totalTasks);
      return `${row.label} | ${row.totalTasks} | ${row.completedTasks} | ${row.pendingTasks} | ${row.overdueTasks} | ${rate}% | ${row.totalTimeMinutes}`;
    }),
    "",
    `Total: ${snapshot.totalTasks} tarefas, ${snapshot.completionRate}% concluido`
  ];

  const content = [
    "BT",
    "/F1 12 Tf",
    "50 790 Td",
    ...lines.flatMap((line, index) => [
      index === 0 ? "/F1 16 Tf" : "/F1 10 Tf",
      `(${escapePdfText(line).slice(0, 110)}) Tj`,
      "0 -18 Td"
    ]),
    "ET"
  ].join("\n");

  const objects = [
    "<< /Type /Catalog /Pages 2 0 R >>",
    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
    "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
    `<< /Length ${Buffer.byteLength(content, "utf8")} >>\nstream\n${content}\nendstream`
  ];

  let pdf = "%PDF-1.4\n";
  const offsets = [0];
  objects.forEach((object, index) => {
    offsets.push(Buffer.byteLength(pdf, "utf8"));
    pdf += `${index + 1} 0 obj\n${object}\nendobj\n`;
  });
  const xrefOffset = Buffer.byteLength(pdf, "utf8");
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  offsets.slice(1).forEach((offset) => {
    pdf += `${String(offset).padStart(10, "0")} 00000 n \n`;
  });
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF`;
  return Buffer.from(pdf, "utf8");
}

router.get("/users/:id", asyncRoute(async (req, res) => {
  const userId = Number(req.params.id);
  const { data: user, error: userError } = await supabase
    .from("users")
    .select("name")
    .eq("id", userId)
    .maybeSingle();

  if (userError) return res.status(400).json({ message: userError.message });

  const { data: assignments, error } = await supabase
    .from("user_task")
    .select("time_spent_minutes, tasks(*)")
    .eq("user_id", userId);

  if (error) return res.status(400).json({ message: error.message });

  const tasks = (assignments || []).map((row) => ({
    ...row.tasks,
    user_task: [{ time_spent_minutes: row.time_spent_minutes }]
  })).filter(Boolean);

  const label = user?.name || `Utilizador ${userId}`;
  return res.json(buildSnapshot(`Estatisticas do utilizador: ${label}`, [toRow(label, tasks)]));
}));

router.get("/projects/:id", asyncRoute(async (req, res) => {
  const projectId = Number(req.params.id);
  const { data: project, error: projectError } = await supabase
    .from("projects")
    .select("name")
    .eq("id", projectId)
    .maybeSingle();

  if (projectError) return res.status(400).json({ message: projectError.message });

  const { data: tasks, error } = await supabase
    .from("tasks")
    .select("*, user_task(time_spent_minutes)")
    .eq("project_id", projectId);

  if (error) return res.status(400).json({ message: error.message });

  const label = project?.name || `Projeto ${projectId}`;
  return res.json(buildSnapshot(`Estatisticas do projeto: ${label}`, [toRow(label, tasks || [])]));
}));

router.get("/tasks/:id", asyncRoute(async (req, res) => {
  const { data: task, error } = await supabase
    .from("tasks")
    .select("*, user_task(time_spent_minutes)")
    .eq("id", Number(req.params.id))
    .maybeSingle();

  if (error) return res.status(400).json({ message: error.message });
  if (!task) return res.status(404).json({ message: "Task not found." });

  return res.json(buildSnapshot(`Estatisticas da tarefa: ${task.title}`, [toRow(task.title, [task])]));
}));

router.get("/export", asyncRoute(async (req, res) => {
  const format = String(req.query.format || "csv").toLowerCase();
  const { data: tasks, error } = await supabase
    .from("tasks")
    .select("*, user_task(time_spent_minutes)")
    .order("project_id", { ascending: true });

  if (error) return res.status(400).json({ message: error.message });

  const tasksByProject = (tasks || []).reduce((groups, task) => {
    const key = task.project_id || 0;
    groups[key] = groups[key] || [];
    groups[key].push(task);
    return groups;
  }, {});
  const rows = Object.entries(tasksByProject).map(([projectId, projectTasks]) =>
    toRow(`Projeto ${projectId}`, projectTasks)
  );
  const snapshot = buildSnapshot("Estatisticas globais", rows.length ? rows : [toRow("Sem tarefas", [])]);
  if (format === "json") return res.json(snapshot);

  if (format === "pdf") {
    const pdf = snapshotToPdf(snapshot);
    res.setHeader("Content-Type", "application/pdf");
    res.setHeader("Content-Disposition", "attachment; filename=taskflow-stats.pdf");
    return res.send(pdf);
  }

  res.setHeader("Content-Type", "text/csv; charset=utf-8");
  res.setHeader("Content-Disposition", "attachment; filename=taskflow-stats.csv");
  return res.send(snapshotToCsv(snapshot));
}));

module.exports = router;
