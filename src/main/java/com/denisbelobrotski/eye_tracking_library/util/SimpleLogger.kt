package com.denisbelobrotski.eye_tracking_library.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SimpleLogger {
    class Log(val time: LocalDateTime, val message: String) {}

    var isEnabled = true

    private val logs = arrayListOf<Log>()

    fun addLog(message: String) {
        if (!isEnabled) {
            return
        }

        logs.add(Log(LocalDateTime.now(), message))
    }

    fun flushLogs(): String {
        if (!isEnabled) {
            logs.clear()
            return ""
        }

        val logsCount = logs.count()

        if (logsCount == 0) {
            return ""
        }

        val stringBuilder = StringBuilder()

        flushMessage(stringBuilder, logs[0])

        for (i in 1 until logsCount) {
            flushDelta(stringBuilder, "delta", logs[i - 1], logs[i])
            flushMessage(stringBuilder, logs[i])
        }

        flushDelta(stringBuilder, "total", logs[0], logs[logsCount - 1])

        val resultString = stringBuilder.toString()
        stringBuilder.clear()
        logs.clear()

        return resultString
    }

    private fun flushMessage(stringBuilder: StringBuilder, log: Log) {
        stringBuilder
            .append(log.message)
            .append(" - ")
            .append(log.time.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS")))
            .append("\n")
    }

    private fun flushDelta(stringBuilder: StringBuilder, deltaName: String, log1: Log, log2: Log) {
        var delta = Duration.between(log1.time, log2.time)

        val minutes = delta.toMinutes()
        delta = delta.minusMinutes(minutes)

        val seconds = delta.seconds
        delta = delta.minusSeconds(seconds)

        val millis = delta.toMillis()
        delta = delta.minusMillis(millis)

        stringBuilder
            .append(deltaName)
            .append(" - ")
            .append("%02d:%02d:%03d".format(minutes, seconds, millis))
            .append("\n")
    }
}
