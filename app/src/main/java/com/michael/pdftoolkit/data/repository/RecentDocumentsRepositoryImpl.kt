package com.michael.pdftoolkit.data.repository

import com.michael.pdftoolkit.data.local.RecentDocumentDao
import com.michael.pdftoolkit.data.local.RecentDocumentEntity
import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository
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
