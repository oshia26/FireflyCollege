package com.fyrefly.fireflycollege.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TimeFormats {

    private val dayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMM")
    private val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMM · HH:mm")

    fun localDateOf(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    fun toEpochMillis(date: LocalDate, time: LocalTime, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atTime(time).atZone(zone).toInstant().toEpochMilli()

    fun hourMinuteOf(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone).toLocalTime()

    fun startOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

    fun formatDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        dayFormatter.format(localDateOf(epochMillis, zone))

    fun formatDateTime(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone))

    /** "due today", "due tomorrow", "due in 5 days", "due on Thu, 20 Nov", "1 day overdue". */
    fun dueLabel(dueAt: Long, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String {
        val dueDate = localDateOf(dueAt, zone)
        val diff = ChronoUnit.DAYS.between(today, dueDate)
        return when {
            diff < 0 -> {
                val days = -(diff)
                if (days == 1L) "1 day overdue" else "$days days overdue"
            }
            diff == 0L -> "due today"
            diff == 1L -> "due tomorrow"
            diff < 7 -> "due in $diff days"
            else -> "due ${dayFormatter.format(dueDate)}"
        }
    }

    /** Compact countdown for chips: "2d 5h", "6h 30m", "45m", or "2d overdue". */
    fun countdown(dueAt: Long, now: Long): String {
        val minutes = (dueAt - now) / 60_000
        val abs = Math.abs(minutes)
        val days = abs / 1440
        val hours = (abs % 1440) / 60
        val mins = abs % 60
        val body = when {
            days > 0 -> if (hours > 0) "${days}d ${hours}h" else "${days}d"
            hours > 0 -> if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
            else -> "${mins}m"
        }
        return if (minutes >= 0) "in $body" else "$body overdue"
    }
}
