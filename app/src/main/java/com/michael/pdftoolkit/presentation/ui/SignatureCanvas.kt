package com.michael.pdftoolkit.presentation.ui

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

private fun DrawScope.drawSignatureStrokes(
    strokes: List<List<Offset>>,
    strokeColor: Color,
    strokeWidth: Float
) {
    strokes.forEach { stroke ->
        if (stroke.size > 1) {
            val path = Path().apply {
                stroke.forEachIndexed { i, pt ->
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
fun SignaturePreview(
    strokes: List<List<Offset>>,
    strokeColor: Color,
    strokeWidth: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSignatureStrokes(strokes, strokeColor, strokeWidth)
        }
    }
}

@Composable
fun SignatureCanvas(
    modifier: Modifier = Modifier,
    strokes: List<List<Offset>>,
    onStrokesChange: (List<List<Offset>>) -> Unit,
    onCanvasSizeChanged: (Int, Int) -> Unit = { _, _ -> },
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 6f,
) {
    val currentStroke = remember { mutableStateListOf<Offset>() }

    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    onCanvasSizeChanged(size.width, size.height)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke.clear()
                            currentStroke.add(offset)
                        },
                        onDrag = { _, delta ->
                            if (currentStroke.isNotEmpty()) {
                                val lastPt = currentStroke.last()
                                currentStroke.add(lastPt + delta)
                            }
                        },
                        onDragEnd = {
                            if (currentStroke.size > 1) {
                                onStrokesChange(strokes + listOf(currentStroke.toList()))
                            }
                            currentStroke.clear()
                        }
                    )
                }
        ) {
            drawSignatureStrokes(strokes, strokeColor, strokeWidth)

            if (currentStroke.size > 1) {
                drawSignatureStrokes(listOf(currentStroke.toList()), strokeColor, strokeWidth)
            }
        }
    }
}

fun captureSignatureToBitmap(
    strokes: List<List<Offset>>,
    strokeColor: Color,
    strokeWidth: Float,
    sourceWidth: Float,
    sourceHeight: Float,
): Bitmap {
    if (strokes.isEmpty() || sourceWidth <= 0f || sourceHeight <= 0f) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE

    strokes.forEach { stroke ->
        stroke.forEach { point ->
            minX = minOf(minX, point.x)
            minY = minOf(minY, point.y)
            maxX = maxOf(maxX, point.x)
            maxY = maxOf(maxY, point.y)
        }
    }

    val padding = strokeWidth * 2f
    minX = (minX - padding).coerceAtLeast(0f)
    minY = (minY - padding).coerceAtLeast(0f)
    maxX = (maxX + padding).coerceAtMost(sourceWidth)
    maxY = (maxY + padding).coerceAtMost(sourceHeight)

    val cropWidth = (maxX - minX).toInt().coerceAtLeast(1)
    val cropHeight = (maxY - minY).toInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    bitmap.eraseColor(android.graphics.Color.TRANSPARENT)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = strokeColor.toArgb()
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    strokes.forEach { stroke ->
        if (stroke.size > 1) {
            val path = android.graphics.Path()
            stroke.forEachIndexed { index, point ->
                val x = point.x - minX
                val y = point.y - minY
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, paint)
        }
    }

    return bitmap
}
