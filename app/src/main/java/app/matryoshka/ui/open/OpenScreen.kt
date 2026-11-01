package app.matryoshka.ui.open

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Got
import app.matryoshka.data.Photos
import app.matryoshka.data.Recorder
import app.matryoshka.data.Settings
import app.matryoshka.data.Where
import app.matryoshka.domain.Content
import app.matryoshka.domain.Figure
import app.matryoshka.domain.Geo
import app.matryoshka.domain.Gesture
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Opened
import app.matryoshka.ui.Nav
import app.matryoshka.ui.art.Doll
import app.matryoshka.ui.art.colors
import app.matryoshka.ui.art.doll
import app.matryoshka.ui.components.Btn
import app.matryoshka.ui.components.Caps
import app.matryoshka.ui.components.Dates
import app.matryoshka.ui.components.Field
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.Kind
import app.matryoshka.ui.components.Round
import app.matryoshka.ui.components.Toast
import app.matryoshka.ui.components.TopBar
import app.matryoshka.ui.components.Voice
import app.matryoshka.ui.make.gestureName
import app.matryoshka.ui.make.lockName
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.Paper
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private enum class Phase { LOCKED, READY, SHOWN }

/** A matryoshka given to me, opened one figure at a time. */
@Composable
fun OpenScreen(id: String, s: Settings, nav: Nav, demoNow: Long? = null) {
    val ctx = LocalContext.current
    val c = ctx.container
    val shelf by c.shelf.collectAsState()
    val g = shelf.got.firstOrNull { it.meta.id == id }
    if (g == null) {
        Column(Modifier.fillMaxSize()) {
            TopBar(stringResource(R.string.app_name), onBack = { nav.close() })
            Text(stringResource(R.string.open_gone), style = T.Body, modifier = Modifier.padding(18.dp))
        }
        return
    }
    // What's been opened so far, decrypted with the keys kept on the phone.
    val opened by produceState<List<Opened>?>(null, g.progress.keys.size) {
        value = withContext(Dispatchers.IO) { g.progress.keys.mapIndexedNotNull { i, k -> g.sealed.open(i, k) } }
    }
    val list = opened ?: return
    // Fully opened before this visit: show everything. Opened just now: stay on the gift until "to the shelf".
    var reviewing by remember(id) { mutableStateOf(g.done) }
    if (reviewing && list.size == g.sealed.count) Review(g, list, s, nav)
    else Opening(g, list, s, nav, demoNow, onFinished = { reviewing = true })
}

@Composable
private fun Header(g: Got, cur: Int, nav: Nav) {
    TopBar(
        g.meta.title.ifBlank { stringResource(R.string.for_you) },
        if (g.meta.authors.isEmpty()) null else stringResource(R.string.from_x, g.meta.authors.joinToString(", ")),
        onBack = { nav.close() },
    )
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.Center) {
        for (i in 0 until g.sealed.count) {
            Box(
                Modifier.padding(horizontal = 2.5.dp).size(width = if (i == cur) 26.dp else maxOf(8, 20 - i * 2).dp, height = 6.dp).clip(CircleShape)
                    .background(when { i < cur -> C.gold.copy(alpha = .6f); i == cur -> C.gold; else -> C.gold.copy(alpha = .18f) })
            )
        }
    }
}

