package app.matryoshka.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.matryoshka.R

/** Light: limewood before it's painted, by a window. Dark: black lacquer, gold and cinnabar, as on a Fedoskino box. */
@Immutable
data class Palette(
    val dark: Boolean,
    val bg: Color,
    val bg2: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val ink: Color,
    val ink2: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val accentSoft: Color,
    val gold: Color,
    val red: Color,
    val green: Color,
    val shelf: Color,
    val shelfDark: Color,
    val silhouette: Color,
    val scrim: Color,
)

val Light = Palette(
    dark = false,
    bg = Color(0xFFF4EBDA), bg2 = Color(0xFFEADCC2), surface = Color(0xFFFFFAF0), surface2 = Color(0xFFF0E4CD), line = Color(0xFFE0CFAF),
    ink = Color(0xFF2A1A12), ink2 = Color(0xFF55402F), muted = Color(0xFF8E765E),
    accent = Color(0xFFA8262E), onAccent = Color(0xFFFFF4E2), accentSoft = Color(0xFFF5DCD4),
    gold = Color(0xFFB07A22), red = Color(0xFFB3262E), green = Color(0xFF3F7A45),
    shelf = Color(0xFFB98352), shelfDark = Color(0xFF7A4C26), silhouette = Color(0x4D2A1A12), scrim = Color(0x992A1A12),
)

val Dark = Palette(
    dark = true,
    bg = Color(0xFF1D110C), bg2 = Color(0xFF2A1711), surface = Color(0xFF2B1912), surface2 = Color(0xFF3A231A), line = Color(0x3BE8B24A),
    ink = Color(0xFFF7E8D0), ink2 = Color(0xFFDCC5A6), muted = Color(0xFFA88D72),
    accent = Color(0xFFE8B24A), onAccent = Color(0xFF2A160A), accentSoft = Color(0xFF3C2A14),
    gold = Color(0xFFE8B24A), red = Color(0xFFD9474F), green = Color(0xFF9CC46F),
    shelf = Color(0xFF8A5630), shelfDark = Color(0xFF3A200E), silhouette = Color(0x8C000000), scrim = Color(0xCC0A0503),
)

/** Paper for what's inside a figure: the same warm sheet in both themes. */
object Paper {
    val bg = Color(0xFFF6EAD3)
    val bg2 = Color(0xFFECDCBF)
    val ink = Color(0xFF3A2214)
    val muted = Color(0xFF8A6A50)
    val accent = Color(0xFFA0662A)
}

object C {
    var p by mutableStateOf(Dark)
    val dark get() = p.dark
    val bg get() = p.bg
    val bg2 get() = p.bg2
    val surface get() = p.surface
    val surface2 get() = p.surface2
    val line get() = p.line
    val ink get() = p.ink
    val ink2 get() = p.ink2
    val muted get() = p.muted
    val accent get() = p.accent
    val onAccent get() = p.onAccent
    val accentSoft get() = p.accentSoft
    val gold get() = p.gold
    val red get() = p.red
    val green get() = p.green
    val shelf get() = p.shelf
    val shelfDark get() = p.shelfDark
    val silhouette get() = p.silhouette
    val scrim get() = p.scrim
}

/** Ruslan Display by Oleg Zhuravlev (SIL OFL): the shop sign. */
val SignFont = FontFamily(Font(R.font.ruslan_display))

/** Lora by Cyreal (SIL OFL): everything else. */
val Serif = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_semibold, FontWeight.SemiBold),
    Font(R.font.lora_bold, FontWeight.Bold),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

object T {
    val Hero get() = TextStyle(fontFamily = SignFont, fontSize = 34.sp, lineHeight = 40.sp, color = C.accent)
    val Title get() = TextStyle(fontFamily = SignFont, fontSize = 25.sp, lineHeight = 31.sp, color = C.accent)
    val Sign get() = TextStyle(fontFamily = SignFont, fontSize = 19.sp, lineHeight = 24.sp, color = C.accent)
    val Heading get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp, color = C.ink)
    val Body get() = TextStyle(fontFamily = Serif, fontSize = 15.5.sp, lineHeight = 22.sp, color = C.ink)
    val Strong get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp, lineHeight = 21.sp, color = C.ink)
    val Italic get() = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 26.sp, color = C.ink)
    val Small get() = TextStyle(fontFamily = Serif, fontSize = 13.sp, lineHeight = 18.sp, color = C.muted)
    val Label get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 17.sp, color = C.ink2)
    val Caps get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.8.sp, color = C.gold)
    val Button get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, lineHeight = 19.sp)
    val Number get() = TextStyle(fontFamily = SignFont, fontSize = 28.sp, lineHeight = 32.sp, color = C.gold)
}

/** 0: as the system, 1: light, 2: dark. */
@Composable
fun MatryoshkaTheme(mode: Int = 0, content: @Composable () -> Unit) {
    val dark = when (mode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val p = if (dark) Dark else Light
    if (C.p != p) C.p = p
    val view = androidx.compose.ui.platform.LocalView.current
    SideEffect {
        if (C.p != p) C.p = p
        (view.context as? android.app.Activity)?.window?.let { w ->
            androidx.core.view.WindowCompat.getInsetsController(w, view).apply { isAppearanceLightStatusBars = !dark; isAppearanceLightNavigationBars = !dark }
        }
    }
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.bg, surface = p.surface, onSurface = p.ink, onBackground = p.ink, outline = p.line, error = p.red)
        else lightColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.bg, surface = p.surface, onSurface = p.ink, onBackground = p.ink, outline = p.line, error = p.red),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
