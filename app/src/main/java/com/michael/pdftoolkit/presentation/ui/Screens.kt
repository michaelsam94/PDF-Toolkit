package com.michael.pdftoolkit.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michael.pdftoolkit.domain.model.PageRange
import com.michael.pdftoolkit.domain.model.PageSignatureSize
import com.michael.pdftoolkit.domain.model.PdfDocument
import com.michael.pdftoolkit.domain.model.SavedSignature
import com.michael.pdftoolkit.presentation.viewmodel.ConvertViewModel
import com.michael.pdftoolkit.presentation.viewmodel.HomeViewModel
import com.michael.pdftoolkit.presentation.viewmodel.MergeViewModel
import com.michael.pdftoolkit.presentation.viewmodel.PdfMergeItem
import com.michael.pdftoolkit.presentation.viewmodel.SignViewModel
import com.michael.pdftoolkit.presentation.viewmodel.SplitViewModel
import com.michael.pdftoolkit.util.DocumentActions
import com.michael.pdftoolkit.util.PageSelectionParser
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Helper to determine metadata details safely via Storage Access Framework
fun queryUriMetadata(context: Context, uri: Uri): Pair<String, Long> {
    var name = "unknown_document.pdf"
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) name = cursor.getString(nameIndex) ?: "document.pdf"
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    // If name retrieval is empty, create a placeholder
    if (name.trim().isEmpty() || name.startsWith("unknown")) {
        name = "imported_${System.currentTimeMillis().toString().takeLast(6)}.pdf"
    }
    return Pair(name, size)
}

fun shareDocument(context: Context, uriString: String) {
    DocumentActions.shareDocument(context, uriString)
}

fun openDocument(context: Context, uriString: String) {
    DocumentActions.openDocument(context, uriString)
}

// Format Unix Mills readable label
fun formatDateTimeLabel(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        "Just now"
    }
}

