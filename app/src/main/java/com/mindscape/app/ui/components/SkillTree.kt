package com.mindscape.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindscape.app.data.NodeIcon
import com.mindscape.app.data.NodeStatus
import com.mindscape.app.data.SkillNode
import com.mindscape.app.data.VideoArtifact
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.ForestBorder
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.NeonLime
import com.mindscape.app.ui.theme.SpaceGrotesk
import com.mindscape.app.ui.theme.TextPrimary

/**
 * Constellation-style skill tree: dashed locked edges, glowing unlocked edges and lime
 * mastered edges, with nodes positioned by their percentage coordinates.
 */
@Composable
fun SkillTree(
    nodes: List<SkillNode>,
    selectedNodeId: String,
    videosForNode: (String) -> List<VideoArtifact>,
    onNodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ForestBg.copy(alpha = 0.95f))
            .border(1.dp, Color(0xFF052E1B), RoundedCornerShape(16.dp)),
    ) {
        val nodeById = nodes.associateBy { it.id }

        Canvas(Modifier.fillMaxSize()) {
            nodes.forEach { node ->
                val parent = node.parentId?.let(nodeById::get) ?: return@forEach
                val start = Offset(
                    size.width * (parent.xPercent / 100f),
                    size.height * (parent.yPercent / 100f),
                )
                val end = Offset(
                    size.width * (node.xPercent / 100f),
                    size.height * (node.yPercent / 100f),
                )
                when (node.status) {
                    NodeStatus.LOCKED -> drawLine(
                        color = Color(0xFF0D2217),
                        start = start,
                        end = end,
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 4.dp.toPx()),
                        ),
                    )

                    else -> {
                        val color = if (node.status == NodeStatus.MASTERED) NeonLime else NeonCyan
                        drawLine(
                            color = color.copy(alpha = 0.16f),
                            start = start,
                            end = end,
                            strokeWidth = 8.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = if (node.status == NodeStatus.MASTERED) 3.dp.toPx() else 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
        }

        nodes.forEach { node ->
            val centerX = maxWidth * (node.xPercent / 100f)
            val centerY = maxHeight * (node.yPercent / 100f)
            SkillNodeView(
                node = node,
                isSelected = node.id == selectedNodeId,
                testedCount = videosForNode(node.id).count { it.tested },
                onClick = { onNodeClick(node.id) },
                modifier = Modifier.offset(x = centerX - 48.dp, y = centerY - 16.dp),
            )
        }
    }
}

@Composable
private fun SkillNodeView(
    node: SkillNode,
    isSelected: Boolean,
    testedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = node.status != NodeStatus.LOCKED
    val circleColor = when (node.status) {
        NodeStatus.MASTERED -> NeonLime
        NodeStatus.UNLOCKED -> ForestBg
        NodeStatus.LOCKED -> Color.Black.copy(alpha = 0.9f)
    }
    val contentColor = when (node.status) {
        NodeStatus.MASTERED -> ForestBg
        NodeStatus.UNLOCKED -> NeonCyan
        NodeStatus.LOCKED -> Color(0xFF047857)
    }
    val borderColor = when (node.status) {
        NodeStatus.MASTERED -> Color.White
        NodeStatus.UNLOCKED -> NeonCyan
        NodeStatus.LOCKED -> ForestBorder
    }

    Column(
        modifier = modifier
            .width(96.dp)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Box(
                    Modifier
                        .size(40.dp)
                        .border(1.dp, NeonCyan.copy(alpha = 0.55f), CircleShape),
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(if (isSelected) 1.08f else 1f)
                    .clip(CircleShape)
                    .background(circleColor)
                    .border(
                        width = if (isSelected || node.status == NodeStatus.MASTERED) 2.dp else 1.dp,
                        color = if (isSelected) NeonCyan else borderColor,
                        shape = CircleShape,
                    )
                    .then(if (node.status == NodeStatus.LOCKED) Modifier.alpha(0.4f) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconFor(node.icon),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(13.dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-6).dp)
                    .clip(RoundedCornerShape(50))
                    .background(ForestBg)
                    .border(1.dp, ForestBorder, RoundedCornerShape(50))
                    .padding(horizontal = 3.dp, vertical = 1.dp),
            ) {
                when (node.status) {
                    NodeStatus.MASTERED -> Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(7.dp),
                    )

                    NodeStatus.LOCKED -> Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(7.dp),
                    )

                    NodeStatus.UNLOCKED -> Text(
                        text = "$testedCount/${node.requiredVideos}",
                        color = TextPrimary,
                        fontSize = 7.sp,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(5.dp))
        Text(
            text = node.title,
            color = if (isSelected) NeonCyan else TextPrimary,
            fontSize = 8.sp,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(92.dp),
        )
    }
}

private fun iconFor(icon: NodeIcon): ImageVector = when (icon) {
    NodeIcon.ATOM -> Icons.Filled.Science
    NodeIcon.LINK -> Icons.Filled.Link
    NodeIcon.MICROCHIP -> Icons.Filled.Memory
    NodeIcon.KEY -> Icons.Filled.VpnKey
    NodeIcon.BOLT -> Icons.Filled.Bolt
    NodeIcon.SCROLL -> Icons.Filled.MenuBook
    NodeIcon.MONUMENT -> Icons.Filled.AccountBalance
    NodeIcon.LANGUAGE -> Icons.Filled.Translate
}
