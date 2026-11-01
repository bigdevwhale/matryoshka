package app.matryoshka.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import app.matryoshka.domain.Paint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** The colours of a painting: dress, headscarf, apron, its border, the dots on the scarf. */
class Colors(val dress: Color, val scarf: Color, val apron: Color, val trim: Color, val dot: Color)

val Paint.colors: Colors
    get() = when (this) {
        Paint.KHOKHLOMA -> Colors(Color(0xFFB3262E), Color(0xFF1F1512), Color(0xFF1F1512), Color(0xFFE8B24A), Color(0xFFE8B24A))
        Paint.GZHEL -> Colors(Color(0xFFF3EFE6), Color(0xFF24489A), Color(0xFFFBF8F1), Color(0xFF24489A), Color(0xFFFFFFFF))
        Paint.POLKHOV -> Colors(Color(0xFFB8203A), Color(0xFFF0B63A), Color(0xFFF7EEDB), Color(0xFF2A7A3A), Color(0xFFB8203A))
        Paint.SEMYONOV -> Colors(Color(0xFFEAA720), Color(0xFFC7262C), Color(0xFFF8EFDA), Color(0xFFC7262C), Color(0xFFFFF3D6))
        Paint.VYATKA -> Colors(Color(0xFF2F5B46), Color(0xFFEFE4CB), Color(0xFFEFE4CB), Color(0xFFB8862E), Color(0xFFC7262C))
    }

/**
 * A matryoshka drawn in a 200×300 box. [open] 0..1 lifts the top half off at the waist; [solid] is the smallest
 * figure that doesn't open; [silhouette] draws only the outline filled with that colour (someone else's figure).
 */
@Composable
fun Doll(paint: Paint, modifier: Modifier = Modifier, open: Float = 0f, solid: Boolean = false, silhouette: Color? = null) {
    Canvas(modifier.aspectRatio(2f / 3f)) { doll(paint, open, solid, silhouette) }
}

private object Shape {
    val body: Path = PathParser().parsePathString(
        "M100,8 C68,8 52,34 54,66 C55,88 64,98 60,110 C36,134 22,174 22,218 C22,268 54,292 100,292 C146,292 178,268 178,218 C178,174 164,134 140,110 C136,98 145,88 146,66 C148,34 132,8 100,8 Z"
    ).toPath()
    val scarf: Path = PathParser().parsePathString(
        "M100,8 C68,8 52,34 54,66 C55,88 64,98 60,110 C80,130 120,130 140,110 C136,98 145,88 146,66 C148,34 132,8 100,8 Z"
    ).toPath()
    val knot: Path = PathParser().parsePathString("M86,122 L100,146 L114,122 Z").toPath()
    val hair: Path = PathParser().parsePathString("M70,64 C70,42 130,42 130,64 C124,52 108,48 100,57 C92,48 76,52 70,64Z").toPath()
    val brows: Path = PathParser().parsePathString("M84,68 Q89,65 94,68 M106,68 Q111,65 116,68").toPath()
    val mouth: Path = PathParser().parsePathString("M94.5,93 Q97.5,89.5 100,92 Q102.5,89.5 105.5,93 Q100,99 94.5,93Z").toPath()
    val apron = Rect(48f, 148f, 152f, 276f)
    val apronPath = Path().apply { addOval(apron) }
    val stem: Path = PathParser().parsePathString(
        "M100,272 C70,250 62,226 82,210 C98,198 86,178 74,176 M100,272 C130,250 138,226 118,210 C102,198 114,178 126,176 M100,268 C100,240 100,220 100,196"
    ).toPath()
    val dots = listOf(70 to 36, 130 to 36, 100 to 20, 61 to 92, 139 to 92, 78 to 116, 122 to 116, 60 to 58, 140 to 58, 86 to 26, 114 to 26)
    const val SPLIT = 150f
}

private val Face = Color(0xFFF7DCC1)
private val Hair = Color(0xFF5A3018)
private val Eye = Color(0xFF2B1A12)
private val Cheek = Color(0x80E5636A)
private val Lips = Color(0xFFC8323C)
private val WoodIn = Color(0xFFD6A76A)
private val WoodDark = Color(0xFFA8743C)
private val WoodLine = Color(0xFF8A5A2C)

