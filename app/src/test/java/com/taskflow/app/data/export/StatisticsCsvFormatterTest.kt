package com.taskflow.app.data.export

import com.taskflow.app.domain.model.StatisticRow
import com.taskflow.app.domain.model.StatisticsSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsCsvFormatterTest {
    private val formatter = StatisticsCsvFormatter()

    private val testLabels = StatisticsExportLabels(
        generatedAt = "Gerado em",
        item = "Item",
        total = "Total",
        completedLabel = "Concluidas",
        pendingLabel = "Pendentes",
        overdueLabel = "Atrasadas",
        completionLabel = "Conclusao",
        timeSpentLabel = "Tempo",
        totalLabel = "Total"
    )

    @Test
    fun format_escapesCommaAndQuotes() {
        val snapshot = StatisticsSnapshot(
            title = "Estatisticas, Projeto",
            generatedAt = 0L,
            rows = listOf(
                StatisticRow(
                    label = "Tarefa \"critica\"",
                    totalTasks = 2,
                    completedTasks = 1,
                    pendingTasks = 1,
                    overdueTasks = 0
                )
            )
        )

        val csv = formatter.format(snapshot, testLabels)

        assertTrue(csv.contains("\"Estatisticas, Projeto\""))
        assertTrue(csv.contains("\"Tarefa \"\"critica\"\"\",2,1,1,0,50%,0"))
    }

    @Test
    fun format_addsTotalLine() {
        val snapshot = StatisticsSnapshot(
            title = "Resumo",
            generatedAt = 0L,
            rows = listOf(
                StatisticRow("Projeto A", 4, 3, 1, 1),
                StatisticRow("Projeto B", 2, 1, 1, 0)
            )
        )

        val csv = formatter.format(snapshot, testLabels)

        assertTrue(csv.contains("Total,6,4,2,1,66%,0"))
        assertEquals(6, snapshot.totalTasks)
        assertEquals(66, snapshot.completionRate)
    }
}