@Composable
private fun Opening(g: Got, list: List<Opened>, s: Settings, nav: Nav, demoNow: Long?, onFinished: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    // The figure on the table. A figure is saved the moment it opens; the table moves on only with "next".
    var i by remember(g.meta.id) { mutableIntStateOf(list.size.coerceAtMost(g.sealed.count - 1)) }
    val f = g.meta.figures[i]
    val solid = i == g.sealed.count - 1
    val seed = if (i == 0) g.meta.seed else list.getOrNull(i - 1)?.next ?: g.meta.seed
    var phase by remember(i) { mutableStateOf(if (f.lock.kind == LockKind.NONE) Phase.READY else Phase.LOCKED) }
    var got by remember(i) { mutableStateOf<Opened?>(null) }
    var sheet by remember(i) { mutableStateOf(false) }
    val open = remember(i) { Animatable(0f) }
    val leave = remember(i) { Animatable(0f) }
    var petals by remember(i) { mutableStateOf(false) }

    fun ready() { if (phase == Phase.LOCKED) { phase = Phase.READY; haptic.performHapticFeedback(HapticFeedbackType.LongPress) } }
    fun reveal() {
        if (phase != Phase.READY) return
        scope.launch {
            val o = got ?: withContext(Dispatchers.Default) { g.sealed.open(i, g.sealed.key(i, seed, null)) }
            if (o == null) { Toast.show(ctx.getString(R.string.open_broken)); return@launch }
            got = o
            phase = Phase.SHOWN
            c.opened(g.meta.id, o)
            if (solid) petals = true else open.animateTo(1f, spring(dampingRatio = .55f, stiffness = Spring.StiffnessVeryLow))
            delay(if (solid) 600 else 200)
            sheet = true
        }
    }
    fun next() {
        val o = got ?: return
        scope.launch {
            sheet = false
            leave.animateTo(1f, tween(380))
            i = o.index + 1
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(C.bg2, C.bg), radius = 1600f))) {
        Column(Modifier.fillMaxSize()) {
            Header(g, i, nav)
            // The stage: the figure stands on the table; the next one waits inside.
            androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).padding(bottom = 6.dp), contentAlignment = Alignment.BottomCenter) {
                // The figure fits the stage: with the keyboard up it gets smaller rather than climbing over the title.
                val w = minOf((220 - i * 14).coerceAtLeast(120).dp, (maxHeight - 24.dp) * (2f / 3f))
                Canvas(Modifier.width(w + 40.dp).height(26.dp)) {
                    drawOval(Brush.radialGradient(listOf(Color(0x8C000000), Color.Transparent)), Offset.Zero, size)
                }
                g.meta.figures.getOrNull(i + 1)?.let { nf ->
                    Doll(
                        nf.paint, Modifier.width(w * .74f).offset(y = (-22 - 30 * open.value).dp)
                            .graphicsLayer { alpha = (open.value * 1.6f).coerceIn(0f, 1f) },
                        solid = i + 1 == g.sealed.count - 1,
                    )
                }
                val wob = rememberInfiniteTransition(label = "wob")
                val angle by wob.animateFloat(0f, 0f, infiniteRepeatable(keyframes {
                    durationMillis = 1600
                    0f at 0; -4f at 320; 3.5f at 640; -2f at 960; 1f at 1280; 0f at 1600
                }), label = "a")
                val pulse by wob.animateFloat(.35f, .8f, infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse), label = "p")
                var hold by remember(i) { mutableFloatStateOf(0f) }
                val lockLabel = lockName(f.lock.kind).lowercase()
                Box(
                    Modifier.padding(bottom = 10.dp).width(w).testTag("figure")
                        .graphicsLayer {
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(.5f, 1f)
                            rotationZ = if (phase == Phase.READY) angle else 0f
                            alpha = 1f - leave.value
                            translationY = leave.value * 120f
                            if (solid && phase == Phase.SHOWN) { scaleX = .75f; scaleY = .75f; translationY = -size.height * .95f }
                        }
                        .pointerInput(i, phase) {
                            detectTapGestures(
                                onPress = {
                                    if (phase == Phase.LOCKED && f.lock.kind == LockKind.GESTURE && f.lock.gesture == Gesture.COVER) {
                                        val job = scope.launch {
                                            val start = System.currentTimeMillis()
                                            while (true) {
                                                hold = ((System.currentTimeMillis() - start) / 1600f).coerceAtMost(1f)
                                                if (hold >= 1f) { ready(); break }
                                                delay(16)
                                            }
                                        }
                                        tryAwaitRelease()
                                        job.cancel()
                                        hold = 0f
                                    }
                                },
                                onTap = {
                                    when (phase) {
                                        Phase.READY -> reveal()
                                        Phase.LOCKED -> if (!(f.lock.kind == LockKind.GESTURE && f.lock.gesture == Gesture.COVER))
                                            Toast.show(ctx.getString(R.string.still_locked, lockLabel))
                                        Phase.SHOWN -> if (!sheet) sheet = true
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (phase == Phase.READY) {
                        Canvas(Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
                            drawCircle(Brush.radialGradient(listOf(C.gold.copy(alpha = pulse * .55f), Color.Transparent)), size.width * .75f, center)
                        }
                    }
                    Canvas(Modifier.fillMaxWidth().aspectRatio(2f / 3f)) { doll(f.paint, open.value, solid) }
                    if (hold > 0f) Canvas(Modifier.size(120.dp)) {
                        drawCircle(C.gold.copy(alpha = .2f), size.minDimension / 2 - 4, style = Stroke(6f))
                        drawArc(C.gold, -90f, 360f * hold, false, Offset(4f, 4f), Size(size.width - 8, size.height - 8), style = Stroke(8f))
                    }
                }
            }
            LockPanel(g, i, f, solid, phase, demoNow, onReady = ::ready, onSecret = { o -> got = o; ready() })
        }
        if (petals) Petals()
        AnimatedVisibility(sheet, Modifier.align(Alignment.BottomCenter), enter = slideInVertically { it }, exit = slideOutVertically { it }) {
            got?.let { o -> ContentSheet(g, o, f, solid, s, onNext = ::next, onDone = onFinished) }
        }
    }
}

