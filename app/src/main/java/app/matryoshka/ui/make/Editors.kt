package app.matryoshka.ui.make

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.matryoshka.R
import app.matryoshka.data.Photos
import app.matryoshka.data.Recorder
import app.matryoshka.data.Where
import app.matryoshka.domain.Content
import app.matryoshka.domain.Geo
import app.matryoshka.domain.Gesture
import app.matryoshka.domain.Layer
import app.matryoshka.domain.Lock
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Paint
import app.matryoshka.domain.Relay
import app.matryoshka.ui.art.colors
import app.matryoshka.ui.components.Btn
import app.matryoshka.ui.components.Caps
import app.matryoshka.ui.components.Card
import app.matryoshka.ui.components.Chip
import app.matryoshka.ui.components.Dates
import app.matryoshka.ui.components.Field
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.Kind
import app.matryoshka.ui.components.Round
import app.matryoshka.ui.components.Toast
import app.matryoshka.ui.components.Voice
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun lockGlyph(k: LockKind) = when (k) {
    LockKind.NONE -> ""
    LockKind.TIME -> "◷"
    LockKind.RIDDLE -> "?"
    LockKind.PLACE -> "⌖"
    LockKind.WORD -> "✎"
    LockKind.GESTURE -> "✋"
}

@Composable
fun lockName(k: LockKind?): String = stringResource(
    when (k) {
        LockKind.TIME -> R.string.lock_time
        LockKind.RIDDLE -> R.string.lock_riddle
        LockKind.PLACE -> R.string.lock_place
        LockKind.WORD -> R.string.lock_word
        LockKind.GESTURE -> R.string.lock_gesture
        else -> R.string.lock_none
    }
)

@Composable
fun paintName(p: Paint): String = stringResource(
    when (p) {
        Paint.KHOKHLOMA -> R.string.p_khokhloma
        Paint.GZHEL -> R.string.p_gzhel
        Paint.POLKHOV -> R.string.p_polkhov
        Paint.SEMYONOV -> R.string.p_semyonov
        Paint.VYATKA -> R.string.p_vyatka
    }
)

@Composable
fun gestureName(g: Gesture): String = stringResource(
    when (g) {
        Gesture.COVER -> R.string.g_cover
        Gesture.SHAKE -> R.string.g_shake
        Gesture.FLIP -> R.string.g_flip
    }
)

