package com.michael.pdftoolkit.domain.usecase

import com.michael.pdftoolkit.domain.model.PageRange
import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.repository.PdfRepository
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository

class SplitPdfUseCase(
    private val pdfRepository: PdfRepository,
    private val recentRepo: RecentDocumentsRepository
) {
    suspend operator fun invoke(
        sourceUri: String,
        ranges: List<PageRange>
    ): List<PdfDocument> {
        val splits = pdfRepository.splitPdf(sourceUri, ranges)
        splits.forEach {
            recentRepo.addDocument(it)
        }
        return splits
    }
}