/** Below the figure: what keeps it shut, and the way to open it. */
@Composable
private fun LockPanel(g: Got, i: Int, f: Figure, solid: Boolean, phase: Phase, demoNow: Long?, onReady: () -> Unit, onSecret: (Opened) -> Unit) {
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 16.dp).heightIn(min = 150.dp)
            .clip(RoundedCornerShape(20.dp)).background(C.surface.copy(alpha = .92f)).border(1.dp, C.line, RoundedCornerShape(20.dp)).padding(16.dp)
    ) {
        if (solid) {
            Caps(stringResource(R.string.smallest))
            Gap(6.dp)
            Text(stringResource(R.string.smallest_b), style = T.Body)
            Gap(8.dp)
            Text(stringResource(R.string.tap_it), style = T.Small.copy(color = C.green))
            return@Column
        }
        Caps(stringResource(R.string.figure_of, i + 1, g.sealed.count) + " · " + lockName(f.lock.kind))
        Gap(6.dp)
        if (phase != Phase.LOCKED) {
            Text(stringResource(if (phase == Phase.READY) R.string.unlocked else R.string.opened_t), style = T.Heading.copy(color = C.green))
            Gap(4.dp)
            Text(stringResource(R.string.unlocked_b), style = T.Body.copy(color = C.ink2))
            if (f.author.isNotBlank()) { Gap(6.dp); Text(stringResource(R.string.painted_x, f.author), style = T.Small) }
            return@Column
        }
        when (f.lock.kind) {
            LockKind.TIME -> TimePanel(f, demoNow, onReady)
            LockKind.RIDDLE, LockKind.WORD -> SecretPanel(g, i, f, onSecret)
            LockKind.PLACE -> PlacePanel(f, onReady)
            LockKind.GESTURE -> GesturePanel(f, onReady)
            LockKind.NONE -> {}
        }
    }
}

@Composable
private fun TimePanel(f: Figure, demoNow: Long?, onReady: () -> Unit) {
    val ctx = LocalContext.current
    var now by remember { mutableLongStateOf(demoNow ?: System.currentTimeMillis()) }
    LaunchedEffect(f.lock.at) {
        while (true) {
            if (demoNow == null) now = System.currentTimeMillis()
            if (now >= f.lock.at) { onReady(); break }
            delay(1000)
        }
    }
    Text(stringResource(R.string.time_opens, Dates.dayTime(f.lock.at, ctx)), style = T.Body)
    Gap(4.dp)
    Text(Dates.left(f.lock.at - now, ctx), style = T.Number)
    Gap(4.dp)
    Text(stringResource(R.string.time_note), style = T.Small)
}

