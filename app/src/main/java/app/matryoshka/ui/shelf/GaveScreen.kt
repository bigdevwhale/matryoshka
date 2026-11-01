package app.matryoshka.ui.shelf

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Shelf
import app.matryoshka.ui.Nav
import app.matryoshka.ui.art.Doll
import app.matryoshka.ui.components.Btn
import app.matryoshka.ui.components.Caps
import app.matryoshka.ui.components.Card
import app.matryoshka.ui.components.Dates
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.Kind
import app.matryoshka.ui.components.TopBar
import app.matryoshka.ui.components.Voice
import app.matryoshka.ui.make.lockName
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.launch

/** A matryoshka I gave: its figures, who painted them, and what came back. */
@Composable
fun GaveScreen(id: String, shelf: Shelf, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    val g = shelf.given.firstOrNull { it.id == id }
    var ask by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        TopBar(g?.title?.ifBlank { g.to } ?: stringResource(R.string.app_name), g?.let { stringResource(R.string.to_x, it.to) }, onBack = { nav.close() })
        if (g == null) {
            Text(stringResource(R.string.gave_unknown), style = T.Body, modifier = Modifier.padding(18.dp))
            return
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                val n = g.paints.size
                g.paints.forEachIndexed { i, p ->
                    val w = (78 - i * (36f / maxOf(1, n - 1))).dp
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Doll(p, Modifier.width(w), solid = i == n - 1)
                        Text(if (i == n - 1) "★" else lockName(g.locks.getOrNull(i)), style = T.Small.copy(color = if (i == g.mine) C.accent else C.muted), maxLines = 1)
                    }
                }
            }
            if (g.authors.isNotEmpty()) {
                Caps(stringResource(R.string.painted_by))
                Gap(4.dp)
                Text(g.authors.joinToString(", "), style = T.Body)
                Gap(6.dp)
            }
            Text(
                if (g.sealer) stringResource(R.string.gave_sealed, Dates.day(g.at, ctx)) else stringResource(R.string.gave_passed, g.mine + 1),
                style = T.Small,
            )
            Gap(18.dp)
            Text(stringResource(R.string.replies), style = T.Sign)
            Gap(8.dp)
            if (g.replies.isEmpty()) Text(stringResource(R.string.replies_none), style = T.Small)
            for (r in g.replies) {
                Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Text(stringResource(R.string.reply_from, r.from.ifBlank { g.to }, Dates.day(r.at, ctx)), style = T.Strong)
                    if (r.notes.isEmpty()) Text(stringResource(R.string.reply_empty), style = T.Small)
                    for (n in r.notes) {
                        Gap(10.dp)
                        Caps(stringResource(R.string.figure_n, n.figure + 1))
                        if (n.text.isNotBlank()) Text(n.text, style = T.Italic)
                        n.voice?.let { Gap(6.dp); Voice(it, n.voiceMs, C.ink, C.accent) }
                    }
                }
            }
            Gap(16.dp)
        }
        Column(Modifier.navigationBarsPadding().padding(18.dp)) {
            if (g.sealer) {
                Btn(stringResource(R.string.send_again), {
                    scope.launch { c.resend(g.id, g.title.ifBlank { g.to })?.let { ctx.startActivity(c.shareIntent(it).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
                }, Modifier.fillMaxWidth())
                Gap(8.dp)
            }
            Btn(stringResource(R.string.forget), { ask = true }, Modifier.fillMaxWidth(), Kind.GHOST)
        }
    }
    if (ask && g != null) AlertDialog(
        onDismissRequest = { ask = false },
        title = { Text(stringResource(R.string.forget_q), style = T.Heading) },
        text = { Text(stringResource(R.string.forget_given_b), style = T.Body) },
        confirmButton = { TextButton({ ask = false; scope.launch { c.forgetGiven(g.id); nav.close() } }) { Text(stringResource(R.string.forget), color = C.red) } },
        dismissButton = { TextButton({ ask = false }) { Text(stringResource(R.string.cancel), color = C.ink2) } },
        containerColor = C.surface,
    )
}
