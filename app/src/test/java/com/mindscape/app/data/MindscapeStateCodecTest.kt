package com.mindscape.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MindscapeStateCodecTest {

    private fun sampleState(): MindscapeUiState = MindscapeUiState(
        domains = listOf(
            Domain(
                id = "quantum",
                label = "Physics & Cosmos",
                nodes = listOf(
                    SkillNode(
                        id = "q-entanglement",
                        title = "Entanglement",
                        status = NodeStatus.UNLOCKED,
                        parentId = null,
                        xPercent = 25f,
                        yPercent = 48f,
                        icon = NodeIcon.LINK,
                        xp = 350,
                        requiredVideos = 2,
                    ),
                ),
            ),
        ),
        activeDomainId = "quantum",
        selectedNodeId = "q-entanglement",
        activeArtifactId = "art-test",
        videoFeed = listOf(
            VideoArtifact(
                id = "art-test",
                title = "Imported lesson",
                author = "@test",
                nodeId = "q-entanglement",
                tested = false,
                thumbnailKey = ThumbnailCatalog.ENTANGLEMENT,
                caption = "Imported from youtube.com",
                summary = "A generated summary.",
                quiz = Quiz(
                    question = "What is measured?",
                    options = listOf("Spin", "Color", "Mass", "Charge"),
                    correctIndex = 0,
                    explanation = "Spin is measured.",
                ),
                keyPoints = listOf("Point one", "Point two"),
                sourceUrl = "https://www.youtube.com/watch?v=ZuvK-od647c",
                videoId = "ZuvK-od647c",
                thumbnailUrl = "https://i.ytimg.com/vi/ZuvK-od647c/hqdefault.jpg",
                transcript = "A transcript",
                watched = true,
                positionMs = 42_000L,
                durationMs = 60_000L,
            ),
        ),
        userXp = 950,
        answers = mapOf("art-test" to 0),
        learnTab = LearnTab.QUIZ,
    )

    @Test
    fun `round trips the full learning state`() {
        val original = sampleState()

        val restored = MindscapeStateCodec.decode(MindscapeStateCodec.encode(original))

        assertEquals(original, restored)
    }

    @Test
    fun `corrupt json is discarded`() {
        assertNull(MindscapeStateCodec.decode("{not valid json"))
    }
}
