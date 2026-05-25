package com.example.domain.usecase

import com.example.domain.model.PdfDocument
import com.example.domain.repository.PdfRepository
import com.example.domain.repository.RecentDocumentsRepository

class MarkdownToPdfUseCase(
    private val pdfRepository: PdfRepository,
    private val recentRepo: RecentDocumentsRepository
) {
    suspend operator fun invoke(
        markdownText: String,
        outputName: String
    ): PdfDocument {
        val converted = pdfRepository.convertMarkdownToPdf(markdownText, outputName)
        recentRepo.addDocument(converted)
        return converted
    }
}
