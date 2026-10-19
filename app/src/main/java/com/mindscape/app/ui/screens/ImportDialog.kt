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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mindscape.app.data.Domain
import com.mindscape.app.ui.components.GlassIconButton
import com.mindscape.app.ui.components.GlassSurface
import com.mindscape.app.ui.components.GradientButton
import com.mindscape.app.ui.components.MonoLabel
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.ForestBorder
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.RoseSoft
import com.mindscape.app.ui.theme.SpaceGrotesk
import com.mindscape.app.ui.theme.TextMuted
import com.mindscape.app.ui.theme.TextPrimary
import androidx.compose.ui.text.style.TextAlign
import com.mindscape.app.ui.theme.ForestCard
import kotlinx.coroutines.launch

@Composable
fun ImportDialog(
    domains: List<Domain>,
    activeDomainId: String,
    onDismiss: () -> Unit,
    onExtract: suspend (url: String, nodeId: String) -> Unit,
    onImported: () -> Unit,
) {
    val activeDomain = domains.firstOrNull { it.id == activeDomainId }
    val nodes = activeDomain?.nodes.orEmpty()
    val scope = rememberCoroutineScope()

    var url by rememberSaveable {
        mutableStateOf("https://tiktok.com/@learn/video/quantum-101")
    }
    var selectedNodeId by rememberSaveable(activeDomainId) {
        mutableStateOf(nodes.firstOrNull()?.id.orEmpty())
    }
    var nodeMenuExpanded by remember { mutableStateOf(false) }
    var isExtracting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val selectedNode = nodes.firstOrNull { it.id == selectedNodeId }

    Dialog(
        onDismissRequest = { if (!isExtracting) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isExtracting,
            dismissOnClickOutside = !isExtracting,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                highlighted = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 340.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                Box {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Link,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = "Collect Video & Pin to Map",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Mindscape extracts key points to generate lessons",
                                color = TextMuted.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                            )
                        }

                        Column {
                            MonoLabel("VIDEO LINK", fontSize = 9.sp, letterSpacing = 0.8.sp)
                            Spacer(Modifier.size(4.dp))
                            OutlinedTextField(
                                value = url,
                                onValueChange = {
                                    url = it
                                    error = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = SpaceGrotesk,
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = ForestBorder.copy(alpha = 0.8f),
                                    focusedContainerColor = ForestBg,
                                    unfocusedContainerColor = ForestBg,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = NeonCyan,
                                ),
                            )
                        }

                        Column {
                            MonoLabel("SELECT TOPIC NODE", fontSize = 9.sp, letterSpacing = 0.8.sp)
                            Spacer(Modifier.size(4.dp))
                            Box {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ForestBg)
                                        .border(
                                            1.dp,
                                            ForestBorder.copy(alpha = 0.8f),
                                            RoundedCornerShape(12.dp),
                                        )
                                        .clickable { nodeMenuExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = selectedNode?.title ?: "Choose a node",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                    )
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = "Choose topic node",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                DropdownMenu(
                                    expanded = nodeMenuExpanded,
                                    onDismissRequest = { nodeMenuExpanded = false },
                                    modifier = Modifier.background(ForestCard),
                                ) {
                                    nodes.forEach { node ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = node.title,
                                                    color = if (node.id == selectedNodeId) NeonCyan else TextPrimary,
                                                    fontSize = 11.sp,
                                                )
                                            },
                                            onClick = {
                                                selectedNodeId = node.id
                                                nodeMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        if (error != null) {
                            Text(
                                text = error.orEmpty(),
                                color = RoseSoft,
                                fontSize = 9.sp,
                            )
                        }

                        GradientButton(
                            onClick = {
                                if (url.isBlank() || selectedNodeId.isBlank()) {
                                    error = "Paste a video link first."
                                } else {
                                    error = null
                                    scope.launch {
                                        isExtracting = true
                                        try {
                                            onExtract(url.trim(), selectedNodeId)
                                            onImported()
                                        } catch (t: Exception) {
                                            error = t.message ?: "Could not import this video."
                                        } finally {
                                            isExtracting = false
                                        }
                                    }
                                }
                            },
                            enabled = !isExtracting && url.isNotBlank() && selectedNodeId.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp),
                        ) {
                            if (isExtracting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = ForestBg,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    Icons.Filled.AutoFixHigh,
                                    contentDescription = null,
                                    tint = ForestBg,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = if (isExtracting) {
                                    "Extracting Lesson..."
                                } else {
                                    "Extract & Add to Map"
                                },
                                color = ForestBg,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }

                    GlassIconButton(
                        contentDescription = "Close import dialog",
                        onClick = { if (!isExtracting) onDismiss() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp),
                        iconTint = TextMuted,
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}
