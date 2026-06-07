package com.taskflow.app.domain.model

data class StatisticsSnapshot(
    val title: String,
    val generatedAt: Long,
    val rows: List<StatisticRow>
) {
    val totalTasks: Int
        get() = rows.sumOf { it.totalTasks }

    val completedTasks: Int
        get() = rows.sumOf { it.completedTasks }

    val pendingTasks: Int
        get() = rows.sumOf { it.pendingTasks }

    val overdueTasks: Int
        get() = rows.sumOf { it.overdueTasks }

    val completionRate: Int
        get() = if (totalTasks == 0) 0 else (completedTasks * 100) / totalTasks
}

data class StatisticRow(
    val label: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val overdueTasks: Int,
    val totalTimeMinutes: Int = 0
) {
    val completionRate: Int
        get() = if (totalTasks == 0) 0 else (completedTasks * 100) / totalTasks
}

enum class StatisticsExportFormat {
    CSV,
    PDF
}

enum class StatisticsGrouping {
    BY_USER,
    BY_PROJECT,
    BY_TASK
}
