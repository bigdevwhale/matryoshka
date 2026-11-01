package app.matryoshka.domain

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Keys for the figures.
 *
 * The outer figure's seed lies in the file. Each figure's key is HMAC(seed, answer key), where the answer key
 * is PBKDF2 of the normalised answer for a RIDDLE or WORD and nothing otherwise. Inside each figure lies the
 * seed of the next one. So a figure opens only after every figure around it, and a riddle only with its answer:
 * taking the file apart doesn't help.
 */
object Crypto {
    private val rnd = SecureRandom()
    const val ITER = 60_000

    fun random(n: Int) = ByteArray(n).also { rnd.nextBytes(it) }

    fun answerKey(answer: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(norm(answer).toCharArray(), salt, ITER, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    fun hmac(key: ByteArray, data: ByteArray): ByteArray =
        Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(key, "HmacSHA256")) }.doFinal(data)

    fun figureKey(seed: ByteArray, answerKey: ByteArray?): ByteArray =
        hmac(seed, "matryoshka/figure".toByteArray() + (answerKey ?: ByteArray(0)))

    fun seal(key: ByteArray, plain: ByteArray): ByteArray {
        val iv = random(12)
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv)) }
        return iv + c.doFinal(plain)
    }

    /** Null when the key is wrong: GCM checks it. */
    fun open(key: ByteArray, data: ByteArray): ByteArray? = runCatching {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, data, 0, 12))
        }
        c.doFinal(data, 12, data.size - 12)
    }.getOrNull()
}

private fun ByteArray.b64(): String = Base64.getEncoder().encodeToString(this)
private fun String.unb64(): ByteArray = Base64.getDecoder().decode(this)

/** One figure as the recipient sees it before opening: painting, painter, lock. */
data class Figure(val paint: Paint, val author: String, val lock: Lock, val salt: ByteArray? = null)

/** A sealed matryoshka's cover: what anyone holding the file can read. */
data class Meta(
    val id: String,
    val title: String,
    val to: String,
    val authors: List<String>,
    val created: Long,
    val seed: ByteArray,
    /** From the outside in; the last one is the solid figure with the gift. */
    val figures: List<Figure>,
)

/** A figure opened: what it held, and the seed of the next one. */
data class Opened(val index: Int, val key: ByteArray, val content: Content, val next: ByteArray?)

/** A matryoshka going round the givers before it is sealed. [turn] is whose figure comes next. */
data class Relay(
    val id: String,
    val title: String,
    val to: String,
    val people: List<String>,
    val turn: Int,
    val layers: List<Layer>,
    val giftPaint: Paint,
    val gift: Content,
    val created: Long,
) {
    val last get() = turn >= people.size - 1
    val next get() = people.getOrNull(turn + 1)
    fun draft() = Draft(id, title, to, layers, giftPaint, gift, created)
}

/** What the recipient sends back: a few words (and maybe a voice) for some of the figures. */
data class Reply(val id: String, val from: String, val at: Long, val notes: List<ReplyNote>)
data class ReplyNote(val figure: Int, val text: String, val voice: ByteArray? = null, val voiceMs: Long = 0)

sealed interface Parsed {
    class Doll(val sealed: Sealed) : Parsed
    class Going(val relay: Relay) : Parsed
    class Back(val reply: Reply) : Parsed
}

/** A sealed .matr file in memory. */
class Sealed(val meta: Meta, val bytes: ByteArray, private val entries: Map<String, ByteArray>) {
    val count get() = meta.figures.size

    /** The key of figure [i] from its seed and (for a riddle or word) the answer. Slow on purpose when there's an answer. */
    fun key(i: Int, seed: ByteArray, answer: String?): ByteArray {
        val f = meta.figures[i]
        val ak = if (f.lock.kind.secret && f.salt != null) Crypto.answerKey(answer.orEmpty(), f.salt) else null
        return Crypto.figureKey(seed, ak)
    }

    /** Opens figure [i] with its key, or null if the key is wrong. */
    fun open(i: Int, key: ByteArray): Opened? {
        val head = entries["f$i.note"]?.let { Crypto.open(key, it) } ?: return null
        val j = JSONObject(String(head, Charsets.UTF_8))
        val photo = entries["f$i.photo"]?.let { Crypto.open(key, it) }
        val voice = entries["f$i.voice"]?.let { Crypto.open(key, it) }
        val c = Content(j.optString("note"), photo, voice, j.optLong("voiceMs"), j.optString("link"))
        return Opened(i, key, c, j.optString("next").takeIf { it.isNotEmpty() }?.unb64())
    }
}

