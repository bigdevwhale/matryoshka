package app.matryoshka.ui.shelf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Given
import app.matryoshka.data.Got
import app.matryoshka.data.Settings
import app.matryoshka.data.Shelf
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Paint
import app.matryoshka.ui.Nav
import app.matryoshka.ui.Overlay
import app.matryoshka.ui.art.Doll
import app.matryoshka.ui.components.Btn
import app.matryoshka.ui.components.Card
import app.matryoshka.ui.components.Dates
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.IconBtn
import app.matryoshka.ui.components.Kind
import app.matryoshka.ui.components.Ornament
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.launch

@Composable
fun ShelfScreen(shelf: Shelf, s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch { c.incoming.value = c.import(uri) }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = 18.dp)) {
        Gap(14.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.app_name), style = T.Hero, modifier = Modifier.weight(1f))
            IconBtn(Icons.Outlined.Settings, { nav.open(Overlay.Settings) }, desc = stringResource(R.string.settings))
        }
        Text(stringResource(R.string.tagline), style = T.Body.copy(color = C.ink2))
        Gap(16.dp)
        Card(onClick = { nav.open(Overlay.New) }, color = C.surface) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Doll(Paint.SEMYONOV, Modifier.width(62.dp))
                Column(Modifier.padding(start = 14.dp).weight(1f)) {
                    Text(stringResource(R.string.make), style = T.Sign)
                    Gap(4.dp)
                    Text(stringResource(R.string.make_sub), style = T.Small.copy(color = C.ink2))
                }
            }
        }
        Gap(8.dp)
        Section(stringResource(R.string.shelf_got))
        if (shelf.got.isEmpty()) Empty(stringResource(R.string.shelf_got_empty))
        else ShelfRow(shelf.got.map { g -> Item(g.meta.figures.first().paint, gotTitle(g), gotSub(g), gotState(g), !g.done) { nav.open(Overlay.Open(g.meta.id)) } })
        Section(stringResource(R.string.shelf_given))
        if (shelf.given.isEmpty()) Empty(stringResource(R.string.shelf_given_empty))
        else ShelfRow(shelf.given.map { g -> Item(g.paints.firstOrNull() ?: Paint.KHOKHLOMA, g.title.ifBlank { g.to }, givenSub(g), givenState(g), g.replies.isNotEmpty()) { nav.open(Overlay.Gave(g.id)) } })
        Gap(22.dp)
        Btn(stringResource(R.string.open_file), { pick.launch(arrayOf("*/*")) }, Modifier.fillMaxWidth(), Kind.GHOST, icon = Icons.Outlined.FileOpen)
        Gap(6.dp)
        Text(stringResource(R.string.open_file_sub), style = T.Small, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Gap(18.dp)
        Ornament()
        Gap(18.dp)
    }
}

@Composable
private fun Section(text: String) {
    Gap(20.dp)
    Text(text, style = T.Sign)
    Gap(6.dp)
}

@Composable
private fun Empty(text: String) {
    Text(text, style = T.Small, modifier = Modifier.padding(vertical = 6.dp))
}

private class Item(val paint: Paint, val title: String, val sub: String, val state: String, val lit: Boolean, val onClick: () -> Unit)

@Composable
private fun ShelfRow(items: List<Item>) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().offset(y = 116.dp).height(14.dp).shadow(8.dp, RoundedCornerShape(3.dp))
                .clip(RoundedCornerShape(3.dp)).background(Brush.verticalGradient(listOf(C.shelf, C.shelfDark)))
        )
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (it in items) {
                Column(Modifier.width(104.dp).clickable(onClick = it.onClick), horizontalAlignment = Alignment.CenterHorizontally) {
                    Gap(8.dp)
                    Doll(it.paint, Modifier.height(108.dp))
                    Gap(22.dp)
                    Text(it.title, style = T.Label.copy(color = C.ink), maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                    Text(it.sub, style = T.Small, maxLines = 2, textAlign = TextAlign.Center)
                    Text(it.state, style = T.Small.copy(color = if (it.lit) C.green else C.gold), maxLines = 2, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun gotTitle(g: Got) = g.meta.title.ifBlank { stringResource(R.string.for_you) }

@Composable
private fun gotSub(g: Got) = g.meta.authors.joinToString(", ").let { if (it.isBlank()) "" else stringResource(R.string.from_x, it) }

@Composable
fun gotState(g: Got): String {
    if (g.done) return stringResource(R.string.state_done)
    val next = g.meta.figures[g.opened]
    if (next.lock.kind == LockKind.TIME && next.lock.at > System.currentTimeMillis()) return stringResource(R.string.state_wait, Dates.day(next.lock.at, LocalContext.current))
    return stringResource(R.string.state_opened, g.opened, g.sealed.count)
}

@Composable
private fun givenSub(g: Given) = pluralStringResource(R.plurals.figures, g.paints.size, g.paints.size)

@Composable
private fun givenState(g: Given): String = when {
    g.replies.isNotEmpty() -> pluralStringResource(R.plurals.replies, g.replies.size, g.replies.size)
    g.sealer -> stringResource(R.string.state_sent)
    else -> stringResource(R.string.state_passed)
}
