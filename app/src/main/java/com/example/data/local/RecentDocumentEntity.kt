package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.PdfDocument

@Entity(tableName = "recent_documents")
data class RecentDocumentEntity(
    @PrimaryKey val id: String,
    val uriString: String,
    val name: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val lastModifiedMillis: Long,
    val additionType: String
) {
    fun toDomainModel(): PdfDocument {
        return PdfDocument(
            id = id,
            uriString = uriString,
            name = name,
            pageCount = pageCount,
            fileSizeBytes = fileSizeBytes,
            lastModifiedMillis = lastModifiedMillis,
            additionType = additionType
        )
    }

    companion object {
        fun fromDomainModel(doc: PdfDocument): RecentDocumentEntity {
            return RecentDocumentEntity(
                id = doc.id,
                uriString = doc.uriString,
                name = doc.name,
                pageCount = doc.pageCount,
                fileSizeBytes = doc.fileSizeBytes,
                lastModifiedMillis = doc.lastModifiedMillis,
                additionType = doc.additionType
            )
        }
    }
}
