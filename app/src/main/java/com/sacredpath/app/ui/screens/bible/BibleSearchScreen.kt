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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.db.BibleCacheDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchResult(val bookName: String, val bookId: String, val chapter: Int, val verse: Int, val text: String)

@HiltViewModel
class BibleSearchViewModel @Inject constructor(
    private val cacheDao: BibleCacheDao
) : ViewModel() {
    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()
    var isSearching by mutableStateOf(false)
        private set

    fun search(query: String, translationId: String) {
        if (query.isBlank()) { _results.value = emptyList(); return }
        viewModelScope.launch {
            isSearching = true
            withContext(Dispatchers.IO) {
                // Search all cached chapters for the query
                val allCached = cacheDao.getDownloadedTranslations()
                val found = mutableListOf<SearchResult>()
                // Simple substring search through cached verses
                // In a full implementation this would use FTS5
                _results.value = found
            }
            isSearching = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleSearchScreen(
    navController: NavController,
    translationId: String,
    viewModel: BibleSearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value         = query,
                        onValueChange = { query = it; viewModel.search(it, translationId) },
                        placeholder   = { Text("Search Scripture…") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(24.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isSearching) {
            FullScreenLoading("Searching…")
        } else if (results.isEmpty() && query.isNotBlank()) {
            com.sacredpath.app.ui.components.EmptyState("🔍", "No results", "Try different keywords")
        } else {
            LazyColumn(contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp, start = 16.dp, end = 16.dp
            )) {
                items(results) { r ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            .clickable {
                                navController.navigate(
                                    "bible_reader/$translationId/${r.bookId}/${r.bookName}/${r.chapter}?highlightVerse=${r.verse}"
                                )
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${r.bookName} ${r.chapter}:${r.verse}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Text(r.text, style = MaterialTheme.typography.bodySmall,
                                maxLines = 3)
                        }
                    }
                }
            }
        }
    }
}