/** The .matr file: a zip with matryoshka.json and encrypted pieces next to it. */
object Matr {
    const val EXT = "matr"
    const val MIME = "application/x-matryoshka"
    private const val HEAD = "matryoshka.json"
    private const val FORMAT = "matryoshka"

    fun newId(): String = UUID.randomUUID().toString().replace("-", "").take(16)

    // ---- sealing ----

    fun seal(d: Draft): ByteArray {
        val n = d.layers.size + 1
        val seeds = List(n) { Crypto.random(32) }
        val figures = ArrayList<Figure>()
        val out = LinkedHashMap<String, ByteArray>()
        for (i in 0 until n) {
            val layer = d.layers.getOrNull(i)
            val lock = layer?.lock ?: Lock()
            val salt = if (lock.kind.secret) Crypto.random(16) else null
            val paint = layer?.paint ?: d.giftPaint
            val author = layer?.author?.trim() ?: ""
            figures += Figure(paint, author, lock, salt)
            val ak = if (salt != null) Crypto.answerKey(layer!!.answer, salt) else null
            val key = Crypto.figureKey(seeds[i], ak)
            val c = layer?.content ?: d.gift
            val head = JSONObject().put("note", c.note).put("link", c.link).put("voiceMs", c.voiceMs)
            if (i + 1 < n) head.put("next", seeds[i + 1].b64())
            out["f$i.note"] = Crypto.seal(key, head.toString().toByteArray(Charsets.UTF_8))
            c.photo?.let { out["f$i.photo"] = Crypto.seal(key, it) }
            c.voice?.let { out["f$i.voice"] = Crypto.seal(key, it) }
        }
        val j = JSONObject()
            .put("format", FORMAT).put("v", 1).put("kind", "doll")
            .put("id", d.id).put("title", d.title).put("to", d.to).put("created", d.created)
            .put("authors", JSONArray(d.authors))
            .put("seed", seeds[0].b64())
            .put("figures", JSONArray(figures.map { f ->
                JSONObject().put("paint", f.paint.name).put("author", f.author).put("lock", lockJson(f.lock))
                    .apply { f.salt?.let { put("salt", it.b64()) } }
            }))
        return zip(mapOf(HEAD to j.toString(1).toByteArray(Charsets.UTF_8)) + out)
    }

    // ---- the relay ----

    /** Each piece is encrypted with a key that sits right next to it: it keeps a glance out, not a determined giver. */
    fun relay(r: Relay): ByteArray {
        val out = LinkedHashMap<String, ByteArray>()
        val layers = JSONArray()
        fun put(prefix: String, c: Content): String {
            val k = Crypto.random(32)
            val head = JSONObject().put("note", c.note).put("link", c.link).put("voiceMs", c.voiceMs)
            out["$prefix.note"] = Crypto.seal(k, head.toString().toByteArray(Charsets.UTF_8))
            c.photo?.let { out["$prefix.photo"] = Crypto.seal(k, it) }
            c.voice?.let { out["$prefix.voice"] = Crypto.seal(k, it) }
            return k.b64()
        }
        r.layers.forEachIndexed { i, l ->
            layers.put(JSONObject().put("paint", l.paint.name).put("author", l.author).put("lock", lockJson(l.lock))
                .put("answer", l.answer.toByteArray(Charsets.UTF_8).b64()).put("key", put("f$i", l.content)))
        }
        val j = JSONObject()
            .put("format", FORMAT).put("v", 1).put("kind", "relay")
            .put("id", r.id).put("title", r.title).put("to", r.to).put("created", r.created)
            .put("people", JSONArray(r.people)).put("turn", r.turn)
            .put("giftPaint", r.giftPaint.name).put("giftKey", put("g", r.gift))
            .put("layers", layers)
        return zip(mapOf(HEAD to j.toString(1).toByteArray(Charsets.UTF_8)) + out)
    }

    // ---- the reply ----

    fun reply(r: Reply): ByteArray {
        val out = LinkedHashMap<String, ByteArray>()
        val notes = JSONArray()
        r.notes.forEachIndexed { k, n ->
            notes.put(JSONObject().put("figure", n.figure).put("text", n.text).put("voiceMs", n.voiceMs))
            n.voice?.let { out["r$k.voice"] = it }
        }
        val j = JSONObject().put("format", FORMAT).put("v", 1).put("kind", "reply")
            .put("id", r.id).put("from", r.from).put("at", r.at).put("notes", notes)
        return zip(mapOf(HEAD to j.toString(1).toByteArray(Charsets.UTF_8)) + out)
    }

    // ---- reading ----

