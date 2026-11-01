package app.matryoshka.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.matryoshka.BuildConfig
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Settings
import app.matryoshka.ui.Nav
import app.matryoshka.ui.components.Caps
import app.matryoshka.ui.components.Card
import app.matryoshka.ui.components.Chip
import app.matryoshka.ui.components.CommunityLinks
import app.matryoshka.ui.components.Field
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.Ornament
import app.matryoshka.ui.components.TopBar
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.delay

@Composable
fun SettingsPage(s: Settings, nav: Nav) {
    val c = LocalContext.current.container
    var name by remember { mutableStateOf(s.name) }
    LaunchedEffect(name) { delay(400); if (name.trim() != s.name) c.settings.update { it.copy(name = name.trim()) } }
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.settings), onBack = { nav.close() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 18.dp)) {
            Gap(8.dp)
            Caps(stringResource(R.string.f_me))
            Gap(6.dp)
            Field(name, { name = it }, Modifier.fillMaxWidth(), stringResource(R.string.f_me_hint))
            Gap(4.dp)
            Text(stringResource(R.string.name_note), style = T.Small)
            Gap(20.dp)
            Caps(stringResource(R.string.theme))
            Gap(8.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(R.string.theme_system, R.string.theme_light, R.string.theme_dark).forEachIndexed { i, r ->
                    Chip(stringResource(r), s.theme == i, { c.write { settings.update { it.copy(theme = i) } } })
                }
            }
            Gap(20.dp)
            Caps(stringResource(R.string.language))
            Gap(8.dp)
            val cur = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((tag, label) in listOf("" to stringResource(R.string.lang_system), "ru" to stringResource(R.string.lang_ru), "en" to stringResource(R.string.lang_en))) {
                    Chip(label, cur == tag || (tag.isNotEmpty() && cur.startsWith(tag)), {
                        AppCompatDelegate.setApplicationLocales(if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
                    })
                }
            }
            Gap(22.dp)
            Card(Modifier.fillMaxWidth(), color = C.surface2) {
                Caps(stringResource(R.string.about_t))
                Gap(6.dp)
                Text(stringResource(R.string.about_b), style = T.Small.copy(color = C.ink2))
            }
            Gap(22.dp)
            Caps(stringResource(R.string.community))
            Gap(8.dp)
            CommunityLinks()
            Gap(18.dp)
            Ornament()
            Gap(8.dp)
            Text(stringResource(R.string.version, BuildConfig.VERSION_NAME), style = T.Small)
            Gap(24.dp)
        }
    }
}
