package ke.eelaminnovations.kangaishop.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import ke.eelaminnovations.kangaishop.ui.reports.ReportSummary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {

    fun generateReportPdf(context: Context, summary: ReportSummary): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1B5E20") // Dark green primary
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#555555")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textBoldPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var y = 50f

        // Draw Header
        canvas.drawText("Kangai Shop — Business Report", 40f, y, titlePaint)
        y += 25f
        
        val dateStr = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $dateStr", 40f, y, textPaint)
        y += 15f
        canvas.drawText("Reporting Period: ${summary.period.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}", 40f, y, textPaint)
        
        y += 20f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Section: Milk Production
        canvas.drawText("🥛 MILK PRODUCTION SUMMARY", 40f, y, sectionPaint)
        y += 20f

        // Table Header
        canvas.drawText("Metric", 50f, y, textBoldPaint)
        canvas.drawText("Morning", 200f, y, textBoldPaint)
        canvas.drawText("Evening", 320f, y, textBoldPaint)
        canvas.drawText("Total Received", 440f, y, textBoldPaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // Litres row
        canvas.drawText("Litres", 50f, y, textPaint)
        canvas.drawText(formatLitres(summary.morningLitres), 200f, y, textPaint)
        canvas.drawText(formatLitres(summary.eveningLitres), 320f, y, textPaint)
        canvas.drawText(formatLitres(summary.totalMilkLitres), 440f, y, textBoldPaint)
        y += 18f

        // Value row
        canvas.drawText("Value (Est.)", 50f, y, textPaint)
        canvas.drawText("—", 200f, y, textPaint)
        canvas.drawText("—", 320f, y, textPaint)
        canvas.drawText(formatKes(summary.totalMilkValue), 440f, y, textBoldPaint)

        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        // Section: Debts & Receivables
        canvas.drawText("💰 FINANCIAL LEDGER SUMMARY", 40f, y, sectionPaint)
        y += 20f

        canvas.drawText("Total Supplier Debts (We owe suppliers):", 50f, y, textPaint)
        canvas.drawText(formatKes(summary.totalOwedOut), 400f, y, textBoldPaint)
        y += 18f
        canvas.drawText("Total Customer Credit (Customers owe us):", 50f, y, textPaint)
        canvas.drawText(formatKes(summary.totalOwedIn), 400f, y, textBoldPaint)

        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        // Supplier lists
        if (summary.supplierBalances.isNotEmpty()) {
            canvas.drawText("Top Supplier Balances", 40f, y, sectionPaint)
            y += 20f
            summary.supplierBalances.take(10).forEach { (person, balance) ->
                canvas.drawText(person.name, 50f, y, textPaint)
                canvas.drawText(formatKes(balance), 400f, y, textPaint)
                y += 16f
                if (y > 780f) return@forEach
            }
            y += 15f
        }

        // Customer lists
        if (summary.customerBalances.isNotEmpty() && y < 780f) {
            canvas.drawText("Top Customer Balances", 40f, y, sectionPaint)
            y += 20f
            summary.customerBalances.take(10).forEach { (person, balance) ->
                canvas.drawText(person.name, 50f, y, textPaint)
                canvas.drawText(formatKes(balance), 400f, y, textPaint)
                y += 16f
                if (y > 780f) return@forEach
            }
        }

        // Draw Footer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#888888")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Kangai Shop App — Built by Eelam Innovations. Confidential & Proprietary.", 150f, 820f, footerPaint)

        document.finishPage(page)

        val file = File(context.cacheDir, "kangaishop_report_${summary.period.name.lowercase()}.pdf")
        file.outputStream().use {
            document.writeTo(it)
        }
        document.close()

        return file
    }
}