    fun read(bytes: ByteArray): Parsed? = runCatching {
        val e = unzip(bytes)
        val j = JSONObject(String(e[HEAD] ?: return null, Charsets.UTF_8))
        if (j.optString("format") != FORMAT) return null
        when (j.optString("kind")) {
            "doll" -> {
                val figs = j.getJSONArray("figures")
                val meta = Meta(
                    id = j.getString("id"), title = j.optString("title"), to = j.optString("to"),
                    authors = j.optJSONArray("authors").strings(), created = j.optLong("created"),
                    seed = j.getString("seed").unb64(),
                    figures = List(figs.length()) { i ->
                        val f = figs.getJSONObject(i)
                        Figure(paint(f.optString("paint")), f.optString("author"), lock(f.optJSONObject("lock")),
                            f.optString("salt").takeIf { it.isNotEmpty() }?.unb64())
                    },
                )
                Parsed.Doll(Sealed(meta, bytes, e))
            }
            "relay" -> {
                fun content(prefix: String, key: String): Content {
                    val k = key.unb64()
                    val h = JSONObject(String(Crypto.open(k, e.getValue("$prefix.note"))!!, Charsets.UTF_8))
                    return Content(h.optString("note"), e["$prefix.photo"]?.let { Crypto.open(k, it) },
                        e["$prefix.voice"]?.let { Crypto.open(k, it) }, h.optLong("voiceMs"), h.optString("link"))
                }
                val ls = j.getJSONArray("layers")
                Parsed.Going(Relay(
                    id = j.getString("id"), title = j.optString("title"), to = j.optString("to"),
                    people = j.optJSONArray("people").strings(), turn = j.optInt("turn"),
                    layers = List(ls.length()) { i ->
                        val l = ls.getJSONObject(i)
                        Layer(paint(l.optString("paint")), l.optString("author"), lock(l.optJSONObject("lock")),
                            String(l.optString("answer").unb64(), Charsets.UTF_8), content("f$i", l.getString("key")))
                    },
                    giftPaint = paint(j.optString("giftPaint")), gift = content("g", j.getString("giftKey")),
                    created = j.optLong("created"),
                ))
            }
            "reply" -> {
                val ns = j.getJSONArray("notes")
                Parsed.Back(Reply(j.getString("id"), j.optString("from"), j.optLong("at"), List(ns.length()) { k ->
                    val n = ns.getJSONObject(k)
                    ReplyNote(n.optInt("figure"), n.optString("text"), e["r$k.voice"], n.optLong("voiceMs"))
                }))
            }
            else -> null
        }
    }.getOrNull()

    // ---- pieces ----

    fun lockJson(l: Lock): JSONObject = JSONObject().put("kind", l.kind.name).apply {
        when (l.kind) {
            LockKind.TIME -> put("at", l.at)
            LockKind.RIDDLE -> put("question", l.question).put("hint", l.hint)
            LockKind.WORD -> put("who", l.who).put("hint", l.hint)
            LockKind.PLACE -> put("lat", l.lat).put("lon", l.lon).put("place", l.place).put("radius", l.radius)
            LockKind.GESTURE -> put("gesture", l.gesture.name)
            LockKind.NONE -> {}
        }
    }

    fun lock(j: JSONObject?): Lock {
        if (j == null) return Lock()
        val kind = runCatching { LockKind.valueOf(j.optString("kind")) }.getOrDefault(LockKind.NONE)
        return Lock(kind, j.optLong("at"), j.optString("question"), j.optString("who"), j.optString("hint"),
            j.optDouble("lat", 0.0), j.optDouble("lon", 0.0), j.optString("place"), j.optInt("radius", 100),
            runCatching { Gesture.valueOf(j.optString("gesture")) }.getOrDefault(Gesture.COVER))
    }

    fun paint(s: String) = runCatching { Paint.valueOf(s) }.getOrDefault(Paint.KHOKHLOMA)

    private fun JSONArray?.strings(): List<String> = if (this == null) emptyList() else List(length()) { optString(it) }

    private fun zip(entries: Map<String, ByteArray>): ByteArray {
        val bo = ByteArrayOutputStream()
        ZipOutputStream(bo).use { z ->
            for ((name, data) in entries) {
                z.putNextEntry(ZipEntry(name))
                z.write(data)
                z.closeEntry()
            }
        }
        return bo.toByteArray()
    }

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> {
        val out = HashMap<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { z ->
            var total = 0L
            while (true) {
                val e = z.nextEntry ?: break
                if (e.isDirectory || e.name.contains("..") || e.name.contains('/')) continue
                val data = z.readBytes()
                total += data.size
                if (total > 64L shl 20) error("too big")
                out[e.name] = data
            }
        }
        return out
    }
}
