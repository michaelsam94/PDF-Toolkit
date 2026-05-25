package com.michael.pdftoolkit.util

object PageSelectionParser {
    fun allPages(totalPages: Int): Set<Int> {
        if (totalPages <= 0) return emptySet()
        return (0 until totalPages).toSet()
    }

    fun singlePage(oneBasedPage: Int, totalPages: Int): Result<Set<Int>> {
        if (oneBasedPage !in 1..totalPages) {
            return Result.failure(IllegalArgumentException("Page must be between 1 and $totalPages."))
        }
        return Result.success(setOf(oneBasedPage - 1))
    }

    fun parseToZeroBasedIndices(input: String, totalPages: Int): Result<Set<Int>> {
        if (totalPages <= 0) {
            return Result.failure(IllegalArgumentException("This PDF has no pages."))
        }

        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter at least one page number."))
        }

        val indices = linkedSetOf<Int>()
        trimmed.split(",", ";").forEach { rawPart ->
            val part = rawPart.trim()
            if (part.isEmpty()) return@forEach

            if ("-" in part) {
                val rangeParts = part.split("-", limit = 2)
                if (rangeParts.size != 2) {
                    return Result.failure(IllegalArgumentException("Invalid range: $part"))
                }
                val start = rangeParts[0].trim().toIntOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid range: $part"))
                val end = rangeParts[1].trim().toIntOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid range: $part"))
                if (start !in 1..totalPages || end !in 1..totalPages || end < start) {
                    return Result.failure(
                        IllegalArgumentException("Range $part must stay between pages 1 and $totalPages.")
                    )
                }
                for (page in start..end) {
                    indices.add(page - 1)
                }
            } else {
                val page = part.toIntOrNull()
                    ?: return Result.failure(IllegalArgumentException("Invalid page number: $part"))
                if (page !in 1..totalPages) {
                    return Result.failure(
                        IllegalArgumentException("Page $page must be between 1 and $totalPages.")
                    )
                }
                indices.add(page - 1)
            }
        }

        if (indices.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter at least one valid page number."))
        }

        return Result.success(indices)
    }
}
