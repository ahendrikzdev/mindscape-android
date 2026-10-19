package com.mindscape.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindscape.app.data.VideoArtifact
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.ForestCard
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.NeonEmerald
import com.mindscape.app.ui.theme.NeonLime
import com.mindscape.app.ui.theme.SpaceGrotesk
import com.mindscape.app.ui.theme.TextMuted
import com.mindscape.app.ui.theme.TextPrimary

/**
 * Full-bleed feed card with the prototype's horizontal gestures:
 * swipe left opens the lesson, swipe right focuses the topic on the map.
 */
@Composable
fun SwipeableVideoCard(
    artifact: VideoArtifact,
    onOpenLesson: () -> Unit,
    onShowInMap: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val thresholdPx = with(density) { 60.dp.toPx() }
    val maxDragPx = with(density) { 140.dp.toPx() }
    val fadeDistancePx = with(density) { 80.dp.toPx() }

    var dragOffset by remember(artifact.id) { mutableFloatStateOf(0f) }
    val leftProgress = (-dragOffset / fadeDistancePx).coerceIn(0f, 1f)
    val rightProgress = (dragOffset / fadeDistancePx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(ForestCard)
            .border(1.dp, NeonEmerald.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .pointerInput(artifact.id) {
                detectHorizontalDragGestures(
                    onDragStart = { dragOffset = 0f },
                    onDragCancel = { dragOffset = 0f },
                    onDragEnd = {
                        val finalOffset = dragOffset
                        dragOffset = 0f
                        when {
                            finalOffset <= -thresholdPx -> {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onOpenLesson()
                            }

                            finalOffset >= thresholdPx -> {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onShowInMap()
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset = (dragOffset + dragAmount).coerceIn(-maxDragPx, maxDragPx)
                    },
                )
            },
    ) {
        Image(
            painter = painterResource(artifact.thumbnailRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to ForestBg.copy(alpha = 0.82f),
                        0.34f to ForestBg.copy(alpha = 0.2f),
                        1f to ForestBg.copy(alpha = 0.96f),
                    ),
                ),
        )

        SwipeEdge(alpha = leftProgress, left = true)
        SwipeEdge(alpha = rightProgress, left = false)

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassPill(modifier = Modifier.widthIn(max = 190.dp)) {
                Icon(
                    Icons.Filled.PlayCircle,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(11.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = artifact.author,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GlassPill {
                MonoLabel(
                    text = if (artifact.tested) "MASTERED" else "QUIZ PENDING",
                    color = if (artifact.tested) NeonLime else TextMuted,
                    fontSize = 8.sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 170.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GlassIconButton(
                contentDescription = "Delete video",
                onClick = onDelete,
                iconTint = Color(0xFFFB7185),
            ) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
            GlassIconButton(
                contentDescription = "Show on map",
                onClick = onShowInMap,
                iconTint = NeonLime,
            ) {
                Icon(
                    Icons.Filled.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.86f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = artifact.title,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            GlassSurface(
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(10.dp),
            ) {
                Text(
                    text = "\"${artifact.caption}\"",
                    color = TextPrimary.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            GradientButton(
                onClick = onOpenLesson,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    tint = ForestBg,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "Open Lesson & Quiz",
                    color = ForestBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SwipeEdge(alpha: Float, left: Boolean) {
    val color = if (left) NeonCyan else NeonLime
    val brush = if (left) {
        Brush.horizontalGradient(listOf(color.copy(alpha = 0.4f), Color.Transparent))
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, color.copy(alpha = 0.4f)))
    }
    Box(
        modifier = Modifier
            .align(if (left) Alignment.CenterStart else Alignment.CenterEnd)
            .fillMaxHeight()
            .width(96.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(brush),
    ) {
        GlassPill(
            modifier = Modifier
                .align(if (left) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 14.dp),
        ) {
            if (left) {
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(11.dp),
                )
                Spacer(Modifier.size(5.dp))
                Text("Learn", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("Map", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(5.dp))
                Icon(
                    Icons.Filled.Explore,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(11.dp),
                )
            }
        }
    }
}

@Composable
fun VideoMiniCard(
    artifact: VideoArtifact,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier.width(112.dp),
        shape = RoundedCornerShape(16.dp),
        highlighted = selected,
        contentPadding = PaddingValues(6.dp),
    ) {
        Column(Modifier.clickable(onClick = onClick)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ForestCard),
            ) {
                Image(
                    painter = painterResource(artifact.thumbnailRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            ),
                        ),
                )
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(16.dp),
                )
                if (artifact.tested) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(NeonLime),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = ForestBg,
                            modifier = Modifier.size(9.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = artifact.title,
                color = TextPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artifact.author,
                color = TextMuted.copy(alpha = 0.8f),
                fontSize = 8.sp,
                fontFamily = SpaceGrotesk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
