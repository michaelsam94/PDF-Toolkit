package com.example.domain.usecase

import android.graphics.Bitmap
import com.example.domain.model.PdfDocument
import com.example.domain.model.SignaturePlacement
import com.example.domain.repository.PdfRepository
import com.example.domain.repository.RecentDocumentsRepository

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
