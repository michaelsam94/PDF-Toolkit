package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.PdfRepositoryImpl
import com.example.data.repository.RecentDocumentsRepositoryImpl
import com.example.domain.repository.PdfRepository
import com.example.domain.repository.RecentDocumentsRepository
import com.example.domain.usecase.MarkdownToPdfUseCase
import com.example.domain.usecase.MergePdfsUseCase
import com.example.domain.usecase.SplitPdfUseCase
import com.example.domain.usecase.StampSignatureUseCase

class PdfToolkitApp : Application() {
    
    lateinit var appDatabase: AppDatabase
    lateinit var pdfRepository: PdfRepository
    lateinit var recentRepo: RecentDocumentsRepository

    lateinit var mergePdfsUseCase: MergePdfsUseCase
    lateinit var splitPdfUseCase: SplitPdfUseCase
    lateinit var stampSignatureUseCase: StampSignatureUseCase
    lateinit var markdownToPdfUseCase: MarkdownToPdfUseCase

    override fun onCreate() {
        super.onCreate()
        
        appDatabase = AppDatabase.getDatabase(this)
        pdfRepository = PdfRepositoryImpl(this)
        recentRepo = RecentDocumentsRepositoryImpl(appDatabase.recentDocumentDao())

        mergePdfsUseCase = MergePdfsUseCase(pdfRepository, recentRepo)
        splitPdfUseCase = SplitPdfUseCase(pdfRepository, recentRepo)
        stampSignatureUseCase = StampSignatureUseCase(pdfRepository, recentRepo)
        markdownToPdfUseCase = MarkdownToPdfUseCase(pdfRepository, recentRepo)
    }
}
