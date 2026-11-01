package app.matryoshka.domain

/** The five paintings a figure can wear. */
enum class Paint { KHOKHLOMA, GZHEL, POLKHOV, SEMYONOV, VYATKA }

/** What keeps a figure shut. RIDDLE and WORD are real cryptography; the rest the recipient's phone checks. */
enum class LockKind { NONE, TIME, RIDDLE, PLACE, WORD, GESTURE;
    val secret get() = this == RIDDLE || this == WORD
}

enum class Gesture { COVER, SHAKE, FLIP }

/**
 * The public side of a lock: everything the recipient may see before it opens.
 * The answer to a RIDDLE or WORD is never here: it lives only in a [Layer] of a draft.
 */
data class Lock(
    val kind: LockKind = LockKind.NONE,
    /** TIME: opens at this moment (epoch ms). */
    val at: Long = 0,
    /** RIDDLE: the question. */
    val question: String = "",
    /** WORD: who knows the word. */
    val who: String = "",
    /** A hint shown after the first wrong answer. */
    val hint: String = "",
    /** PLACE: where, and a name for it. */
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val place: String = "",
    val radius: Int = 100,
    val gesture: Gesture = Gesture.COVER,
)

/** What is inside a figure: a note, a photo, a voice, a link — any of them. */
data class Content(
    val note: String = "",
    val photo: ByteArray? = null,
    val voice: ByteArray? = null,
    val voiceMs: Long = 0,
    val link: String = "",
) {
    val empty get() = note.isBlank() && photo == null && voice == null && link.isBlank()
    override fun equals(other: Any?) = other is Content && note == other.note && link == other.link && voiceMs == other.voiceMs &&
        photo.contentEq(other.photo) && voice.contentEq(other.voice)
    override fun hashCode() = note.hashCode() * 31 + link.hashCode() + (photo?.size ?: 0) + (voice?.size ?: 0)
}

private fun ByteArray?.contentEq(o: ByteArray?) = if (this == null) o == null else o != null && contentEquals(o)

/** One figure being made: the painting, who painted it, the lock (with its answer) and what's inside. */
data class Layer(
    val paint: Paint,
    val author: String,
    val lock: Lock = Lock(),
    /** RIDDLE / WORD only: the answer that becomes the key. */
    val answer: String = "",
    val content: Content = Content(),
)

/**
 * A matryoshka before it is sealed. [layers] go from the outside in; the smallest, solid figure
 * holds [gift] and has no lock.
 */
data class Draft(
    val id: String,
    val title: String,
    val to: String,
    val layers: List<Layer>,
    val giftPaint: Paint = Paint.KHOKHLOMA,
    val gift: Content = Content(),
    val created: Long = System.currentTimeMillis(),
) {
    val authors: List<String> get() = layers.map { it.author.trim() }.filter { it.isNotEmpty() }.distinct()
}

/** Answers are compared without case, "ё", spaces or punctuation: «Пломбир!» opens what «пломбир» locked. */
fun norm(s: String): String = s.lowercase().replace('ё', 'е').filter { it.isLetterOrDigit() }

object Geo {
    /** Metres between two points on the Earth. */
    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dp = p2 - p1
        val dl = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dp / 2).let { it * it } + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2).let { it * it }
        return 2 * r * Math.asin(Math.sqrt(a.coerceIn(0.0, 1.0)))
    }

    /** Degrees clockwise from north to go from the first point to the second. */
    fun bearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dl = Math.toRadians(lon2 - lon1)
        val y = Math.sin(dl) * Math.cos(p2)
        val x = Math.cos(p1) * Math.sin(p2) - Math.sin(p1) * Math.cos(p2) * Math.cos(dl)
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
    }

    /**
     * Coordinates out of whatever was pasted: "55.75, 37.62", a Google Maps link (@55.75,37.62 or q=55.75,37.62),
     * a Yandex Maps link (ll=37.62,55.75 — longitude first, or pt=), a geo: URI.
     */
    fun parse(text: String): Pair<Double, Double>? {
        val t = text.trim()
        fun ok(lat: Double, lon: Double) = if (lat in -90.0..90.0 && lon in -180.0..180.0 && !(lat == 0.0 && lon == 0.0)) lat to lon else null
        val num = "(-?\\d{1,3}(?:\\.\\d+)?)"
        Regex("[?&](?:ll|pt|rtext)=$num(?:,|%2C)$num", RegexOption.IGNORE_CASE).find(t)?.let { m ->
            return ok(m.groupValues[2].toDouble(), m.groupValues[1].toDouble())
        }
        Regex("@$num,$num").find(t)?.let { return ok(it.groupValues[1].toDouble(), it.groupValues[2].toDouble()) }
        Regex("(?:geo:|[?&]q=|[?&]query=)$num(?:,|%2C)\\s*$num", RegexOption.IGNORE_CASE).find(t)?.let {
            return ok(it.groupValues[1].toDouble(), it.groupValues[2].toDouble())
        }
        Regex("^$num\\s*[,; ]\\s*$num$").find(t)?.let { return ok(it.groupValues[1].toDouble(), it.groupValues[2].toDouble()) }
        return null
    }
}
