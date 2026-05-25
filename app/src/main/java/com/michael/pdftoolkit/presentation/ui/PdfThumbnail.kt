package com.michael.pdftoolkit.presentation.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.michael.pdftoolkit.util.DocumentActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PdfThumbnail(
    uriString: String,
    pageIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(uriString, pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uriString, pageIndex) {
        withContext(Dispatchers.IO) {
            try {
                DocumentActions.openPdfDescriptor(context, uriString)?.use { pfd ->
                    val renderer = PdfRenderer(pfd)
                    if (renderer.pageCount > pageIndex) {
                        val page = renderer.openPage(pageIndex)
                        val ratio = page.height.toFloat() / page.width.toFloat()

                        val widthVal = 180
                        val heightVal = (widthVal * ratio).toInt().coerceAtLeast(180)

                        val bmp = Bitmap.createBitmap(widthVal, heightVal, Bitmap.Config.ARGB_8888)
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        bitmap = bmp
                        page.close()
                    }
                    renderer.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "PDF Page $pageIndex Preview",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "PDF Icon Placeholder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
