package com.michael.pdftoolkit.domain.usecase

import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.repository.PdfRepository
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository

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
