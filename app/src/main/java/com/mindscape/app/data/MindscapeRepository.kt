package com.mindscape.app.data

import com.mindscape.app.R

/**
 * Seed data mirroring the original Mindscape HTML prototype.
 *
 * Drawable thumbnails are bundled with the APK, so the app never needs network access.
 */
object MindscapeRepository {

    const val INITIAL_XP = 850

    val importThumbnails: List<Int> = listOf(
        R.drawable.thumb_superposition,
        R.drawable.thumb_double_slit,
        R.drawable.thumb_entanglement,
        R.drawable.thumb_bell_test,
        R.drawable.thumb_qubits,
    )

    fun domains(): List<Domain> = listOf(
        Domain(
            id = "quantum",
            label = "Physics & Cosmos",
            nodes = listOf(
                SkillNode(
                    id = "q-mechanics-101",
                    title = "Superposition",
                    status = NodeStatus.MASTERED,
                    parentId = null,
                    xPercent = 50f,
                    yPercent = 15f,
                    icon = NodeIcon.ATOM,
                    xp = 200,
                    requiredVideos = 2,
                ),
                SkillNode(
                    id = "q-entanglement",
                    title = "Entanglement",
                    status = NodeStatus.UNLOCKED,
                    parentId = "q-mechanics-101",
                    xPercent = 25f,
                    yPercent = 48f,
                    icon = NodeIcon.LINK,
                    xp = 350,
                    requiredVideos = 2,
                ),
                SkillNode(
                    id = "q-computing",
                    title = "Qubit Basics",
                    status = NodeStatus.UNLOCKED,
                    parentId = "q-mechanics-101",
                    xPercent = 75f,
                    yPercent = 48f,
                    icon = NodeIcon.MICROCHIP,
                    xp = 400,
                    requiredVideos = 2,
                ),
                SkillNode(
                    id = "q-cryptography",
                    title = "Quantum Keys",
                    status = NodeStatus.LOCKED,
                    parentId = "q-entanglement",
                    xPercent = 25f,
                    yPercent = 82f,
                    icon = NodeIcon.KEY,
                    xp = 550,
                    requiredVideos = 3,
                ),
                SkillNode(
                    id = "q-teleportation",
                    title = "Teleportation",
                    status = NodeStatus.LOCKED,
                    parentId = "q-computing",
                    xPercent = 75f,
                    yPercent = 82f,
                    icon = NodeIcon.BOLT,
                    xp = 600,
                    requiredVideos = 3,
                ),
            ),
        ),
        Domain(
            id = "history",
            label = "Ancient World",
            nodes = listOf(
                SkillNode(
                    id = "h-mesopotamia",
                    title = "Cuneiform",
                    status = NodeStatus.MASTERED,
                    parentId = null,
                    xPercent = 50f,
                    yPercent = 15f,
                    icon = NodeIcon.SCROLL,
                    xp = 250,
                    requiredVideos = 2,
                ),
                SkillNode(
                    id = "h-egypt-pyramids",
                    title = "Pyramid Tech",
                    status = NodeStatus.UNLOCKED,
                    parentId = "h-mesopotamia",
                    xPercent = 30f,
                    yPercent = 50f,
                    icon = NodeIcon.MONUMENT,
                    xp = 350,
                    requiredVideos = 2,
                ),
                SkillNode(
                    id = "h-rosetta-stone",
                    title = "Rosetta Stone",
                    status = NodeStatus.LOCKED,
                    parentId = "h-egypt-pyramids",
                    xPercent = 30f,
                    yPercent = 82f,
                    icon = NodeIcon.LANGUAGE,
                    xp = 500,
                    requiredVideos = 2,
                ),
            ),
        ),
    )

    fun videos(): List<VideoArtifact> = listOf(
        VideoArtifact(
            id = "art-1",
            title = "Schrödinger Cat & Wave States",
            author = "@science_daily",
            nodeId = "q-mechanics-101",
            tested = true,
            thumbnailRes = R.drawable.thumb_superposition,
            caption = "Until a state is measured, quantum particles exist in multiple possibilities simultaneously.",
            summary = "Superposition is the core quantum principle where particles exist in multiple states at once until measured.",
            quiz = Quiz(
                question = "When does a quantum system collapse into a single clear state?",
                options = listOf("When it gets warmer", "Upon observation or measurement", "After 10 seconds automatically"),
                correctIndex = 1,
            ),
        ),
        VideoArtifact(
            id = "art-2",
            title = "Double Slit Light Experiment",
            author = "@lab_shorts",
            nodeId = "q-mechanics-101",
            tested = true,
            thumbnailRes = R.drawable.thumb_double_slit,
            caption = "Firing photons through two slits creates wave patterns even when sent one by one.",
            summary = "Demonstrates wave-particle duality. Individual particles interfere with their own probability waves.",
            quiz = Quiz(
                question = "What pattern appears on screen when single photons pass unobserved through two slits?",
                options = listOf("Two straight lines", "An interference pattern of bright and dark bands", "A single dot"),
                correctIndex = 1,
            ),
        ),
        VideoArtifact(
            id = "art-3",
            title = "Quantum Entanglement Explained",
            author = "@cosmos_bits",
            nodeId = "q-entanglement",
            tested = true,
            thumbnailRes = R.drawable.thumb_entanglement,
            caption = "Two entangled particles instantaneously mirror changes in state regardless of distance.",
            summary = "Entanglement links quantum states across vast distances without physical connection.",
            quiz = Quiz(
                question = "What happens to particle B when entangled particle A is measured?",
                options = listOf("Nothing changes", "Particle B state is determined instantly", "Particle B disappears"),
                correctIndex = 1,
            ),
        ),
        VideoArtifact(
            id = "art-4",
            title = "Bell Test Experiments",
            author = "@physics_lab",
            nodeId = "q-entanglement",
            tested = false,
            thumbnailRes = R.drawable.thumb_bell_test,
            caption = "Proving that local hidden variables cannot account for quantum entanglement correlations.",
            summary = "John Bell formulated equations proving quantum interactions cannot be explained by classical physics.",
            quiz = Quiz(
                question = "What did the violation of Bell inequalities prove?",
                options = listOf("Gravity is fake", "Quantum correlations are non-local", "Light speed varies"),
                correctIndex = 1,
            ),
        ),
        VideoArtifact(
            id = "art-5",
            title = "Qubits vs Classical Bits",
            author = "@tech_today",
            nodeId = "q-computing",
            tested = false,
            thumbnailRes = R.drawable.thumb_qubits,
            caption = "Classical bits store 0 or 1. Quantum qubits harness superposition to evaluate parallel paths.",
            summary = "Qubits scale exponentially, giving quantum computers huge speed advantages for complex math.",
            quiz = Quiz(
                question = "How do N entangled qubits scale computational states?",
                options = listOf("Linearly (N)", "Exponentially (2^N states)", "Inversely"),
                correctIndex = 1,
            ),
        ),
    )
}
