package com.mindscape.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.ForestBorder
import com.mindscape.app.ui.theme.ForestSurface
import com.mindscape.app.ui.theme.GlassBorder
import com.mindscape.app.ui.theme.GlassFill
import com.mindscape.app.ui.theme.GlassPillFill
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.NeonEmerald
import com.mindscape.app.ui.theme.NeonLime
import com.mindscape.app.ui.theme.SpaceGrotesk
import com.mindscape.app.ui.theme.TextPrimary

/** Translucent card used throughout the app (the native take on the CSS glass cards). */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val fill = if (highlighted) ForestSurface.copy(alpha = 0.92f) else GlassFill
    val stroke = if (highlighted) NeonLime.copy(alpha = 0.4f) else GlassBorder
    Box(
        modifier = modifier
            .shadow(
                elevation = if (highlighted) 14.dp else 8.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(shape)
            .background(fill)
            .border(1.dp, stroke, shape),
    ) {
        Box(Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(GlassPillFill)
            .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun GlassIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(36.dp),
    iconTint: Color = TextPrimary,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.09f), CircleShape)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides iconTint) {
            content()
        }
    }
}

/** Cyan → emerald → lime action button from the prototype. */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val brush = if (enabled) {
        Brush.linearGradient(listOf(NeonCyan, NeonEmerald, NeonLime))
    } else {
        Brush.linearGradient(listOf(ForestBorder, ForestBorder))
    }
    Row(
        modifier = modifier
            .clip(shape)
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun MonoLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = NeonCyan,
    fontSize: TextUnit = 9.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    letterSpacing: TextUnit = 1.sp,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontFamily = SpaceGrotesk,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Ambient radial glows plus the faint 32dp atlas grid from the HTML background. */
@Composable
fun AtlasBackground(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.5f, 0f),
                radius = size.width * 0.85f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(NeonEmerald.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.4f),
                radius = size.width * 0.65f,
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(NeonLime.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.8f),
                radius = size.width * 0.7f,
            ),
        )

        val step = 32.dp.toPx()
        val gridColor = Color.White.copy(alpha = 0.035f)
        var x = 0f
        while (x <= size.width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}

/** Text/icon color for content rendered on the gradient button. */
val OnGradient: Color = ForestBg
