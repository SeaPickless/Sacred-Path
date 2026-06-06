package com.sacredpath.app.ui.screens.memorization

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.db.MemorizationDao
import com.sacredpath.app.data.db.MemorizationEntity
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemViewModel @Inject constructor(
    private val memDao: MemorizationDao,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val dueToday: StateFlow<List<MemorizationEntity>> = flow {
        val pid = settingsRepo.settingsFlow.first().activeProfileId
        emit(memDao.getDueNow(pid))
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // SM-2 review
    fun review(entity: MemorizationEntity, quality: Int) {
        viewModelScope.launch {
            var ef  = entity.easeFactor
            var inv = entity.interval
            var rep = entity.repetitions
            if (quality < 3) { rep = 0; inv = 1 }
            else {
                inv = when (rep) { 0 -> 1; 1 -> 6; else -> (inv * ef).toInt() }
                rep++
            }
            ef = (ef + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)).coerceAtLeast(1.3).toFloat()
            val status = when { rep >= 5 -> "mastered"; rep > 0 -> "learning"; else -> "new" }
            val next   = System.currentTimeMillis() + inv * 86_400_000L
            memDao.update(entity.copy(easeFactor = ef, interval = inv, repetitions = rep, status = status, nextReviewAt = next, lastReviewedAt = System.currentTimeMillis()))
            settingsRepo.addXP(if (quality >= 3) 2 else 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemHomeScreen(navController: NavController, viewModel: MemViewModel = hiltViewModel()) {
    val due by viewModel.dueToday.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Verse Memorization") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        if (due.isEmpty()) {
            Column(modifier = Modifier.padding(padding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                com.sacredpath.app.ui.components.EmptyState("🎉", "All caught up!",
                    "No verses due today. Add verses from the Bible Reader by long-pressing any verse.")
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("${due.size} verse(s) due for review", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)) }
                items(due, key = { it.id }) { verse ->
                    Card(shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${verse.bookName} ${verse.chapter}:${verse.verse}",
                                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(verse.verseText, style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider()
                            Text("How well did you recall this?", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1 to "1\nBad", 2 to "2\nHard", 3 to "3\nOK", 4 to "4\nGood", 5 to "5\nEasy").forEach { (q, label) ->
                                    OutlinedButton(
                                        onClick = { viewModel.review(verse, q) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(4.dp)
                                    ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
