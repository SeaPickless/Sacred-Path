package com.sacredpath.app.ui.screens.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sacredpath.app.data.model.DailyVerse
import com.sacredpath.app.data.repository.*
import com.sacredpath.app.data.db.ReadingHistoryDao
import com.sacredpath.app.data.db.ReadingHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val dailyVerse: DailyVerse? = null,
    val verseLoading: Boolean   = true,
    val recentChapters: List<ReadingHistoryEntity> = emptyList(),
    val xp: Int     = 0,
    val streak: Int = 0,
    val rankName: String  = "Seeker",
    val rankBadge: String = "🪨",
    val rankColor: Long   = 0xFF9E9E9E,
    val rankMinXP: Int    = 0,
    val rankMaxXP: Int    = 99
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dailyVerseRepo: DailyVerseRepository,
    private val settingsRepo: SettingsRepository,
    private val bibleRepo: BibleRepository,
    private val historyDao: ReadingHistoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { settings ->
                _state.update {
                    it.copy(xp = settings.xp, streak = settings.streak)
                        .applyRank(settings.xp)
                }
            }
        }
        loadVerse()
        loadHistory()
    }

    fun loadVerse(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(verseLoading = true) }
            val settings = settingsRepo.settingsFlow.first()
            val verse = try {
                if (forceRefresh)
                    dailyVerseRepo.refreshVerse(settings.translationId)
                else
                    dailyVerseRepo.getTodayVerse(settings.translationId)
            } catch (_: Exception) { null }
            _state.update { it.copy(dailyVerse = verse, verseLoading = false) }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val settings = settingsRepo.settingsFlow.first()
            val history = historyDao.getRecent(settings.activeProfileId, 3)
            _state.update { it.copy(recentChapters = history) }
        }
    }
}

private val RANKS = listOf(
    listOf("Seeker", "🪨", 0xFF9E9E9EL, 0, 99),
    listOf("Disciple", "🥉", 0xFFCD7F32L, 100, 499),
    listOf("Faithful", "📗", 0xFF4CAF50L, 500, 1499),
    listOf("Devoted", "🔵", 0xFF2196F3L, 1500, 3999),
    listOf("Shepherd", "🟣", 0xFF9C27B0L, 4000, 8999),
    listOf("Elder", "🟡", 0xFFFFC107L, 9000, 19999),
    listOf("Apostle", "🔴", 0xFFF44336L, 20000, 49999),
    listOf("Saint", "💎", 0xFF00BCD4L, 50000, 99999),
    listOf("Prophet", "👑", 0xFFFFD700L, 100000, Int.MAX_VALUE),
)

@Suppress("UNCHECKED_CAST")
fun HomeUiState.applyRank(xp: Int): HomeUiState {
    val rank = RANKS.lastOrNull { xp >= (it[3] as Int) } ?: RANKS.first()
    return copy(
        rankName  = rank[0] as String,
        rankBadge = rank[1] as String,
        rankColor = rank[2] as Long,
        rankMinXP = rank[3] as Int,
        rankMaxXP = rank[4] as Int
    )
}
