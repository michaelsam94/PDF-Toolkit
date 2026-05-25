package com.michael.pdftoolkit.util

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object DocumentActions {
    private fun authority(context: Context) = "${context.packageName}.fileprovider"

    fun resolveForExternal(context: Context, uriString: String): Uri {
        val uri = Uri.parse(uriString)
        if (uri.scheme == "file") {
            val path = uri.path ?: throw IllegalArgumentException("Invalid file path")
            return FileProvider.getUriForFile(context, authority(context), File(path))
        }
        return uri
    }

    fun openPdfDescriptor(context: Context, uriString: String): ParcelFileDescriptor? {
        val uri = Uri.parse(uriString)
        return when (uri.scheme) {
            "file" -> {
                val path = uri.path ?: return null
                ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
            }
            else -> context.contentResolver.openFileDescriptor(uri, "r")
        }
    }

    fun shareDocument(context: Context, uriString: String) {
        try {
            val uri = resolveForExternal(context, uriString)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share PDF").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun openDocument(context: Context, uriString: String) {
        try {
            val uri = resolveForExternal(context, uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to open PDF files.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportToUri(context: Context, sourceUriString: String, destinationUri: Uri): Boolean {
        return try {
            copySourceToDestination(context, sourceUriString, destinationUri)
            Toast.makeText(context, "File saved", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Save failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            false
        }
    }

    fun saveToDownloads(context: Context, sourceUriString: String, fileName: String): Boolean {
        val safeName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloadsViaMediaStore(context, sourceUriString, safeName)
            } else {
                saveToDownloadsLegacy(context, sourceUriString, safeName)
            }
            Toast.makeText(context, "Saved to Downloads: $safeName", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Could not save to Downloads: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun saveToDownloadsViaMediaStore(
        context: Context,
        sourceUriString: String,
        fileName: String
    ) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val destinationUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Could not create file in Downloads")

        try {
            copySourceToDestination(context, sourceUriString, destinationUri)
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(destinationUri, contentValues, null, null)
        } catch (e: Exception) {
            resolver.delete(destinationUri, null, null)
            throw e
        }
    }

    @Suppress("DEPRECATION")
    private fun saveToDownloadsLegacy(context: Context, sourceUriString: String, fileName: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            throw IOException("Downloads folder unavailable")
        }

        var destFile = File(downloadsDir, fileName)
        if (destFile.exists()) {
            val baseName = fileName.removeSuffix(".pdf")
            destFile = File(downloadsDir, "${baseName}_${System.currentTimeMillis()}.pdf")
        }

        openSourceInputStream(context, sourceUriString).use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(destFile.absolutePath),
            arrayOf("application/pdf"),
            null
        )
    }

    private fun copySourceToDestination(context: Context, sourceUriString: String, destinationUri: Uri) {
        openSourceInputStream(context, sourceUriString).use { input ->
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                input.copyTo(output)
            } ?: throw IOException("Could not write to destination")
        }
    }

    private fun openSourceInputStream(context: Context, sourceUriString: String) =
        when (val scheme = Uri.parse(sourceUriString).scheme) {
            "file" -> {
                val path = Uri.parse(sourceUriString).path
                    ?: throw IOException("Invalid source file path")
                File(path).inputStream()
            }
            else -> context.contentResolver.openInputStream(Uri.parse(sourceUriString))
                ?: throw IOException("Could not read source file")
        }
}
