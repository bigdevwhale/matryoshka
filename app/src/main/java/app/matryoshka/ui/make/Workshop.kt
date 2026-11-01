package app.matryoshka.ui.make

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Settings
import app.matryoshka.data.Shelf
import app.matryoshka.domain.Layer
import app.matryoshka.domain.Matr
import app.matryoshka.domain.Paint
import app.matryoshka.domain.Relay
import app.matryoshka.domain.Content
import app.matryoshka.ui.Nav
import app.matryoshka.ui.Overlay
import app.matryoshka.ui.art.Doll
import app.matryoshka.ui.components.Btn
import app.matryoshka.ui.components.Caps
import app.matryoshka.ui.components.Card
import app.matryoshka.ui.components.Chip
import app.matryoshka.ui.components.Field
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.IconBtn
import app.matryoshka.ui.components.Kind
import app.matryoshka.ui.components.Ornament
import app.matryoshka.ui.components.TopBar
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.launch

val Relay.shared get() = people.size > 1

/** The paintings in the order new figures get them. */
val Order = listOf(Paint.KHOKHLOMA, Paint.GZHEL, Paint.POLKHOV, Paint.SEMYONOV, Paint.VYATKA)

@Composable
fun WorkshopScreen(shelf: Shelf, s: Settings, nav: Nav) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = 18.dp)) {
        Gap(14.dp)
        Text(stringResource(R.string.tab_workshop), style = T.Hero)
        Text(stringResource(R.string.workshop_sub), style = T.Body.copy(color = C.ink2))
        Gap(16.dp)
        Btn(stringResource(R.string.new_doll), { nav.open(Overlay.New) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
        Gap(18.dp)
        if (shelf.drafts.isEmpty()) Text(stringResource(R.string.drafts_empty), style = T.Small)
        for (r in shelf.drafts) {
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp), onClick = { nav.open(Overlay.Edit(r.id)) }) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val paints = r.layers.map { it.paint } + r.giftPaint
                    paints.take(7).forEachIndexed { i, p ->
                        val hidden = r.shared && i != r.turn && i < r.layers.size && i < r.turn
                        Doll(p, Modifier.width((40 - i * 3).dp), solid = i == paints.size - 1, silhouette = if (hidden) C.silhouette else null)
                    }
                }
                Gap(10.dp)
                Text(r.title.ifBlank { stringResource(R.string.to_x, r.to) }, style = T.Heading)
                Text(
                    if (r.shared) stringResource(R.string.draft_turn, r.turn + 1, r.people.size)
                    else pluralStringResource(R.plurals.figures, r.layers.size + 1, r.layers.size + 1),
                    style = T.Small.copy(color = if (r.shared) C.green else C.muted),
                )
            }
        }
        Gap(14.dp)
        Card(Modifier.fillMaxWidth(), color = C.surface2) {
            Caps(stringResource(R.string.how_t))
            Gap(6.dp)
            Text(stringResource(R.string.how_b), style = T.Small.copy(color = C.ink2))
        }
        Gap(18.dp)
        Ornament()
        Gap(18.dp)
    }
}

/** Starting a matryoshka: for whom, on what occasion, alone or passed round. */
@Composable
fun NewDoll(s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    var to by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var me by remember { mutableStateOf(s.name) }
    var together by remember { mutableStateOf(false) }
    val people = remember { mutableStateListOf("", "") }
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.new_doll), onBack = { nav.close() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
            Gap(6.dp)
            Caps(stringResource(R.string.f_to))
            Gap(6.dp)
            Field(to, { to = it }, Modifier.fillMaxWidth(), stringResource(R.string.f_to_hint))
            Gap(14.dp)
            Caps(stringResource(R.string.f_title))
            Gap(6.dp)
            Field(title, { title = it }, Modifier.fillMaxWidth(), stringResource(R.string.f_title_hint))
            Gap(14.dp)
            Caps(stringResource(R.string.f_me))
            Gap(6.dp)
            Field(me, { me = it }, Modifier.fillMaxWidth(), stringResource(R.string.f_me_hint))
            Gap(18.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(stringResource(R.string.mode_alone), !together, { together = false })
                Chip(stringResource(R.string.mode_together), together, { together = true })
            }
            Gap(10.dp)
            Text(stringResource(if (together) R.string.mode_together_b else R.string.mode_alone_b), style = T.Small.copy(color = C.ink2))
            if (together) {
                Gap(14.dp)
                Caps(stringResource(R.string.f_people))
                Gap(6.dp)
                Text("1. " + me.ifBlank { stringResource(R.string.you) }, style = T.Strong)
                people.forEachIndexed { i, p ->
                    Gap(8.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 2}.", style = T.Strong, modifier = Modifier.width(26.dp))
                        Field(p, { people[i] = it }, Modifier.weight(1f), stringResource(R.string.f_person_hint))
                        if (people.size > 1) {
                            Gap(0.dp)
                            IconBtn(Icons.Rounded.Close, { people.removeAt(i) }, Modifier.padding(start = 8.dp))
                        }
                    }
                }
                if (people.size < 7) {
                    Gap(8.dp)
                    Btn(stringResource(R.string.add_person), { people.add("") }, Modifier.fillMaxWidth(), Kind.GHOST, icon = Icons.Rounded.Add)
                }
            }
            Gap(20.dp)
        }
        val others = people.map { it.trim() }.filter { it.isNotEmpty() }
        val ok = to.isNotBlank() && me.isNotBlank() && (!together || others.isNotEmpty())
        Column(Modifier.navigationBarsPadding().padding(18.dp)) {
            Btn(stringResource(R.string.start), {
                scope.launch {
                    if (s.name.isBlank()) c.settings.update { it.copy(name = me.trim()) }
                    val id = Matr.newId()
                    val n = if (together) 1 else 3
                    val r = Relay(
                        id = id, title = title.trim(), to = to.trim(),
                        people = if (together) listOf(me.trim()) + others else listOf(me.trim()),
                        turn = 0,
                        layers = List(n) { Layer(Order[it % Order.size], me.trim()) },
                        giftPaint = Order[n % Order.size], gift = Content(), created = System.currentTimeMillis(),
                    )
                    c.saveDraft(r)
                    nav.replace(Overlay.Edit(id))
                }
            }, Modifier.fillMaxWidth(), enabled = ok)
        }
    }
}