fun DrawScope.doll(paint: Paint, open: Float = 0f, solid: Boolean = false, silhouette: Color? = null) {
    val k = size.width / 200f
    withTransform({ scale(k, k, pivot = Offset.Zero) }) {
        if (silhouette != null) {
            drawPath(Shape.body, silhouette)
            return@withTransform
        }
        val c = paint.colors
        if (solid || open <= 0f) {
            art(paint, c)
            if (!solid) drawLine(Color(0x4D000000), Offset(31f, Shape.SPLIT), Offset(169f, Shape.SPLIT), 1.2f)
            return@withTransform
        }
        translate(0f, 10f * open) {
            clipRect(-40f, Shape.SPLIT, 240f, 340f) { art(paint, c) }
            drawOval(WoodIn.copy(alpha = open.coerceIn(0f, 1f)), Offset(32f, Shape.SPLIT - 9f), Size(136f, 18f))
            drawOval(WoodLine.copy(alpha = open.coerceIn(0f, 1f)), Offset(32f, Shape.SPLIT - 9f), Size(136f, 18f), style = Stroke(2f))
            drawOval(WoodDark.copy(alpha = open.coerceIn(0f, 1f)), Offset(42f, Shape.SPLIT - 5f), Size(116f, 12f))
        }
        withTransform({
            translate(-18f * open, -86f * open)
            rotate(-14f * open, pivot = Offset(40f, Shape.SPLIT))
        }) {
            clipRect(-40f, -40f, 240f, Shape.SPLIT) { art(paint, c) }
            drawOval(WoodDark.copy(alpha = open.coerceIn(0f, 1f)), Offset(32f, Shape.SPLIT - 9f), Size(136f, 18f))
        }
    }
}

private fun DrawScope.art(paint: Paint, c: Colors) {
    drawPath(Shape.body, c.dress)
    for (i in 0 until 13) circle(34f + i * 11f, 272f - abs(6 - i) * 2.2f, 2.4f, c.trim)
    drawOval(c.apron, Shape.apron.topLeft, Shape.apron.size)
    clipPath(Shape.apronPath) { pattern(paint) }
    drawOval(c.trim, Shape.apron.topLeft, Shape.apron.size, style = Stroke(3f))
    drawPath(Shape.scarf, c.scarf)
    for ((x, y) in Shape.dots) circle(x.toFloat(), y.toFloat(), 2.6f, c.dot)
    drawPath(Shape.knot, c.scarf)
    drawOval(Face, Offset(70f, 38f), Size(60f, 64f))
    drawPath(Shape.hair, Hair)
    drawPath(Shape.brows, Hair, style = Stroke(1.5f, cap = StrokeCap.Round))
    drawOval(Eye, Offset(86.2f, 71.4f), Size(5.6f, 7.2f))
    drawOval(Eye, Offset(108.2f, 71.4f), Size(5.6f, 7.2f))
    circle(82f, 87f, 7f, Cheek)
    circle(118f, 87f, 7f, Cheek)
    drawPath(Shape.mouth, Lips)
    ellipse(72f, 168f, 11f, 58f, 10f, Color(0x1AFFFFFF))
    ellipse(78f, 30f, 9f, 15f, 25f, Color(0x29FFFFFF))
    drawPath(Shape.body, Color(0x59000000), style = Stroke(1.5f))
}

private fun DrawScope.circle(x: Float, y: Float, r: Float, color: Color) = drawCircle(color, r, Offset(x, y))

private fun DrawScope.ellipse(x: Float, y: Float, rx: Float, ry: Float, angle: Float, color: Color, outline: Color? = null) {
    rotate(angle, pivot = Offset(x, y)) {
        drawOval(color, Offset(x - rx, y - ry), Size(rx * 2, ry * 2))
        if (outline != null) drawOval(outline, Offset(x - rx, y - ry), Size(rx * 2, ry * 2), style = Stroke(1f))
    }
}

private fun DrawScope.flower(x: Float, y: Float, r: Float, n: Int, petal: Color, center: Color, outline: Color? = null) {
    for (i in 0 until n) {
        val a = 360f / n * i
        val rad = Math.toRadians(a.toDouble())
        ellipse(x + cos(rad).toFloat() * r * .55f, y + sin(rad).toFloat() * r * .55f, r * .5f, r * .3f, a, petal, outline)
    }
    circle(x, y, r * .32f, center)
    if (outline != null) drawCircle(outline, r * .32f, Offset(x, y), style = Stroke(1f))
}

private fun DrawScope.ring(x: Float, y: Float, r: Float, fill: Color, line: Color, w: Float) {
    drawCircle(fill, r, Offset(x, y))
    drawCircle(line, r, Offset(x, y), style = Stroke(w))
}

