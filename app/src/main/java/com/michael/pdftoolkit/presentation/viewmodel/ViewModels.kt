package com.michael.pdftoolkit.presentation.viewmodel

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.michael.pdftoolkit.domain.model.PageRange
import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.data.repository.SavedSignaturesRepository
import com.michael.pdftoolkit.domain.model.SavedSignature
import com.michael.pdftoolkit.domain.model.PageSignatureSize
import com.michael.pdftoolkit.domain.model.SignaturePlacement
import com.michael.pdftoolkit.domain.repository.RecentDocumentsRepository
import com.michael.pdftoolkit.domain.usecase.MarkdownToPdfUseCase
import com.michael.pdftoolkit.domain.usecase.MergePdfsUseCase
import com.michael.pdftoolkit.domain.usecase.SplitPdfUseCase
import com.michael.pdftoolkit.domain.usecase.StampSignatureUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ==========================================
// 1. HOME VIEW MODEL
// ==========================================
class HomeViewModel(
    private val recentRepo: RecentDocumentsRepository
) : ViewModel() {

    val recentDocuments = recentRepo.getRecentDocuments().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            recentRepo.removeDocument(id)
        }
    }

    fun addDocumentToHistory(document: PdfDocument) {
        viewModelScope.launch {
            recentRepo.addDocument(document)
        }
    }
}

// ==========================================
// 2. MERGE VIEW MODEL
// ==========================================
data class PdfMergeItem(
    val uriString: String,
    val name: String,
    val pageCount: Int,
    val sizeLabel: String
)

data class MergeUiState(
    val selectedFiles: List<PdfMergeItem> = emptyList(),
    val isProcessing: Boolean = false,
    val progressMessage: String = "",
    val error: String? = null,
    val mergedDocument: PdfDocument? = null
)

class MergeViewModel(
    private val mergePdfsUseCase: MergePdfsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MergeUiState())
    val state: StateFlow<MergeUiState> = _state.asStateFlow()

    fun addFileForMerge(item: PdfMergeItem) {
        _state.update { original ->
            if (original.selectedFiles.any { it.uriString == item.uriString }) {
                original // Skip duplicates
            } else {
                original.copy(selectedFiles = original.selectedFiles + item, error = null)
            }
        }
    }

    fun removeFileFromMerge(uriString: String) {
        _state.update { original ->
            original.copy(selectedFiles = original.selectedFiles.filterNot { it.uriString == uriString })
        }
    }

    fun reorderFiles(fromIndex: Int, toIndex: Int) {
        _state.update { original ->
            val list = original.selectedFiles.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            original.copy(selectedFiles = list)
        }
    }

    fun startMerge(outputName: String) {
        val files = _state.value.selectedFiles
        if (files.size < 2) {
            _state.update { it.copy(error = "Please select at least 2 PDF documents to merge.") }
            return
        }

        val cleanName = outputName.trim().ifEmpty { "merged_document" }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progressMessage = "Initializing...", error = null, mergedDocument = null) }
            try {
                val result = mergePdfsUseCase(
                    sourceUris = files.map { it.uriString },
                    outputName = cleanName,
                    onProgress = { current, total ->
                        _state.update { it.copy(progressMessage = "Combining file $current of $total...") }
                    }
                )
                _state.update { it.copy(isProcessing = false, mergedDocument = result, selectedFiles = emptyList()) }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, error = e.localizedMessage ?: "Merge failed. Verify chosen files are not encrypted.") }
            }
        }
    }

    fun clearResult() {
        _state.update { it.copy(mergedDocument = null, error = null) }
    }
}

// ==========================================
// 3. SPLIT VIEW MODEL
// ==========================================
data class SplitUiState(
    val sourceUriString: String? = null,
    val sourceName: String = "",
    val sourcePageCount: Int = 0,
    val ranges: List<PageRange> = emptyList(),
    val isProcessing: Boolean = false,
    val error: String? = null,
    val splitResults: List<PdfDocument> = emptyList()
)

class SplitViewModel(
    private val splitPdfUseCase: SplitPdfUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplitUiState())
    val state: StateFlow<SplitUiState> = _state.asStateFlow()

    fun setSourceDocument(uriString: String, name: String, pageCount: Int) {
        _state.update {
            SplitUiState(
                sourceUriString = uriString,
                sourceName = name,
                sourcePageCount = pageCount,
                ranges = emptyList()
            )
        }
    }

    fun addRange(start: Int, end: Int) {
        val maxPages = _state.value.sourcePageCount
        if (start < 1 || end > maxPages || start > end) {
            _state.update { it.copy(error = "Invalid ranges. Must be between page 1 and $maxPages.") }
            return
        }
        _state.update { original ->
            original.copy(ranges = original.ranges + PageRange(start, end), error = null)
        }
    }

    fun removeRange(index: Int) {
        _state.update { original ->
            original.copy(ranges = original.ranges.filterIndexed { i, _ -> i != index })
        }
    }

    fun startSplit() {
        val uri = _state.value.sourceUriString
        val ranges = _state.value.ranges
        if (uri == null) {
            _state.update { it.copy(error = "Please select a source PDF document first.") }
            return
        }
        if (ranges.isEmpty()) {
            _state.update { it.copy(error = "Please add at least one page split range.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, error = null, splitResults = emptyList()) }
            try {
                val results = splitPdfUseCase(uri, ranges)
                _state.update { it.copy(isProcessing = false, splitResults = results, ranges = emptyList()) }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, error = e.localizedMessage ?: "Split operation failed.") }
            }
        }
    }

    fun clearResult() {
        _state.update { it.copy(splitResults = emptyList(), error = null) }
    }
}

