package com.example.domain.usecase

import com.example.domain.model.PdfDocument
import com.example.domain.repository.PdfRepository
import com.example.domain.repository.RecentDocumentsRepository

class MergePdfsUseCase(
    private val pdfRepository: PdfRepository,
    private val recentRepo: RecentDocumentsRepository
) {
    suspend operator fun invoke(
        sourceUris: List<String>,
        outputName: String,
        onProgress: suspend (current: Int, total: Int) -> Unit
    ): PdfDocument {
        val merged = pdfRepository.mergePdfs(sourceUris, outputName, onProgress)
        recentRepo.addDocument(merged)
        return merged
    }
}
