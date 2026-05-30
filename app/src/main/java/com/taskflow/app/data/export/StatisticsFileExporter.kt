package com.taskflow.app.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.taskflow.app.domain.model.StatisticsExportFormat
import com.taskflow.app.domain.model.StatisticsSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StatisticsFileExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val csvFormatter: StatisticsCsvFormatter
) {
    fun export(snapshot: StatisticsSnapshot, format: StatisticsExportFormat): File =
        when (format) {
            StatisticsExportFormat.CSV -> exportCsv(snapshot)
            StatisticsExportFormat.PDF -> exportPdf(snapshot)
        }

    fun shareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mimeType = when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "csv" -> "text/csv"
            else -> "application/octet-stream"
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun exportCsv(snapshot: StatisticsSnapshot): File {
        val file = exportFile(snapshot, "csv")
        file.writeText(csvFormatter.format(snapshot))
        return file
    }

    private fun exportPdf(snapshot: StatisticsSnapshot): File {
        val file = exportFile(snapshot, "pdf")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 11f
        }
        val bodyPaint = Paint().apply { textSize = 10f }

        var y = 42f
        canvas.drawText(snapshot.title, 32f, y, titlePaint)
        y += 24f
        canvas.drawText("Gerado em: ${snapshot.generatedAt.formatDate()}", 32f, y, bodyPaint)
        y += 30f

        canvas.drawText("Item", 32f, y, headerPaint)
        canvas.drawText("Total", 260f, y, headerPaint)
        canvas.drawText("Concl.", 318f, y, headerPaint)
        canvas.drawText("Pend.", 382f, y, headerPaint)
        canvas.drawText("Atras.", 446f, y, headerPaint)
        canvas.drawText("Taxa", 512f, y, headerPaint)
        y += 18f

        snapshot.rows.forEach { row ->
            canvas.drawText(row.label.take(36), 32f, y, bodyPaint)
            canvas.drawText(row.totalTasks.toString(), 260f, y, bodyPaint)
            canvas.drawText(row.completedTasks.toString(), 318f, y, bodyPaint)
            canvas.drawText(row.pendingTasks.toString(), 382f, y, bodyPaint)
            canvas.drawText(row.overdueTasks.toString(), 446f, y, bodyPaint)
            canvas.drawText("${row.completionRate}%", 512f, y, bodyPaint)
            y += 18f
        }

        y += 12f
        canvas.drawText("Total: ${snapshot.totalTasks}", 32f, y, headerPaint)
        canvas.drawText("Concluidas: ${snapshot.completedTasks}", 130f, y, headerPaint)
        canvas.drawText("Pendentes: ${snapshot.pendingTasks}", 260f, y, headerPaint)
        canvas.drawText("Atrasadas: ${snapshot.overdueTasks}", 390f, y, headerPaint)
        canvas.drawText("Conclusao: ${snapshot.completionRate}%", 32f, y + 20f, headerPaint)

        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun exportFile(snapshot: StatisticsSnapshot, extension: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeTitle = snapshot.title
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "estatisticas" }

        return File(dir, "${safeTitle}_${snapshot.generatedAt}.$extension")
    }

    private fun Long.formatDate(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(this))

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
    }
}