@Composable
private fun SecretPanel(g: Got, i: Int, f: Figure, onSecret: (Opened) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember(i) { mutableStateOf("") }
    var wrong by remember(i) { mutableIntStateOf(0) }
    var busy by remember(i) { mutableStateOf(false) }
    val shake = remember(i) { Animatable(0f) }
    val word = f.lock.kind == LockKind.WORD
    Text(if (word) stringResource(R.string.word_q, f.lock.who) else f.lock.question, style = T.Heading)
    Gap(10.dp)
    fun check() {
        if (busy || text.isBlank()) return
        busy = true
        scope.launch {
            val seed = if (i == 0) g.meta.seed else g.sealed.open(i - 1, g.progress.keys[i - 1])?.next ?: g.meta.seed
            val o = withContext(Dispatchers.Default) { g.sealed.open(i, g.sealed.key(i, seed, text)) }
            busy = false
            if (o != null) onSecret(o)
            else {
                wrong++
                for (x in listOf(-14f, 12f, -8f, 5f, 0f)) shake.animateTo(x, tween(60))
            }
        }
    }
    Row(Modifier.offset(x = shake.value.dp), verticalAlignment = Alignment.CenterVertically) {
        Field(text, { text = it }, Modifier.weight(1f), stringResource(if (word) R.string.word_hint else R.string.answer_hint), onDone = ::check)
        Gap(0.dp)
        Box(Modifier.padding(start = 8.dp)) {
            Btn(stringResource(if (busy) R.string.checking else R.string.open_it), ::check, enabled = !busy && text.isNotBlank())
        }
    }
    Gap(8.dp)
    Text(
        when {
            wrong > 0 && f.lock.hint.isNotBlank() -> stringResource(R.string.wrong_hint, f.lock.hint)
            wrong > 0 -> stringResource(R.string.wrong)
            else -> stringResource(R.string.secret_note)
        },
        style = T.Small.copy(color = if (wrong > 0) C.red else C.muted),
    )
}

@Composable
private fun PlacePanel(f: Figure, onReady: () -> Unit) {
    val ctx = LocalContext.current
    var allowed by remember { mutableStateOf(Where.allowed(ctx)) }
    var dist by remember { mutableStateOf<Double?>(null) }
    var bearing by remember { mutableFloatStateOf(0f) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allowed = Where.allowed(ctx) }
    if (allowed) DisposableEffect(Unit) {
        val stop = Where.follow(ctx) { l ->
            val d = Geo.distance(l.latitude, l.longitude, f.lock.lat, f.lock.lon)
            dist = d
            bearing = Geo.bearing(l.latitude, l.longitude, f.lock.lat, f.lock.lon).toFloat()
            if (d <= f.lock.radius + minOf(l.accuracy.toDouble(), 50.0)) onReady()
        }
        onDispose { stop() }
    }
    Text(stringResource(R.string.place_q, f.lock.place.ifBlank { stringResource(R.string.place_some) }), style = T.Heading)
    Gap(10.dp)
    if (!allowed) {
        Text(stringResource(R.string.place_need), style = T.Small)
        Gap(8.dp)
        Btn(stringResource(R.string.place_allow), { ask.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }, Modifier.fillMaxWidth())
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(56.dp).clip(CircleShape).border(1.dp, C.line, CircleShape), contentAlignment = Alignment.Center) {
            val gold = C.gold
            Canvas(Modifier.size(28.dp).rotate(bearing)) {
                val p = Path().apply {
                    moveTo(size.width / 2, 0f); lineTo(size.width * .78f, size.height); lineTo(size.width / 2, size.height * .78f); lineTo(size.width * .22f, size.height); close()
                }
                drawPath(p, gold)
            }
        }
        Column(Modifier.padding(start = 14.dp)) {
            Text(dist?.let { meters(it) } ?: "…", style = T.Number)
            Text(stringResource(R.string.place_dir, compass(bearing), f.lock.radius), style = T.Small)
        }
    }
    Gap(8.dp)
    Text(
        stringResource(R.string.place_map), style = T.Small.copy(color = C.accent, textDecoration = TextDecoration.Underline),
        modifier = Modifier.clickable {
            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${f.lock.lat},${f.lock.lon}?q=${f.lock.lat},${f.lock.lon}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        },
    )
}

@Composable
private fun meters(d: Double): String = if (d >= 1000) stringResource(R.string.km, "%.1f".format(d / 1000).replace('.', ',')) else stringResource(R.string.m, d.roundToInt())

@Composable
private fun compass(b: Float): String {
    val names = androidx.compose.ui.res.stringArrayResource(R.array.compass)
    return names[((b + 22.5f) / 45f).toInt() % 8]
}

