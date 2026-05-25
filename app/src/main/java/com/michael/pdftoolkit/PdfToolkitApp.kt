package com.michael.pdftoolkit

import android.app.Application
import com.michael.pdftoolkit.data.local.AppDatabase
import com.michael.pdftoolkit.data.repository.PdfRepositoryImpl
import com.michael.pdftoolkit.data.repository.RecentDocumentsRepositoryImpl
import com.michael.pdftoolkit.data.repository.SavedSignaturesRepository
import com.michael.pdftoolkit.domain.repository.PdfRepository
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository
import com.michael.pdftoolkit.domain.usecase.MarkdownToPdfUseCase
import com.michael.pdftoolkit.domain.usecase.MergePdfsUseCase
import com.michael.pdftoolkit.domain.usecase.SplitPdfUseCase
import com.michael.pdftoolkit.domain.usecase.StampSignatureUseCase

class PdfToolkitApp : Application() {
    
    lateinit var appDatabase: AppDatabase
    lateinit var pdfRepository: PdfRepository
    lateinit var recentRepo: RecentDocumentsRepository
    lateinit var savedSignaturesRepository: SavedSignaturesRepository

    lateinit var mergePdfsUseCase: MergePdfsUseCase
    lateinit var splitPdfUseCase: SplitPdfUseCase
    lateinit var stampSignatureUseCase: StampSignatureUseCase
    lateinit var markdownToPdfUseCase: MarkdownToPdfUseCase

    override fun onCreate() {
        super.onCreate()
        
        appDatabase = AppDatabase.getDatabase(this)
        pdfRepository = PdfRepositoryImpl(this)
        recentRepo = RecentDocumentsRepositoryImpl(appDatabase.recentDocumentDao())
        savedSignaturesRepository = SavedSignaturesRepository(appDatabase.savedSignatureDao())

        mergePdfsUseCase = MergePdfsUseCase(pdfRepository, recentRepo)
        splitPdfUseCase = SplitPdfUseCase(pdfRepository, recentRepo)
        stampSignatureUseCase = StampSignatureUseCase(pdfRepository, recentRepo)
        markdownToPdfUseCase = MarkdownToPdfUseCase(pdfRepository, recentRepo)
    }
}
