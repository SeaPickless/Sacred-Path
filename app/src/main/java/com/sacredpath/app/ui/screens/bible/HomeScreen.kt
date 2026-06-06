package com.sacredpath.app.ui.screens.bible

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
// PullToRefresh — Material3 experimental
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sacredpath.app.ui.components.*
import com.sacredpath.app.ui.navigation.Routes
import java.util.Calendar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    rootNav: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            else      -> "Good evening"
        }
    }

    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.loadVerse(forceRefresh = true)
            pullRefreshState.endRefresh()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text(
                        text  = greeting,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text      = "Your daily journey through God's Word",
                        style     = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier  = Modifier.padding(top = 2.dp)
                    )
                }
                StreakBadge(streak = state.streak)
            }

            Spacer(Modifier.height(16.dp))

            // ── XP / Rank Card ───────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rank circle
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(state.rankColor).copy(alpha = 0.15f),
                            border = BorderStroke(2.dp, Color(state.rankColor)),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(state.rankBadge, fontSize = 24.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = "${state.rankBadge} ${state.rankName}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text  = "${state.xp.formatXP()} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    XPProgressBar(
                        xp    = state.xp,
                        minXP = state.rankMinXP,
                        maxXP = if (state.rankMaxXP == Int.MAX_VALUE) state.xp + 10000 else state.rankMaxXP,
                        color = Color(state.rankColor),
                        height = 10
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Verse of the Day ─────────────────────────────────────────────
            SectionHeader(
                title   = "✨ Verse of the Day",
                trailing = {
                    IconButton(
                        onClick  = { viewModel.loadVerse(forceRefresh = true) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "New verse",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            )

            when {
                state.verseLoading -> {
                    Card(
                        modifier  = Modifier.fillMaxWidth().height(120.dp),
                        shape     = RoundedCornerShape(16.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color    = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    "Selecting a random verse…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                state.dailyVerse != null -> {
                    VerseCard(
                        verse      = state.dailyVerse!!,
                        onNavigate = {
                            navController.navigate(
                                "bible_reader/${state.dailyVerse!!.bookId}" +
                                "/${state.dailyVerse!!.bookId}" +
                                "/${state.dailyVerse!!.book}" +
                                "/${state.dailyVerse!!.chapter}" +
                                "?highlightVerse=${state.dailyVerse!!.verse}"
                            )
                        },
                        onShare = {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT,
                                    "\"${state.dailyVerse!!.text}\" — ${state.dailyVerse!!.reference}\n\nShared via Sacred Path")
                            }
                            context.startActivity(Intent.createChooser(share, "Share Verse"))
                        }
                    )
                    Text(
                        text  = "From ${state.dailyVerse!!.book} · ${state.dailyVerse!!.translationAbbr} · Pull down for new verse",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontStyle = FontStyle.Italic,
                        modifier  = Modifier.padding(top = 4.dp)
                    )
                }
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No verse available. Check your connection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            TextButton(onClick = { viewModel.loadVerse() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Quick Access ─────────────────────────────────────────────────
            SectionHeader("Quick Access")
            val quickTiles = listOf(
                Triple("Read Bible",  Icons.Outlined.MenuBook,        MaterialTheme.colorScheme.primary),
                Triple("Search",      Icons.Outlined.Search,          MaterialTheme.colorScheme.secondary),
                Triple("Bookmarks",   Icons.Outlined.Bookmark,        MaterialTheme.colorScheme.tertiary),
                Triple("Highlights",  Icons.Outlined.Edit,            Color(0xFF9B59B6)),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickTiles.take(2).forEach { (label, icon, color) ->
                    QuickTile(
                        label = label,
                        icon  = icon,
                        color = color,
                        modifier = Modifier.weight(1f)
                    ) {
                        when (label) {
                            "Read Bible" -> navController.navigate("book_picker/de4e12af7f28f599-01")
                            "Search"     -> navController.navigate("bible_search/de4e12af7f28f599-01")
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickTiles.drop(2).forEach { (label, icon, color) ->
                    QuickTile(
                        label = label,
                        icon  = icon,
                        color = color,
                        modifier = Modifier.weight(1f)
                    ) {
                        when (label) {
                            "Bookmarks"  -> navController.navigate(Routes.BOOKMARKS)
                            "Highlights" -> navController.navigate(Routes.HIGHLIGHTS)
                        }
                    }
                }
            }

            // ── Continue Reading ─────────────────────────────────────────────
            if (state.recentChapters.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Continue Reading")
                state.recentChapters.forEach { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                navController.navigate(
                                    "bible_reader/${entry.translationId}/${entry.bookId}/${entry.bookName}/${entry.chapter}"
                                )
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.MenuBook, null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp))
                            Text(
                                text     = "${entry.bookName} ${entry.chapter}",
                                style    = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        PullToRefreshContainer(
            state    = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun QuickTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier  = modifier.clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        border    = BorderStroke(0.dp, Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun Int.formatXP(): String =
    if (this >= 1000) "${this / 1000},${(this % 1000).toString().padStart(3, '0')}" else toString()
