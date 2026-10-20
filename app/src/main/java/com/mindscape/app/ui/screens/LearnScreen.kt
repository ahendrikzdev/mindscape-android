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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindscape.app.data.LearnTab
import com.mindscape.app.data.MindscapeUiState
import com.mindscape.app.data.VideoArtifact
import com.mindscape.app.domain.MindscapeEngine
import com.mindscape.app.ui.components.GlassIconButton
import com.mindscape.app.ui.components.GlassPill
import com.mindscape.app.ui.components.GlassSurface
import com.mindscape.app.ui.components.MonoLabel
import com.mindscape.app.ui.theme.ForestBg
import com.mindscape.app.ui.theme.ForestBorder
import com.mindscape.app.ui.theme.NeonCyan
import com.mindscape.app.ui.theme.NeonLime
import com.mindscape.app.ui.theme.Rose
import com.mindscape.app.ui.theme.RoseSoft
import com.mindscape.app.ui.theme.TextMuted
import com.mindscape.app.ui.theme.TextPrimary

@Composable
fun LearnScreen(
    state: MindscapeUiState,
    onClose: () -> Unit,
    onSelectTab: (LearnTab) -> Unit,
    onAnswer: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artifact = state.activeArtifact
    val node = artifact?.let { video ->
        state.activeDomain.nodes.firstOrNull { it.id == video.nodeId }
    } ?: state.selectedNode

    val statusLabel = when {
        artifact == null -> "No video"
        artifact.tested -> "100% Done"
        artifact.quiz == null -> "Watch only"
        else -> "Quiz Pending"
    }
    val progress = if (artifact == null) 0f else MindscapeEngine.quizProgress(artifact)
    val title = artifact?.title
        ?: node?.let { "${it.title} (Unmapped)" }
        ?: "Select a video to start learning"
    val summary = artifact?.summary
        ?: "Import video links into this topic node to generate quick lessons and unlock quizzes."

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
                    text = "MINDSCAPE STUDY LAB",
                    color = NeonCyan,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Lesson & Quiz",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            GlassIconButton(
                contentDescription = "Close study panel",
                onClick = onClose,
                iconTint = NeonCyan,
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                )
            }
        }

        GlassSurface(
            highlighted = true,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(10.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        MonoLabel("ACTIVE LESSON", fontSize = 9.sp, letterSpacing = 1.5.sp)
                    }
                    GlassPill(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        MonoLabel(
                            text = "EXPLORER",
                            color = NeonCyan,
                            fontSize = 7.sp,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                MonoLabel(
                    text = "Topic: ${node?.title ?: "Unmapped"}",
                    color = TextMuted.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp,
                )
            }
        }

        GlassSurface(
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(4.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                LearnTabButton(
                    label = "1. Lesson",
                    selected = state.learnTab == LearnTab.LESSON,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTab(LearnTab.LESSON) },
                )
                LearnTabButton(
                    label = "2. Quiz",
                    selected = state.learnTab == LearnTab.QUIZ,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectTab(LearnTab.QUIZ) },
                )
            }
        }

        if (state.learnTab == LearnTab.LESSON) {
            LessonTab(
                artifact = artifact,
                summary = summary,
                progress = progress,
                statusLabel = statusLabel,
            )
        } else {
            QuizTab(
                artifact = artifact,
                answers = state.answers,
                onAnswer = onAnswer,
            )
        }
    }
}

@Composable
private fun LessonTab(
    artifact: VideoArtifact?,
    summary: String,
    progress: Float,
    statusLabel: String,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "Key Takeaway",
                        color = Color(0xFFCFFAFE),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                GlassPill(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                    MonoLabel(
                        text = artifact?.author ?: "AI SUMMARY",
                        color = NeonLime,
                        fontSize = 8.sp,
                        letterSpacing = 0.3.sp,
                    )
                }
            }
            Text(
                text = summary,
                color = TextPrimary.copy(alpha = 0.8f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }
    }

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Video Quiz Status",
                    color = Color(0xFFA7F3D0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                MonoLabel(text = statusLabel, color = NeonLime, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ForestBg)
                    .border(1.dp, ForestBorder.copy(alpha = 0.8f), RoundedCornerShape(50))
                    .padding(2.dp),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(NeonLime),
                )
            }
        }
    }
}

