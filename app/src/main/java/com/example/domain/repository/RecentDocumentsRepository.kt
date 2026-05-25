package com.example.domain.repository

import com.example.domain.model.PdfDocument
import kotlinx.coroutines.flow.Flow

interface RecentDocumentsRepository {
    fun getRecentDocuments(): Flow<List<PdfDocument>>
    suspend fun addDocument(document: PdfDocument)
    suspend fun removeDocument(id: String)
}
