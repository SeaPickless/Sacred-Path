package com.sacredpath.app.data.model

import kotlinx.serialization.Serializable

// ── API.Bible network response models ────────────────────────────────────────

@Serializable
data class ApiBibleBooksResponse(val data: List<ApiBibleBook>)

@Serializable
data class ApiBibleBook(
    val id: String,
    val bibleId: String,
    val abbreviation: String,
    val name: String,
    val nameLong: String
)

@Serializable
data class ApiBibleChapterResponse(val data: ApiBibleChapterData)

@Serializable
data class ApiBibleChapterData(
    val id: String,
    val bibleId: String,
    val bookId: String,
    val number: String,
    val content: String,
    val verseCount: Int,
    val copyright: String = ""
)

// ── App domain models ─────────────────────────────────────────────────────────

data class BibleBook(
    val id: String,           // API.Bible canonical ID e.g. GEN, MAT, REV, 1CO, SNG
    val name: String,         // Human name e.g. "Song of Solomon"
    val abbreviation: String, // Short e.g. "SOS"
    val testament: Testament,
    val chapterCount: Int
)

enum class Testament { OLD, NEW }

data class BibleVerse(
    val number: Int,
    val text: String
)

data class DailyVerse(
    val reference: String,   // e.g. "John 3:16"
    val text: String,
    val book: String,
    val bookId: String,
    val chapter: Int,
    val verse: Int,
    val testament: Testament,
    val translationAbbr: String,
    val dateKey: String       // YYYY-MM-DD — used as cache key
)

// ── Translations ──────────────────────────────────────────────────────────────

data class BibleTranslation(
    val id: String,
    val name: String,
    val abbr: String
)

object BibleTranslations {
    val KJV  = BibleTranslation("de4e12af7f28f599-01", "King James Version",         "KJV")
    val NIV  = BibleTranslation("06125adad2d5898a-01", "New International Version",   "NIV")
    val ESV  = BibleTranslation("9879dbb7cfe39e4d-04", "English Standard Version",    "ESV")
    val NLT  = BibleTranslation("65eec8e0b60e656b-01", "New Living Translation",      "NLT")
    val NASB = BibleTranslation("f72b840c855f362c-04", "New American Standard Bible", "NASB")
    val AMP  = BibleTranslation("bf8f1c7f3ef34e27-01", "Amplified Bible",             "AMP")
    val WEB  = BibleTranslation("9879dbb7cfe39e4d-01", "World English Bible",         "WEB")

    val ALL  = listOf(KJV, NIV, ESV, NLT, NASB, AMP, WEB)
    fun byId(id: String) = ALL.find { it.id == id } ?: KJV
}