// ==========================================
// 4. SIGN VIEW MODEL
// ==========================================
data class SignUiState(
    val sourceUriString: String? = null,
    val sourceName: String = "",
    val sourcePageCount: Int = 0,
    val selectedPageIndex: Int = 0,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val signedDocument: PdfDocument? = null,
    val savedSignatures: List<SavedSignature> = emptyList()
)

class SignViewModel(
    private val stampSignatureUseCase: StampSignatureUseCase,
    private val savedSignaturesRepository: SavedSignaturesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignUiState())
    val state: StateFlow<SignUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            savedSignaturesRepository.observeAll().collect { signatures ->
                _state.update { it.copy(savedSignatures = signatures) }
            }
        }
    }

    fun setSourceDocument(uriString: String, name: String, pageCount: Int) {
        _state.update {
            it.copy(
                sourceUriString = uriString,
                sourceName = name,
                sourcePageCount = pageCount,
                selectedPageIndex = 0,
                signedDocument = null,
                error = null
            )
        }
    }

    fun selectPage(index: Int) {
        if (index in 0 until _state.value.sourcePageCount) {
            _state.update { it.copy(selectedPageIndex = index) }
        }
    }

    fun stampSignature(
        signatureBitmap: Bitmap,
        pageSizes: Map<Int, PageSignatureSize>,
        outputName: String
    ) {
        val uri = _state.value.sourceUriString ?: return
        if (pageSizes.isEmpty()) {
            _state.update { it.copy(error = "Select at least one page to sign.") }
            return
        }

        val placement = SignaturePlacement(pageSizes = pageSizes)

        val cleanName = outputName.trim().ifEmpty { "signed_${_state.value.sourceName}" }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, error = null, signedDocument = null) }
            try {
                val result = stampSignatureUseCase(
                    sourceUri = uri,
                    signatureBitmap = signatureBitmap,
                    placement = placement,
                    outputName = cleanName
                )
                _state.update { it.copy(isProcessing = false, signedDocument = result) }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, error = e.localizedMessage ?: "Failed to sign PDF.") }
            }
        }
    }

    fun clearResult() {
        _state.update { it.copy(signedDocument = null, error = null) }
    }

    fun saveSignature(
        name: String,
        strokes: List<List<Offset>>,
        strokeColor: Color,
        strokeWidth: Float
    ) {
        if (name.isBlank() || strokes.isEmpty()) return
        viewModelScope.launch {
            savedSignaturesRepository.save(name, strokes, strokeColor, strokeWidth)
        }
    }

    fun deleteSavedSignature(id: String) {
        viewModelScope.launch {
            savedSignaturesRepository.delete(id)
        }
    }
}

// ==========================================
// 5. CONVERT VIEW MODEL
// ==========================================
data class ConvertUiState(
    val markdownText: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val convertedDocument: PdfDocument? = null
)

class ConvertViewModel(
    private val markdownToPdfUseCase: MarkdownToPdfUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ConvertUiState())
    val state: StateFlow<ConvertUiState> = _state.asStateFlow()

    fun updateMarkdownText(text: String) {
        _state.update { it.copy(markdownText = text) }
    }

    fun startConversion(outputName: String) {
        val text = _state.value.markdownText.trim()
        if (text.isEmpty()) {
            _state.update { it.copy(error = "Markdown/Text content cannot be empty.") }
            return
        }

        val cleanName = outputName.trim().ifEmpty { "parsed_document" }

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, error = null, convertedDocument = null) }
            try {
                val result = markdownToPdfUseCase(text, cleanName)
                _state.update { it.copy(isProcessing = false, convertedDocument = result, markdownText = "") }
            } catch (e: Exception) {
                _state.update { it.copy(isProcessing = false, error = e.localizedMessage ?: "Conversion failed.") }
            }
        }
    }

    fun clearResult() {
        _state.update { it.copy(convertedDocument = null, error = null) }
    }
}

// ==========================================
// 6. SHARED VIEWMODEL PROVIDER FACTORY
// ==========================================
class PdfViewModelFactory(
    private val app: com.michael.pdftoolkit.PdfToolkitApp
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(app.recentRepo) as T
            }
            modelClass.isAssignableFrom(MergeViewModel::class.java) -> {
                MergeViewModel(app.mergePdfsUseCase) as T
            }
            modelClass.isAssignableFrom(SplitViewModel::class.java) -> {
                SplitViewModel(app.splitPdfUseCase) as T
            }
            modelClass.isAssignableFrom(SignViewModel::class.java) -> {
                SignViewModel(app.stampSignatureUseCase, app.savedSignaturesRepository) as T
            }
            modelClass.isAssignableFrom(ConvertViewModel::class.java) -> {
                ConvertViewModel(app.markdownToPdfUseCase) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
