package app.matryoshka.ui.make

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Settings
import app.matryoshka.data.Shelf
import app.matryoshka.domain.Layer
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Relay
import app.matryoshka.ui.Nav
import app.matryoshka.ui.art.Doll
import app.matryoshka.ui.components.Btn
import app.matryoshka.ui.components.Card
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.IconBtn
import app.matryoshka.ui.components.Kind
import app.matryoshka.ui.components.Scrim
import app.matryoshka.ui.components.SheetBox
import app.matryoshka.ui.components.Toast
import app.matryoshka.ui.components.TopBar
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_LAYERS = 8

@Composable
fun EditScreen(id: String, shelf: Shelf, s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    val r0 = shelf.drafts.firstOrNull { it.id == id }
    if (r0 == null) {
        Column(Modifier.fillMaxSize()) {
            TopBar(stringResource(R.string.tab_workshop), onBack = { nav.close() })
            Text(stringResource(R.string.draft_gone), style = T.Body, modifier = Modifier.padding(18.dp))
        }
        return
    }
    var r by remember(id) {
        // My figure in a shared matryoshka is added the moment I open it.
        val start = if (r0.shared && r0.layers.size <= r0.turn)
            r0.copy(layers = r0.layers + Layer(Order[r0.turn % Order.size], s.name.ifBlank { r0.people.getOrElse(r0.turn) { "" } }))
        else r0
        mutableStateOf(start)
    }
    LaunchedEffect(r) {
        delay(500)
        c.saveDraft(r)
    }
    val organizer = !r.shared || r.turn == 0
    val gift = r.layers.size
    var sel by remember(id) { mutableIntStateOf(if (r.shared) r.turn else 0) }
    var sealing by remember { mutableStateOf(false) }
    var drop by remember { mutableStateOf(false) }
    fun mine(i: Int) = !r.shared || i == r.turn
    fun edit(i: Int, l: Layer) { r = r.copy(layers = r.layers.toMutableList().also { it[i] = l }) }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            r.title.ifBlank { stringResource(R.string.to_x, r.to) },
            if (r.shared) stringResource(R.string.draft_turn, r.turn + 1, r.people.size) else stringResource(R.string.to_x, r.to),
            onBack = { nav.close() },
        ) { IconBtn(Icons.Outlined.DeleteOutline, { drop = true }, desc = stringResource(R.string.delete)) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom,
            ) {
                val n = r.layers.size + 1
                for (i in 0 until n) {
                    val isGift = i == gift
                    val paint = if (isGift) r.giftPaint else r.layers[i].paint
                    val hidden = r.shared && ((isGift && !organizer) || (!isGift && !mine(i)))
                    val w = (70 - i * (32f / maxOf(1, n - 1))).dp
                    val lift by animateFloatAsState(if (sel == i) -6f else 0f, label = "lift")
                    Box(
                        Modifier.clip(RoundedCornerShape(12.dp)).background(if (sel == i) C.accentSoft else C.bg.copy(alpha = 0f))
                            .clickable { sel = i }.padding(start = 3.dp, end = 3.dp, top = 4.dp, bottom = 10.dp),
                    ) {
                        Doll(paint, Modifier.width(w).offset(y = lift.dp), solid = isGift, silhouette = if (hidden) C.silhouette else null)
                        val k = if (isGift) null else r.layers[i].lock.kind
                        if (k != null && k != LockKind.NONE && !hidden) {
                            Box(Modifier.align(Alignment.TopEnd).size(20.dp).clip(CircleShape).background(C.bg).border(1.dp, C.gold, CircleShape), contentAlignment = Alignment.Center) {
                                Text(lockGlyph(k), style = T.Small.copy(color = C.gold))
                            }
                        }
                    }
                }
                if (!r.shared && r.layers.size < MAX_LAYERS) {
                    Box(
                        Modifier.padding(start = 6.dp, bottom = 10.dp).size(44.dp, 64.dp).clip(RoundedCornerShape(12.dp))
                            .border(1.dp, C.gold, RoundedCornerShape(12.dp)).clickable {
                                val l = Layer(Order[r.layers.size % Order.size], s.name.ifBlank { r.people.firstOrNull().orEmpty() })
                                r = r.copy(layers = r.layers + l)
                                sel = r.layers.size - 1
                            },
                        contentAlignment = Alignment.Center,
                    ) { Text("+", style = T.Title.copy(color = C.gold)) }
                }
            }
            if (r.shared) {
                val after = r.people.drop(r.turn + 1)
                Text(
                    if (after.isEmpty()) stringResource(R.string.relay_you_last) else stringResource(R.string.relay_after, after.joinToString(", ")),
                    style = T.Small,
                )
                Gap(8.dp)
            }
            when {
                sel == gift && organizer -> GiftEditor(r, { r = it })
                sel == gift -> Hidden(stringResource(R.string.gift_hidden, r.people.firstOrNull().orEmpty()))
                mine(sel) -> LayerEditor(
                    sel, r.layers[sel], { edit(sel, it) },
                    canRemove = !r.shared && r.layers.size > 1,
                    onRemove = { r = r.copy(layers = r.layers.toMutableList().also { it.removeAt(sel) }); sel = (sel - 1).coerceAtLeast(0) },
                )
                else -> Hidden(stringResource(R.string.figure_hidden, r.layers[sel].author.ifBlank { "—" }))
            }
            Gap(20.dp)
        }
        val problems = problems(r)
        Column(Modifier.navigationBarsPadding().padding(start = 18.dp, end = 18.dp, bottom = 14.dp, top = 6.dp)) {
            problems.firstOrNull()?.let {
                Text(it, style = T.Small.copy(color = C.red), modifier = Modifier.padding(bottom = 8.dp))
            }
            val passing = r.shared && !r.last
            Btn(
                if (passing) stringResource(R.string.pass_to, r.next.orEmpty()) else stringResource(R.string.seal),
                { sealing = true }, Modifier.fillMaxWidth(), enabled = problems.isEmpty(),
            )
        }
    }
    if (sealing) SealSheet(r, onDismiss = { sealing = false }) {
        sealing = false
        scope.launch {
            val f = if (r.shared && !r.last) c.pass(r) else c.seal(r)
            ctx.startActivity(c.shareIntent(f).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            Toast.show(ctx.getString(if (r.shared && !r.last) R.string.passed_toast else R.string.sealed_toast))
            nav.close()
        }
    }
    if (drop) AlertDialog(
        onDismissRequest = { drop = false },
        title = { Text(stringResource(R.string.delete_draft_q), style = T.Heading) },
        text = { Text(stringResource(R.string.delete_draft_b), style = T.Body) },
        confirmButton = { TextButton({ drop = false; scope.launch { c.deleteDraft(r.id); nav.close() } }) { Text(stringResource(R.string.delete), color = C.red) } },
        dismissButton = { TextButton({ drop = false }) { Text(stringResource(R.string.cancel), color = C.ink2) } },
        containerColor = C.surface,
    )
}

