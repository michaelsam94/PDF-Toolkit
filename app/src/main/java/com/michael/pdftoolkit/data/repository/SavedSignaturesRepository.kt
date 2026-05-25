package com.michael.pdftoolkit.data.repository

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.michael.pdftoolkit.data.local.SavedSignatureDao
import com.michael.pdftoolkit.data.local.SavedSignatureEntity
import com.michael.pdftoolkit.domain.model.SavedSignature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedSignaturesRepository(
    private val dao: SavedSignatureDao
) {
    fun observeAll(): Flow<List<SavedSignature>> {
        return dao.getAll().map { entities -> entities.map { it.toDomainModel() } }
    }

    suspend fun save(
        name: String,
        strokes: List<List<Offset>>,
        strokeColor: Color,
        strokeWidth: Float
    ) {
        dao.insert(
            SavedSignatureEntity.fromDomainModel(
                name = name,
                strokes = strokes,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth
            )
        )
    }

    suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}
