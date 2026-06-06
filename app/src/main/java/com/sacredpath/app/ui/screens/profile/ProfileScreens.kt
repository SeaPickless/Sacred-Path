package com.sacredpath.app.ui.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.foundation.BorderStroke

// ── ProfileViewModel ──────────────────────────────────────────────────────────
@HiltViewModel
class ProfileViewModel @Inject constructor(
    val settingsRepo: SettingsRepository
) : ViewModel() {
    val settings = settingsRepo.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun setTheme(mode: String)     = viewModelScope.launch { settingsRepo.setTheme(mode) }
    fun setTranslation(id: String) = viewModelScope.launch { settingsRepo.setTranslation(id) }
    fun setFontSize(s: String)     = viewModelScope.launch { settingsRepo.setFontSize(s) }
}

// ── Rank data ─────────────────────────────────────────────────────────────────
data class RankInfo(val name: String, val badge: String, val color: Color, val minXP: Int, val maxXP: Int)

val RANKS = listOf(
    RankInfo("Seeker",   "🪨", Color(0xFF9E9E9E),   0,      99),
    RankInfo("Disciple", "🥉", Color(0xFFCD7F32),   100,    499),
    RankInfo("Faithful", "📗", Color(0xFF4CAF50),   500,    1499),
    RankInfo("Devoted",  "🔵", Color(0xFF2196F3),   1500,   3999),
    RankInfo("Shepherd", "🟣", Color(0xFF9C27B0),   4000,   8999),
    RankInfo("Elder",    "🟡", Color(0xFFFFC107),   9000,   19999),
    RankInfo("Apostle",  "🔴", Color(0xFFF44336),   20000,  49999),
    RankInfo("Saint",    "💎", Color(0xFF00BCD4),   50000,  99999),
    RankInfo("Prophet",  "👑", Color(0xFFFFD700),   100000, Int.MAX_VALUE),
)
fun rankForXP(xp: Int) = RANKS.lastOrNull { xp >= it.minXP } ?: RANKS.first()

// ── ProfileHomeScreen ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHomeScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Profile") }) }) { padding ->
        if (settings == null) {
            com.sacredpath.app.ui.components.FullScreenLoading()
            return@Scaffold
        }
        val prefs = settings!!
        val rank = rankForXP(prefs.xp)
        val nextRank = RANKS.getOrNull(RANKS.indexOf(rank) + 1)

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp, start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero card
            item {
                Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("S", style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sacred Path Reader", style = MaterialTheme.typography.headlineMedium)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(rank.badge, fontSize = 16.sp)
                                    Text(rank.name, style = MaterialTheme.typography.labelMedium,
                                        color = rank.color)
                                }
                            }
                            com.sacredpath.app.ui.components.StreakBadge(prefs.streak)
                        }
                        // XP bar
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${prefs.xp.formatXP()} XP", style = MaterialTheme.typography.labelMedium)
                            if (nextRank != null)
                                Text("${(nextRank.minXP - prefs.xp).formatXP()} to ${nextRank.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        com.sacredpath.app.ui.components.XPProgressBar(
                            xp    = prefs.xp,
                            minXP = rank.minXP,
                            maxXP = if (rank.maxXP == Int.MAX_VALUE) prefs.xp + 10000 else rank.maxXP,
                            color = rank.color,
                            height = 10
                        )
                    }
                }
            }

            // Rank progression
            item {
                Text("Rank Progression", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically) {
                    RANKS.forEachIndexed { i, r ->
                        val isUnlocked = prefs.xp >= r.minXP
                        val isCurrent  = rank.name == r.name
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 4.dp)) {
                            Surface(shape = CircleShape, color = if (isUnlocked) r.color.copy(0.2f) else Color.Gray.copy(0.1f),
                                border = if (isCurrent) BorderStroke(2.dp, r.color) else null,
                                modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(r.badge, fontSize = 16.sp,
                                        color = if (isUnlocked) Color.Unspecified else Color.Gray)
                                }
                            }
                            Text(r.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (isCurrent) r.color else MaterialTheme.colorScheme.onSurface.copy(0.4f))
                        }
                    }
                }
            }

            // Settings list
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    val items = listOf(
                        Triple(Icons.Outlined.Settings,       "Settings",          "settings"),
                        Triple(Icons.Outlined.Palette,        "Theme",             "theme_selector"),
                        Triple(Icons.Outlined.EmojiEvents,    "Achievements",      "achievements"),
                        Triple(Icons.Outlined.BarChart,       "Statistics",        "statistics"),
                        Triple(Icons.Outlined.SwitchAccount,  "Switch Profile",    "profile_switcher"),
                    )
                    items.forEachIndexed { i, (icon, label, route) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { navController.navigate(route) }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(0.1f),
                                modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.3f), modifier = Modifier.size(18.dp))
                        }
                        if (i < items.lastIndex) HorizontalDivider(modifier = Modifier.padding(start = 66.dp))
                    }
                }
            }
        }
    }
}

