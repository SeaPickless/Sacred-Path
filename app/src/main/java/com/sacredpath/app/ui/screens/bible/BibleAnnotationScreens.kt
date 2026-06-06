package com.sacredpath.app.ui.screens.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.db.*
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Shared ViewModel ──────────────────────────────────────────────────────────
@HiltViewModel
class AnnotationsViewModel @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val highlightDao: HighlightDao,
    private val historyDao: ReadingHistoryDao,
    private val settingsRepo: SettingsRepository
) : ViewModel() {
    private var profileId = ""

    init {
        viewModelScope.launch {
            profileId = settingsRepo.settingsFlow.first().activeProfileId
        }
    }

    val bookmarks: StateFlow<List<BookmarkEntity>> = settingsRepo.settingsFlow
        .flatMapLatest { bookmarkDao.getAll(it.activeProfileId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val highlights: StateFlow<List<HighlightEntity>> = settingsRepo.settingsFlow
        .flatMapLatest { highlightDao.getAll(it.activeProfileId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteBookmark(id: String) = viewModelScope.launch { bookmarkDao.delete(id) }
}

// ── Bookmarks ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavController,
    viewModel: AnnotationsViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            com.sacredpath.app.ui.components.EmptyState(
                "🔖", "No bookmarks yet",
                "Tap any verse while reading to bookmark it.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp, bottom = 80.dp,
                start = 16.dp, end = 16.dp
            )) {
                items(bookmarks, key = { it.id }) { bm ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            .clickable {
                                navController.navigate(
                                    "bible_reader/${bm.translationId}/${bm.bookId}/${bm.bookName}/${bm.chapter}?highlightVerse=${bm.verse}"
                                )
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, null,
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${bm.bookName} ${bm.chapter}:${bm.verse}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                                if (bm.label.isNotBlank())
                                    Text(bm.label, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.deleteBookmark(bm.id) },
                                modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Highlights ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightsScreen(
    navController: NavController,
    viewModel: AnnotationsViewModel = hiltViewModel()
) {
    val highlights by viewModel.highlights.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Highlights") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (highlights.isEmpty()) {
            com.sacredpath.app.ui.components.EmptyState(
                "🖊️", "No highlights yet",
                "Tap a verse to highlight it in 5 colors.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp, bottom = 80.dp,
                start = 16.dp, end = 16.dp
            )) {
                items(highlights, key = { it.id }) { hl ->
                    val color = HIGHLIGHT_COLORS[hl.color] ?: Color(0xFFF9E4A0)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            .clickable {
                                navController.navigate(
                                    "bible_reader/${hl.translationId}/${hl.bookId}/${hl.bookName}/${hl.chapter}?highlightVerse=${hl.verse}"
                                )
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(16.dp)
                                    .then(Modifier.background(color, androidx.compose.foundation.shape.CircleShape))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${hl.bookName} ${hl.chapter}:${hl.verse}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                                Text("${hl.color.replaceFirstChar { it.uppercase() }} highlight",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                            Icon(Icons.Default.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

// ── Reading History ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryScreen(
    navController: NavController,
    viewModel: AnnotationsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        com.sacredpath.app.ui.components.EmptyState(
            "📖", "Your history will appear here",
            "Chapters you read will be listed here.",
            modifier = Modifier.padding(padding)
        )
    }
}

// ── Compare Translations ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareTranslationsScreen(
    navController: NavController,
    translationId: String,
    bookId: String,
    bookName: String,
    chapter: Int
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compare: $bookName $chapter") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Side-by-side comparison — coming in next build",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}