@Composable
private fun Swatches(on: Paint, onPick: (Paint) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (p in Paint.entries) {
            val c = p.colors
            Column(Modifier.weight(1f).clickable { onPick(p) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(46.dp).clip(CircleShape).border(2.dp, if (p == on) C.accent else C.line, CircleShape).padding(3.dp).clip(CircleShape)
                ) {
                    Column {
                        Box(Modifier.fillMaxWidth().weight(1f).background(c.scarf))
                        Box(Modifier.fillMaxWidth().weight(1f).background(c.dress))
                    }
                }
                Gap(4.dp)
                Text(paintName(p).replace("-", "-​"), style = T.Small.copy(color = if (p == on) C.accent else C.muted, fontSize = 11.5.sp, lineHeight = 14.sp), textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}

@Composable
fun LayerEditor(index: Int, l: Layer, onChange: (Layer) -> Unit, canRemove: Boolean, onRemove: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Caps(stringResource(R.string.figure_n_paint, index + 1))
        Gap(10.dp)
        Swatches(l.paint) { onChange(l.copy(paint = it)) }
        Gap(12.dp)
        Field(l.author, { onChange(l.copy(author = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_author_hint))
    }
    Gap(12.dp)
    Card(Modifier.fillMaxWidth()) {
        Caps(stringResource(R.string.inside))
        Gap(8.dp)
        ContentEditor(l.content, "f$index") { onChange(l.copy(content = it)) }
    }
    Gap(12.dp)
    Card(Modifier.fillMaxWidth()) {
        Caps(stringResource(R.string.lock))
        Gap(8.dp)
        LockEditor(l, index, onChange)
    }
    if (canRemove) {
        Gap(12.dp)
        Btn(stringResource(R.string.remove_figure), onRemove, Modifier.fillMaxWidth(), Kind.GHOST)
    }
}

@Composable
fun GiftEditor(r: Relay, onChange: (Relay) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Caps(stringResource(R.string.gift_t))
        Gap(6.dp)
        Text(stringResource(R.string.gift_b), style = T.Small.copy(color = C.ink2))
        Gap(10.dp)
        Swatches(r.giftPaint) { onChange(r.copy(giftPaint = it)) }
    }
    Gap(12.dp)
    Card(Modifier.fillMaxWidth()) {
        Caps(stringResource(R.string.inside))
        Gap(8.dp)
        ContentEditor(r.gift, "gift", link = true, hint = stringResource(R.string.gift_hint)) { onChange(r.copy(gift = it)) }
    }
}

@Composable
fun ContentEditor(c: Content, key: String, link: Boolean = false, hint: String = "", onChange: (Content) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    Field(c.note, { onChange(c.copy(note = it)) }, Modifier.fillMaxWidth(), hint.ifEmpty { stringResource(R.string.note_hint) }, lines = 4)
    if (link) {
        Gap(10.dp)
        Field(c.link, { onChange(c.copy(link = it.trim())) }, Modifier.fillMaxWidth(), stringResource(R.string.link_hint), caps = false)
    }
    Gap(12.dp)
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val b = withContext(Dispatchers.IO) { Photos.compress(ctx, uri) }
            if (b != null) onChange(c.copy(photo = b)) else Toast.show(ctx.getString(R.string.photo_failed))
        }
    }
    val photo = c.photo
    if (photo != null) {
        val bmp = remember(photo) { Photos.decode(photo, 900)?.asImageBitmap() }
        if (bmp != null) Image(
            bmp, null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(bmp.width.toFloat() / bmp.height.coerceAtLeast(1)).clip(Round),
        )
        Gap(6.dp)
        Btn(stringResource(R.string.photo_remove), { onChange(c.copy(photo = null)) }, Modifier.fillMaxWidth(), Kind.QUIET)
    } else {
        Btn(stringResource(R.string.photo_add), { pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            Modifier.fillMaxWidth(), Kind.GHOST, icon = Icons.Outlined.AddPhotoAlternate)
    }
    Gap(10.dp)
    VoiceEditor(c, key, onChange)
}

@Composable
private fun VoiceEditor(c: Content, key: String, onChange: (Content) -> Unit) {
    val ctx = LocalContext.current
    val rec = remember { Recorder(ctx) }
    var on by remember { mutableStateOf(false) }
    var ms by remember { mutableLongStateOf(0L) }
    val file = remember(key) { File(ctx.cacheDir, "rec-$key.m4a") }
    DisposableEffect(Unit) { onDispose { rec.stop() } }
    LaunchedEffect(on) {
        while (on) {
            ms = rec.elapsed
            if (ms >= 120_000) { on = false; rec.stop()?.let { onChange(c.copy(voice = file.readBytes(), voiceMs = it)) } }
            delay(200)
        }
    }
    fun start() { on = rec.start(file); if (!on) Toast.show(ctx.getString(R.string.mic_failed)) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) start() }
    val voice = c.voice
    when {
        on -> Btn(stringResource(R.string.voice_stop, Dates.duration(ms)), {
            on = false
            val d = rec.stop()
            if (d != null && file.exists()) onChange(c.copy(voice = file.readBytes(), voiceMs = d))
        }, Modifier.fillMaxWidth(), Kind.PRIMARY, icon = Icons.Rounded.Stop)
        voice != null -> {
            Voice(voice, c.voiceMs, C.ink, C.accent)
            Gap(6.dp)
            Btn(stringResource(R.string.voice_remove), { onChange(c.copy(voice = null, voiceMs = 0)) }, Modifier.fillMaxWidth(), Kind.QUIET)
        }
        else -> Btn(stringResource(R.string.voice_add), {
            if (ctx.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) start()
            else ask.launch(Manifest.permission.RECORD_AUDIO)
        }, Modifier.fillMaxWidth(), Kind.GHOST, icon = Icons.Outlined.Mic)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LockEditor(l: Layer, index: Int, onChange: (Layer) -> Unit) {
    val lock = l.lock
    fun set(x: Lock) = onChange(l.copy(lock = x))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (k in LockKind.entries) {
            Chip((lockGlyph(k) + " " + lockName(k)).trim(), lock.kind == k, { set(lock.copy(kind = k)) })
        }
    }
    Gap(10.dp)
    Text(
        stringResource(
            when (lock.kind) {
                LockKind.NONE -> if (index == 0) R.string.lock_none_b0 else R.string.lock_none_b
                LockKind.TIME -> R.string.lock_time_b
                LockKind.RIDDLE -> R.string.lock_riddle_b
                LockKind.PLACE -> R.string.lock_place_b
                LockKind.WORD -> R.string.lock_word_b
                LockKind.GESTURE -> R.string.lock_gesture_b
            }
        ),
        style = T.Small.copy(color = C.ink2),
    )
    when (lock.kind) {
        LockKind.TIME -> TimeLock(lock) { set(it) }
        LockKind.RIDDLE -> {
            Gap(10.dp)
            Field(lock.question, { set(lock.copy(question = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_question), lines = 2)
            Gap(8.dp)
            Field(l.answer, { onChange(l.copy(answer = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_answer))
            Gap(8.dp)
            Field(lock.hint, { set(lock.copy(hint = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_hint))
        }
        LockKind.WORD -> {
            Gap(10.dp)
            Field(lock.who, { set(lock.copy(who = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_who))
            Gap(8.dp)
            Field(l.answer, { onChange(l.copy(answer = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_word))
            Gap(8.dp)
            Field(lock.hint, { set(lock.copy(hint = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_hint))
        }
        LockKind.PLACE -> PlaceLock(lock) { set(it) }
        LockKind.GESTURE -> {
            Gap(10.dp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (g in Gesture.entries) Chip(gestureName(g), lock.gesture == g, { set(lock.copy(gesture = g)) })
            }
        }
        LockKind.NONE -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeLock(lock: Lock, set: (Lock) -> Unit) {
    val ctx = LocalContext.current
    val zone = ZoneId.systemDefault()
    fun at(d: LocalDate, t: LocalTime) = LocalDateTime.of(d, t).atZone(zone).toInstant().toEpochMilli()
    Gap(10.dp)
    Text(
        if (lock.at > 0) Dates.dayTime(lock.at, ctx) else stringResource(R.string.time_none),
        style = T.Heading.copy(color = if (lock.at > 0) C.ink else C.muted),
    )
    Gap(8.dp)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip(stringResource(R.string.time_hour), false, { set(lock.copy(at = System.currentTimeMillis() + 3_600_000L)) })
        Chip(stringResource(R.string.time_morning), false, { set(lock.copy(at = at(LocalDate.now().plusDays(1), LocalTime.of(8, 0)))) })
        Chip(stringResource(R.string.time_evening), false, { set(lock.copy(at = at(LocalDate.now().plusDays(1), LocalTime.of(19, 0)))) })
    }
    Gap(8.dp)
    Btn(stringResource(R.string.time_pick), {
        val start = if (lock.at > 0) java.time.Instant.ofEpochMilli(lock.at).atZone(zone).toLocalDateTime() else LocalDateTime.now().plusDays(1).withHour(8).withMinute(0)
        DatePickerDialog(ctx, { _, y, m, d ->
            TimePickerDialog(ctx, { _, h, min ->
                set(lock.copy(at = at(LocalDate.of(y, m + 1, d), LocalTime.of(h, min))))
            }, start.hour, start.minute, true).show()
        }, start.year, start.monthValue - 1, start.dayOfMonth).show()
    }, Modifier.fillMaxWidth(), Kind.GHOST, icon = Icons.Outlined.Event)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlaceLock(lock: Lock, set: (Lock) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var raw by remember { mutableStateOf(if (lock.lat != 0.0 || lock.lon != 0.0) "%.5f, %.5f".format(java.util.Locale.ROOT, lock.lat, lock.lon) else "") }
    var finding by remember { mutableStateOf(false) }
    fun here() {
        finding = true
        scope.launch {
            val l = Where.now(ctx)
            finding = false
            if (l == null) Toast.show(ctx.getString(R.string.where_failed))
            else {
                set(lock.copy(lat = l.latitude, lon = l.longitude))
                raw = "%.5f, %.5f".format(java.util.Locale.ROOT, l.latitude, l.longitude)
            }
        }
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r -> if (r.values.any { it }) here() }
    Gap(10.dp)
    Field(lock.place, { set(lock.copy(place = it)) }, Modifier.fillMaxWidth(), stringResource(R.string.f_place))
    Gap(8.dp)
    Field(raw, {
        raw = it
        Geo.parse(it)?.let { (la, lo) -> set(lock.copy(lat = la, lon = lo)) }
    }, Modifier.fillMaxWidth(), stringResource(R.string.f_coords), caps = false)
    Gap(4.dp)
    Text(
        if (lock.lat != 0.0 || lock.lon != 0.0) "✓ " + "%.5f, %.5f".format(java.util.Locale.ROOT, lock.lat, lock.lon)
        else stringResource(R.string.coords_help),
        style = T.Small.copy(color = if (lock.lat != 0.0 || lock.lon != 0.0) C.green else C.muted),
    )
    Gap(8.dp)
    Btn(stringResource(if (finding) R.string.where_finding else R.string.where_here), {
        if (Where.allowed(ctx)) here() else ask.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }, Modifier.fillMaxWidth(), Kind.GHOST, enabled = !finding, icon = Icons.Outlined.MyLocation)
    Gap(10.dp)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (m in listOf(50, 100, 300)) Chip(stringResource(R.string.radius_m, m), lock.radius == m, { set(lock.copy(radius = m)) })
    }
}