private fun DrawScope.pattern(p: Paint) {
    when (p) {
        Paint.KHOKHLOMA -> {
            val g = Color(0xFFE8B24A)
            val r = Color(0xFFC8323C)
            drawPath(Shape.stem, g, style = Stroke(2.6f, cap = StrokeCap.Round))
            listOf(Triple(70, 236, -40), Triple(130, 236, 40), Triple(66, 206, -70), Triple(134, 206, 70), Triple(88, 184, -20),
                Triple(112, 184, 20), Triple(84, 256, -60), Triple(116, 256, 60))
                .forEach { (x, y, a) -> ellipse(x.toFloat(), y.toFloat(), 11f, 5f, a.toFloat(), g) }
            listOf(76 to 224, 81 to 229, 72 to 231, 124 to 224, 119 to 229, 128 to 231, 100 to 252, 95 to 258, 105 to 258).forEach { (x, y) ->
                circle(x.toFloat(), y.toFloat(), 4.2f, r)
                circle(x - 1.2f, y - 1.4f, 1.1f, Color(0xFFFFD28A))
            }
            flower(100f, 186f, 20f, 7, g, r)
        }
        Paint.GZHEL -> {
            val b = Color(0xFF24489A)
            flower(100f, 206f, 40f, 8, b, b)
            flower(100f, 206f, 22f, 8, Color(0xFFFBF8F1), b)
            circle(100f, 206f, 6f, b)
            listOf(Triple(62, 258, 30), Triple(138, 258, -30), Triple(60, 160, -30), Triple(140, 160, 30))
                .forEach { (x, y, a) -> ellipse(x.toFloat(), y.toFloat(), 14f, 5f, a.toFloat(), b) }
            listOf(100 to 262, 100 to 158, 78 to 248, 122 to 248).forEach { (x, y) -> circle(x.toFloat(), y.toFloat(), 3f, b) }
        }
        Paint.POLKHOV -> {
            val k = Color(0xFF1A1210)
            listOf(Triple(68, 214, -30), Triple(132, 214, 30), Triple(74, 246, -60), Triple(126, 246, 60))
                .forEach { (x, y, a) -> ellipse(x.toFloat(), y.toFloat(), 16f, 7f, a.toFloat(), Color(0xFF2A7A3A), k) }
            ring(100f, 206f, 30f, Color(0xFFB8203A), k, 1.4f)
            ring(100f, 206f, 20f, Color(0xFFEC6A86), k, 1.2f)
            ring(100f, 206f, 10f, Color(0xFFB8203A), k, 1.2f)
            ring(100f, 166f, 9f, Color(0xFF2F5FB3), k, 1f)
            ring(100f, 252f, 10f, Color(0xFFF0B63A), k, 1f)
            circle(100f, 252f, 4f, Color(0xFFB8203A))
        }
        Paint.SEMYONOV -> {
            listOf(Triple(70, 232, -35), Triple(130, 232, 35), Triple(86, 256, -70), Triple(114, 256, 70), Triple(100, 170, 0))
                .forEach { (x, y, a) -> ellipse(x.toFloat(), y.toFloat(), 13f, 5f, a.toFloat(), Color(0xFF3F8A3A)) }
            listOf(Triple(82f, 198f, 15f), Triple(118f, 198f, 15f), Triple(100f, 226f, 17f))
                .forEach { (x, y, r) -> flower(x, y, r * 1.7f, 5, Color(0xFFD3272D), Color(0xFF1A1210)) }
            listOf(100 to 176, 66 to 210, 134 to 210).forEach { (x, y) -> circle(x.toFloat(), y.toFloat(), 4.5f, Color(0xFFEAA720)) }
        }
        Paint.VYATKA -> {
            listOf(Triple(76, 196, -40), Triple(124, 196, 40), Triple(80, 240, -50), Triple(120, 240, 50), Triple(100, 168, 0), Triple(100, 262, 0))
                .forEach { (x, y, a) -> ellipse(x.toFloat(), y.toFloat(), 15f, 5.5f, a.toFloat(), Color(0xFF2F5B46)) }
            listOf(100 to 212, 92 to 206, 108 to 206, 96 to 220, 104 to 220, 100 to 198, 88 to 216, 112 to 216).forEach { (x, y) ->
                circle(x.toFloat(), y.toFloat(), 5f, Color(0xFFC7262C))
                circle(x - 1.5f, y - 1.5f, 1.2f, Color(0xFFFFD9B0))
            }
        }
    }
}
