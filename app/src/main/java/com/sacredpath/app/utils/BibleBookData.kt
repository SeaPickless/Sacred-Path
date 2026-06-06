package com.sacredpath.app.utils

import com.sacredpath.app.data.model.BibleBook
import com.sacredpath.app.data.model.Testament

// ── Single source of truth for all 66 books ──────────────────────────────────
// id   = canonical API.Bible book ID (verified against api.scripture.api.bible)
// Never derive this from name.take(3) — John→JHN, Judges→JDG (not JUD=Jude)

object BibleBookData {

    val OLD_TESTAMENT: List<BibleBook> = listOf(
        BibleBook("GEN", "Genesis",            "GEN", Testament.OLD, 50),
        BibleBook("EXO", "Exodus",             "EXO", Testament.OLD, 40),
        BibleBook("LEV", "Leviticus",          "LEV", Testament.OLD, 27),
        BibleBook("NUM", "Numbers",            "NUM", Testament.OLD, 36),
        BibleBook("DEU", "Deuteronomy",        "DEU", Testament.OLD, 34),
        BibleBook("JOS", "Joshua",             "JOS", Testament.OLD, 24),
        BibleBook("JDG", "Judges",             "JDG", Testament.OLD, 21),
        BibleBook("RUT", "Ruth",               "RUT", Testament.OLD,  4),
        BibleBook("1SA", "1 Samuel",           "1SA", Testament.OLD, 31),
        BibleBook("2SA", "2 Samuel",           "2SA", Testament.OLD, 24),
        BibleBook("1KI", "1 Kings",            "1KI", Testament.OLD, 22),
        BibleBook("2KI", "2 Kings",            "2KI", Testament.OLD, 25),
        BibleBook("1CH", "1 Chronicles",       "1CH", Testament.OLD, 29),
        BibleBook("2CH", "2 Chronicles",       "2CH", Testament.OLD, 36),
        BibleBook("EZR", "Ezra",               "EZR", Testament.OLD, 10),
        BibleBook("NEH", "Nehemiah",           "NEH", Testament.OLD, 13),
        BibleBook("EST", "Esther",             "EST", Testament.OLD, 10),
        BibleBook("JOB", "Job",                "JOB", Testament.OLD, 42),
        BibleBook("PSA", "Psalms",             "PSA", Testament.OLD,150),
        BibleBook("PRO", "Proverbs",           "PRO", Testament.OLD, 31),
        BibleBook("ECC", "Ecclesiastes",       "ECC", Testament.OLD, 12),
        BibleBook("SNG", "Song of Solomon",    "SNG", Testament.OLD,  8),
        BibleBook("ISA", "Isaiah",             "ISA", Testament.OLD, 66),
        BibleBook("JER", "Jeremiah",           "JER", Testament.OLD, 52),
        BibleBook("LAM", "Lamentations",       "LAM", Testament.OLD,  5),
        BibleBook("EZK", "Ezekiel",            "EZK", Testament.OLD, 48),
        BibleBook("DAN", "Daniel",             "DAN", Testament.OLD, 12),
        BibleBook("HOS", "Hosea",              "HOS", Testament.OLD, 14),
        BibleBook("JOL", "Joel",               "JOL", Testament.OLD,  3),
        BibleBook("AMO", "Amos",               "AMO", Testament.OLD,  9),
        BibleBook("OBA", "Obadiah",            "OBA", Testament.OLD,  1),
        BibleBook("JON", "Jonah",              "JON", Testament.OLD,  4),
        BibleBook("MIC", "Micah",              "MIC", Testament.OLD,  7),
        BibleBook("NAM", "Nahum",              "NAM", Testament.OLD,  3),
        BibleBook("HAB", "Habakkuk",           "HAB", Testament.OLD,  3),
        BibleBook("ZEP", "Zephaniah",          "ZEP", Testament.OLD,  3),
        BibleBook("HAG", "Haggai",             "HAG", Testament.OLD,  2),
        BibleBook("ZEC", "Zechariah",          "ZEC", Testament.OLD, 14),
        BibleBook("MAL", "Malachi",            "MAL", Testament.OLD,  4)
    )

    val NEW_TESTAMENT: List<BibleBook> = listOf(
        BibleBook("MAT", "Matthew",            "MAT", Testament.NEW, 28),
        BibleBook("MRK", "Mark",               "MRK", Testament.NEW, 16),
        BibleBook("LUK", "Luke",               "LUK", Testament.NEW, 24),
        BibleBook("JHN", "John",               "JHN", Testament.NEW, 21),
        BibleBook("ACT", "Acts",               "ACT", Testament.NEW, 28),
        BibleBook("ROM", "Romans",             "ROM", Testament.NEW, 16),
        BibleBook("1CO", "1 Corinthians",      "1CO", Testament.NEW, 16),
        BibleBook("2CO", "2 Corinthians",      "2CO", Testament.NEW, 13),
        BibleBook("GAL", "Galatians",          "GAL", Testament.NEW,  6),
        BibleBook("EPH", "Ephesians",          "EPH", Testament.NEW,  6),
        BibleBook("PHP", "Philippians",        "PHP", Testament.NEW,  4),
        BibleBook("COL", "Colossians",         "COL", Testament.NEW,  4),
        BibleBook("1TH", "1 Thessalonians",    "1TH", Testament.NEW,  5),
        BibleBook("2TH", "2 Thessalonians",    "2TH", Testament.NEW,  3),
        BibleBook("1TI", "1 Timothy",          "1TI", Testament.NEW,  6),
        BibleBook("2TI", "2 Timothy",          "2TI", Testament.NEW,  4),
        BibleBook("TIT", "Titus",              "TIT", Testament.NEW,  3),
        BibleBook("PHM", "Philemon",           "PHM", Testament.NEW,  1),
        BibleBook("HEB", "Hebrews",            "HEB", Testament.NEW, 13),
        BibleBook("JAS", "James",              "JAS", Testament.NEW,  5),
        BibleBook("1PE", "1 Peter",            "1PE", Testament.NEW,  5),
        BibleBook("2PE", "2 Peter",            "2PE", Testament.NEW,  3),
        BibleBook("1JN", "1 John",             "1JN", Testament.NEW,  5),
        BibleBook("2JN", "2 John",             "2JN", Testament.NEW,  1),
        BibleBook("3JN", "3 John",             "3JN", Testament.NEW,  1),
        BibleBook("JUD", "Jude",               "JUD", Testament.NEW,  1),
        BibleBook("REV", "Revelation",         "REV", Testament.NEW, 22)
    )

    val ALL: List<BibleBook> = OLD_TESTAMENT + NEW_TESTAMENT

    // Quick lookup by API.Bible ID
    private val byId: Map<String, BibleBook> = ALL.associateBy { it.id }
    fun findById(id: String): BibleBook? = byId[id]

    // Quick lookup by name
    private val byName: Map<String, BibleBook> = ALL.associateBy { it.name }
    fun findByName(name: String): BibleBook? = byName[name]

    // Total verse counts (OT=23,145 / NT=7,957 / Total=31,102)
    val totalOTBooks  = OLD_TESTAMENT.size   // 39
    val totalNTBooks  = NEW_TESTAMENT.size   // 27
    val totalBooks    = ALL.size             // 66
}
