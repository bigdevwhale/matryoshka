package app.matryoshka.art

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import app.matryoshka.data.Photos
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Demo pictures and a demo voice, made in code so the demo carries no one's real photos. */
object Fake {
    private fun p(c: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c }

    /** A birthday cake on a table and a white cat who has plans for it. */
    fun cake(): ByteArray {
        val w = 1200
        val h = 800
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply { shader = LinearGradient(0f, 0f, 0f, h * .7f, 0xFFF0D3A8.toInt(), 0xFFE2B88A.toInt(), Shader.TileMode.CLAMP) })
        c.drawRect(0f, h * .7f, w.toFloat(), h.toFloat(), p(0xFFB98A5A.toInt()))
        c.drawRect(0f, h * .7f, w.toFloat(), h * .72f, p(0xFF8E6440.toInt()))
        c.drawOval(RectF(260f, 520f, 780f, 620f), p(0xFFFFFFFF.toInt()))
        c.drawRect(330f, 380f, 710f, 570f, p(0xFFF4D9E2.toInt()))
        c.drawOval(RectF(330f, 345f, 710f, 415f), p(0xFFFFF4F7.toInt()))
        c.drawOval(RectF(330f, 535f, 710f, 605f), p(0xFFF4D9E2.toInt()))
        for (i in 0 until 9) c.drawCircle(350f + i * 42f, 395f, 9f, p(0xFFC8323C.toInt()))
        for (i in 0 until 3) {
            val x = 450f + i * 70f
            c.drawRect(x, 280f, x + 12f, 375f, p(0xFFE8B24A.toInt()))
            c.drawOval(RectF(x - 6f, 240f, x + 18f, 285f), p(0xFFFF9A3C.toInt()))
            c.drawOval(RectF(x - 1f, 255f, x + 13f, 282f), p(0xFFFFE08A.toInt()))
        }
        val cat = p(0xFFF8F8F2.toInt())
        c.drawOval(RectF(760f, 400f, 1060f, 650f), cat)
        c.drawCircle(860f, 380f, 95f, cat)
        c.drawPath(android.graphics.Path().apply { moveTo(785f, 330f); lineTo(790f, 245f); lineTo(850f, 300f); close() }, cat)
        c.drawPath(android.graphics.Path().apply { moveTo(900f, 300f); lineTo(950f, 240f); lineTo(945f, 335f); close() }, cat)
        c.drawCircle(830f, 375f, 10f, p(0xFF333333.toInt()))
        c.drawCircle(895f, 375f, 10f, p(0xFF333333.toInt()))
        c.drawCircle(862f, 405f, 7f, p(0xFFE08A8A.toInt()))
        c.drawCircle(800f, 455f, 22f, p(0xFFF4D9E2.toInt()))
        c.drawRect(1000f, 560f, 1150f, 600f, cat)
        return Photos.jpeg(b, 88)
    }

    /** Two cups on a café windowsill. */
    fun coffee(): ByteArray {
        val w = 1200
        val h = 900
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply { shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), 0xFF9FB7C8.toInt(), 0xFFE9D9C0.toInt(), Shader.TileMode.CLAMP) })
        c.drawRect(0f, h * .62f, w.toFloat(), h.toFloat(), p(0xFF6B4A33.toInt()))
        c.drawRect(560f, 0f, 600f, h * .62f, p(0xFF4A3424.toInt()))
        for ((x, col) in listOf(330f to 0xFFF6F1E7.toInt(), 760f to 0xFFE7C9A0.toInt())) {
            c.drawOval(RectF(x - 150f, 640f, x + 150f, 700f), p(0xFFF7F3EA.toInt()))
            c.drawRoundRect(RectF(x - 100f, 470f, x + 100f, 660f), 40f, 40f, p(col))
            c.drawOval(RectF(x - 100f, 445f, x + 100f, 500f), p(0xFF7A4A2A.toInt()))
            c.drawOval(RectF(x - 40f, 460f, x + 40f, 485f), p(0xFFE8CBA0.toInt()))
            c.drawArc(RectF(x + 80f, 510f, x + 160f, 610f), -90f, 180f, false, p(col).apply { style = Paint.Style.STROKE; strokeWidth = 18f })
        }
        return Photos.jpeg(b, 88)
    }

    /** A soft three-note chime as a WAV (MediaPlayer plays it like any voice note). */
    fun chime(seconds: Float = 3.2f): ByteArray {
        val rate = 22_050
        val n = (rate * seconds).toInt()
        val pcm = ShortArray(n)
        val notes = listOf(523.25 to 0.0, 659.25 to .45, 783.99 to .9)
        for (i in 0 until n) {
            val t = i.toDouble() / rate
            var v = 0.0
            for ((f, at) in notes) if (t >= at) v += sin(2 * PI * f * (t - at)) * exp(-(t - at) * 2.2) * .28
            pcm[i] = (v.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        val out = ByteArrayOutputStream()
        val head = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        head.put("RIFF".toByteArray()).putInt(36 + n * 2).put("WAVE".toByteArray()).put("fmt ".toByteArray())
            .putInt(16).putShort(1).putShort(1).putInt(rate).putInt(rate * 2).putShort(2).putShort(16)
            .put("data".toByteArray()).putInt(n * 2)
        out.write(head.array())
        val body = ByteBuffer.allocate(n * 2).order(ByteOrder.LITTLE_ENDIAN)
        pcm.forEach { body.putShort(it) }
        out.write(body.array())
        return out.toByteArray()
    }
}
