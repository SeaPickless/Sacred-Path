package com.sacredpath.app.ui.screens.quiz

import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.repository.QuizQuestion
import com.sacredpath.app.data.repository.QuizRepository
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke

// ── QuizSessionViewModel ──────────────────────────────────────────────────────
// Calls quizRepo.getRandomQuestions() at runtime — random each session

data class QuizSessionState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int             = 0,
    val selectedOption: Int?          = null,
    val score: Int                    = 0,
    val finished: Boolean             = false,
    val xpEarned: Int                 = 0,
    val loading: Boolean              = true
)

@HiltViewModel
class QuizSessionViewModel @Inject constructor(
    private val quizRepo: QuizRepository,
    private val settingsRepo: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizSessionState())
    val state: StateFlow<QuizSessionState> = _state.asStateFlow()

    fun startQuiz(category: String, difficulty: String, mode: String) {
        viewModelScope.launch {
            // Runtime random selection — no pre-determined order
            val questions = quizRepo.getRandomQuestions(category, difficulty, count = 10)
            _state.value = QuizSessionState(questions = questions, loading = false)
        }
    }

    fun selectOption(optionIndex: Int) {
        val s = _state.value
        if (s.selectedOption != null) return  // already answered
        val isCorrect = optionIndex == s.questions[s.currentIndex].correctIndex
        _state.value = s.copy(
            selectedOption = optionIndex,
            score = if (isCorrect) s.score + 1 else s.score
        )
    }

    fun nextQuestion(category: String, difficulty: String, mode: String) {
        val s = _state.value
        val nextIdx = s.currentIndex + 1
        if (nextIdx >= s.questions.size) {
            // Quiz finished — calculate XP
            val perfect  = s.score == s.questions.size
            val xp       = (s.score * 10) + (if (perfect) 20 else 0)
            _state.value = s.copy(finished = true, xpEarned = xp)
            viewModelScope.launch {
                val profileId = settingsRepo.settingsFlow.first().activeProfileId
                quizRepo.saveRecord(profileId, category, difficulty, mode, s.score, s.questions.size, xp)
            }
        } else {
            _state.value = s.copy(currentIndex = nextIdx, selectedOption = null)
        }
    }
}

