
package com.example.myapplication.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import java.util.Date
@IgnoreExtraProperties
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Long = Date().time,
    val userId: String = "",
    val mood: String = "neutral",
    val imageUrl: String? = null
) {

    constructor() : this("", "", "", 0, "", "neutral", null)
}
