package com.example.vibeplayer.data

data class LyricLine(val timeMs: Long, val text: String)

object LrcParser {
    private val TAG = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")

    fun parse(raw: String): List<LyricLine> {
        val out = mutableListOf<LyricLine>()
        for (line in raw.lines()) {
            val stamps = TAG.findAll(line).toList()
            if (stamps.isEmpty()) continue
            val text = line.substring(stamps.last().range.last + 1).trim()
            if (text.isEmpty()) continue
            for (s in stamps) {
                val min = s.groupValues[1].toIntOrNull() ?: continue
                val sec = s.groupValues[2].toIntOrNull() ?: continue
                val frac = when (s.groupValues[3].length) {
                    1 -> s.groupValues[3].toLongOrNull()?.times(100) ?: 0
                    2 -> s.groupValues[3].toLongOrNull()?.times(10) ?: 0
                    3 -> s.groupValues[3].toLongOrNull() ?: 0
                    else -> 0
                }
                out += LyricLine(min * 60_000L + sec * 1000L + frac, text)
            }
        }
        return out.sortedBy { it.timeMs }
    }
}