// ── QuizHomeScreen ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHomeScreen(navController: NavController) {
    var selectedDifficulty by remember { mutableStateOf("medium") }

    val categories = listOf(
        Triple("oldTestament", "Old Testament",    "📜"),
        Triple("newTestament", "New Testament",    "✝️"),
        Triple("characters",   "Bible Characters", "👤"),
        Triple("prophecy",     "Prophecy",         "🔮"),
        Triple("theology",     "Theology",         "🙏"),
        Triple("mixed",        "Mixed",            "📖"),
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Quizzes") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Challenge card
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    navController.navigate("quiz_session/mixed/$selectedDifficulty/daily")
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape  = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("🏆 Daily Challenge",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Text("Random questions every session",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.ArrowForward, null,
                        tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Difficulty selector
            Text("Difficulty",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("easy", "medium", "hard").forEach { d ->
                    FilterChip(
                        selected = selectedDifficulty == d,
                        onClick  = { selectedDifficulty = d },
                        label    = { Text(d.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Categories grid
            Text("Choose a Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (id, label, emoji) ->
                            Card(
                                modifier = Modifier.weight(1f).clickable {
                                    navController.navigate("quiz_session/$id/$selectedDifficulty/casual")
                                },
                                shape     = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(emoji, fontSize = 30.sp)
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                    Text("Randomized each session",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Study mode
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    navController.navigate("quiz_session/mixed/mixed/study")
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.School, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Study Mode", style = MaterialTheme.typography.labelLarge)
                        Text("No timer · explanations after each answer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Icon(Icons.Default.ChevronRight, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        }
    }
}

// ── QuizSessionScreen ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSessionScreen(
    navController: NavController,
    category: String,
    difficulty: String,
    mode: String,
    viewModel: QuizSessionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(category, difficulty, mode) {
        viewModel.startQuiz(category, difficulty, mode)
    }

    // Navigate to result when finished
    LaunchedEffect(state.finished) {
        if (state.finished && state.questions.isNotEmpty()) {
            navController.navigate(
                "quiz_result/${state.score}/${state.questions.size}/${state.xpEarned}/$category/$difficulty/$mode"
            ) {
                popUpTo("quiz_session/$category/$difficulty/$mode") { inclusive = true }
            }
        }
    }

    if (state.loading || state.questions.isEmpty()) {
        com.sacredpath.app.ui.components.FullScreenLoading("Picking random questions…")
        return
    }

    val current  = state.questions[state.currentIndex]
    val progress = (state.currentIndex).toFloat() / state.questions.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                actions = {
                    Text(
                        "${state.currentIndex + 1} / ${state.questions.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Difficulty badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (current.difficulty) {
                    "easy"   -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    "hard"   -> MaterialTheme.colorScheme.errorContainer
                    else     -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = current.difficulty.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = when (current.difficulty) {
                        "easy" -> Color(0xFF2E7D32)
                        "hard" -> MaterialTheme.colorScheme.error
                        else   -> MaterialTheme.colorScheme.secondary
                    }
                )
            }

            // Question text
            Text(
                text  = current.text,
                style = MaterialTheme.typography.displaySmall,
                lineHeight = 34.sp
            )

            // Options
            current.options.forEachIndexed { idx, option ->
                val isAnswered = state.selectedOption != null
                val isCorrect  = idx == current.correctIndex
                val isChosen   = idx == state.selectedOption

                val bgColor = when {
                    !isAnswered                  -> MaterialTheme.colorScheme.surface
                    isCorrect                    -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    isChosen && !isCorrect       -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else                         -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    !isAnswered                  -> MaterialTheme.colorScheme.outline
                    isCorrect                    -> Color(0xFF4CAF50)
                    isChosen && !isCorrect       -> MaterialTheme.colorScheme.error
                    else                         -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isAnswered) { viewModel.selectOption(idx) },
                    shape     = RoundedCornerShape(12.dp),
                    border    = BorderStroke(1.5.dp, borderColor),
                    colors    = CardDefaults.cardColors(containerColor = bgColor),
                    elevation = CardDefaults.cardElevation(if (!isAnswered) 1.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Letter badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = listOf("A","B","C","D")[idx],
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Text(option, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (isAnswered && isCorrect)
                            Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                        if (isAnswered && isChosen && !isCorrect)
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Explanation (shown after answering)
            if (state.selectedOption != null) {
                Card(
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Explanation",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(current.explanation, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Button(
                    onClick  = { viewModel.nextQuestion(category, difficulty, mode) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.currentIndex + 1 >= state.questions.size) "See Results" else "Next Question")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ── QuizResultScreen ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    navController: NavController,
    score: Int,
    total: Int,
    xpEarned: Int,
    category: String,
    difficulty: String,
    mode: String
) {
    val pct   = if (total > 0) score * 100 / total else 0
    val emoji = when { score == total -> "🏆"; pct >= 80 -> "🌟"; pct >= 60 -> "✅"; else -> "📖" }
    val headline = when { score == total -> "Perfect Score!"; pct >= 80 -> "Excellent!"; pct >= 60 -> "Well Done!"; else -> "Keep Studying!" }

    val scaleAnim by animateFloatAsState(
        targetValue    = 1f,
        animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label          = "result_scale"
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Results") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Score circle
            Card(
                shape  = RoundedCornerShape(50),
                modifier = Modifier.size(180.dp).scale(scaleAnim),
                border = BorderStroke(
                    4.dp,
                    when { score == total -> Color(0xFFC9A84C); pct >= 60 -> Color(0xFF4CAF50); else -> MaterialTheme.colorScheme.error }
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 36.sp)
                        Text("$pct%", style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary)
                        Text("$score/$total", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            Text(headline, style = MaterialTheme.typography.displayMedium)

            // XP card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("XP EARNED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                        ),
                        color = MaterialTheme.colorScheme.secondary)
                    Text("+$xpEarned XP", style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.secondary)
                    if (score == total)
                        Text("Includes perfect score bonus!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f))
                }
            }

            // Stat row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("✅", "Correct",   score.toString()),
                    Triple("❌", "Incorrect", (total - score).toString()),
                    Triple("⚡","Difficulty", difficulty.replaceFirstChar { it.uppercase() })
                ).forEach { (emoji2, label, value) ->
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(emoji2, fontSize = 20.sp)
                            Text(value, style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Actions
            Button(
                onClick  = { navController.navigate("quiz_session/$category/$difficulty/$mode") {
                    popUpTo("quiz_result/$score/$total/$xpEarned/$category/$difficulty/$mode") { inclusive = true }
                }},
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Try Again")
            }
            OutlinedButton(
                onClick  = { navController.navigate("quiz_home") {
                    popUpTo("quiz_home") { inclusive = true }
                }},
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.GridView, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("All Categories")
            }
        }
    }
}
