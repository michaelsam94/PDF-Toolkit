package com.michael.pdftoolkit.data.local

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.michael.pdftoolkit.domain.model.SavedSignature
import com.michael.pdftoolkit.util.SignatureStrokeCodec
import java.util.UUID

@Entity(tableName = "saved_signatures")
data class SavedSignatureEntity(
    @PrimaryKey val id: String,
    val name: String,
    val strokesJson: String,
    val strokeColorArgb: Int,
    val strokeWidth: Float,
    val savedAtMillis: Long
) {
    fun toDomainModel(): SavedSignature {
        return SavedSignature(
            id = id,
            name = name,
            strokes = SignatureStrokeCodec.decode(strokesJson),
            strokeColor = Color(strokeColorArgb),
            strokeWidth = strokeWidth,
            savedAtMillis = savedAtMillis
        )
    }

    companion object {
        fun fromDomainModel(
            name: String,
            strokes: List<List<Offset>>,
            strokeColor: Color,
            strokeWidth: Float
        ): SavedSignatureEntity {
            return SavedSignatureEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                strokesJson = SignatureStrokeCodec.encode(strokes),
                strokeColorArgb = strokeColor.toArgb(),
                strokeWidth = strokeWidth,
                savedAtMillis = System.currentTimeMillis()
            )
        }
    }
}
