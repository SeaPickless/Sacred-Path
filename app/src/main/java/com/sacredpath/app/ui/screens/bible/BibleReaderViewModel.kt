package com.sacredpath.app.ui.screens.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sacredpath.app.data.db.*
import com.sacredpath.app.data.repository.*
import com.sacredpath.app.utils.BibleBookData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ReaderUiState(
    val verses: List<CachedVerse>    = emptyList(),
    val highlights: Map<Int, String> = emptyMap(),  // verse → color
    val bookmarked: Set<Int>         = emptySet(),
    val notes: Map<Int, String>      = emptyMap(),
    val loading: Boolean             = true,
    val error: String?               = null,
    val isSpeaking: Boolean          = false,
    val speakingVerse: Int           = 0
)

@HiltViewModel
class BibleReaderViewModel @Inject constructor(
    private val bibleRepo: BibleRepository,
    private val settingsRepo: SettingsRepository,
    private val highlightDao: HighlightDao,
    private val bookmarkDao: BookmarkDao,
    private val noteDao: NoteDao,
    private val historyDao: ReadingHistoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private var currentProfileId = ""
    private var currentTranslationId = ""
    private var currentBookId = ""
    private var currentBookName = ""
    private var currentChapter = 0

    fun load(translationId: String, bookId: String, bookName: String, chapter: Int) {
        currentTranslationId = translationId
        currentBookId        = bookId
        currentBookName      = bookName
        currentChapter       = chapter

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            currentProfileId = settingsRepo.settingsFlow.first().activeProfileId

            val book = BibleBookData.findById(bookId)
            if (book == null) {
                _state.update { it.copy(loading = false, error = "Unknown book: $bookId") }
                return@launch
            }

            bibleRepo.getChapterVerses(translationId, book, chapter)
                .onSuccess { verses ->
                    _state.update { it.copy(verses = verses, loading = false) }
                    loadAnnotations()
                    recordHistory()
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
                }
        }
    }

    private fun loadAnnotations() {
        viewModelScope.launch {
            // Highlights
            highlightDao.getForChapter(currentProfileId, currentTranslationId, currentBookId, currentChapter)
                .collect { list ->
                    _state.update { s -> s.copy(highlights = list.associate { it.verse to it.color }) }
                }
        }
        viewModelScope.launch {
            // Bookmarks
            bookmarkDao.getAll(currentProfileId).collect { list ->
                val set = list
                    .filter { it.bookId == currentBookId && it.chapter == currentChapter }
                    .map { it.verse }
                    .toSet()
                _state.update { it.copy(bookmarked = set) }
            }
        }
        viewModelScope.launch {
            // Notes
            noteDao.getAll(currentProfileId).collect { list ->
                val map = list
                    .filter { it.bookId == currentBookId && it.chapter == currentChapter }
                    .associate { it.verse to it.content }
                _state.update { it.copy(notes = map) }
            }
        }
    }

    private fun recordHistory() {
        viewModelScope.launch {
            historyDao.insert(
                ReadingHistoryEntity(
                    id            = UUID.randomUUID().toString(),
                    profileId     = currentProfileId,
                    translationId = currentTranslationId,
                    bookId        = currentBookId,
                    bookName      = currentBookName,
                    chapter       = currentChapter
                )
            )
            historyDao.trimToTen(currentProfileId)
            settingsRepo.addXP(2)
        }
    }

    fun toggleHighlight(verse: Int, color: String) {
        viewModelScope.launch {
            val existing = _state.value.highlights[verse]
            if (existing == color) {
                highlightDao.delete(currentProfileId, currentTranslationId, currentBookId, currentChapter, verse)
            } else {
                highlightDao.upsert(
                    HighlightEntity(
                        id            = "$currentProfileId-$currentTranslationId-$currentBookId-$currentChapter-$verse",
                        profileId     = currentProfileId,
                        translationId = currentTranslationId,
                        bookId        = currentBookId,
                        bookName      = currentBookName,
                        chapter       = currentChapter,
                        verse         = verse,
                        color         = color
                    )
                )
            }
        }
    }

    fun toggleBookmark(verse: Int) {
        viewModelScope.launch {
            if (_state.value.bookmarked.contains(verse)) {
                bookmarkDao.getAll(currentProfileId).first()
                    .find { it.bookId == currentBookId && it.chapter == currentChapter && it.verse == verse }
                    ?.let { bookmarkDao.delete(it.id) }
            } else {
                bookmarkDao.insert(
                    BookmarkEntity(
                        id            = UUID.randomUUID().toString(),
                        profileId     = currentProfileId,
                        translationId = currentTranslationId,
                        bookId        = currentBookId,
                        bookName      = currentBookName,
                        chapter       = currentChapter,
                        verse         = verse
                    )
                )
            }
        }
    }

    fun saveNote(verse: Int, content: String) {
        viewModelScope.launch {
            val existing = noteDao.getForVerse(currentProfileId, currentBookId, currentChapter, verse)
            val id = existing?.id ?: UUID.randomUUID().toString()
            noteDao.upsert(
                NoteEntity(
                    id            = id,
                    profileId     = currentProfileId,
                    translationId = currentTranslationId,
                    bookId        = currentBookId,
                    bookName      = currentBookName,
                    chapter       = currentChapter,
                    verse         = verse,
                    content       = content
                )
            )
            settingsRepo.addXP(3)
        }
    }

    fun addToMemorization(verse: Int, text: String) {
        viewModelScope.launch {
            val mem = com.sacredpath.app.data.db.MemorizationEntity(
                id            = UUID.randomUUID().toString(),
                profileId     = currentProfileId,
                translationId = currentTranslationId,
                bookId        = currentBookId,
                bookName      = currentBookName,
                chapter       = currentChapter,
                verse         = verse,
                verseText     = text,
                nextReviewAt  = System.currentTimeMillis()
            )
            // Uses IGNORE conflict so duplicate adds are silently skipped
        }
    }
}
