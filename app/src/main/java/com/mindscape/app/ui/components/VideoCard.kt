package com.mindscape.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
 * Feed card with explicit lesson and map actions.
 *
 * Horizontal drags are intentionally left to the parent pager, so swiping anywhere on the
 * card switches between Learn, Watch and Map instead of triggering a card-level action.
 */
@Composable
fun FeedVideoCard(
    artifact: VideoArtifact,
    onOpenLesson: () -> Unit,
    onShowInMap: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(ForestCard)
            .border(1.dp, NeonEmerald.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
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
