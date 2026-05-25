package com.michael.pdftoolkit.domain.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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

data class PageSignatureSize(
    val widthRatio: Float,
    val heightRatio: Float
)

data class SignaturePlacement(
    val pageSizes: Map<Int, PageSignatureSize>,
    val bottomMarginRatio: Float = 0.05f
)

data class SavedSignature(
    val id: String,
    val name: String,
    val strokes: List<List<Offset>>,
    val strokeColor: Color,
    val strokeWidth: Float,
    val savedAtMillis: Long
)
