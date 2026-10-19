package com.mindscape.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindscape.app.MindscapeViewModel
import com.mindscape.app.data.LearnTab
import com.mindscape.app.ui.components.AtlasBackground
import com.mindscape.app.ui.components.GlassPill
import com.mindscape.app.ui.components.GradientButton
import com.mindscape.app.ui.components.MonoLabel
import com.mindscape.app.ui.screens.ImportDialog
import com.mindscape.app.ui.screens.LearnScreen
import com.mindscape.app.ui.screens.MapScreen
import com.mindscape.app.ui.screens.WatchScreen
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.ForestBorder
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.NeonLime
import com.mindscape.app.ui.theme.SpaceGrotesk
import com.mindscape.app.ui.theme.TextMuted
import com.mindscape.app.ui.theme.TextPrimary
import kotlinx.coroutines.launch

private const val LEARN_PAGE = 0
private const val WATCH_PAGE = 1
private const val MAP_PAGE = 2

@Composable
fun MindscapeApp(viewModel: MindscapeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = WATCH_PAGE, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var showImport by rememberSaveable { mutableStateOf(false) }

    fun openPanel(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    BackHandler(enabled = showImport || pagerState.currentPage != WATCH_PAGE) {
        if (showImport) {
            showImport = false
        } else {
            openPanel(WATCH_PAGE)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestBg),
    ) {
        AtlasBackground(Modifier.matchParentSize())
        val contentWidth = minOf(maxWidth, 460.dp)

        Box(
            modifier = Modifier
                .width(contentWidth)
                .fillMaxHeight()
                .align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false,
                    key = { it },
                ) { page ->
                    when (page) {
                        LEARN_PAGE -> LearnScreen(
                            state = state,
                            onClose = { openPanel(WATCH_PAGE) },
                            onSelectTab = viewModel::setLearnTab,
                            onAnswer = viewModel::answerQuestion,
                            modifier = Modifier.fillMaxSize(),
                        )

                        WATCH_PAGE -> WatchScreen(
                            state = state,
                            onOpenLesson = { videoId ->
                                viewModel.selectArtifact(videoId)
                                viewModel.setLearnTab(LearnTab.LESSON)
                                openPanel(LEARN_PAGE)
                            },
                            onShowInMap = { videoId ->
                                val video = state.videoFeed.firstOrNull { it.id == videoId }
                                if (video != null) {
                                    viewModel.selectNode(video.nodeId)
                                    viewModel.selectArtifact(videoId)
                                }
                                openPanel(MAP_PAGE)
                            },
                            onDelete = viewModel::removeVideo,
                            onCollectVideo = { showImport = true },
                            modifier = Modifier.fillMaxSize(),
                        )

                        MAP_PAGE -> MapScreen(
                            state = state,
                            onDomainSelected = viewModel::selectDomain,
                            onNodeSelected = viewModel::selectNode,
                            onArtifactSelected = viewModel::selectArtifact,
                            onCollectVideo = { showImport = true },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Header(
                    currentPage = pagerState.currentPage,
                    onGoLearn = { openPanel(LEARN_PAGE) },
                    onGoWatch = { openPanel(WATCH_PAGE) },
                    onGoMap = { openPanel(MAP_PAGE) },
                    modifier = Modifier.align(Alignment.TopCenter),
                )

                Footer(
                    currentPage = pagerState.currentPage,
                    onGoLearn = { openPanel(LEARN_PAGE) },
                    onGoWatch = { openPanel(WATCH_PAGE) },
                    onGoMap = { openPanel(MAP_PAGE) },
                    onCollectVideo = { showImport = true },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }

    if (showImport) {
        ImportDialog(
            domains = state.domains,
            activeDomainId = state.activeDomainId,
            onDismiss = { showImport = false },
            onExtract = { url, nodeId -> viewModel.importVideo(url, nodeId) },
            onImported = {
                showImport = false
                viewModel.setLearnTab(LearnTab.LESSON)
                openPanel(LEARN_PAGE)
            },
        )
    }
}

@Composable
private fun Header(
    currentPage: Int,
    onGoLearn: () -> Unit,
    onGoWatch: () -> Unit,
    onGoMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ForestBg,
                            ForestBg.copy(alpha = 0.9f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassPill(
                modifier = Modifier.clickable(onClick = onGoLearn),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(10.dp),
                )
                Spacer(Modifier.size(5.dp))
                Text(
                    text = "Learn",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            GlassPill(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp)) {
                PageDot(
                    selected = currentPage == LEARN_PAGE,
                    onClick = onGoLearn,
                )
                PageDot(
                    selected = currentPage == WATCH_PAGE,
                    onClick = onGoWatch,
                )
                PageDot(
                    selected = currentPage == MAP_PAGE,
                    onClick = onGoMap,
                )
            }

            GlassPill(
                modifier = Modifier.clickable(onClick = onGoMap),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Map",
                    color = Color(0xFFA7F3D0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.size(5.dp))
                Icon(
                    Icons.Filled.Explore,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
    }
}

@Composable
private fun PageDot(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val width by animateDpAsState(
        targetValue = if (selected) 20.dp else 8.dp,
        animationSpec = tween(durationMillis = 250),
        label = "pageDot",
    )
    Box(
        modifier = Modifier
            .size(28.dp, 24.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(width, 8.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) NeonLime else ForestBorder),
        )
    }
}

@Composable
private fun Footer(
    currentPage: Int,
    onGoLearn: () -> Unit,
    onGoWatch: () -> Unit,
    onGoMap: () -> Unit,
    onCollectVideo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        GlassPill(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FooterItem(
                    icon = Icons.Filled.School,
                    label = "Learn",
                    active = currentPage == LEARN_PAGE,
                    onClick = onGoLearn,
                )
                FooterItem(
                    icon = Icons.Filled.PlayCircle,
                    label = "Watch",
                    active = currentPage == WATCH_PAGE,
                    onClick = onGoWatch,
                )

                Box(Modifier.padding(horizontal = 8.dp)) {
                    GradientButton(
                        onClick = onCollectVideo,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(38.dp)
                            .offset(y = (-10).dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Collect video",
                            tint = ForestBg,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                FooterItem(
                    icon = Icons.Filled.Explore,
                    label = "Map",
                    active = currentPage == MAP_PAGE,
                    onClick = onGoMap,
                )
            }
        }
    }
}

@Composable
private fun FooterItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (active) NeonLime else TextMuted.copy(alpha = 0.7f)
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(13.dp),
        )
        Spacer(Modifier.size(2.dp))
        Text(
            text = label.uppercase(),
            color = tint,
            fontSize = 8.sp,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}
