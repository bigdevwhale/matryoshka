package app.matryoshka.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.matryoshka.Incoming
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Settings
import app.matryoshka.data.Shelf
import app.matryoshka.ui.components.Toast
import app.matryoshka.ui.components.ToastHost
import app.matryoshka.ui.components.room
import app.matryoshka.ui.make.EditScreen
import app.matryoshka.ui.make.NewDoll
import app.matryoshka.ui.make.WorkshopScreen
import app.matryoshka.ui.onboarding.Onboarding
import app.matryoshka.ui.open.OpenScreen
import app.matryoshka.ui.settings.SettingsPage
import app.matryoshka.ui.shelf.GaveScreen
import app.matryoshka.ui.shelf.ShelfScreen
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T

enum class Tab { SHELF, WORKSHOP }

sealed interface Overlay {
    /** A matryoshka given to me, opened figure by figure. */
    data class Open(val id: String) : Overlay
    /** A draft (or my turn in a shared one) in the workshop. */
    data class Edit(val id: String) : Overlay
    /** One I gave: its figures and the replies. */
    data class Gave(val id: String) : Overlay
    data object New : Overlay
    data object Settings : Overlay
}

class Nav(tab: Tab, first: Overlay?) {
    var tab by mutableStateOf(tab)
    val stack = mutableStateListOf<Overlay>().apply { if (first != null) add(first) }
    fun open(o: Overlay) { stack.add(o) }
    fun close() { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }
    fun replace(o: Overlay) { close(); open(o) }
}

@Composable
fun Root(s: Settings, tab: Tab = Tab.SHELF, overlay: Overlay? = null) {
    val ctx = LocalContext.current
    val c = ctx.container
    if (!s.onboarded) { Onboarding(s); return }
    val shelf by c.shelf.collectAsState()
    val incoming by c.incoming.collectAsState()
    val nav = remember { Nav(tab, overlay) }

    LaunchedEffect(incoming) {
        when (val i = incoming) {
            is Incoming.Doll -> nav.open(Overlay.Open(i.id))
            is Incoming.Turn -> { nav.tab = Tab.WORKSHOP; nav.open(Overlay.Edit(i.id)) }
            is Incoming.Back -> { nav.open(Overlay.Gave(i.id)); Toast.show(ctx.getString(R.string.reply_came, i.from.ifBlank { "—" })) }
            Incoming.Bad -> Toast.show(ctx.getString(R.string.bad_file))
            null -> {}
        }
        if (incoming != null) c.incoming.value = null
    }

    Box(Modifier.fillMaxSize().background(C.bg).room()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (nav.tab) {
                    Tab.SHELF -> ShelfScreen(shelf, s, nav)
                    Tab.WORKSHOP -> WorkshopScreen(shelf, s, nav)
                }
            }
            Tabs(nav)
        }
        BackHandler(nav.stack.isEmpty() && nav.tab != Tab.SHELF) { nav.tab = Tab.SHELF }
        for (o in nav.stack.toList()) {
            // An overlay takes every touch: nothing reaches the screen underneath.
            Box(Modifier.fillMaxSize().background(C.bg).room().clickable(remember { MutableInteractionSource() }, null) {}) {
                Layer(o, shelf, s, nav)
            }
        }
        ToastHost(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 90.dp))
    }
}

@Composable
private fun Layer(o: Overlay, shelf: Shelf, s: Settings, nav: Nav) {
    BackHandler { nav.close() }
    when (o) {
        is Overlay.Open -> OpenScreen(o.id, s, nav)
        is Overlay.Edit -> EditScreen(o.id, shelf, s, nav)
        is Overlay.Gave -> GaveScreen(o.id, shelf, nav)
        Overlay.New -> NewDoll(s, nav)
        Overlay.Settings -> SettingsPage(s, nav)
    }
}

@Composable
private fun Tabs(nav: Nav) {
    Row(
        Modifier.fillMaxWidth().background(C.bg2).navigationBarsPadding().height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
    ) {
        TabItem(Icons.Outlined.ViewWeek, stringResource(R.string.tab_shelf), nav.tab == Tab.SHELF) { nav.tab = Tab.SHELF }
        TabItem(Icons.Outlined.Handyman, stringResource(R.string.tab_workshop), nav.tab == Tab.WORKSHOP) { nav.tab = Tab.WORKSHOP }
    }
}

@Composable
private fun TabItem(icon: ImageVector, label: String, on: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 28.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (on) C.accent else C.muted, modifier = Modifier.size(24.dp))
        Text(label, style = T.Small.copy(color = if (on) C.accent else C.muted))
    }
}
