package com.mindscape.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindscape.app.data.Domain
import com.mindscape.app.data.MindscapeUiState
import com.mindscape.app.domain.MindscapeEngine
import com.mindscape.app.ui.components.GlassPill
import com.mindscape.app.ui.components.GlassSurface
import com.mindscape.app.ui.components.GradientButton
import com.mindscape.app.ui.components.MonoLabel
import com.mindscape.app.ui.components.ProgressRing
import com.mindscape.app.ui.components.SkillTree
import com.mindscape.app.ui.components.VideoMiniCard
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.ForestCard
import com.mindscape.app.ui.theme.ForestBorder
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.NeonLime
import com.mindscape.app.ui.theme.SpaceGrotesk
import com.mindscape.app.ui.theme.TextMuted
import com.mindscape.app.ui.theme.TextPrimary
import com.mindscape.app.data.NodeStatus
import java.text.NumberFormat
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    state: MindscapeUiState,
    onDomainSelected: (String) -> Unit,
    onNodeSelected: (String) -> Unit,
    onArtifactSelected: (String) -> Unit,
    onCollectVideo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val domain = state.activeDomain
    val selectedNode = state.selectedNode
    val nodeVideos = selectedNode?.let { state.videosForNode(it.id) } ?: emptyList()
    val testedCount = nodeVideos.count { it.tested }
    val masteredCount = domain.nodes.count { it.status == NodeStatus.MASTERED }
    val progress = MindscapeEngine.percentMastered(domain)
    val percent = (progress * 100).roundToInt()
    val numberFormat = remember { NumberFormat.getIntegerInstance() }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 64.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                MonoLabel(
                    text = "KNOWLEDGE MAP",
                    color = NeonLime,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Map",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            GradientButton(
                onClick = onCollectVideo,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = ForestBg,
                    modifier = Modifier.size(10.dp),
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = "Collect Video",
                    color = ForestBg,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        GlassSurface(
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonoLabel(
                    text = "REALM:",
                    color = TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
                RealmSelector(
                    domains = state.domains,
                    activeDomainId = state.activeDomainId,
                    onDomainSelected = onDomainSelected,
                )
            }
        }

        GlassSurface(
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    MonoLabel(
                        text = "TOTAL PROGRESS",
                        color = NeonCyan,
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = numberFormat.format(state.userXp),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = "pts",
                            color = NeonLime,
                            fontSize = 11.sp,
                        )
                    }
                    Text(
                        text = "$percent% Map Completed ($masteredCount of ${domain.nodes.size} Mastered)",
                        color = TextMuted.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                    )
                }
                ProgressRing(
                    progress = progress,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        GlassSurface(
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AccountTree,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.size(5.dp))
                        Text(
                            text = "Skill Tree Map",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    GlassPill(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        MonoLabel(
                            text = "TAP NODES TO EXPLORE",
                            color = NeonLime,
                            fontSize = 7.sp,
                            letterSpacing = 0.4.sp,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                SkillTree(
                    nodes = domain.nodes,
                    selectedNodeId = state.selectedNodeId,
                    videosForNode = { state.videosForNode(it) },
                    onNodeClick = onNodeSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Tap any node to view assigned videos and quizzes",
                    modifier = Modifier.fillMaxWidth(),
                    color = TextMuted.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonoLabel(
                    text = "${(selectedNode?.title ?: "NODE").uppercase()} VIDEOS",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f),
                )
                MonoLabel(
                    text = "$testedCount of ${selectedNode?.requiredVideos ?: 0} COMPLETED",
                    color = NeonLime,
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp,
                )
            }

            if (nodeVideos.isEmpty()) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                ) {
                    Text(
                        text = "No videos added yet. Tap + to collect one!",
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                    )
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = nodeVideos, key = { it.id }) { video ->
                        VideoMiniCard(
                            artifact = video,
                            selected = video.id == state.activeArtifactId,
                            onClick = { onArtifactSelected(video.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RealmSelector(
    domains: List<Domain>,
    activeDomainId: String,
    onDomainSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val activeDomain = domains.firstOrNull { it.id == activeDomainId }
    val shape = RoundedCornerShape(12.dp)

    Box {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(ForestBg)
                .border(1.dp, ForestBorder, shape)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = activeDomain?.label ?: "Select realm",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = "Choose realm",
                tint = NeonCyan,
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ForestCard),
        ) {
            domains.forEach { domain ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = domain.label,
                            color = if (domain.id == activeDomainId) NeonCyan else TextPrimary,
                            fontSize = 11.sp,
                        )
                    },
                    onClick = {
                        expanded = false
                        onDomainSelected(domain.id)
                    },
                )
            }
        }
    }
}
