package com.taskflow.app.data.export

import com.taskflow.app.domain.model.StatisticsSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StatisticsExportLabels(
    val generatedAt: String,
    val item: String,
    val total: String,
    val completedLabel: String,
    val pendingLabel: String,
    val overdueLabel: String,
    val completionLabel: String,
    val timeSpentLabel: String,
    val totalLabel: String
)

class StatisticsCsvFormatter @Inject constructor() {
    fun format(snapshot: StatisticsSnapshot, labels: StatisticsExportLabels): String = buildString {
        appendLine(snapshot.title.csvLine())
        appendLine("${labels.generatedAt},${snapshot.generatedAt.formatDate().csvCell()}")
        appendLine()
        appendLine(
            listOf(
                labels.item,
                labels.total,
                labels.completedLabel,
                labels.pendingLabel,
                labels.overdueLabel,
                labels.completionLabel,
                labels.timeSpentLabel
            ).joinToString(",")
        )
        snapshot.rows.forEach { row ->
            appendLine(
                listOf(
                    row.label,
                    row.totalTasks.toString(),
                    row.completedTasks.toString(),
                    row.pendingTasks.toString(),
                    row.overdueTasks.toString(),
                    "${row.completionRate}%",
                    row.totalTimeMinutes.toString()
                ).joinToString(",") { it.csvCell() }
            )
        }
        appendLine()
        appendLine(
            listOf(
                labels.totalLabel,
                snapshot.totalTasks.toString(),
                snapshot.completedTasks.toString(),
                snapshot.pendingTasks.toString(),
                snapshot.overdueTasks.toString(),
                "${snapshot.completionRate}%",
                snapshot.rows.sumOf { it.totalTimeMinutes }.toString()
            ).joinToString(",") { it.csvCell() }
        )
    }

    private fun String.csvLine(): String = csvCell()

    private fun String.csvCell(): String {
        val escaped = replace("\"", "\"\"")
        return if (any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    private fun Long.formatDate(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(this))
}
