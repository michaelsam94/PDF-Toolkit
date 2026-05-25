package com.michael.pdftoolkit.presentation.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.michael.pdftoolkit.util.DocumentActions

@Composable
fun PdfExportActions(
    uriString: String,
    fileName: String,
    modifier: Modifier = Modifier,
    showShare: Boolean = true
) {
    val context = LocalContext.current

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        if (destinationUri != null) {
            DocumentActions.exportToUri(context, uriString, destinationUri)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { DocumentActions.saveToDownloads(context, uriString, fileName) }
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Downloads")
        }

        OutlinedButton(
            onClick = {
                saveLauncher.launch(
                    if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.SaveAlt,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save to...")
        }

        if (showShare) {
            Button(onClick = { shareDocument(context, uriString) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }
        }
    }
}