@Composable
private fun LearnTabButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                1.dp,
                if (selected) NeonCyan.copy(alpha = 0.4f) else Color.Transparent,
                shape,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) NeonCyan else TextMuted.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QuizTab(
    artifact: VideoArtifact?,
    answers: Map<String, Int>,
    onAnswer: (String, Int) -> Unit,
) {
    GlassSurface(
        highlighted = true,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Quiz,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "Quick Quiz",
                        color = NeonLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(NeonLime)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "+100 POINTS",
                        color = ForestBg,
                        fontSize = 8.sp,
                        fontFamily = com.mindscape.app.ui.theme.SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            val quiz = artifact?.quiz
            Text(
                text = quiz?.question ?: if (artifact == null) {
                    "Select a video to generate a quiz question..."
                } else {
                    "This video is watch-only. Re-import it with a transcript to generate a quiz."
                },
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 17.sp,
            )

            if (artifact == null) {
                Text(
                    text = "Open a video from the Watch feed to unlock this quiz.",
                    color = TextMuted.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                )
            } else if (quiz == null) {
                Text(
                    text = "No quiz is available for this video yet.",
                    color = TextMuted.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                )
            } else {
                val selectedAnswer = answers[artifact.id]
                quiz.options.forEachIndexed { index, option ->
                    QuizOptionRow(
                        text = option,
                        isSelected = selectedAnswer == index,
                        isCorrectOption = index == quiz.correctIndex,
                        tested = artifact.tested,
                        enabled = selectedAnswer == null && !artifact.tested,
                        onClick = { onAnswer(artifact.id, index) },
                    )
                }
                QuizFeedback(
                    selectedAnswer = selectedAnswer,
                    correctIndex = quiz.correctIndex,
                    tested = artifact.tested,
                )
            }
        }
    }
}

@Composable
private fun QuizOptionRow(
    text: String,
    isSelected: Boolean,
    isCorrectOption: Boolean,
    tested: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val showWrong = isSelected && !isCorrectOption
    val showCorrect = (isSelected && isCorrectOption) || (tested && isCorrectOption)
    val shape = RoundedCornerShape(12.dp)

    val background = when {
        showWrong -> Rose.copy(alpha = 0.2f)
        showCorrect -> NeonLime.copy(alpha = 0.2f)
        else -> ForestBg
    }
    val borderColor = when {
        showWrong -> Rose
        showCorrect -> NeonLime
        else -> ForestBorder.copy(alpha = 0.8f)
    }
    val contentColor = when {
        showWrong -> RoseSoft
        showCorrect -> NeonLime
        else -> TextPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = contentColor,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = if (showWrong || showCorrect) FontWeight.Bold else FontWeight.Normal,
        )
        Spacer(Modifier.size(8.dp))
        when {
            showWrong -> Icon(
                Icons.Filled.Cancel,
                contentDescription = null,
                tint = RoseSoft,
                modifier = Modifier.size(12.dp),
            )

            showCorrect -> Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = NeonLime,
                modifier = Modifier.size(12.dp),
            )

            else -> Box(
                Modifier
                    .size(10.dp)
                    .border(1.dp, NeonCyan, RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
private fun QuizFeedback(
    selectedAnswer: Int?,
    correctIndex: Int,
    tested: Boolean,
) {
    if (selectedAnswer == null && !tested) return

    val isCorrect = selectedAnswer == null || selectedAnswer == correctIndex
    val color = if (isCorrect) NeonLime else RoseSoft
    val background = if (isCorrect) NeonLime.copy(alpha = 0.2f) else Rose.copy(alpha = 0.2f)
    val border = if (isCorrect) NeonLime.copy(alpha = 0.5f) else Rose.copy(alpha = 0.5f)
    val message = when {
        selectedAnswer == null -> "Quiz completed."
        selectedAnswer == correctIndex -> "Correct! (+100 Points)"
        else -> "Not quite. Review the lesson summary!"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = message,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
