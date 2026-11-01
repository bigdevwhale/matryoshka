package app.matryoshka.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.matryoshka.ui.theme.C
import app.matryoshka.ui.theme.T
import kotlinx.coroutines.delay

val Round = RoundedCornerShape(18.dp)
val Small = RoundedCornerShape(12.dp)

/** The room: lacquer (or limewood) with a warm light from above. */
fun Modifier.room() = this.background(
    Brush.radialGradient(listOf(C.bg2, C.bg), center = Offset(300f, -200f), radius = 2200f)
).background(C.bg.copy(alpha = 0f))

@Composable
fun Card(modifier: Modifier = Modifier, pad: Dp = 16.dp, color: Color = C.surface, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.clip(Round).background(color).border(1.dp, C.line, Round)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(pad),
        content = content,
    )
}

enum class Kind { PRIMARY, GHOST, QUIET }

@Composable
fun Btn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, kind: Kind = Kind.PRIMARY, enabled: Boolean = true, icon: ImageVector? = null, ink: Color? = null) {
    val (bg, fg) = when (kind) {
        Kind.PRIMARY -> C.accent to C.onAccent
        Kind.GHOST -> Color.Transparent to (ink ?: C.accent)
        Kind.QUIET -> C.surface2 to C.ink
    }
    Row(
        modifier.clip(Small).background(if (enabled) bg else C.surface2)
            .then(if (kind == Kind.GHOST) Modifier.border(1.dp, ink?.copy(alpha = .3f) ?: C.line, Small) else Modifier)
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (enabled) fg else C.muted, modifier = Modifier.size(18.dp))
            Box(Modifier.width(8.dp))
        }
        Text(text, style = T.Button.copy(color = if (enabled) fg else C.muted), textAlign = TextAlign.Center)
    }
}

@Composable
fun IconBtn(icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = C.ink2, desc: String? = null) {
    Box(modifier.size(44.dp).clip(CircleShape).border(1.dp, C.line, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, desc, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun Chip(text: String, on: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        style = T.Label.copy(color = if (on) C.onAccent else C.ink2),
        modifier = modifier.clip(CircleShape).background(if (on) C.accent else Color.Transparent)
            .border(1.dp, if (on) C.accent else C.line, CircleShape).clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
    )
}

@Composable
fun Caps(text: String, modifier: Modifier = Modifier) = Text(text.uppercase(), style = T.Caps, modifier = modifier)

@Composable
fun Gap(h: Dp) = Box(Modifier.height(h))

/** A text field drawn in the room's own colours. */
@Composable
fun Field(
    value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, hint: String = "", lines: Int = 1,
    style: TextStyle = T.Body, number: Boolean = false, onDone: (() -> Unit)? = null, caps: Boolean = true,
) {
    BasicTextField(
        value = value, onValueChange = onChange, textStyle = style, cursorBrush = SolidColor(C.accent),
        singleLine = lines == 1, minLines = lines,
        keyboardOptions = KeyboardOptions(
            capitalization = if (caps) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
            keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text,
            imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        modifier = modifier,
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth().clip(Small).background(C.bg).border(1.dp, C.line, Small).padding(horizontal = 14.dp, vertical = 12.dp)) {
                if (value.isEmpty()) Text(hint, style = style.copy(color = C.muted))
                inner()
            }
        },
    )
}

@Composable
fun TopBar(title: String, sub: String? = null, onBack: (() -> Unit)? = null, right: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconBtn(Icons.AutoMirrored.Rounded.ArrowBack, onBack)
            Box(Modifier.width(10.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = T.Sign, maxLines = 1)
            if (sub != null) Text(sub, style = T.Small, maxLines = 1)
        }
        right?.invoke()
    }
}

/** A dimmed layer over everything with something on top; tapping outside dismisses. */
@Composable
fun Scrim(onDismiss: (() -> Unit)?, content: @Composable BoxScope.() -> Unit) {
    BackHandler(enabled = onDismiss != null) { onDismiss?.invoke() }
    Box(
        Modifier.fillMaxSize().background(C.scrim)
            .clickable(remember { MutableInteractionSource() }, null) { onDismiss?.invoke() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(Modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null) {}) { content() }
    }
}

@Composable
fun SheetBox(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)).background(C.bg2)
            .border(1.dp, C.line, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .navigationBarsPadding().padding(20.dp),
        content = content,
    )
}

/** Gold and cinnabar dots, like the border of a painted box. */
@Composable
fun Ornament(modifier: Modifier = Modifier) {
    val gold = C.gold
    val red = C.red
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        val step = 24.dp.toPx()
        var x = 6.dp.toPx()
        while (x < size.width) {
            drawCircle(gold.copy(alpha = .7f), 2.dp.toPx(), Offset(x, size.height / 2))
            drawCircle(red.copy(alpha = .7f), 2.5.dp.toPx(), Offset(x + step / 2, size.height / 2))
            x += step
        }
    }
}

/** A short note at the bottom of the screen. */
object Toast {
    var text by mutableStateOf<String?>(null)
    fun show(s: String) { text = s }
}

@Composable
fun ToastHost(modifier: Modifier = Modifier) {
    val t = Toast.text
    LaunchedEffect(t) { if (t != null) { delay(2600); Toast.text = null } }
    AnimatedVisibility(t != null, modifier, enter = fadeIn() + slideInVertically { it / 2 }, exit = fadeOut() + slideOutVertically { it / 2 }) {
        var last by remember { mutableStateOf("") }
        if (t != null) last = t
        Text(last, style = T.Body.copy(color = C.bg), textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp).clip(Small).background(C.ink).padding(horizontal = 16.dp, vertical = 11.dp))
    }
}
