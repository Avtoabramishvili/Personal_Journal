package com.example.myapplication.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry)
    @Update
    suspend fun update(entry: JournalEntry)
    @Delete
    suspend fun delete(entry: JournalEntry)
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllEntries(userId: String): LiveData<List<JournalEntry>>
    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    suspend fun getEntryById(entryId: String): JournalEntry?
    @Query("SELECT * FROM journal_entries WHERE id = :entryId LIMIT 1")
    fun observeEntryById(entryId: String): LiveData<JournalEntry>
    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}