@Composable
private fun GesturePanel(f: Figure, onReady: () -> Unit) {
    val ctx = LocalContext.current
    Text(gestureName(f.lock.gesture), style = T.Heading)
    Gap(6.dp)
    Text(
        stringResource(
            when (f.lock.gesture) {
                Gesture.COVER -> R.string.g_cover_do
                Gesture.SHAKE -> R.string.g_shake_do
                Gesture.FLIP -> R.string.g_flip_do
            }
        ),
        style = T.Body.copy(color = C.ink2),
    )
    DisposableEffect(f.lock.gesture) {
        val stop = Sensors.watch(ctx, f.lock.gesture, onReady)
        onDispose { stop() }
    }
}

/** What was inside: on warm paper, with a place to answer. */
@Composable
private fun ContentSheet(g: Got, o: Opened, f: Figure, solid: Boolean, s: Settings, onNext: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxWidth().heightIn(max = 620.dp).clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(Brush.verticalGradient(listOf(Paper.bg, Paper.bg2)))
            .clickable(remember { MutableInteractionSource() }, null) {}
            .navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).size(44.dp, 5.dp).clip(CircleShape).background(Paper.ink.copy(alpha = .2f)))
        Gap(12.dp)
        Text(
            (if (solid) stringResource(R.string.smallest) else stringResource(R.string.figure_n, o.index + 1)).uppercase() +
                if (f.author.isNotBlank()) " · " + stringResource(R.string.from_x, f.author).uppercase() else "",
            style = T.Caps.copy(color = Paper.accent),
        )
        Gap(6.dp)
        Inside(o.content)
        Gap(14.dp)
        Reaction(g, o.index)
        Gap(14.dp)
        if (solid) {
            Btn(stringResource(R.string.send_reply), {
                scope.launch {
                    val file = c.reply(c.got(g.meta.id) ?: g, s.name)
                    ctx.startActivity(c.shareIntent(file).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    onDone()
                }
            }, Modifier.fillMaxWidth())
            Gap(8.dp)
            Btn(stringResource(R.string.to_shelf), onDone, Modifier.fillMaxWidth(), Kind.GHOST, ink = Paper.ink)
        } else Btn(stringResource(R.string.next_figure), onNext, Modifier.fillMaxWidth())
    }
}

/** A note, a photo, a voice, a link: whatever a figure holds. */
@Composable
fun Inside(content: Content) {
    val ctx = LocalContext.current
    if (content.note.isNotBlank()) Text(content.note, style = T.Italic.copy(color = Paper.ink))
    content.photo?.let { p ->
        val bmp = remember(p) { Photos.decode(p, 1200)?.asImageBitmap() }
        if (bmp != null) {
            Gap(12.dp)
            Box(Modifier.fillMaxWidth().rotate(-1.5f).background(Color.White).padding(10.dp)) {
                Image(bmp, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().aspectRatio(bmp.width.toFloat() / bmp.height.coerceAtLeast(1)))
            }
        }
    }
    content.voice?.let { v ->
        Gap(12.dp)
        Voice(v, content.voiceMs, Paper.ink, Paper.accent)
    }
    if (content.link.isNotBlank()) {
        Gap(12.dp)
        Text(
            content.link, style = T.Body.copy(color = Paper.accent, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.clickable {
                val u = if (content.link.startsWith("http")) content.link else "https://${content.link}"
                runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            },
        )
    }
}

/** A few words (and maybe a voice) back to whoever painted this figure. */
@Composable
private fun Reaction(g: Got, figure: Int) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    var text by remember(figure) { mutableStateOf(g.progress.notes[figure].orEmpty()) }
    LaunchedEffect(text) { delay(500); if (text != g.progress.notes[figure].orEmpty()) c.note(g.meta.id, figure, text) }
    val rec = remember { Recorder(ctx) }
    var on by remember { mutableStateOf(false) }
    var ms by remember { mutableLongStateOf(0L) }
    var has by remember(figure) { mutableStateOf(g.progress.voices[figure]) }
    val file: File = remember(figure) { c.store.voiceFile(g.meta.id, figure) }
    DisposableEffect(Unit) { onDispose { rec.stop() } }
    LaunchedEffect(on) { while (on) { ms = rec.elapsed; delay(200) } }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) on = rec.start(file) }
    Text(stringResource(R.string.reaction).uppercase(), style = T.Caps.copy(color = Paper.accent))
    Gap(6.dp)
    androidx.compose.foundation.text.BasicTextField(
        text, { text = it }, textStyle = T.Body.copy(color = Paper.ink),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Paper.accent),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().clip(Round).background(Color.White.copy(alpha = .55f)).padding(12.dp)) {
                if (text.isEmpty()) Text(stringResource(R.string.reaction_hint), style = T.Body.copy(color = Paper.muted))
                inner()
            }
        },
    )
    Gap(8.dp)
    val h = has
    when {
        on -> Text(
            stringResource(R.string.voice_stop, Dates.duration(ms)), style = T.Label.copy(color = Paper.accent),
            modifier = Modifier.clickable {
                on = false
                val d = rec.stop()
                if (d != null) { has = d; scope.launch { c.voice(g.meta.id, figure, d) } }
            }.padding(vertical = 6.dp),
        )
        h != null && file.exists() -> {
            Voice(file.readBytes(), h, Paper.ink, Paper.accent)
            Text(stringResource(R.string.voice_remove), style = T.Small.copy(color = Paper.muted), modifier = Modifier.clickable {
                has = null
                scope.launch { c.voice(g.meta.id, figure, null) }
            }.padding(vertical = 6.dp))
        }
        else -> Text(
            "● " + stringResource(R.string.reaction_voice), style = T.Label.copy(color = Paper.accent),
            modifier = Modifier.clickable {
                if (ctx.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) on = rec.start(file)
                else ask.launch(Manifest.permission.RECORD_AUDIO)
            }.padding(vertical = 6.dp),
        )
    }
    Text(stringResource(R.string.reaction_note), style = T.Small.copy(color = Paper.muted))
}

