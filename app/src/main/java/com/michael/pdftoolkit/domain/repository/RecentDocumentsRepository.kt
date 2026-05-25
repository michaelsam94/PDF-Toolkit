package com.michael.pdftoolkit.domain.repository

import com.michael.pdftoolkit.domain.model.PdfDocument
import kotlinx.coroutines.flow.Flow

interface RecentDocumentsRepository {
    fun getRecentDocuments(): Flow<List<PdfDocument>>
    suspend fun addDocument(document: PdfDocument)
    suspend fun removeDocument(id: String)
}
