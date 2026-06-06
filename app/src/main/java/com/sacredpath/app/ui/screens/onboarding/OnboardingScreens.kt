package com.sacredpath.app.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.repository.BibleRepository
import com.sacredpath.app.data.repository.SettingsRepository
import com.sacredpath.app.utils.BibleBookData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val settingsRepo: SettingsRepository,
    val bibleRepo: BibleRepository
) : ViewModel() {
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress
    var downloadingBook by mutableStateOf("")
    var isDownloading   by mutableStateOf(false)

    fun finishOnboarding() = viewModelScope.launch { settingsRepo.setOnboardingDone() }

    fun downloadKJV(translationId: String, onDone: () -> Unit) {
        if (isDownloading) return
        isDownloading = true
        viewModelScope.launch(Dispatchers.IO) {
            val books = BibleBookData.ALL
            books.forEachIndexed { i, book ->
                withContext(Dispatchers.Main) { downloadingBook = book.name }
                for (ch in 1..book.chapterCount) {
                    if (!bibleRepo.hasChapterCached(translationId, book.id, ch)) {
                        bibleRepo.getChapterVerses(translationId, book, ch)
                        delay(150) // respect rate limit
                    }
                }
                val pct = (i + 1).toFloat() / books.size
                withContext(Dispatchers.Main) { _downloadProgress.value = pct }
            }
            withContext(Dispatchers.Main) { isDownloading = false; onDone() }
        }
    }
}

// ── WelcomeScreen ─────────────────────────────────────────────────────────────
@Composable
fun WelcomeScreen(navController: NavController) {
    val alpha by animateFloatAsState(targetValue = 1f,
        animationSpec = tween(800), label = "alpha")
    val slide by animateFloatAsState(targetValue = 0f,
        animationSpec = spring(dampingRatio = 0.6f), label = "slide",
        initialValue = 40f)

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.alpha(alpha).offset(y = slide.dp).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("✝", fontSize = 64.sp, color = MaterialTheme.colorScheme.secondary)
            Text("Sacred Path", style = MaterialTheme.typography.displayLarge,
                color = Color.White)
            Text("Your daily journey through God's Word",
                style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.8f))

            Spacer(Modifier.height(16.dp))

            val features = listOf(
                "📖 All 66 books — Old & New Testament",
                "🔊 Auto-read with voice & verse tracking",
                "🧠 Random quizzes to test your knowledge",
                "🙏 Prayer journal & devotional streaks",
                "👥 Study groups with shared prayer board",
            )
            features.forEach { f ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(f, style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.9f))
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("create_profile") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Get Started", style = MaterialTheme.typography.labelLarge,
                    color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, tint = Color.White)
            }
        }
    }
}

// ── CreateProfileScreen ───────────────────────────────────────────────────────
@Composable
fun CreateProfileScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    val colors = listOf(
        Color(0xFF7B2D3E), Color(0xFFC9A84C), Color(0xFF8A9A5B), Color(0xFF9B59B6),
        Color(0xFF2980B9), Color(0xFFE67E22), Color(0xFF27AE60), Color(0xFFC0392B),
    )
    var selectedColor by remember { mutableStateOf(colors.first()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Step 1 of 3", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary)
        Text("Create Your Profile", style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary)
        Text("This helps Sacred Path personalize your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f))

        // Avatar preview
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Surface(shape = CircleShape, color = selectedColor, modifier = Modifier.size(80.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displayMedium, color = Color.White
                    )
                }
            }
        }

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Your name") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        Text("Choose a color", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            colors.forEach { c ->
                Box(
                    modifier = Modifier.size(40.dp).background(c, CircleShape)
                        .then(if (c == selectedColor) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                        .clickable { selectedColor = c }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { navController.navigate("download_bible") },
            modifier = Modifier.fillMaxWidth(),
            enabled  = name.isNotBlank()
        ) { Text("Next →") }
    }
}

// ── DownloadBibleScreen ───────────────────────────────────────────────────────
@Composable
fun DownloadBibleScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val progress by viewModel.downloadProgress.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Step 2 of 3", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary)
        Text("Download Your Bible", style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary)
        Text(
            "Sacred Path downloads all 39 Old Testament and 27 New Testament books so you can read offline, anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
        )

        // KJV option card
        Card(shape = RoundedCornerShape(14.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("KJV", style = MaterialTheme.typography.labelLarge)
                Text("King James Version", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        }

        if (viewModel.isDownloading) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Downloading: ${viewModel.downloadingBook}…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.downloadKJV("de4e12af7f28f599-01") {
                    navController.navigate("set_reminder")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled  = !viewModel.isDownloading
        ) {
            if (viewModel.isDownloading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            else Text("Download KJV")
        }
        TextButton(onClick = { navController.navigate("set_reminder") }, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now")
        }
    }
}

// ── SetReminderScreen ─────────────────────────────────────────────────────────
@Composable
fun SetReminderScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val timeOptions = listOf("6:00 AM","7:00 AM","8:00 AM","9:00 AM","12:00 PM","6:00 PM","8:00 PM","9:00 PM")
    var readingTime by remember { mutableStateOf("8:00 AM") }
    var prayerTime  by remember { mutableStateOf("8:00 PM") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Step 3 of 3", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary)
        Text("Set Daily Reminders", style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary)
        Text("We'll gently nudge you to stay in God's Word every day.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))

        Text("📖 Daily Reading Reminder", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            timeOptions.forEach { t ->
                FilterChip(selected = readingTime == t, onClick = { readingTime = t }, label = { Text(t) })
            }
        }

        Text("🙏 Prayer Reminder", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            timeOptions.forEach { t ->
                FilterChip(selected = prayerTime == t, onClick = { prayerTime = t }, label = { Text(t) })
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.finishOnboarding()
                navController.navigate("main") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start Reading! 🎉") }
        TextButton(
            onClick = {
                viewModel.finishOnboarding()
                navController.navigate("main") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Skip reminders") }
    }
}
