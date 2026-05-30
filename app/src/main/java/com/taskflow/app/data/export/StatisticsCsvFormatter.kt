package com.taskflow.app.data.export

import com.taskflow.app.domain.model.StatisticsSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StatisticsCsvFormatter @Inject constructor() {
    fun format(snapshot: StatisticsSnapshot): String = buildString {
        appendLine(snapshot.title.csvLine())
        appendLine("Gerado em,${snapshot.generatedAt.formatDate().csvCell()}")
        appendLine()
        appendLine("Item,Total,Concluidas,Pendentes,Atrasadas,Taxa de conclusao,Tempo total (min)")
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
                "Total",
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
