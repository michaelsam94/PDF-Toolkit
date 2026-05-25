package com.example.domain.usecase

import com.example.domain.model.PageRange
import com.example.domain.model.PdfDocument
import com.example.domain.repository.PdfRepository
import com.example.domain.repository.RecentDocumentsRepository

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
