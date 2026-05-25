package com.michael.pdftoolkit.playstore

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.michael.pdftoolkit.PdfToolkitApp
import com.michael.pdftoolkit.data.repository.SavedSignaturesRepository
import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository
import com.michael.pdftoolkit.presentation.viewmodel.MergeViewModel
import com.michael.pdftoolkit.presentation.viewmodel.PdfMergeItem
import com.michael.pdftoolkit.presentation.viewmodel.SignViewModel
import com.michael.pdftoolkit.presentation.viewmodel.SplitViewModel

object PlayStoreTestFixtures {
  private val sampleDocuments =
    listOf(
      PdfDocument(
        id = "doc-annual-report",
        uriString = "content://playstore/annual_report.pdf",
        name = "Annual_Report_2025.pdf",
        pageCount = 24,
        fileSizeBytes = 2_200_000L,
        lastModifiedMillis = 1_746_000_000_000L,
        additionType = "Imported",
      ),
      PdfDocument(
        id = "doc-contract",
        uriString = "content://playstore/service_contract.pdf",
        name = "Service_Contract_Draft.pdf",
        pageCount = 8,
        fileSizeBytes = 640_000L,
        lastModifiedMillis = 1_745_900_000_000L,
        additionType = "Imported",
      ),
      PdfDocument(
        id = "doc-invoice",
        uriString = "content://playstore/invoice_march.pdf",
        name = "Invoice_March.pdf",
        pageCount = 2,
        fileSizeBytes = 180_000L,
        lastModifiedMillis = 1_745_800_000_000L,
        additionType = "Imported",
      ),
      PdfDocument(
        id = "doc-merged",
        uriString = "content://playstore/merged_proposal.pdf",
        name = "Merged_Proposal.pdf",
        pageCount = 15,
        fileSizeBytes = 1_450_000L,
        lastModifiedMillis = 1_745_700_000_000L,
        additionType = "Merged",
      ),
    )

  suspend fun seedRecentDocuments(recentRepo: RecentDocumentsRepository) {
    sampleDocuments.forEach { recentRepo.addDocument(it) }
  }

  fun seedMerge(viewModel: MergeViewModel) {
    listOf(
      PdfMergeItem(
        uriString = "content://playstore/chapter_1.pdf",
        name = "Chapter_01_Intro.pdf",
        pageCount = 12,
        sizeLabel = "1.2 MB",
      ),
      PdfMergeItem(
        uriString = "content://playstore/chapter_2.pdf",
        name = "Chapter_02_Methods.pdf",
        pageCount = 18,
        sizeLabel = "2.4 MB",
      ),
      PdfMergeItem(
        uriString = "content://playstore/appendix.pdf",
        name = "Appendix_References.pdf",
        pageCount = 6,
        sizeLabel = "0.8 MB",
      ),
    ).forEach { viewModel.addFileForMerge(it) }
  }

  fun seedSplit(viewModel: SplitViewModel) {
    viewModel.setSourceDocument(
      uriString = "content://playstore/quarterly_review.pdf",
      name = "Quarterly_Review_Q1.pdf",
      pageCount = 20,
    )
    viewModel.addRange(start = 1, end = 5)
    viewModel.addRange(start = 6, end = 12)
  }

  fun seedSign(viewModel: SignViewModel) {
    viewModel.setSourceDocument(
      uriString = "content://playstore/nda_agreement.pdf",
      name = "NDA_Agreement_v3.pdf",
      pageCount = 4,
    )
  }

  suspend fun seedSavedSignatures(savedSignaturesRepository: SavedSignaturesRepository) {
    val strokes =
      listOf(
        listOf(Offset(10f, 40f), Offset(60f, 20f), Offset(110f, 45f)),
      )
    savedSignaturesRepository.save(
      name = "Work Signature",
      strokes = strokes,
      strokeColor = Color.Black,
      strokeWidth = 6f,
    )
  }

  suspend fun seedAll(app: PdfToolkitApp) {
    app.appDatabase.recentDocumentDao().deleteAll()
    app.appDatabase.savedSignatureDao().deleteAll()
    seedRecentDocuments(app.recentRepo)
    seedSavedSignatures(app.savedSignaturesRepository)
  }
}
