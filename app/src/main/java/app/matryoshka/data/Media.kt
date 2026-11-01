package app.matryoshka.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File

object Photos {
    const val MAX = 1600

    /** A picked photo, turned upright, no longer than [MAX] px on its long side, as a JPEG. */
    fun compress(ctx: Context, uri: Uri): ByteArray? = runCatching {
        val src = ImageDecoder.createSource(ctx.contentResolver, uri)
        val bmp = ImageDecoder.decodeBitmap(src) { d, info, _ ->
            val w = info.size.width
            val h = info.size.height
            val k = MAX.toFloat() / maxOf(w, h)
            if (k < 1f) d.setTargetSize((w * k).toInt().coerceAtLeast(1), (h * k).toInt().coerceAtLeast(1))
            d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        jpeg(bmp)
    }.getOrNull()

    fun jpeg(b: Bitmap, q: Int = 85): ByteArray = ByteArrayOutputStream().also { b.compress(Bitmap.CompressFormat.JPEG, q, it) }.toByteArray()

    fun decode(bytes: ByteArray, px: Int = 1200): Bitmap? = runCatching {
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, o)
        var s = 1
        while (maxOf(o.outWidth, o.outHeight) / (s * 2) >= px) s *= 2
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = s })
    }.getOrNull()
}

/** A voice note: AAC in an .m4a, at most two minutes. */
class Recorder(private val ctx: Context) {
    private var rec: MediaRecorder? = null
    private var started = 0L
    var file: File? = null
        private set

    fun start(to: File): Boolean = runCatching {
        stop()
        to.parentFile?.mkdirs()
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(ctx) else @Suppress("DEPRECATION") MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioEncodingBitRate(64_000)
        r.setAudioSamplingRate(44_100)
        r.setMaxDuration(120_000)
        r.setOutputFile(to.absolutePath)
        r.prepare()
        r.start()
        rec = r
        file = to
        started = System.currentTimeMillis()
        true
    }.getOrDefault(false)

    val elapsed get() = if (rec == null) 0L else System.currentTimeMillis() - started

    /** Stops and says how long it was, or null if nothing usable was recorded. */
    fun stop(): Long? {
        val r = rec ?: return null
        rec = null
        val ms = System.currentTimeMillis() - started
        val ok = runCatching { r.stop() }.isSuccess
        r.release()
        return if (ok && ms > 600) ms.coerceAtMost(120_000) else null
    }
}

/** Plays a voice held in memory (it goes through a cache file: MediaPlayer wants one). */
class Player(private val ctx: Context) {
    private var mp: MediaPlayer? = null

    fun play(bytes: ByteArray, onDone: () -> Unit): Boolean = runCatching {
        stop()
        val f = File(ctx.cacheDir, "play.m4a").apply { writeBytes(bytes) }
        val p = MediaPlayer()
        p.setDataSource(f.absolutePath)
        p.setOnCompletionListener { stop(); onDone() }
        p.prepare()
        p.start()
        mp = p
        true
    }.getOrDefault(false)

    val position: Float get() = mp?.let { if (it.duration > 0) it.currentPosition.toFloat() / it.duration else 0f } ?: 0f
    val playing get() = mp?.isPlaying == true

    fun stop() {
        mp?.let { runCatching { it.stop() }; it.release() }
        mp = null
    }
}
