package com.michael.pdftoolkit.domain.usecase

import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.repository.PdfRepository
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository

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