/** Everything opened: the smallest figure, and every note again. */
@Composable
private fun Review(g: Got, list: List<Opened>, s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        Header(g, g.sealed.count, nav)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                g.meta.figures.forEachIndexed { i, f ->
                    Doll(f.paint, Modifier.width((64 - i * 5).coerceAtLeast(26).dp).padding(horizontal = 1.dp), solid = i == g.sealed.count - 1)
                }
            }
            Text(stringResource(R.string.review_t), style = T.Sign)
            Gap(10.dp)
            for (o in list) {
                val f = g.meta.figures[o.index]
                Column(Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(Round).background(Paper.bg).padding(16.dp)) {
                    Text(
                        (if (o.index == g.sealed.count - 1) stringResource(R.string.smallest) else stringResource(R.string.figure_n, o.index + 1)).uppercase() +
                            if (f.author.isNotBlank()) " · " + f.author.uppercase() else "",
                        style = T.Caps.copy(color = Paper.accent),
                    )
                    Gap(6.dp)
                    Inside(o.content)
                    g.progress.notes[o.index]?.let {
                        Gap(8.dp)
                        Text(stringResource(R.string.you_said, it), style = T.Small.copy(color = Paper.muted))
                    }
                }
            }
        }
        Column(Modifier.navigationBarsPadding().padding(18.dp)) {
            Btn(stringResource(if (g.progress.replied) R.string.send_reply_again else R.string.send_reply), {
                scope.launch {
                    val file = c.reply(g, s.name)
                    ctx.startActivity(c.shareIntent(file).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }, Modifier.fillMaxWidth())
        }
    }
}

/** Petals and gold leaf falling for the last figure. */
@Composable
private fun Petals() {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(4200, easing = LinearEasing)) }
    val bits = remember { List(46) { Triple(Random.nextFloat(), Random.nextFloat() * .35f, Random.nextFloat()) } }
    val cols = listOf(Color(0xFFC8323C), Color(0xFFE8B24A), Color(0xFFF7E8D0), Color(0xFF24489A), Color(0xFFEC6A86))
    Canvas(Modifier.fillMaxSize()) {
        bits.forEachIndexed { k, (x, delay, spin) ->
            val p = ((t.value - delay) / .65f).coerceIn(0f, 1f)
            if (p <= 0f || p >= 1f) return@forEachIndexed
            val y = -30f + p * (size.height + 60f)
            val xx = x * size.width + sin(p * 9f + k) * 30f
            rotate(p * 540f * (spin - .5f) * 2, Offset(xx, y)) {
                drawOval(cols[k % cols.size].copy(alpha = 1f - p * .6f), Offset(xx - 8f, y - 11f), Size(16f, 22f))
            }
        }
    }
}
