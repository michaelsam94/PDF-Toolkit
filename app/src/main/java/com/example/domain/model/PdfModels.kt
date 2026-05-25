package com.example.domain.model

import java.util.UUID

data class PdfDocument(
    val id: String = UUID.randomUUID().toString(),
    val uriString: String,
    val name: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val lastModifiedMillis: Long,
    val additionType: String = "Imported" // Imported, Merged, Signed, Converted
) {
    val sizeLabel: String
        get() = when {
            fileSizeBytes < 1024 -> "$fileSizeBytes B"
            fileSizeBytes < 1024 * 1024 -> String.format("%.1f KB", fileSizeBytes / 1024f)
            else -> String.format("%.1f MB", fileSizeBytes / (1024f * 1024f))
        }
}

data class PageRange(
    val start: Int,
    val end: Int
)

data class SignaturePlacement(
    val pageIndex: Int,
    val xRatio: Float,
    val yRatio: Float,
    val widthRatio: Float
)
