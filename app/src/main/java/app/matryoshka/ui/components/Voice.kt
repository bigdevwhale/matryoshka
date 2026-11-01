package app.matryoshka.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.matryoshka.data.Player
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** A voice note: a play button, a wave that lights up as it plays, the length. */
@Composable
fun Voice(bytes: ByteArray, ms: Long, ink: Color, accent: Color, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val player = remember { Player(ctx) }
    var playing by remember { mutableStateOf(false) }
    var pos by remember { mutableFloatStateOf(0f) }
    DisposableEffect(Unit) { onDispose { player.stop() } }
    LaunchedEffect(playing) {
        while (playing) { pos = player.position; delay(80) }
    }
    val seed = remember(bytes) { bytes.size % 97 }
    Row(modifier.fillMaxWidth().clip(Round).background(ink.copy(alpha = .07f)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(ink).clickable {
                if (playing) { player.stop(); playing = false; pos = 0f }
                else playing = player.play(bytes) { playing = false; pos = 0f }
            },
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Canvas(Modifier.weight(1f).height(34.dp).padding(horizontal = 10.dp)) {
            val n = 38
            val w = size.width / n
            for (i in 0 until n) {
                val h = (.2f + abs(sin(i * 1.7f + seed) * .6f + cos(i * .6f) * .2f)).coerceIn(.15f, 1f) * size.height
                val lit = i.toFloat() / n < pos
                drawRoundRect(accent.copy(alpha = if (lit) 1f else .4f), Offset(i * w + w * .15f, (size.height - h) / 2), Size(w * .7f, h), CornerRadius(w))
            }
        }
        Text(Dates.duration(ms), style = T.Small.copy(color = ink.copy(alpha = .7f)))
    }
}
