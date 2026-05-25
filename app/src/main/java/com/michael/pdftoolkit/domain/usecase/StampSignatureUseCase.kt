package com.michael.pdftoolkit.domain.usecase

import android.graphics.Bitmap
import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.model.SignaturePlacement
import com.michael.pdftoolkit.domain.repository.PdfRepository
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository

class StampSignatureUseCase(
    private val pdfRepository: PdfRepository,
    private val recentRepo: RecentDocumentsRepository
) {
    suspend operator fun invoke(
        sourceUri: String,
        signatureBitmap: Bitmap,
        placement: SignaturePlacement,
        outputName: String
    ): PdfDocument {
        val stamped = pdfRepository.stampSignature(sourceUri, signatureBitmap, placement, outputName)
        recentRepo.addDocument(stamped)
        return stamped
    }
}
