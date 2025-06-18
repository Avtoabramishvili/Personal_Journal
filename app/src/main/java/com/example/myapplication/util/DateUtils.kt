package com.example.myapplication.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDateTime(date: Date): String {
        val formatter = SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault())
        return formatter.format(date)
    }
    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
        return formatter.format(date)
    }
    fun formatLongToDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault())
        return formatter.format(date)
    }
    fun formatLongToDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
        return formatter.format(date)
    }
}