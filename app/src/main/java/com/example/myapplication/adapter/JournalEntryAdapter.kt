package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.room.JournalEntry
import com.example.myapplication.util.DateUtils
import com.google.android.material.imageview.ShapeableImageView

class JournalEntryAdapter(
    private val onClick: (JournalEntry) -> Unit
) : ListAdapter<JournalEntry, JournalEntryAdapter.JournalEntryViewHolder>(JournalEntryDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal_entry, parent, false)
        return JournalEntryViewHolder(view, onClick)
    }
    override fun onBindViewHolder(holder: JournalEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class JournalEntryViewHolder(
        itemView: View,
        private val onClick: (JournalEntry) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        private val moodImageView: ShapeableImageView = itemView.findViewById(R.id.moodImageView)
        private var currentEntry: JournalEntry? = null
        init {
            itemView.setOnClickListener {
                currentEntry?.let { onClick(it) }
            }
        }
        fun bind(entry: JournalEntry) {
            currentEntry = entry
            titleTextView.text = entry.title
            dateTextView.text = DateUtils.formatLongToDateTime(entry.date)
            contentTextView.text = entry.content
            val moodIcon = when (entry.mood.lowercase()) {
                "happy" -> R.drawable.ic_happy
                "sad" -> R.drawable.ic_sad
                "neutral" -> R.drawable.ic_neutral
                "angry" -> R.drawable.ic_angry
                else -> R.drawable.ic_neutral
            }
            moodImageView.setImageResource(moodIcon)
        }
    }
}
class JournalEntryDiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
    override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
        return oldItem == newItem
    }
}