// ==========================================
// 1. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTab: (String) -> Unit,
    initialSearchQuery: String = "",
) {
    val context = LocalContext.current
    val recentList by viewModel.recentDocuments.collectAsState()
    var searchQuery by remember { mutableStateOf(initialSearchQuery) }

    val filteredRecentList = remember(recentList, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            recentList
        } else {
            recentList.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // SAF Selection launcher to allow user manual PDF file Import
    val fileImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val (name, size) = queryUriMetadata(context, uri)
            // Resolve safe page count via background loading
            try {
                // Persist read status permissions for system reboot
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (pe: Exception) {
                    pe.printStackTrace()
                }

                val targetDoc = PdfDocument(
                    uriString = uri.toString(),
                    name = name,
                    pageCount = 1, // Will evaluate dynamically when used, fallback default to 1
                    fileSizeBytes = size,
                    lastModifiedMillis = System.currentTimeMillis(),
                    additionType = "Imported"
                )
                viewModel.addDocumentToHistory(targetDoc)
                Toast.makeText(context, "Successfully Imported: $name", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading document: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search recent documents") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Merge",
                icon = Icons.Default.Merge,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("merge_split") }
            )
            QuickActionButton(
                title = "Split",
                icon = Icons.Default.CallSplit,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("merge_split") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Sign",
                icon = Icons.Default.Fingerprint,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("sign") }
            )
            QuickActionButton(
                title = "Convert",
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab("convert") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "All PDF operations stay on your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent documents
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (searchQuery.isNotEmpty()) "Search results" else "Recent documents",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${filteredRecentList.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History items rendering state
        if (filteredRecentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "No files found",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matches found" else "No recent documents",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search" else "Import a PDF or use a tool above.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRecentList, key = { it.id }) { doc ->
                    PdfFileHistoryCard(
                        doc = doc,
                        onOpenClick = { openDocument(context, doc.uriString) },
                        onDeleteClick = { viewModel.deleteDocument(doc.id) },
                        onShareClick = { shareDocument(context, doc.uriString) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Import button
        Button(
            onClick = { fileImportLauncher.launch(arrayOf("application/pdf")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import PDF")
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PdfFileHistoryCard(
    doc: PdfDocument,
    onOpenClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        if (destinationUri != null) {
            DocumentActions.exportToUri(context, doc.uriString, destinationUri)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenClick)
            .testTag("task_item_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    PdfThumbnail(
                        uriString = doc.uriString,
                        pageIndex = 0,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val badgeColor = when (doc.additionType.lowercase()) {
                        "merged" -> Color(0xFF6750A4)
                        "signed" -> Color(0xFF0F9F6E)
                        "split" -> Color(0xFFB45309)
                        "converted" -> Color(0xFF6D28D9)
                        else -> Color(0xFF49454F)
                    }

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
                                .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = doc.additionType,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = badgeColor
                            )
                        }

                        Text(
                            text = "${doc.pageCount} ${if (doc.pageCount == 1) "page" else "pages"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = doc.sizeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatDateTimeLabel(doc.lastModifiedMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { DocumentActions.saveToDownloads(context, doc.uriString, doc.name) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Save to Downloads",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        saveLauncher.launch(
                            if (doc.name.endsWith(".pdf", ignoreCase = true)) doc.name else "${doc.name}.pdf"
                        )
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SaveAlt,
                        contentDescription = "Save to folder",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share File",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove From List",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. MERGE SCREEN
// ==========================================
@Composable
fun MergeScreenTab(
    viewModel: MergeViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var outputFileName by remember { mutableStateOf("merged_document_${System.currentTimeMillis().toString().takeLast(4)}") }

    // Multi-Select files picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            uris.forEach { uri ->
                val (name, size) = queryUriMetadata(context, uri)
                try {
                    // Persist access permissions
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) { e.printStackTrace() }

                    val item = PdfMergeItem(
                        uriString = uri.toString(),
                        name = name,
                        pageCount = 1, // Fallback default count, evaluated on merge
                        sizeLabel = String.format(Locale.getDefault(), "%.1f MB", size / (1024f * 1024f))
                    )
                    viewModel.addFileForMerge(item)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error adding file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "PDF Merger Utility",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Append multiple PDF files into one local document.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Actions selector
        OutlinedButton(
            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Files")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select PDF Files to Merge", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.selectedFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Merge,
                        contentDescription = "Merge selection empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No documents queued",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select two or more files to start.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Queue lists
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(state.selectedFiles) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                PdfThumbnail(uriString = item.uriString, pageIndex = 0, modifier = Modifier.fillMaxSize())
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Position: #${index + 1} · Size: ${item.sizeLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Index moving shifts
                            Row {
                                if (index > 0) {
                                    IconButton(onClick = { viewModel.reorderFiles(index, index - 1) }) {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up")
                                    }
                                }
                                if (index < state.selectedFiles.size - 1) {
                                    IconButton(onClick = { viewModel.reorderFiles(index, index + 1) }) {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down")
                                    }
                                }
                                IconButton(onClick = { viewModel.removeFileFromMerge(item.uriString) }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom output field name
        OutlinedTextField(
            value = outputFileName,
            onValueChange = { outputFileName = it },
            label = { Text("Output PDF Document Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = { Text(".pdf", modifier = Modifier.padding(end = 12.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicators
        if (state.isProcessing) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.progressMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Show successful results banner
        if (state.mergedDocument != null) {
            val mergedDoc = state.mergedDocument!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Success", tint = Color(0xFF065F46))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Merge Complete", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text(
                                "Generated as ${mergedDoc.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PdfExportActions(
                        uriString = mergedDoc.uriString,
                        fileName = mergedDoc.name,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.error != null) {
            Text(
                text = state.error ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.startMerge(outputFileName) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isProcessing && state.selectedFiles.size >= 2
        ) {
            Text("Compile & Merge Documents (${state.selectedFiles.size})", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 3. SPLIT SCREEN
// ==========================================
@Composable
fun SplitScreenTab(
    viewModel: SplitViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var inputStartPage by remember { mutableStateOf("1") }
    var inputEndPage by remember { mutableStateOf("1") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val (name, size) = queryUriMetadata(context, uri)
            try {
                // Read exact page count using PdfBox background
                val app = context.applicationContext as com.michael.pdftoolkit.PdfToolkitApp
                coroutineScope.launch {
                    val count = app.pdfRepository.getPdfPageCount(uri.toString())
                    viewModel.setSourceDocument(uri.toString(), name, count)
                    inputStartPage = "1"
                    inputEndPage = count.toString().coerceAtMost("5")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error verifying PDF index: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "PDF Splitter Slicer",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose a PDF file and specify page ranges to harvest new document fractions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected indicator file
        if (state.sourceUriString == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.CallSplit, contentDescription = "Select PDF", modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Step 1: Click here to select a PDF", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        PdfThumbnail(uriString = state.sourceUriString!!, pageIndex = 0, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(state.sourceName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Total pages: ${state.sourcePageCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("Change")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add sub range settings if file is chosen
        if (state.sourceUriString != null) {
            Text("Step 2: Add Split ranges to slice", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputStartPage,
                            onValueChange = { inputStartPage = it },
                            label = { Text("Start Page") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = inputEndPage,
                            onValueChange = { inputEndPage = it },
                            label = { Text("End Page") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                val start = inputStartPage.toIntOrNull() ?: 1
                                val end = inputEndPage.toIntOrNull() ?: 1
                                viewModel.addRange(start, end)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Queue")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Layout representation of ranges map
        if (state.ranges.isNotEmpty()) {
            Text("Range queue for slicing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.ranges) { index, range ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Pages ${range.start} – ${range.end}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete range",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.removeRange(index) },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isProcessing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Extracting Page Buffers...", modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Output results
        if (state.splitResults.isNotEmpty()) {
            Text("Outputs Generated (${state.splitResults.size} files)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.splitResults) { splitDoc ->
                    SplitResultCard(
                        splitDoc = splitDoc,
                        onSaveToDownloads = {
                            DocumentActions.saveToDownloads(context, splitDoc.uriString, splitDoc.name)
                        },
                        onShare = { shareDocument(context, splitDoc.uriString) }
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (state.error != null) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 6.dp))
        }

        Button(
            onClick = { viewModel.startSplit() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isProcessing && state.ranges.isNotEmpty()
        ) {
            Text("Exec Split Operations", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SplitResultCard(
    splitDoc: PdfDocument,
    onSaveToDownloads: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        if (destinationUri != null) {
            DocumentActions.exportToUri(context, splitDoc.uriString, destinationUri)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF0D8))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Split outcome", tint = Color(0xFF3C763D))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(splitDoc.name, fontWeight = FontWeight.Bold, color = Color(0xFF3C763D), maxLines = 1)
                Text("${splitDoc.pageCount} Pages · ${splitDoc.sizeLabel}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF3C763D))
            }
            IconButton(onClick = onSaveToDownloads) {
                Icon(imageVector = Icons.Default.Download, contentDescription = "Save to Downloads", tint = Color(0xFF3C763D))
            }
            IconButton(
                onClick = {
                    saveLauncher.launch(
                        if (splitDoc.name.endsWith(".pdf", ignoreCase = true)) splitDoc.name else "${splitDoc.name}.pdf"
                    )
                }
            ) {
                Icon(imageVector = Icons.Default.SaveAlt, contentDescription = "Save to folder", tint = Color(0xFF3C763D))
            }
            IconButton(onClick = onShare) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF3C763D))
            }
        }
    }
}

// ==========================================
// 4. SIGN SCREEN
// ==========================================
private val SignaturePresetColors = listOf(
    Color.Black,
    Color.Blue,
    Color.Red,
    Color(0xFF0F5132)
)

private enum class SignatureApplyMode {
    ALL,
    SINGLE,
    CUSTOM
}

private const val DefaultSignatureWidthRatio = 0.25f
private const val DefaultSignatureHeightRatio = 0.10f

private fun defaultPageSignatureSize() = PageSignatureSize(
    widthRatio = DefaultSignatureWidthRatio,
    heightRatio = DefaultSignatureHeightRatio
)

@Composable
private fun SavedSignatureCard(
    saved: SavedSignature,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(132.dp)
            .clickable(onClick = onLoad),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            SignaturePreview(
                strokes = saved.strokes,
                strokeColor = saved.strokeColor,
                strokeWidth = saved.strokeWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = saved.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete saved signature",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignScreenTab(
    viewModel: SignViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var drawnStrokes = remember { mutableStateListOf<List<Offset>>() }
    var redoStrokes = remember { mutableStateListOf<List<Offset>>() }
    var activeSigColor by remember { mutableStateOf(Color.Black) }
    var selectedStrokeWidth by remember { mutableFloatStateOf(6f) }
    var outputName by remember { mutableStateOf("signed_document_${System.currentTimeMillis().toString().takeLast(3)}") }
    var pageSizeByIndex by remember { mutableStateOf<Map<Int, PageSignatureSize>>(emptyMap()) }
    var sizeConfigPageIndex by remember { mutableIntStateOf(0) }
    var pageApplyMode by remember { mutableStateOf(SignatureApplyMode.SINGLE) }
    var customPagesInput by remember { mutableStateOf("1") }
    var signatureCanvasWidth by remember { mutableIntStateOf(0) }
    var signatureCanvasHeight by remember { mutableIntStateOf(0) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showSaveSignatureDialog by remember { mutableStateOf(false) }
    var saveSignatureName by remember { mutableStateOf("") }

    val isPresetColor = SignaturePresetColors.any { it == activeSigColor }
    val configuredPageSize = pageSizeByIndex[sizeConfigPageIndex] ?: defaultPageSignatureSize()

    LaunchedEffect(state.sourceUriString) {
        pageSizeByIndex = emptyMap()
        sizeConfigPageIndex = 0
    }

    LaunchedEffect(pageApplyMode, state.selectedPageIndex) {
        if (pageApplyMode == SignatureApplyMode.SINGLE) {
            sizeConfigPageIndex = state.selectedPageIndex
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val (name, size) = queryUriMetadata(context, uri)
            val app = context.applicationContext as com.michael.pdftoolkit.PdfToolkitApp
            coroutineScope.launch {
                val count = app.pdfRepository.getPdfPageCount(uri.toString())
                viewModel.setSourceDocument(uri.toString(), name, count)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Secure Local Digital Signer",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Draw your signature on mobile vector canvas and stamp it directly into pages.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Document select
        if (state.sourceUriString == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Select PDF", modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Step 1: Choose PDF to sign", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        PdfThumbnail(
                            uriString = state.sourceUriString!!,
                            pageIndex = sizeConfigPageIndex,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            state.sourceName,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${state.sourcePageCount} ${if (state.sourcePageCount == 1) "page" else "pages"} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    OutlinedButton(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Switch")
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vector Draw panel
        Text("Step 2: Draw Signature on canvas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        SignatureCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            strokes = drawnStrokes,
            onStrokesChange = { paths ->
                drawnStrokes.clear()
                drawnStrokes.addAll(paths)
                redoStrokes.clear()
            },
            onCanvasSizeChanged = { width, height ->
                signatureCanvasWidth = width
                signatureCanvasHeight = height
            },
            strokeColor = activeSigColor,
            strokeWidth = selectedStrokeWidth,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Signature control swatches
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SignaturePresetColors.forEach { color ->
                    val isSelected = activeSigColor == color
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(if (isSelected) 3.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { activeSigColor = color }
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(activeSigColor)
                        .border(
                            width = if (!isPresetColor) 3.dp else 1.dp,
                            color = if (!isPresetColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { showColorPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Open color palette",
                        tint = if (activeSigColor.luminance() > 0.6f) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = drawnStrokes.isNotEmpty(),
                    onClick = {
                        val last = drawnStrokes.removeAt(drawnStrokes.lastIndex)
                        redoStrokes.add(last)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo stroke"
                    )
                }
                IconButton(
                    enabled = redoStrokes.isNotEmpty(),
                    onClick = {
                        val stroke = redoStrokes.removeAt(redoStrokes.lastIndex)
                        drawnStrokes.add(stroke)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo stroke"
                    )
                }
                OutlinedButton(
                    onClick = {
                        saveSignatureName = "My signature"
                        showSaveSignatureDialog = true
                    },
                    enabled = drawnStrokes.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = {
                        drawnStrokes.clear()
                        redoStrokes.clear()
                    },
                    enabled = drawnStrokes.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }

        if (state.savedSignatures.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Saved signatures",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.savedSignatures, key = { it.id }) { saved ->
                    SavedSignatureCard(
                        saved = saved,
                        onLoad = {
                            drawnStrokes.clear()
                            drawnStrokes.addAll(saved.strokes)
                            redoStrokes.clear()
                            activeSigColor = saved.strokeColor
                            selectedStrokeWidth = saved.strokeWidth
                        },
                        onDelete = { viewModel.deleteSavedSignature(saved.id) }
                    )
                }
            }
        }

        if (showColorPicker) {
            SignatureColorPickerDialog(
                initialColor = activeSigColor,
                onDismiss = { showColorPicker = false },
                onColorSelected = { color ->
                    activeSigColor = color
                    showColorPicker = false
                }
            )
        }

        if (showSaveSignatureDialog) {
            AlertDialog(
                onDismissRequest = { showSaveSignatureDialog = false },
                title = { Text("Save signature") },
                text = {
                    OutlinedTextField(
                        value = saveSignatureName,
                        onValueChange = { saveSignatureName = it },
                        label = { Text("Signature name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = saveSignatureName.isNotBlank(),
                        onClick = {
                            viewModel.saveSignature(
                                name = saveSignatureName,
                                strokes = drawnStrokes.toList(),
                                strokeColor = activeSigColor,
                                strokeWidth = selectedStrokeWidth
                            )
                            showSaveSignatureDialog = false
                            Toast.makeText(context, "Signature saved", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveSignatureDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Position and page selection
        if (state.sourceUriString != null && drawnStrokes.isNotEmpty()) {
            Text("Step 3: Pages & placement", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Apply signature to", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = pageApplyMode == SignatureApplyMode.ALL,
                            onClick = { pageApplyMode = SignatureApplyMode.ALL },
                            label = { Text("All pages") }
                        )
                        FilterChip(
                            selected = pageApplyMode == SignatureApplyMode.SINGLE,
                            onClick = {
                                pageApplyMode = SignatureApplyMode.SINGLE
                                customPagesInput = "${state.selectedPageIndex + 1}"
                            },
                            label = { Text("One page") }
                        )
                        FilterChip(
                            selected = pageApplyMode == SignatureApplyMode.CUSTOM,
                            onClick = { pageApplyMode = SignatureApplyMode.CUSTOM },
                            label = { Text("Custom") }
                        )
                    }

                    when (pageApplyMode) {
                        SignatureApplyMode.ALL -> {
                            Text(
                                "Signature will be added to all ${state.sourcePageCount} pages.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        SignatureApplyMode.SINGLE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Target page ${state.selectedPageIndex + 1} of ${state.sourcePageCount}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row {
                                    IconButton(
                                        enabled = state.selectedPageIndex > 0,
                                        onClick = {
                                            val newIndex = state.selectedPageIndex - 1
                                            viewModel.selectPage(newIndex)
                                            sizeConfigPageIndex = newIndex
                                            customPagesInput = "${newIndex + 1}"
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Previous page")
                                    }
                                    IconButton(
                                        enabled = state.selectedPageIndex < state.sourcePageCount - 1,
                                        onClick = {
                                            val newIndex = state.selectedPageIndex + 1
                                            viewModel.selectPage(newIndex)
                                            sizeConfigPageIndex = newIndex
                                            customPagesInput = "${newIndex + 1}"
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Next page")
                                    }
                                }
                            }
                        }
                        SignatureApplyMode.CUSTOM -> {
                            OutlinedTextField(
                                value = customPagesInput,
                                onValueChange = { customPagesInput = it },
                                label = { Text("Page numbers") },
                                placeholder = { Text("e.g. 1, 3, 5 or 1-4") },
                                supportingText = {
                                    Text("Use commas or ranges. Valid pages: 1-${state.sourcePageCount}")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                "Signature size",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Page ${sizeConfigPageIndex + 1} of ${state.sourcePageCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (pageApplyMode != SignatureApplyMode.SINGLE) {
                            Row {
                                IconButton(
                                    enabled = sizeConfigPageIndex > 0,
                                    onClick = {
                                        val newIndex = sizeConfigPageIndex - 1
                                        sizeConfigPageIndex = newIndex
                                        viewModel.selectPage(newIndex)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Previous page size"
                                    )
                                }
                                IconButton(
                                    enabled = sizeConfigPageIndex < state.sourcePageCount - 1,
                                    onClick = {
                                        val newIndex = sizeConfigPageIndex + 1
                                        sizeConfigPageIndex = newIndex
                                        viewModel.selectPage(newIndex)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Next page size"
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Width: ${(configuredPageSize.widthRatio * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = configuredPageSize.widthRatio,
                        onValueChange = { width ->
                            val current = pageSizeByIndex[sizeConfigPageIndex] ?: defaultPageSignatureSize()
                            pageSizeByIndex = pageSizeByIndex + (sizeConfigPageIndex to current.copy(widthRatio = width))
                        },
                        valueRange = 0.12f..0.4f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Height: ${(configuredPageSize.heightRatio * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = configuredPageSize.heightRatio,
                        onValueChange = { height ->
                            val current = pageSizeByIndex[sizeConfigPageIndex] ?: defaultPageSignatureSize()
                            pageSizeByIndex = pageSizeByIndex + (sizeConfigPageIndex to current.copy(heightRatio = height))
                        },
                        valueRange = 0.05f..0.25f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Each page can have its own width and height. Signature stays at the bottom center.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = outputName,
                onValueChange = { outputName = it },
                label = { Text("Output signed filename label") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (state.isProcessing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Complete stamp button action outcomes
        if (state.signedDocument != null) {
            val signedDoc = state.signedDocument!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Signed", tint = Color(0xFF047857))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Document signed", fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                            Text(signedDoc.name, style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PdfExportActions(
                        uriString = signedDoc.uriString,
                        fileName = signedDoc.name,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.error != null) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 6.dp))
        }

        Button(
            onClick = {
                if (signatureCanvasWidth <= 0 || signatureCanvasHeight <= 0) {
                    Toast.makeText(context, "Signature canvas is not ready yet.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val bitmap = captureSignatureToBitmap(
                    strokes = drawnStrokes,
                    strokeColor = activeSigColor,
                    strokeWidth = selectedStrokeWidth,
                    sourceWidth = signatureCanvasWidth.toFloat(),
                    sourceHeight = signatureCanvasHeight.toFloat()
                )
                val pageIndices = when (pageApplyMode) {
                    SignatureApplyMode.ALL -> PageSelectionParser.allPages(state.sourcePageCount)
                    SignatureApplyMode.SINGLE -> setOf(state.selectedPageIndex)
                    SignatureApplyMode.CUSTOM -> {
                        PageSelectionParser.parseToZeroBasedIndices(
                            customPagesInput,
                            state.sourcePageCount
                        ).getOrElse { error ->
                            Toast.makeText(
                                context,
                                error.message ?: "Invalid page numbers.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }
                    }
                }
                val defaultSize = defaultPageSignatureSize()
                val pageSizes = pageIndices.associateWith { pageIndex ->
                    pageSizeByIndex[pageIndex] ?: defaultSize
                }
                viewModel.stampSignature(
                    signatureBitmap = bitmap,
                    pageSizes = pageSizes,
                    outputName = outputName
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isProcessing && state.sourceUriString != null && drawnStrokes.isNotEmpty()
        ) {
            Text("Stamp Signature & Save File", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 5. CONVERT SCREEN
// ==========================================
@Composable
fun ConvertScreenTab(
    viewModel: ConvertViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var outputFileName by remember { mutableStateOf("converted_report_${System.currentTimeMillis().toString().takeLast(3)}") }

    val defaultMarkdownTemplate = """
# EXECUTIVE BRIEFING
## Secure Offline Document Generation
Date: May 25, 2026

- ✅ Zero networks required
- ✅ Full hardware acceleration 
- ✅ Layout renders instantaneously on device memory

### Summary and Deliverables
This document serves as verification that all confidential data mapping calculations are completed strictly offline.
Please compile, stamp authentication signatures, and distribute locally.
    """.trimIndent()

    var textInputState by remember { mutableStateOf(defaultMarkdownTemplate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Markdown Document Compiler",
            modifier = Modifier.testTag("submit_button"), // Assign tag for screening/testing
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Parse written Markdown syntax notation directly into structured, multipage PDFs.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Markdown Input Editor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            
            OutlinedButton(
                onClick = { textInputState = "" },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Clear Text")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Large Multiline Mono edit block
        OutlinedTextField(
            value = textInputState,
            onValueChange = { textInputState = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            placeholder = { Text("Write # Header \n - list item \n into editor...") },
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = outputFileName,
            onValueChange = { outputFileName = it },
            label = { Text("Output Document File Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = { Text(".pdf", modifier = Modifier.padding(end = 12.dp)) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isProcessing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Show successful results banner
        if (state.convertedDocument != null) {
            val convertedDoc = state.convertedDocument!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Converted", tint = Color(0xFF047857))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Conversion complete", fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                            Text(
                                convertedDoc.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PdfExportActions(
                        uriString = convertedDoc.uriString,
                        fileName = convertedDoc.name,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.error != null) {
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 6.dp))
        }

        Button(
            onClick = {
                viewModel.updateMarkdownText(textInputState)
                viewModel.startConversion(outputFileName)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isProcessing && textInputState.trim().isNotEmpty()
        ) {
            Text("Compile Markdown to PDF Document", fontWeight = FontWeight.Bold)
        }
    }
}
