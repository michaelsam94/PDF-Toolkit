package com.example.domain.repository

import android.graphics.Bitmap
import com.example.domain.model.PageRange
import com.example.domain.model.PdfDocument
import com.example.domain.model.SignaturePlacement

interface PdfRepository {
    suspend fun mergePdfs(
        sourceUris: List<String>,
        outputName: String,
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): PdfDocument

    suspend fun splitPdf(
        sourceUri: String,
        ranges: List<PageRange>
    ): List<PdfDocument>

    suspend fun stampSignature(
        sourceUri: String,
        signatureBitmap: Bitmap,
        placement: SignaturePlacement,
        outputName: String
    ): PdfDocument

    suspend fun convertMarkdownToPdf(
        markdownText: String,
        outputName: String
    ): PdfDocument

    suspend fun getPdfPageCount(uriString: String): Int
}
