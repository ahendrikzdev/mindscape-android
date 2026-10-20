package com.mindscape.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindscape.app.data.MindscapeUiState
import com.mindscape.app.ui.components.GlassSurface
import com.mindscape.app.ui.components.GradientButton
import com.mindscape.app.ui.components.MonoLabel
import com.mindscape.app.ui.components.FeedVideoCard
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.TextMuted
import com.mindscape.app.ui.theme.TextPrimary

@Composable
fun WatchScreen(
    state: MindscapeUiState,
    onOpenLesson: (String) -> Unit,
    onShowInMap: (String) -> Unit,
    onDelete: (String) -> Unit,
    onCollectVideo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.videoFeed.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                contentPadding = PaddingValues(24.dp),
                modifier = Modifier.fillMaxSize(0.82f),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(34.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Your watch feed is empty",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    MonoLabel(
                        text = "COLLECT A VIDEO TO BEGIN",
                        color = TextMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                    )
                    Spacer(Modifier.height(18.dp))
                    GradientButton(onClick = onCollectVideo) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = ForestBg,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "Collect Video",
                            color = ForestBg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { state.videoFeed.size })

    LaunchedEffect(state.videoFeed.size) {
        if (state.videoFeed.isNotEmpty() && pagerState.currentPage > state.videoFeed.lastIndex) {
            pagerState.scrollToPage(state.videoFeed.lastIndex)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 60.dp, bottom = 88.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        pageSpacing = 12.dp,
        beyondViewportPageCount = 1,
        key = { page -> state.videoFeed.getOrNull(page)?.id ?: "page-$page" },
    ) { page ->
        val artifact = state.videoFeed.getOrNull(page) ?: return@VerticalPager
        FeedVideoCard(
            artifact = artifact,
            onOpenLesson = { onOpenLesson(artifact.id) },
            onShowInMap = { onShowInMap(artifact.id) },
            onDelete = { onDelete(artifact.id) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
