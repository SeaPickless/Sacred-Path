package com.sacredpath.app.data.repository

import android.util.Log
import com.sacredpath.app.BuildConfig
import com.sacredpath.app.data.api.ApiBibleService
import com.sacredpath.app.data.db.*
import com.sacredpath.app.data.model.*
import com.sacredpath.app.utils.BibleBookData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CachedVerse(val number: Int, val text: String)

@Singleton
class BibleRepository @Inject constructor(
    private val api: ApiBibleService,
    private val db: BibleDatabase
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val apiKey = BuildConfig.API_BIBLE_KEY

    // ── Books ────────────────────────────────────────────────────────────────
    // Always returns from local BibleBookData — no API call needed for book list.
    // All 66 books (OT + NT) are bundled with correct IDs and chapter counts.
    fun getOldTestamentBooks(): List<BibleBook> = BibleBookData.OLD_TESTAMENT
    fun getNewTestamentBooks(): List<BibleBook> = BibleBookData.NEW_TESTAMENT
    fun getAllBooks(): List<BibleBook> = BibleBookData.ALL

    // ── Chapter verses ────────────────────────────────────────────────────────
    // 1. Check Room cache first (fast, offline)
    // 2. If not cached → fetch from API.Bible → parse → save to Room
    suspend fun getChapterVerses(
        translationId: String,
        book: BibleBook,
        chapter: Int
    ): Result<List<CachedVerse>> = withContext(Dispatchers.IO) {
        try {
            // Check cache
            val cached = db.bibleCache().getChapter(translationId, book.id, chapter)
            if (cached != null) {
                val verses = json.decodeFromString<List<CachedVerse>>(cached.versesJson)
                return@withContext Result.success(verses)
            }

            // Fetch from API.Bible
            val chapterId = "${book.id}.$chapter"
            val response = api.getChapter(translationId, chapterId, apiKey)
            val verses = parseChapterText(response.data.content)

            if (verses.isEmpty()) {
                return@withContext Result.failure(Exception("Empty chapter received"))
            }

            // Save to Room cache
            db.bibleCache().insertChapter(
                BibleCacheEntity(
                    translationId = translationId,
                    bookId        = book.id,
                    bookName      = book.name,
                    chapter       = chapter,
                    versesJson    = json.encodeToString(verses)
                )
            )

            Result.success(verses)
        } catch (e: Exception) {
            Log.e("BibleRepository", "getChapterVerses failed", e)
            Result.failure(e)
        }
    }

    // Parses API.Bible plain-text format:  "[1] In the beginning... [2] The earth..."
    private fun parseChapterText(raw: String): List<CachedVerse> {
        val cleaned = raw
            .replace(Regex("<[^>]*>"), "")  // strip any residual HTML
            .replace("\u00B6", "")           // pilcrow
            .trim()

        val regex = Regex("""\[(\d+)]\s*(.*?)(?=\[\d+]|$)""", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(cleaned).mapNotNull { match ->
            val num = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
            val text = match.groupValues[2].replace(Regex("\\s+"), " ").trim()
            if (text.isNotEmpty()) CachedVerse(num, text) else null
        }.toList()
    }

    suspend fun hasChapterCached(translationId: String, bookId: String, chapter: Int): Boolean =
        withContext(Dispatchers.IO) {
            db.bibleCache().hasChapter(translationId, bookId, chapter) > 0
        }

    suspend fun getDownloadedTranslations(): List<String> =
        withContext(Dispatchers.IO) { db.bibleCache().getDownloadedTranslations() }
}