// ── SettingsScreen ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        val prefs = settings ?: return@Scaffold
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Font Size
            Text("Font Size", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("small","medium","large","xl").forEach { s ->
                    FilterChip(selected = prefs.fontSize == s, onClick = { viewModel.setFontSize(s) },
                        label = { Text(s.replaceFirstChar { it.uppercase() }) })
                }
            }
            // Preview
            Card(shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = "\"For God so loved the world…\" — John 3:16",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = when(prefs.fontSize) { "small" -> 13.sp; "large" -> 19.sp; "xl" -> 22.sp; else -> 16.sp },
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// ── ThemeSelectorScreen ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()
    val themes = listOf(
        Triple("light",     "☀️ Light",     "Classic cream & burgundy"),
        Triple("dark",      "🌙 Dark",      "Deep navy for night reading"),
        Triple("parchment", "📜 Parchment", "Ancient manuscript feel"),
        Triple("night",     "🌑 Night",     "Ultra dark for bedtime"),
    )
    Scaffold(topBar = { TopAppBar(title = { Text("Choose Theme") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            themes.forEach { (mode, label, desc) ->
                val isSelected = settings?.themeMode == mode
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.setTheme(mode) },
                    shape  = RoundedCornerShape(14.dp),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    elevation = CardDefaults.cardElevation(if (isSelected) 3.dp else 1.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(label.take(2), fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(label.drop(3), style = MaterialTheme.typography.labelLarge)
                            Text(desc, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                        if (isSelected) Icon(Icons.Default.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ── AchievementsScreen ────────────────────────────────────────────────────────
data class Achievement(val id: String, val emoji: String, val title: String, val desc: String, val xp: Int, val check: (Int, Int) -> Boolean)
val ACHIEVEMENTS = listOf(
    Achievement("first_read",   "📖", "First Step",       "Read your first chapter",       10,  { xp, _ -> xp >= 5 }),
    Achievement("streak_7",     "🔥", "7-Day Flame",      "Maintain a 7-day streak",        50,  { _, s -> s >= 7 }),
    Achievement("streak_30",    "💪", "Month Faithful",   "Maintain a 30-day streak",       200, { _, s -> s >= 30 }),
    Achievement("xp_100",       "🥉", "Disciple",         "Earn 100 XP",                    20,  { xp, _ -> xp >= 100 }),
    Achievement("xp_1000",      "🥈", "Devoted Reader",   "Earn 1,000 XP",                  100, { xp, _ -> xp >= 1000 }),
    Achievement("xp_10000",     "🥇", "Bible Scholar",    "Earn 10,000 XP",                 500, { xp, _ -> xp >= 10000 }),
    Achievement("prayer_10",    "🙏", "Prayer Warrior",   "Log 10 prayers",                 40,  { xp, _ -> xp >= 30 }),
    Achievement("verse_10",     "💎", "Scripture Keeper", "Memorize 10 verses",             150, { xp, _ -> xp >= 150 }),
    Achievement("perfect_quiz", "🎯", "Perfect Quiz",     "Score 100% on any quiz",         30,  { xp, _ -> xp >= 20 }),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()
    val xp = settings?.xp ?: 0; val streak = settings?.streak ?: 0
    val unlocked = ACHIEVEMENTS.filter { it.check(xp, streak) }
    Scaffold(topBar = { TopAppBar(title = { Text("Achievements") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("${unlocked.size} / ${ACHIEVEMENTS.size} Unlocked", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.padding(bottom = 4.dp)) }
            items(ACHIEVEMENTS, key = { it.id }) { a ->
                val done = a.check(xp, streak)
                Card(shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().then(if (!done) Modifier.alpha(0.35f) else Modifier)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(a.emoji, fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.title, style = MaterialTheme.typography.labelLarge)
                            Text(a.desc, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondary.copy(0.12f)) {
                            Text("+${a.xp} XP", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── StatisticsScreen ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Statistics") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        val stats = listOf(
            Triple("⭐", "Total XP",      settings?.xp?.formatXP() ?: "0"),
            Triple("🔥", "Day Streak",    settings?.streak?.toString() ?: "0"),
        )
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 80.dp, start = 16.dp, end = 16.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    stats.forEach { (emoji, label, value) ->
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(emoji, fontSize = 24.sp)
                                Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── ProfileSwitcherScreen ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitcherScreen(navController: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Switch Profile") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("👤", fontSize = 48.sp)
                Text("Multi-profile support", style = MaterialTheme.typography.headlineMedium)
                Text("Coming in next build", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
        }
    }
}

private fun Int.formatXP(): String =
    if (this >= 1000) "${this / 1000},${(this % 1000).toString().padStart(3, '0')}" else toString()
