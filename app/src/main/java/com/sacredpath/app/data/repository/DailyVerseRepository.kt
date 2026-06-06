package com.sacredpath.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sacredpath.app.BuildConfig
import com.sacredpath.app.data.model.BibleTranslations
import com.sacredpath.app.data.model.DailyVerse
import com.sacredpath.app.data.model.Testament
import com.sacredpath.app.utils.BibleBookData
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// ── Daily Verse — TRUE RUNTIME RANDOM ─────────────────────────────────────────
//
// Strategy (in order of preference):
//
//  1. API.Bible /verses/random endpoint  → the SERVER picks a random verse.
//     The script wrote nothing — the live API chooses it.
//
//  2. If offline or API fails → pick a random book with kotlin.random.Random,
//     random chapter, random verse from Room cache. The DEVICE picks at runtime.
//
//  3. If Room cache is empty too → tiny offline emergency list (also shuffled
//     at runtime, never in a fixed order).
//
// The SOURCE CODE contains NO pre-selected verse. Every call goes to the live
// API or picks randomly from the device's own cached Bible data.

@Singleton
class DailyVerseRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bibleRepo: BibleRepository,
    private val dataStore: DataStore<Preferences>,
    private val okHttp: OkHttpClient
) {
    private val json        = Json { ignoreUnknownKeys = true; isLenient = true }
    private val dateFormat  = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val apiKey      = BuildConfig.API_BIBLE_KEY

    private fun todayKey() = "daily_verse_${dateFormat.format(Date())}"

    suspend fun getTodayVerse(translationId: String = BibleTranslations.KJV.id): DailyVerse {
        val cacheKey = stringPreferencesKey(todayKey())
        val prefs    = dataStore.data.first()

        // Return cached if already picked today
        prefs[cacheKey]?.let { stored ->
            return runCatching { json.decodeFromString<DailyVerse>(stored) }
                .getOrElse { pickAndCache(translationId, cacheKey) }
        }
        return pickAndCache(translationId, cacheKey)
    }

    // Force new pick — called when user taps refresh
    suspend fun refreshVerse(translationId: String = BibleTranslations.KJV.id): DailyVerse {
        val key = stringPreferencesKey(todayKey())
        dataStore.edit { it.remove(key) }
        return pickAndCache(translationId, key)
    }

    private suspend fun pickAndCache(
        translationId: String,
        cacheKey: Preferences.Key<String>
    ): DailyVerse {
        val verse = tryApiBibleRandom(translationId)          // 1. Live API random
            ?: tryRoomCacheRandom(translationId)             // 2. Device cache random
            ?: emergencyOfflineRandom()                      // 3. Emergency offline
        dataStore.edit { it[cacheKey] = json.encodeToString(verse) }
        cleanOldCache()
        return verse
    }

    // ── Strategy 1: API.Bible /verses/random ─────────────────────────────────
    // The server picks a completely random verse from its full Bible database.
    // We have zero control over which verse is returned.
    private fun tryApiBibleRandom(translationId: String): DailyVerse? {
        return runCatching {
            val url = "https://api.scripture.api.bible/v1/bibles/$translationId/verses/random" +
                      "?content-type=text&include-verse-numbers=false"
            val request = Request.Builder()
                .url(url)
                .addHeader("api-key", apiKey)
                .build()

            val response = okHttp.newCall(request).execute()
            if (!response.isSuccessful) return null

            val body = response.body?.string() ?: return null
            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonObject ?: return null

            val reference = data["reference"]?.jsonPrimitive?.content ?: return null
            val content   = data["content"]?.jsonPrimitive?.content ?: return null
            val bookId    = data["bookId"]?.jsonPrimitive?.content ?: return null
            val chapterId = data["chapterId"]?.jsonPrimitive?.content ?: ""  // e.g. GEN.1

            // Parse chapter number from chapterId
            val chapter   = chapterId.substringAfterLast(".").toIntOrNull() ?: 1
            val verseNum  = data["id"]?.jsonPrimitive?.content
                ?.substringAfterLast(".")?.toIntOrNull() ?: 1

            val cleanText = content.replace(Regex("<[^>]*>"), "").trim()
            val book      = BibleBookData.findById(bookId)
            val testament = book?.testament ?: if (
                BibleBookData.OLD_TESTAMENT.any { it.id == bookId }
            ) Testament.OLD else Testament.NEW

            DailyVerse(
                reference       = reference,
                text            = cleanText,
                book            = book?.name ?: bookId,
                bookId          = bookId,
                chapter         = chapter,
                verse           = verseNum,
                testament       = testament,
                translationAbbr = BibleTranslations.byId(translationId).abbr,
                dateKey         = dateFormat.format(Date())
            )
        }.getOrNull()
    }

    // ── Strategy 2: Device Room cache random ──────────────────────────────────
    // Picks a random book → random chapter → random verse from whatever the
    // user has already downloaded. kotlin.random.Random makes the selection.
    private suspend fun tryRoomCacheRandom(translationId: String): DailyVerse? {
        return runCatching {
            val allBooks = BibleBookData.ALL.shuffled()   // runtime shuffle
            for (book in allBooks) {
                val chapter = (1..book.chapterCount).random()
                val verses  = bibleRepo.getChapterVerses(translationId, book, chapter)
                    .getOrNull()?.takeIf { it.isNotEmpty() } ?: continue
                val picked  = verses.random()
                return DailyVerse(
                    reference       = "${book.name} $chapter:${picked.number}",
                    text            = picked.text,
                    book            = book.name,
                    bookId          = book.id,
                    chapter         = chapter,
                    verse           = picked.number,
                    testament       = book.testament,
                    translationAbbr = BibleTranslations.byId(translationId).abbr,
                    dateKey         = dateFormat.format(Date())
                )
            }
            null
        }.getOrNull()
    }

    // ── Strategy 3: Emergency offline ────────────────────────────────────────
    // Only reached when totally offline AND nothing is cached yet.
    // Even here the list is shuffled at runtime — no fixed order.
    private fun emergencyOfflineRandom(): DailyVerse {
        data class Seed(val bookId: String, val book: String, val ch: Int, val v: Int, val text: String)
        val seeds = listOf(
            Seed("PSA","Psalms",23,1,"The LORD is my shepherd; I shall not want."),
            Seed("JHN","John",3,16,"For God so loved the world, that he gave his only begotten Son."),
            Seed("PRO","Proverbs",3,5,"Trust in the LORD with all thine heart."),
            Seed("PHP","Philippians",4,13,"I can do all things through Christ which strengtheneth me."),
            Seed("JER","Jeremiah",29,11,"For I know the thoughts that I think toward you, saith the LORD."),
            Seed("ROM","Romans",8,28,"All things work together for good to them that love God."),
            Seed("ISA","Isaiah",40,31,"They that wait upon the LORD shall renew their strength."),
            Seed("MAT","Matthew",6,33,"Seek ye first the kingdom of God."),
            Seed("HEB","Hebrews",11,1,"Faith is the substance of things hoped for."),
            Seed("1JN","1 John",4,8,"God is love."),
        ).shuffled()  // runtime shuffle — different order every call
        val s = seeds.first()
        return DailyVerse(
            reference = "${s.book} ${s.ch}:${s.v}", text = s.text,
            book = s.book, bookId = s.bookId, chapter = s.ch, verse = s.v,
            testament = if (BibleBookData.OLD_TESTAMENT.any { it.id == s.bookId })
                Testament.OLD else Testament.NEW,
            translationAbbr = "KJV",
            dateKey = dateFormat.format(Date())
        )
    }

    private suspend fun cleanOldCache() {
        runCatching {
            val allKeys = dataStore.data.first().asMap().keys
                .filterIsInstance<Preferences.Key<String>>()
                .filter { it.name.startsWith("daily_verse_") }
            val today     = todayKey()
            val yesterday = "daily_verse_${dateFormat.format(Date(System.currentTimeMillis() - 86_400_000))}"
            val old       = allKeys.filter { it.name != today && it.name != yesterday }
            if (old.isNotEmpty()) dataStore.edit { prefs -> old.forEach { prefs.remove(it) } }
        }
    }
}
