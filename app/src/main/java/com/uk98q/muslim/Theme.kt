package com.uk98q.muslim

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ReadexPro: FontFamily = FontFamily(
    Font(R.font.readex_pro_regular, FontWeight.Normal),
    Font(R.font.readex_pro_medium, FontWeight.Medium),
    Font(R.font.readex_pro_bold, FontWeight.Bold)
)

/** Multiplier for the Settings text-size level (0 small ... 3 extra large). */
val LocalTextScale = compositionLocalOf { 1f }

fun textScaleFor(level: Int): Float = when (level) {
    0 -> 0.9f
    2 -> 1.15f
    3 -> 1.3f
    else -> 1f
}

private val MuslimColors: ColorScheme = darkColorScheme(
    primary = Dawn,
    secondary = Mint,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MuslimTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MuslimColors, content = content)
}

/** Text that always uses Readex Pro and follows the text-size setting. */
@Composable
fun Txt(
    text: String,
    size: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    val scale = LocalTextScale.current
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = ReadexPro,
        fontWeight = weight,
        fontSize = (size * scale).sp,
        lineHeight = (size * scale * 1.45f).sp,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/** Translucent glass surface: soft gradient fill, hairline border, rounded corners. */
fun Modifier.glass(radius: Dp = 26.dp, tint: Color? = null): Modifier {
    val shape = RoundedCornerShape(radius)
    val fill = if (tint == null) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.05f)))
    } else {
        Brush.linearGradient(listOf(tint.copy(alpha = 0.30f), Color.White.copy(alpha = 0.04f)))
    }
    return this
        .clip(shape)
        .background(fill)
        .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    radius: Dp = 26.dp,
    padding: Dp = 20.dp,
    tint: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = modifier.glass(radius, tint)
    val interactive = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(modifier = interactive.padding(padding), content = content)
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    diameter: Dp = 44.dp
) {
    Box(
        modifier = Modifier.size(diameter).glass(diameter / 2).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.size(diameter * 0.45f)
        )
    }
}

@Composable
fun CircleGlassButton(icon: ImageVector, tint: Color, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(70.dp).glass(35.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = description, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent else Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Txt(text, 14f, color = if (selected) Color.Black else Color.White, weight = FontWeight.Medium, align = TextAlign.Center)
    }
}

/** Pure black base (OLED friendly) with soft colored light behind the glass. */
@Composable
fun AmbientBackground(tint: Color, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(tint.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.08f),
                        radius = 620.dp.toPx()
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Dusk.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.92f),
                        radius = 560.dp.toPx()
                    )
                )
            },
        content = content
    )
}

@Composable
fun TopHeader(title: String, onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", onBack)
        Spacer(Modifier.width(12.dp))
        Txt(title, 18f, modifier = Modifier.weight(1f), weight = FontWeight.Bold)
        trailing()
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    GlassPanel(modifier = Modifier.fillMaxWidth(), radius = 26.dp, padding = 20.dp) {
        Txt(title, 14f, color = SecondaryText, weight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

/** Keeps the fill arrangement helpers in one place for the screens. */
val CenterArrangement = Arrangement.Center
