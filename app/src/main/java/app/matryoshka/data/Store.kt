package app.matryoshka.data

import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Matr
import app.matryoshka.domain.Meta
import app.matryoshka.domain.Paint
import app.matryoshka.domain.Parsed
import app.matryoshka.domain.Relay
import app.matryoshka.domain.Reply
import app.matryoshka.domain.Sealed
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Base64

/** A matryoshka someone gave me, and how far I've got into it. */
data class Got(val sealed: Sealed, val progress: Progress) {
    val meta: Meta get() = sealed.meta
    val opened get() = progress.keys.size
    val done get() = opened >= sealed.count
}

/** The keys of the figures already opened (so they can be looked at again), and what I said about them. */
data class Progress(
    val keys: List<ByteArray> = emptyList(),
    val added: Long = System.currentTimeMillis(),
    val notes: Map<Int, String> = emptyMap(),
    /** Figures with a recorded voice reaction: the audio sits next to the progress file. */
    val voices: Map<Int, Long> = emptyMap(),
    val replied: Boolean = false,
)

/** A matryoshka I gave (or painted a figure of): what's needed to show it on the shelf. */
data class Given(
    val id: String,
    val title: String,
    val to: String,
    val at: Long,
    val authors: List<String>,
    val paints: List<Paint>,
    val locks: List<LockKind>,
    /** I sealed it, so the sealed file is kept to send again. */
    val sealer: Boolean,
    /** My figure in a shared matryoshka (−1 for my own). */
    val mine: Int = -1,
    val replies: List<Reply> = emptyList(),
)

data class Shelf(val got: List<Got>, val given: List<Given>, val drafts: List<Relay>) {
    companion object { val EMPTY = Shelf(emptyList(), emptyList(), emptyList()) }
}

/** Everything lives in files: one .matr per matryoshka, a small JSON beside it. Nothing leaves the phone by itself. */
class Store(root: File) {
    private val gotDir = File(root, "got").apply { mkdirs() }
    private val givenDir = File(root, "given").apply { mkdirs() }
    private val draftDir = File(root, "drafts").apply { mkdirs() }
    private val replyDir = File(root, "replies").apply { mkdirs() }

    private fun b64(b: ByteArray) = Base64.getEncoder().encodeToString(b)
    private fun unb64(s: String) = Base64.getDecoder().decode(s)
    private fun safe(id: String) = id.filter { it.isLetterOrDigit() }.take(40).ifEmpty { "x" }

    fun load(): Shelf {
        val got = gotDir.listFiles { f -> f.extension == Matr.EXT }.orEmpty().mapNotNull { f ->
            val s = (Matr.read(f.readBytes()) as? Parsed.Doll)?.sealed ?: return@mapNotNull null
            Got(s, progress(s.meta.id))
        }.sortedByDescending { it.progress.added }
        val replies = replyDir.listFiles { f -> f.extension == Matr.EXT }.orEmpty()
            .mapNotNull { (Matr.read(it.readBytes()) as? Parsed.Back)?.reply }.groupBy { it.id }
        val given = givenDir.listFiles { f -> f.extension == "json" }.orEmpty().mapNotNull { f ->
            runCatching { given(JSONObject(f.readText())) }.getOrNull()?.let { it.copy(replies = replies[it.id].orEmpty().sortedBy { r -> r.at }) }
        }.sortedByDescending { it.at }
        val drafts = draftDir.listFiles { f -> f.extension == Matr.EXT }.orEmpty().mapNotNull { f ->
            (Matr.read(f.readBytes()) as? Parsed.Going)?.relay
        }.sortedByDescending { it.created }
        return Shelf(got, given, drafts)
    }

    // ---- received ----

    fun putGot(s: Sealed) {
        File(gotDir, "${safe(s.meta.id)}.${Matr.EXT}").writeBytes(s.bytes)
        if (!progressFile(s.meta.id).exists()) saveProgress(s.meta.id, Progress())
    }

    private fun progressFile(id: String) = File(gotDir, "${safe(id)}.json")
    fun voiceFile(id: String, figure: Int) = File(gotDir, "${safe(id)}_r$figure.m4a")

    fun progress(id: String): Progress = runCatching {
        val j = JSONObject(progressFile(id).readText())
        val keys = j.optJSONArray("keys") ?: JSONArray()
        val notes = j.optJSONObject("notes") ?: JSONObject()
        val voices = j.optJSONObject("voices") ?: JSONObject()
        Progress(
            List(keys.length()) { unb64(keys.getString(it)) }, j.optLong("added"),
            notes.keys().asSequence().associate { it.toInt() to notes.getString(it) },
            voices.keys().asSequence().associate { it.toInt() to voices.getLong(it) },
            j.optBoolean("replied"),
        )
    }.getOrDefault(Progress())

    fun saveProgress(id: String, p: Progress) {
        val j = JSONObject().put("keys", JSONArray(p.keys.map(::b64))).put("added", p.added).put("replied", p.replied)
            .put("notes", JSONObject().apply { p.notes.forEach { (k, v) -> put(k.toString(), v) } })
            .put("voices", JSONObject().apply { p.voices.forEach { (k, v) -> put(k.toString(), v) } })
        progressFile(id).writeText(j.toString())
    }

    fun deleteGot(id: String) {
        File(gotDir, "${safe(id)}.${Matr.EXT}").delete()
        progressFile(id).delete()
        gotDir.listFiles { f -> f.name.startsWith("${safe(id)}_r") }?.forEach { it.delete() }
    }

    // ---- given ----

    fun putGiven(g: Given, sealed: ByteArray?) {
        val j = JSONObject().put("id", g.id).put("title", g.title).put("to", g.to).put("at", g.at)
            .put("authors", JSONArray(g.authors)).put("paints", JSONArray(g.paints.map { it.name }))
            .put("locks", JSONArray(g.locks.map { it.name })).put("sealer", g.sealer).put("mine", g.mine)
        File(givenDir, "${safe(g.id)}.json").writeText(j.toString())
        if (sealed != null) File(givenDir, "${safe(g.id)}.${Matr.EXT}").writeBytes(sealed)
    }

    fun givenFile(id: String): File? = File(givenDir, "${safe(id)}.${Matr.EXT}").takeIf { it.exists() }

    fun deleteGiven(id: String) {
        File(givenDir, "${safe(id)}.json").delete()
        File(givenDir, "${safe(id)}.${Matr.EXT}").delete()
        replyDir.listFiles { f -> f.name.startsWith("${safe(id)}_") }?.forEach { it.delete() }
    }

    private fun given(j: JSONObject): Given {
        fun arr(k: String) = j.optJSONArray(k)?.let { a -> List(a.length()) { a.getString(it) } }.orEmpty()
        return Given(j.getString("id"), j.optString("title"), j.optString("to"), j.optLong("at"), arr("authors"),
            arr("paints").map(Matr::paint), arr("locks").map { runCatching { LockKind.valueOf(it) }.getOrDefault(LockKind.NONE) },
            j.optBoolean("sealer"), j.optInt("mine", -1))
    }

    fun putReply(r: Reply, bytes: ByteArray) {
        File(replyDir, "${safe(r.id)}_${r.at}.${Matr.EXT}").writeBytes(bytes)
    }

    // ---- drafts ----

    fun putDraft(r: Relay) {
        File(draftDir, "${safe(r.id)}.${Matr.EXT}").writeBytes(Matr.relay(r))
    }

    fun deleteDraft(id: String) {
        File(draftDir, "${safe(id)}.${Matr.EXT}").delete()
    }
}
