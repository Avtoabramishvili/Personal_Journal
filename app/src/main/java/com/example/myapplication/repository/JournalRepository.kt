package com.example.myapplication.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.myapplication.room.AppDatabase
import com.example.myapplication.room.JournalEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JournalRepository(context: Context) {
    private val journalEntryDao = AppDatabase.getDatabase(context).journalEntryDao()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    fun getAllEntries(): LiveData<List<JournalEntry>> {
        val userId = auth.currentUser?.uid
        Log.d("JournalRepo", "getAllEntries called for userId: $userId")
        if (userId == null) {
            Log.e("JournalRepo", "User ID არის Null.")
            return androidx.lifecycle.MutableLiveData(emptyList())
        }
        syncEntriesWithFirebase(userId)
        return journalEntryDao.getAllEntries(userId)
    }
    private fun syncEntriesWithFirebase(userId: String) {
        Log.d("FirebaseSync", "Starting syncEntriesWithFirebase for userId: $userId")
        database.child("users").child(userId).child("entries")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseSync", "onDataChange triggered for user: $userId. Children count: ${snapshot.childrenCount}")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Log.d("FirebaseSync", "Deleting all for user: $userId in Room before re-inserting.")
                            journalEntryDao.deleteAllForUser(userId)
                            var insertedCount = 0
                            snapshot.children.forEach { dataSnapshot ->
                                val entry = dataSnapshot.getValue(JournalEntry::class.java)
                                entry?.let {
                                    val entryWithId = it.copy(id = dataSnapshot.key ?: "")
                                    journalEntryDao.insert(entryWithId)
                                    insertedCount++
                                }
                            }
                            Log.d("FirebaseSync", "Inserted $insertedCount entries from Firebase to Room for user: $userId")
                        } catch (e: Exception) {
                            Log.e("FirebaseSync", "Failed to sync data for user $userId: ${e.message}")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Firebase read cancelled for user $userId: ${error.message}")
                }
            })
    }
    suspend fun addEntry(entry: JournalEntry) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("JournalRepo", "ვერ ხერხდება წაშლა User ID არის Null")
            return
        }
        val entryWithUserId = entry.copy(userId = userId)
        withContext(Dispatchers.IO) {
            try {
                val entryKey = if (entryWithUserId.id.isBlank()) {
                    database.child("users").child(userId).child("entries").push().key
                } else {
                    entryWithUserId.id
                } ?: run {
                    Log.e("JournalRepo", "Failed to generate or retrieve entry key.")
                    return@withContext
                }
                val completeEntry = entryWithUserId.copy(id = entryKey)
                database.child("users").child(userId).child("entries")
                    .child(entryKey).setValue(completeEntry)
                journalEntryDao.insert(completeEntry)
                Log.d("JournalRepo", "Entry added/updated in Firebase and Room: ${completeEntry.title}")
            } catch (e: Exception) {
                Log.e("FirebaseError", "Failed to add entry: ${e.message}")
            }
        }
    }
    suspend fun updateEntry(entry: JournalEntry) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("JournalRepo", "ვერ ხერხდება წაშლა User ID არის Null")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                database.child("users").child(userId).child("entries")
                    .child(entry.id).setValue(entry)
                journalEntryDao.update(entry)
                Log.d("JournalRepo", "Entry updated in Firebase and Room: ${entry.title}")
            } catch (e: Exception) {
                Log.e("FirebaseError", "Failed to update entry: ${e.message}")
            }
        }
    }
    suspend fun deleteEntry(entry: JournalEntry) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("JournalRepo", "ვერ ხერხდება წაშლა User ID არის Null")
            return
        }
        withContext(Dispatchers.IO) {
            try {
                database.child("users").child(userId).child("entries")
                    .child(entry.id).removeValue()
                journalEntryDao.delete(entry)
                Log.d("JournalRepo", "წაიშალა Firebase-დან და Room-დან: ${entry.title}")
            } catch (e: Exception) {
                Log.e("FirebaseError", "ვერ ხერხდება წაშლა ${e.message}")
            }
        }
    }
    fun getEntryById(entryId: String): LiveData<JournalEntry> {
        return journalEntryDao.observeEntryById(entryId)
    }
}