@Composable
private fun Hidden(text: String) {
    Card(Modifier.fillMaxWidth(), color = C.surface2) {
        Text(text, style = T.Body.copy(color = C.ink2))
    }
}

/** What stops the matryoshka from being sealed (or passed on) yet, in the order to fix it. */
@Composable
private fun problems(r: Relay): List<String> {
    val out = ArrayList<String>()
    val mineIdx = if (r.shared) listOf(r.turn) else r.layers.indices.toList()
    for (i in mineIdx) {
        val l = r.layers.getOrNull(i) ?: continue
        val n = i + 1
        when (l.lock.kind) {
            LockKind.RIDDLE -> {
                if (l.lock.question.isBlank()) out += stringResource(R.string.p_question, n)
                if (l.answer.isBlank()) out += stringResource(R.string.p_answer, n)
            }
            LockKind.WORD -> {
                if (l.lock.who.isBlank()) out += stringResource(R.string.p_who, n)
                if (l.answer.isBlank()) out += stringResource(R.string.p_word, n)
            }
            LockKind.TIME -> if (l.lock.at <= 0) out += stringResource(R.string.p_time, n)
            LockKind.PLACE -> if (l.lock.lat == 0.0 && l.lock.lon == 0.0) out += stringResource(R.string.p_place, n)
            else -> {}
        }
    }
    val organizer = !r.shared || r.turn == 0
    if (organizer && r.gift.empty) out += stringResource(R.string.p_gift)
    return out
}

@Composable
private fun SealSheet(r: Relay, onDismiss: () -> Unit, onGo: () -> Unit) {
    Scrim(onDismiss) {
        SheetBox {
            val passing = r.shared && !r.last
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val paint = r.layers.firstOrNull()?.paint ?: r.giftPaint
                Box(contentAlignment = Alignment.BottomCenter) {
                    Doll(paint, Modifier.width(96.dp))
                    if (!passing) Box(
                        Modifier.padding(bottom = 26.dp).size(56.dp).clip(CircleShape).background(C.red).border(2.dp, C.gold, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text(stringResource(R.string.seal_letter), style = T.Title.copy(color = C.gold)) }
                }
            }
            Gap(10.dp)
            Text(stringResource(if (passing) R.string.pass_t else R.string.seal_t), style = T.Title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Gap(6.dp)
            val n = r.layers.size + 1
            val locks = r.layers.count { it.lock.kind != LockKind.NONE }
            val secret = r.layers.count { it.lock.kind.secret }
            Text(
                pluralStringResource(R.plurals.figures, n, n) + " · " + pluralStringResource(R.plurals.locks, locks, locks),
                style = T.Body.copy(color = C.ink2), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            Gap(12.dp)
            Text(
                if (passing) stringResource(R.string.pass_b, r.next.orEmpty())
                else stringResource(R.string.seal_b, secret, r.to),
                style = T.Small.copy(color = C.ink2),
            )
            Gap(16.dp)
            Btn(stringResource(if (passing) R.string.pass_go else R.string.seal_go), onGo, Modifier.fillMaxWidth())
            Gap(8.dp)
            Btn(stringResource(R.string.not_yet), onDismiss, Modifier.fillMaxWidth(), Kind.GHOST)
        }
    }
}

@Composable
fun AddBtn(text: String, onClick: () -> Unit) = Btn(text, onClick, Modifier.fillMaxWidth(), Kind.GHOST, icon = Icons.Rounded.Add)
