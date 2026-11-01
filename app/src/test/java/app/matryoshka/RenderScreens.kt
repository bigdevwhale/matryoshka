package app.matryoshka

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import app.matryoshka.data.DemoSeed
import app.matryoshka.data.Progress
import app.matryoshka.data.Settings
import app.matryoshka.ui.Overlay
import app.matryoshka.ui.Root
import app.matryoshka.ui.Tab
import app.matryoshka.ui.onboarding.Onboarding
import app.matryoshka.ui.theme.MatryoshkaTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Every main screen over the demo shelf, drawn to PNGs (run with `-Prender`), in both languages. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }
    private val app get() = ApplicationProvider.getApplicationContext<MatryoshkaApp>()
    private val big = "demo30th0000001"

    private fun seed(opened: Int = 0): Settings = runBlocking {
        val c = app.container
        c.demo = true
        DemoSeed.seedIfEmpty(app, c)
        // Open the first [opened] figures of the big one, answering as the recipient would.
        val s = c.store.load().got.first { it.meta.id == big }.sealed
        val answers = if (app.ru) listOf(null, "пломбир", null, "калачи") else listOf(null, "snowball", null, "scones")
        var seedB = s.meta.seed
        val keys = ArrayList<ByteArray>()
        for (i in 0 until opened) {
            val k = s.key(i, seedB, answers.getOrNull(i))
            val o = s.open(i, k) ?: break
            keys += k
            seedB = o.next ?: break
        }
        c.store.saveProgress(big, Progress(keys))
        c.refresh()
        c.settings.now()
    }

    private fun shot(name: String, waitFor: String?, act: (() -> Unit)? = null, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent { MatryoshkaTheme { content() } }
        rule.waitUntil(20_000) { rule.mainClock.advanceTimeBy(100); waitFor == null || rule.onAllNodesWithText(waitFor, substring = true).fetchSemanticsNodes().isNotEmpty() }
        rule.mainClock.advanceTimeBy(1_500)
        if (act != null) { act(); rule.mainClock.advanceTimeBy(3_000) }
        rule.waitForIdle()
        Thread.sleep(600)
        rule.mainClock.advanceTimeBy(800)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun s(id: Int) = app.getString(id)

    private fun screen(name: String, tab: Tab, overlay: Overlay? = null, wait: String, opened: Int = 0, act: (() -> Unit)? = null) {
        val st = seed(opened)
        shot(name, wait, act) { Root(st, tab, overlay) }
    }

    /** Touches the figure standing on the table. */
    private fun touchFigure() {
        rule.onNodeWithTag("figure", useUnmergedTree = true).performClick()
    }

    @Test fun welcome() { val st = seed(); shot("0-welcome", null) { Onboarding(st.copy(onboarded = false)) } }
    @Test fun locks() { val st = seed(); shot("0-locks", null) { Onboarding(st.copy(onboarded = false), start = 1) } }

    /** The launcher icon as a PNG for the README and the site. */
    @Test fun logo() {
        val n = 512
        val image = android.graphics.Bitmap.createBitmap(n, n, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(image)
        val clip = android.graphics.Path().apply { addRoundRect(0f, 0f, n.toFloat(), n.toFloat(), n * .22f, n * .22f, android.graphics.Path.Direction.CW) }
        canvas.clipPath(clip)
        val k = n / 80f * 108f
        val off = (n - k) / 2
        for (id in listOf(R.drawable.ic_launcher_background, R.drawable.ic_launcher_foreground)) {
            val d = androidx.core.content.ContextCompat.getDrawable(app, id)!!
            d.setBounds(off.toInt(), off.toInt(), (off + k).toInt(), (off + k).toInt())
            d.draw(canvas)
        }
        File(out.parentFile, "logo.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun shelf() = screen("1-shelf", Tab.SHELF, wait = s(R.string.shelf_given))
    @Test fun ready() = screen("2-ready", Tab.SHELF, Overlay.Open(big), s(R.string.unlocked))
    @Test fun opened() = screen("2-opened", Tab.SHELF, Overlay.Open(big), s(R.string.unlocked)) { touchFigure() }
    @Test fun gift() = screen("5-gift", Tab.SHELF, Overlay.Open(big), s(R.string.tap_it), opened = 5) { touchFigure() }
    @Test fun riddle() = screen("3-riddle", Tab.SHELF, Overlay.Open(big), s(R.string.answer_hint), opened = 1)
    @Test fun place() = screen("4-place", Tab.SHELF, Overlay.Open(big), s(R.string.place_q).substringBefore("%").trim(), opened = 2)
    @Test fun gesture() = screen("5-gesture", Tab.SHELF, Overlay.Open(big), s(R.string.g_cover), opened = 4)
    @Test fun review() = screen("6-review", Tab.SHELF, Overlay.Open("demoweek0000002"), s(R.string.review_t))
    @Test fun workshop() = screen("7-workshop", Tab.WORKSHOP, wait = s(R.string.new_doll))
    @Test fun edit() = screen("8-edit", Tab.WORKSHOP, Overlay.Edit("demohome0000007"), s(R.string.seal))
    @Test fun relay() = screen("9-relay", Tab.WORKSHOP, Overlay.Edit("demodad00000008"), s(R.string.relay_after).substringBefore("%").trim())
    @Test fun gave() = screen("10-gave", Tab.SHELF, Overlay.Gave("demowed00000004"), s(R.string.replies))
    @Test fun settings() = screen("11-settings", Tab.SHELF, Overlay.Settings, s(R.string.theme_dark))
}

@Config(sdk = [34], qualifiers = "en-w411dp-h891dp-night-xxhdpi")
class RenderScreensEn : ScreensBase() { override val lang = "en" }

@Config(sdk = [34], qualifiers = "ru-w411dp-h891dp-night-xxhdpi")
class RenderScreensRu : ScreensBase() { override val lang = "ru" }

@Config(sdk = [34], qualifiers = "ru-w411dp-h891dp-notnight-xxhdpi")
class RenderScreensRuLight : ScreensBase() { override val lang = "ru-light" }
