package com.michael.pdftoolkit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSignatureDao {
    @Query("SELECT * FROM saved_signatures ORDER BY savedAtMillis DESC")
    fun getAll(): Flow<List<SavedSignatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signature: SavedSignatureEntity)

    @Query("DELETE FROM saved_signatures WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM saved_signatures")
    suspend fun deleteAll()
}
