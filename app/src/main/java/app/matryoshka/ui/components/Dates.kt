package app.matryoshka.ui.components

import android.content.Context
import app.matryoshka.R
import app.matryoshka.locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Dates {
    private fun zoned(ms: Long) = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())

    /** "14 March" / «14 марта», with the year only when it isn't this one. */
    fun day(ms: Long, ctx: Context): String {
        val d = zoned(ms)
        val p = if (d.year == LocalDate.now().year) "d MMMM" else "d MMMM yyyy"
        return d.format(DateTimeFormatter.ofPattern(p, ctx.locale))
    }

    fun time(ms: Long, ctx: Context): String = zoned(ms).format(DateTimeFormatter.ofPattern("H:mm", ctx.locale))

    fun dayTime(ms: Long, ctx: Context): String = ctx.getString(R.string.day_time, day(ms, ctx), time(ms, ctx))

    /** "2 d 4 h", "4 h 12 min", "12 min", "less than a minute". */
    fun left(ms: Long, ctx: Context): String {
        val m = ms / 60_000
        val d = m / 1440
        val h = m % 1440 / 60
        val mm = m % 60
        return when {
            d > 0 -> ctx.getString(R.string.left_dh, d, h)
            h > 0 -> ctx.getString(R.string.left_hm, h, mm)
            mm > 0 -> ctx.getString(R.string.left_m, mm)
            else -> ctx.getString(R.string.left_s)
        }
    }

    fun duration(ms: Long): String { val s = (ms + 500) / 1000; return "%d:%02d".format(s / 60, s % 60) }
}
