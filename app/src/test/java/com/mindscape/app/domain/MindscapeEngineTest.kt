package com.mindscape.app.domain

import com.mindscape.app.data.Domain
import com.mindscape.app.data.LearnTab
import com.mindscape.app.data.MindscapeUiState
import com.mindscape.app.data.NodeIcon
import com.mindscape.app.data.NodeStatus
import com.mindscape.app.data.Quiz
import com.mindscape.app.data.SkillNode
import com.mindscape.app.data.VideoArtifact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MindscapeEngineTest {

    private val quiz = Quiz(
        question = "When does the state collapse?",
        options = listOf("Never", "When measured", "When heated"),
        correctIndex = 1,
    )

    private fun artifact(
        id: String,
        nodeId: String = "child",
        tested: Boolean = false,
    ) = VideoArtifact(
        id = id,
        title = "Video $id",
        author = "@test",
        nodeId = nodeId,
        tested = tested,
        thumbnailRes = 0,
        caption = "Caption",
        summary = "Summary",
        quiz = quiz,
    )

    private fun state(): MindscapeUiState = MindscapeUiState(
        domains = listOf(
            Domain(
                id = "quantum",
                label = "Physics & Cosmos",
                nodes = listOf(
                    SkillNode(
                        id = "root",
                        title = "Root",
                        status = NodeStatus.MASTERED,
                        parentId = null,
                        xPercent = 50f,
                        yPercent = 15f,
                        icon = NodeIcon.ATOM,
                        xp = 200,
                        requiredVideos = 2,
                    ),
                    SkillNode(
                        id = "child",
                        title = "Child",
                        status = NodeStatus.UNLOCKED,
                        parentId = "root",
                        xPercent = 25f,
                        yPercent = 48f,
                        icon = NodeIcon.LINK,
                        xp = 350,
                        requiredVideos = 2,
                    ),
                    SkillNode(
                        id = "grandchild",
                        title = "Grandchild",
                        status = NodeStatus.LOCKED,
                        parentId = "child",
                        xPercent = 25f,
                        yPercent = 82f,
                        icon = NodeIcon.KEY,
                        xp = 550,
                        requiredVideos = 2,
                    ),
                ),
            ),
        ),
        activeDomainId = "quantum",
        selectedNodeId = "child",
        activeArtifactId = "v1",
        videoFeed = listOf(artifact("v1"), artifact("v2")),
        userXp = 850,
    )

    @Test
    fun `correct answer marks video tested and awards xp`() {
        val result = MindscapeEngine.answerQuestion(state(), "v1", 1)

        assertTrue(result.videoFeed.first { it.id == "v1" }.tested)
        assertEquals(950, result.userXp)
        assertEquals(1, result.answers["v1"])
    }

    @Test
    fun `correct answer awards xp only once`() {
        val once = MindscapeEngine.answerQuestion(state(), "v1", 1)
        val twice = MindscapeEngine.answerQuestion(once, "v1", 1)

        assertEquals(950, once.userXp)
        assertEquals(950, twice.userXp)
    }

    @Test
    fun `wrong answer records choice but awards nothing`() {
        val result = MindscapeEngine.answerQuestion(state(), "v1", 0)

        assertFalse(result.videoFeed.first { it.id == "v1" }.tested)
        assertEquals(850, result.userXp)
        assertEquals(0, result.answers["v1"])
    }

    @Test
    fun `mastering a node unlocks its children`() {
        val first = MindscapeEngine.answerQuestion(state(), "v1", 1)
        val result = MindscapeEngine.answerQuestion(first, "v2", 1)

        val domain = result.domains.first()
        assertEquals(NodeStatus.MASTERED, domain.nodes.first { it.id == "child" }.status)
        assertEquals(NodeStatus.UNLOCKED, domain.nodes.first { it.id == "grandchild" }.status)
        assertEquals(1050, result.userXp)
    }

    @Test
    fun `percent mastered reflects mastered nodes`() {
        val progress = MindscapeEngine.percentMastered(state().activeDomain)

        assertEquals(1f / 3f, progress, 0.001f)
    }

    @Test
    fun `selecting a realm chooses its first node and video`() {
        val history = Domain(
            id = "history",
            label = "Ancient World",
            nodes = listOf(
                SkillNode(
                    id = "h1",
                    title = "Cuneiform",
                    status = NodeStatus.UNLOCKED,
                    parentId = null,
                    xPercent = 50f,
                    yPercent = 15f,
                    icon = NodeIcon.SCROLL,
                    xp = 100,
                    requiredVideos = 1,
                ),
            ),
        )
        val withHistory = state().copy(
            domains = state().domains + history,
            videoFeed = state().videoFeed + artifact("h-video", nodeId = "h1"),
        )

        val result = MindscapeEngine.selectDomain(withHistory, "history")

        assertEquals("history", result.activeDomainId)
        assertEquals("h1", result.selectedNodeId)
        assertEquals("h-video", result.activeArtifactId)
    }

    @Test
    fun `removing the active video falls back to another video`() {
        val result = MindscapeEngine.removeVideo(state(), "v1")

        assertEquals("v2", result.activeArtifactId)
        assertNull(result.answers["v1"])

        val empty = MindscapeEngine.removeVideo(result, "v2")
        assertNull(empty.activeArtifactId)
        assertTrue(empty.videoFeed.isEmpty())
    }

    @Test
    fun `adding a video selects it and switches to the lesson tab`() {
        val imported = artifact("imported", nodeId = "child")

        val result = MindscapeEngine.addVideo(state(), imported)

        assertEquals("imported", result.videoFeed.first().id)
        assertEquals("imported", result.activeArtifactId)
        assertEquals("child", result.selectedNodeId)
        assertEquals(LearnTab.LESSON, result.learnTab)
    }

    @Test
    fun `quiz progress is pending until tested`() {
        assertEquals(0.2f, MindscapeEngine.quizProgress(artifact("v1")), 0.001f)
        assertEquals(1f, MindscapeEngine.quizProgress(artifact("v1", tested = true)), 0.001f)
        assertEquals(0.2f, MindscapeEngine.quizProgress(null), 0.001f)
    }
}
