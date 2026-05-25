package com.michael.pdftoolkit.util

import androidx.compose.ui.geometry.Offset
import org.json.JSONArray
import org.json.JSONObject

object SignatureStrokeCodec {
    fun encode(strokes: List<List<Offset>>): String {
        val root = JSONArray()
        strokes.forEach { stroke ->
            val strokeArray = JSONArray()
            stroke.forEach { point ->
                strokeArray.put(
                    JSONObject()
                        .put("x", point.x.toDouble())
                        .put("y", point.y.toDouble())
                )
            }
            root.put(strokeArray)
        }
        return root.toString()
    }

    fun decode(json: String): List<List<Offset>> {
        if (json.isBlank()) return emptyList()
        val root = JSONArray(json)
        return buildList {
            for (i in 0 until root.length()) {
                val strokeArray = root.getJSONArray(i)
                val stroke = buildList {
                    for (j in 0 until strokeArray.length()) {
                        val point = strokeArray.getJSONObject(j)
                        add(
                            Offset(
                                point.getDouble("x").toFloat(),
                                point.getDouble("y").toFloat()
                            )
                        )
                    }
                }
                add(stroke)
            }
        }
    }
}
