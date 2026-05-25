package com.example.data.repository

import com.example.data.local.RecentDocumentDao
import com.example.data.local.RecentDocumentEntity
import com.example.domain.model.PdfDocument
import com.example.domain.repository.RecentDocumentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecentDocumentsRepositoryImpl(
    private val dao: RecentDocumentDao
) : RecentDocumentsRepository {

    override fun getRecentDocuments(): Flow<List<PdfDocument>> {
        return dao.getAllDocuments().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addDocument(document: PdfDocument) {
        dao.insertDocument(RecentDocumentEntity.fromDomainModel(document))
    }

    override suspend fun removeDocument(id: String) {
        dao.deleteDocumentById(id)
    }
}
