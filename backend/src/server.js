require("dotenv").config();

const cors = require("cors");
const express = require("express");
const morgan = require("morgan");

const auditRoutes = require("./routes/audit");
const authRoutes = require("./routes/auth");
const evaluationRoutes = require("./routes/evaluations");
const observationRoutes = require("./routes/observations");
const projectRoutes = require("./routes/projects");
const statsRoutes = require("./routes/stats");
const syncQueueRoutes = require("./routes/syncQueue");
const taskRoutes = require("./routes/tasks");
const userRoutes = require("./routes/users");

const app = express();
const port = Number(process.env.PORT || 3000);

app.use(cors());
app.use(express.json({ limit: "2mb" }));
app.use(morgan("dev"));

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

app.use("/v1/auth", authRoutes);
app.use("/v1/audit-log", auditRoutes);
app.use("/v1/projects", projectRoutes);
app.use("/v1/stats", statsRoutes);
app.use("/v1/sync-queue", syncQueueRoutes);
app.use("/v1/users", userRoutes);
app.use("/v1", taskRoutes);
app.use("/v1", observationRoutes);
app.use("/v1", evaluationRoutes);

app.use((req, res) => {
  res.status(404).json({ message: `Route not found: ${req.method} ${req.originalUrl}` });
});

app.use((error, _req, res, _next) => {
  console.error(error);
  res.status(500).json({ message: "Internal server error." });
});

app.listen(port, () => {
  console.log(`TaskFlow API listening on http://localhost:${port}`);
});
