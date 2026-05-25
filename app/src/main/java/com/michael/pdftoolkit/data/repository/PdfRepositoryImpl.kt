package com.michael.pdftoolkit.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument as AndroidPdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.michael.pdftoolkit.domain.model.PageRange
import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.model.PageSignatureSize
import com.michael.pdftoolkit.domain.model.SignaturePlacement
import com.michael.pdftoolkit.domain.repository.PdfRepository
import com.michael.pdftoolkit.util.DocumentActions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfRepositoryImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PdfRepository {

    override suspend fun mergePdfs(
        sourceUris: List<String>,
        outputName: String,
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): PdfDocument = withContext(ioDispatcher) {
        val pdfDocument = AndroidPdfDocument()
        val outputDir = File(context.filesDir, "documents").apply { mkdirs() }
        val finalOutputName = if (outputName.endsWith(".pdf", ignoreCase = true)) outputName else "$outputName.pdf"
        val outputFile = File(outputDir, finalOutputName)

        var pageNum = 0
        sourceUris.forEachIndexed { index, uriStr ->
            val uri = Uri.parse(uriStr)
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    val renderer = android.graphics.pdf.PdfRenderer(pfd)
                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        
                        // Render page onto an exact scale bitmap
                        val width = page.width
                        val height = page.height
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        
                        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        
                        pageNum++
                        val pageInfo = AndroidPdfDocument.PageInfo.Builder(width, height, pageNum).create()
                        val docPage = pdfDocument.startPage(pageInfo)
                        val canvas = docPage.canvas
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                        pdfDocument.finishPage(docPage)
                        
                        bitmap.recycle()
                        page.close()
                    }
                    renderer.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onProgress(index + 1, sourceUris.size)
        }

        val fos = outputFile.outputStream()
        try {
            pdfDocument.writeTo(fos)
        } finally {
            fos.close()
        }
        pdfDocument.close()

        PdfDocument(
            uriString = Uri.fromFile(outputFile).toString(),
            name = finalOutputName,
            pageCount = pageNum,
            fileSizeBytes = outputFile.length(),
            lastModifiedMillis = System.currentTimeMillis(),
            additionType = "Merged"
        )
    }

    override suspend fun splitPdf(
        sourceUri: String,
        ranges: List<PageRange>
    ): List<PdfDocument> = withContext(ioDispatcher) {
        val result = mutableListOf<PdfDocument>()
        val uri = Uri.parse(sourceUri)
        val originalName = getFileNameFromUri(uri)

        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = android.graphics.pdf.PdfRenderer(pfd)
                
                ranges.forEach { range ->
                    val pdfDocument = AndroidPdfDocument()
                    var localPageCount = 0
                    
                    val startIdx = (range.start - 1).coerceAtLeast(0)
                    val endIdx = (range.end - 1).coerceAtMost(renderer.pageCount - 1)
                    
                    for (i in startIdx..endIdx) {
                        val page = renderer.openPage(i)
                        val width = page.width
                        val height = page.height
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        
                        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        
                        localPageCount++
                        val pageInfo = AndroidPdfDocument.PageInfo.Builder(width, height, localPageCount).create()
                        val docPage = pdfDocument.startPage(pageInfo)
                        val canvas = docPage.canvas
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                        pdfDocument.finishPage(docPage)
                        
                        bitmap.recycle()
                        page.close()
                    }

                    if (localPageCount > 0) {
                        val outputDir = File(context.filesDir, "documents").apply { mkdirs() }
                        val cleanBaseName = originalName.substringBeforeLast(".pdf")
                        val outputName = "split_${cleanBaseName}_pages_${range.start}_to_${range.end}.pdf"
                        val outputFile = File(outputDir, outputName)

                        val fos = outputFile.outputStream()
                        try {
                            pdfDocument.writeTo(fos)
                        } finally {
                            fos.close()
                        }
                        
                        result.add(
                            PdfDocument(
                                uriString = Uri.fromFile(outputFile).toString(),
                                name = outputName,
                                pageCount = localPageCount,
                                fileSizeBytes = outputFile.length(),
                                lastModifiedMillis = System.currentTimeMillis(),
                                additionType = "Split"
                            )
                        )
                    }
                    pdfDocument.close()
                }
                renderer.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    override suspend fun stampSignature(
        sourceUri: String,
        signatureBitmap: Bitmap,
        placement: SignaturePlacement,
        outputName: String
    ): PdfDocument = withContext(ioDispatcher) {
        val pdfDocument = AndroidPdfDocument()
        val uri = Uri.parse(sourceUri)
        val outputDir = File(context.filesDir, "documents").apply { mkdirs() }
        val finalOutputName = if (outputName.endsWith(".pdf", ignoreCase = true)) outputName else "$outputName.pdf"
        val outputFile = File(outputDir, finalOutputName)

        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val renderer = android.graphics.pdf.PdfRenderer(pfd)
                var pagesWritten = 0

                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val width = page.width
                    val height = page.height
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                    page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val pageInfo = AndroidPdfDocument.PageInfo.Builder(width, height, i + 1).create()
                    val docPage = pdfDocument.startPage(pageInfo)
                    val canvas = docPage.canvas

                    canvas.drawBitmap(bitmap, 0f, 0f, null)

                    if (i in placement.pageSizes) {
                        val pageSize = placement.pageSizes.getValue(i)
                        val destRect = computeBottomCenterSignatureRect(
                            pageWidth = width,
                            pageHeight = height,
                            pageSize = pageSize,
                            bottomMarginRatio = placement.bottomMarginRatio
                        )
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                        canvas.drawBitmap(signatureBitmap, null, destRect, paint)
                    }

                    pdfDocument.finishPage(docPage)
                    pagesWritten++
                    bitmap.recycle()
                    page.close()
                }
                renderer.close()

                if (pagesWritten == 0) {
                    throw IllegalStateException("Could not read any pages from the selected PDF.")
                }
            } ?: throw IllegalStateException("Could not open the selected PDF.")
        } catch (e: Exception) {
            pdfDocument.close()
            throw e
        }

        val fos = outputFile.outputStream()
        try {
            pdfDocument.writeTo(fos)
        } finally {
            fos.close()
        }
        pdfDocument.close()

        val pageCount = getPdfPageCount(Uri.fromFile(outputFile).toString())

        PdfDocument(
            uriString = Uri.fromFile(outputFile).toString(),
            name = finalOutputName,
            pageCount = pageCount,
            fileSizeBytes = outputFile.length(),
            lastModifiedMillis = System.currentTimeMillis(),
            additionType = "Signed"
        )
    }

    override suspend fun convertMarkdownToPdf(
        markdownText: String,
        outputName: String
    ): PdfDocument = withContext(ioDispatcher) {
        val pdfDocument = AndroidPdfDocument()
        val outputDir = File(context.filesDir, "documents").apply { mkdirs() }
        val finalOutputName = if (outputName.endsWith(".pdf", ignoreCase = true)) outputName else "$outputName.pdf"
        val outputFile = File(outputDir, finalOutputName)

        val pageWidth = 595 // Standard A4 points: 72 points per inch (595 x 842)
        val pageHeight = 842
        val margin = 50f
        val printableWidth = pageWidth - (margin * 2)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            color = Color.BLACK
        }

        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.parseColor("#1A56DB") // Modern Indigo Primary Accent Color
        }

        val subHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.parseColor("#1F2937")
        }

        val contentLines = markdownText.split("\n")
        var currentY = margin
        var pageNum = 1

        var pageInfo = AndroidPdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        contentLines.forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) {
                currentY += 16f
                return@forEach
            }

            val (paint, cleanText, leadingSpacing) = when {
                line.startsWith("# ") -> Triple(headerPaint, line.removePrefix("# "), 28f)
                line.startsWith("## ") -> Triple(subHeaderPaint, line.removePrefix("## "), 22f)
                else -> Triple(textPaint, line, 14f)
            }

            // Using standard static text wrapping layouts
            val layout = StaticLayout.Builder.obtain(cleanText, 0, cleanText.length, paint, printableWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.1f)
                .build()

            // Page wrapping check
            if (currentY + layout.height + leadingSpacing > pageHeight - margin) {
                pdfDocument.finishPage(page)
                pageNum++
                pageInfo = AndroidPdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }

            currentY += leadingSpacing
            canvas.save()
            canvas.translate(margin, currentY)
            layout.draw(canvas)
            canvas.restore()
            currentY += layout.height
        }

        pdfDocument.finishPage(page)

        val fos = outputFile.outputStream()
        try {
            pdfDocument.writeTo(fos)
        } finally {
            fos.close()
        }
        pdfDocument.close()

        PdfDocument(
            uriString = Uri.fromFile(outputFile).toString(),
            name = finalOutputName,
            pageCount = pageNum,
            fileSizeBytes = outputFile.length(),
            lastModifiedMillis = System.currentTimeMillis(),
            additionType = "Converted"
        )
    }

    override suspend fun getPdfPageCount(uriString: String): Int = withContext(ioDispatcher) {
        var count = 0
        try {
            DocumentActions.openPdfDescriptor(context, uriString)?.use { pfd ->
                val renderer = android.graphics.pdf.PdfRenderer(pfd)
                count = renderer.pageCount
                renderer.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        count
    }

    private fun computeBottomCenterSignatureRect(
        pageWidth: Int,
        pageHeight: Int,
        pageSize: PageSignatureSize,
        bottomMarginRatio: Float
    ): android.graphics.RectF {
        val sigW = pageWidth * pageSize.widthRatio
        val sigH = pageHeight * pageSize.heightRatio
        val marginBottom = pageHeight * bottomMarginRatio
        val x = (pageWidth - sigW) / 2f
        val y = pageHeight - sigH - marginBottom
        return android.graphics.RectF(x, y, x + sigW, y + sigH)
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return uri.path?.substringAfterLast('/') ?: "document.pdf"
    }
}
