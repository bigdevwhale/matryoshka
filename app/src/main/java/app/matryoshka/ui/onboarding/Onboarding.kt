package app.matryoshka.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.matryoshka.R
import app.matryoshka.container
import app.matryoshka.data.Settings
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Paint
import app.matryoshka.ui.art.Doll
import app.matryoshka.ui.components.Btn
import app.matryoshka.ui.components.Field
import app.matryoshka.ui.components.Gap
import app.matryoshka.ui.components.Kind
import app.matryoshka.ui.components.Ornament
import app.matryoshka.ui.components.room
import app.matryoshka.ui.make.lockGlyph
import app.matryoshka.ui.make.lockName
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T

/** Four pages: what it is, the locks, how it travels, and how to sign the figures. */
@Composable
fun Onboarding(s: Settings, start: Int = 0) {
    val c = LocalContext.current.container
    var page by remember { mutableIntStateOf(start) }
    var name by remember { mutableStateOf(s.name) }
    val last = 3
    Column(Modifier.fillMaxSize().background(C.bg).room().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.Center) {
            for (i in 0..last) Box(Modifier.padding(3.dp).size(if (i == page) 22.dp else 7.dp, 7.dp).clip(CircleShape).background(if (i == page) C.gold else C.gold.copy(alpha = .25f)))
        }
        AnimatedContent(page, Modifier.weight(1f), transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "page") { p ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
                when (p) {
                    0 -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                            listOf(Paint.KHOKHLOMA, Paint.GZHEL, Paint.POLKHOV, Paint.SEMYONOV, Paint.VYATKA).forEachIndexed { i, paint ->
                                Doll(paint, Modifier.width((92 - i * 15).dp).padding(horizontal = 2.dp), solid = i == 4)
                            }
                        }
                        Gap(28.dp)
                        Text(stringResource(R.string.app_name), style = T.Hero)
                        Gap(10.dp)
                        Text(stringResource(R.string.ob1), style = T.Body.copy(color = C.ink2))
                    }
                    1 -> {
                        Text(stringResource(R.string.ob2_t), style = T.Title)
                        Gap(12.dp)
                        Text(stringResource(R.string.ob2), style = T.Body.copy(color = C.ink2))
                        Gap(16.dp)
                        for ((k, d) in listOf(
                            LockKind.TIME to R.string.ob_time, LockKind.RIDDLE to R.string.ob_riddle, LockKind.PLACE to R.string.ob_place,
                            LockKind.WORD to R.string.ob_word, LockKind.GESTURE to R.string.ob_gesture,
                        )) {
                            Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                                Box(Modifier.size(34.dp).clip(CircleShape).background(C.accentSoft), contentAlignment = Alignment.Center) {
                                    Text(lockGlyph(k), style = T.Heading.copy(color = C.gold))
                                }
                                Column(Modifier.padding(start = 12.dp)) {
                                    Text(lockName(k), style = T.Strong)
                                    Text(stringResource(d), style = T.Small)
                                }
                            }
                        }
                    }
                    2 -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                            Doll(Paint.GZHEL, Modifier.width(70.dp), silhouette = C.silhouette)
                            Doll(Paint.POLKHOV, Modifier.width(70.dp), silhouette = C.silhouette)
                            Doll(Paint.SEMYONOV, Modifier.width(70.dp))
                            Doll(Paint.VYATKA, Modifier.width(70.dp).padding(start = 6.dp), silhouette = C.silhouette.copy(alpha = .15f))
                        }
                        Gap(24.dp)
                        Text(stringResource(R.string.ob3_t), style = T.Title)
                        Gap(12.dp)
                        Text(stringResource(R.string.ob3), style = T.Body.copy(color = C.ink2))
                    }
                    else -> {
                        Doll(Paint.SEMYONOV, Modifier.width(96.dp).align(Alignment.CenterHorizontally))
                        Gap(20.dp)
                        Text(stringResource(R.string.ob4_t), style = T.Title)
                        Gap(10.dp)
                        Text(stringResource(R.string.ob4), style = T.Body.copy(color = C.ink2))
                        Gap(16.dp)
                        Field(name, { name = it }, Modifier.fillMaxWidth(), stringResource(R.string.f_me_hint))
                    }
                }
            }
        }
        Ornament()
        Gap(14.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (page > 0) Btn(stringResource(R.string.back), { page-- }, Modifier.weight(1f), Kind.GHOST)
            Btn(
                stringResource(if (page == last) R.string.ob_go else R.string.next),
                {
                    if (page < last) page++
                    else c.write { settings.update { it.copy(onboarded = true, name = name.trim()) } }
                },
                Modifier.weight(if (page > 0) 1.6f else 1f),
            )
        }
        Gap(14.dp)
    }
}
