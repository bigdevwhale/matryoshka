package app.matryoshka

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import app.matryoshka.data.DemoSeed
import app.matryoshka.data.Given
import app.matryoshka.data.Got
import app.matryoshka.data.SettingsStore
import app.matryoshka.data.Shelf
import app.matryoshka.data.Store
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Matr
import app.matryoshka.domain.Opened
import app.matryoshka.domain.Parsed
import app.matryoshka.domain.Relay
import app.matryoshka.domain.Reply
import app.matryoshka.domain.ReplyNote
import app.matryoshka.work.Nudge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/** What a file opened from outside turned out to be. */
sealed interface Incoming {
    data class Doll(val id: String) : Incoming
    data class Turn(val id: String) : Incoming
    data class Back(val id: String, val from: String) : Incoming
    data object Bad : Incoming
}

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val settings = SettingsStore(app)
    val store = Store(File(app.filesDir, "dolls"))
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    var demo = BuildConfig.DEMO

    val ready = MutableStateFlow(false)
    val shelf = MutableStateFlow(Shelf.EMPTY)
    val incoming = MutableStateFlow<Incoming?>(null)

    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }

    suspend fun refresh() = withContext(Dispatchers.IO) { shelf.value = store.load() }

    fun got(id: String): Got? = shelf.value.got.firstOrNull { it.meta.id == id }

    // ---- files from outside ----

    suspend fun import(uri: Uri): Incoming = withContext(Dispatchers.IO) {
        val bytes = runCatching {
            app.contentResolver.openInputStream(uri)?.use { s ->
                val out = java.io.ByteArrayOutputStream()
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = s.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    if (out.size() > 64 shl 20) return@runCatching null
                }
                out.toByteArray()
            }
        }.getOrNull() ?: return@withContext Incoming.Bad
        import(bytes)
    }

    suspend fun import(bytes: ByteArray): Incoming = withContext(Dispatchers.IO) {
        val r = when (val p = Matr.read(bytes)) {
            is Parsed.Doll -> { store.putGot(p.sealed); Nudge.plan(app, p.sealed.meta, store.progress(p.sealed.meta.id).keys.size); Incoming.Doll(p.sealed.meta.id) }
            is Parsed.Going -> { store.putDraft(p.relay); Incoming.Turn(p.relay.id) }
            is Parsed.Back -> { store.putReply(p.reply, bytes); Incoming.Back(p.reply.id, p.reply.from) }
            null -> Incoming.Bad
        }
        refresh()
        r
    }

    // ---- making ----

    suspend fun saveDraft(r: Relay) = withContext(Dispatchers.IO) {
        store.putDraft(r)
        refresh()
    }

    suspend fun deleteDraft(id: String) = withContext(Dispatchers.IO) {
        store.deleteDraft(id)
        refresh()
    }

    private fun stub(r: Relay, sealer: Boolean, mine: Int) = Given(
        id = r.id, title = r.title, to = r.to, at = System.currentTimeMillis(),
        authors = (r.people + r.layers.map { it.author }).map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
        paints = r.layers.map { it.paint } + r.giftPaint,
        locks = r.layers.map { it.lock.kind } + LockKind.NONE,
        sealer = sealer, mine = mine,
    )

    /** Seals the matryoshka: the file to send, and a place on my "given" shelf. */
    suspend fun seal(r: Relay): File = withContext(Dispatchers.IO) {
        val bytes = Matr.seal(r.draft())
        store.putGiven(stub(r, true, if (r.people.size > 1) r.turn else -1), bytes)
        store.deleteDraft(r.id)
        refresh()
        shareFile(r.title.ifBlank { r.to }, bytes)
    }

    /** My figure is painted: the matryoshka goes on to the next giver. */
    suspend fun pass(r: Relay): File = withContext(Dispatchers.IO) {
        val next = r.copy(turn = r.turn + 1)
        val bytes = Matr.relay(next)
        store.putGiven(stub(r, false, r.turn), null)
        store.deleteDraft(r.id)
        refresh()
        shareFile(r.title.ifBlank { r.to }, bytes)
    }

    suspend fun resend(id: String, title: String): File? = withContext(Dispatchers.IO) {
        store.givenFile(id)?.readBytes()?.let { shareFile(title, it) }
    }

    suspend fun forgetGiven(id: String) = withContext(Dispatchers.IO) { store.deleteGiven(id); refresh() }

    // ---- opening ----

    suspend fun opened(id: String, o: Opened) = withContext(Dispatchers.IO) {
        val p = store.progress(id)
        if (o.index == p.keys.size) {
            store.saveProgress(id, p.copy(keys = p.keys + o.key))
            got(id)?.let { Nudge.plan(app, it.meta, o.index + 1) }
        }
        refresh()
    }

    suspend fun note(id: String, figure: Int, text: String) = withContext(Dispatchers.IO) {
        val p = store.progress(id)
        val notes = if (text.isBlank()) p.notes - figure else p.notes + (figure to text.trim())
        store.saveProgress(id, p.copy(notes = notes))
        refresh()
    }

    suspend fun voice(id: String, figure: Int, ms: Long?) = withContext(Dispatchers.IO) {
        val p = store.progress(id)
        store.saveProgress(id, p.copy(voices = if (ms == null) p.voices - figure else p.voices + (figure to ms)))
        if (ms == null) store.voiceFile(id, figure).delete()
        refresh()
    }

    /** The reactions as a file for the givers. */
    suspend fun reply(g: Got, from: String): File = withContext(Dispatchers.IO) {
        val p = store.progress(g.meta.id)
        val figures = (p.notes.keys + p.voices.keys).sorted()
        val notes = figures.map { f ->
            val v = store.voiceFile(g.meta.id, f).takeIf { it.exists() && p.voices.containsKey(f) }
            ReplyNote(f, p.notes[f].orEmpty(), v?.readBytes(), p.voices[f] ?: 0)
        }
        store.saveProgress(g.meta.id, p.copy(replied = true))
        refresh()
        val bytes = Matr.reply(Reply(g.meta.id, from, System.currentTimeMillis(), notes))
        shareFile((if (app.ru) "Отклик — " else "Reply — ") + g.meta.title.ifBlank { g.meta.to }, bytes)
    }

    suspend fun forgetGot(id: String) = withContext(Dispatchers.IO) { store.deleteGot(id); refresh() }

    // ---- sharing ----

    fun shareFile(name: String, bytes: ByteArray): File {
        val dir = File(app.cacheDir, "share").apply { mkdirs() }
        dir.listFiles()?.forEach { if (System.currentTimeMillis() - it.lastModified() > 86_400_000L) it.delete() }
        val clean = name.replace(Regex("[\\\\/:*?\"<>|\\n\\r\\t]"), " ").trim().take(60).ifEmpty { "matryoshka" }
        return File(dir, "$clean.${Matr.EXT}").also { it.writeBytes(bytes) }
    }

    fun shareIntent(f: File): Intent {
        val uri = FileProvider.getUriForFile(app, "${app.packageName}.files", f)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).setType("application/octet-stream").putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            null,
        )
    }
}

class MatryoshkaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.scope.launch {
            try {
                if (BuildConfig.DEMO) DemoSeed.seedIfEmpty(this@MatryoshkaApp, container)
                container.refresh()
            } finally {
                container.ready.value = true
            }
        }
    }
}

val Context.container: AppContainer get() = (applicationContext as MatryoshkaApp).container

val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"

val Context.locale: Locale get() = resources.configuration.locales[0]
