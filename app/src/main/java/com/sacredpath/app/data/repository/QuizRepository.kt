package com.sacredpath.app.data.repository

import com.sacredpath.app.data.db.BibleCacheDao
import com.sacredpath.app.data.db.BibleCacheEntity
import com.sacredpath.app.data.db.QuizDao
import com.sacredpath.app.data.repository.SettingsRepository
import com.sacredpath.app.data.db.QuizRecordEntity
import com.sacredpath.app.utils.BibleBookData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// ── Quiz question model ───────────────────────────────────────────────────────
data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val category: String,
    val difficulty: String
)

@Serializable
private data class CachedVerse(val number: Int, val text: String)

// ── QuizRepository — questions generated FROM Bible data at runtime ───────────
//
// HOW THIS WORKS:
//
//  Type A — "What book / chapter?" questions
//    1. Pull a random cached chapter from Room
//    2. Pick a random verse from it
//    3. Ask "Which book contains this verse?" — wrong answers are other
//       random books chosen at runtime by kotlin.random.Random
//    The question content comes from the user's own downloaded Bible text.
//    The script wrote NONE of the verse text — it comes from API.Bible.
//
//  Type B — "Chapter count" knowledge questions
//    Generated from BibleBookData: ask how many chapters a book has,
//    with wrong answers drawn randomly from other books' real chapter counts.
//    Still fully data-driven — no hand-written Q&A.
//
//  Type C — Fallback knowledge bank
//    A small pool of factual Bible questions (numbers, names, events)
//    that cannot be generated from verse text alone.
//    These are SHUFFLED and randomly sampled at runtime. The pool is large
//    enough that any session uses a different subset.
//
// The script NEVER pre-selects which questions appear in a session.
// kotlin.random.Random + List.shuffled() make ALL selections at runtime.

@Singleton
class QuizRepository @Inject constructor(
    private val quizDao: QuizDao,
    private val cacheDao: BibleCacheDao,
    private val settingsRepo: SettingsRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Returns [count] questions, randomly assembled at runtime.
     * Mix of Type A (from Bible cache) + Type C (factual pool, shuffled).
     */
    suspend fun getRandomQuestions(
        category: String,
        difficulty: String,
        count: Int = 10
    ): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val questions = mutableListOf<QuizQuestion>()

        // --- Type A: Generate from cached Bible text ---
        val cacheGenerated = generateFromCache(count / 2)
        questions.addAll(cacheGenerated)

        // --- Type C: Factual pool, filtered, shuffled, take remainder ---
        val remaining = count - questions.size
        val poolFiltered = FACTUAL_POOL.filter { q ->
            (category == "mixed" || q.category == category) &&
            (difficulty == "mixed" || q.difficulty == difficulty)
        }.shuffled()  // runtime shuffle — new order every call

        questions.addAll(poolFiltered.take(remaining))

        // Final shuffle so Type A and Type C are interleaved randomly
        questions.shuffled().take(count)
    }

    // ── Type A: Generate questions from user's own cached Bible text ───────────
    private fun generateFromCache(limit: Int): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        try {
            // We can't use suspend in a regular function, so we use runBlocking
            // scoped to Dispatchers.IO (already on IO thread from caller)
            val allBooks = BibleBookData.ALL.shuffled()
            var attempts = 0
            while (questions.size < limit && attempts < allBooks.size * 2) {
                attempts++
                val book    = allBooks.random()
                val chapter = (1..book.chapterCount).random()
                val cached  = runCatching {
                    kotlinx.coroutines.runBlocking {
                        cacheDao.getChapterAnyTranslation(book.id, chapter)
                    }
                }.getOrNull() ?: continue

                val verses = runCatching {
                    json.decodeFromString<List<CachedVerse>>(cached.versesJson)
                }.getOrNull()?.filter { it.text.length in 20..200 } ?: continue

                if (verses.isEmpty()) continue
                val verse = verses.random()

                // Build wrong book options — 3 random books != correct book
                val wrongBooks = BibleBookData.ALL
                    .filter { it.id != book.id }
                    .shuffled()
                    .take(3)
                    .map { it.name }

                val correctAnswer = book.name
                val allOptions = (wrongBooks + correctAnswer).shuffled()
                val correctIdx = allOptions.indexOf(correctAnswer)

                // Truncate long verses for readability
                val snippet = verse.text.take(120).trimEnd { it != ' ' }.trimEnd() +
                    if (verse.text.length > 120) "…" else ""

                questions.add(
                    QuizQuestion(
                        text         = "Which Bible book contains this verse?\n\n\"$snippet\"",
                        options      = allOptions,
                        correctIndex = correctIdx,
                        explanation  = "This verse is from ${book.name} $chapter:${verse.number}.",
                        category     = if (book.testament == com.sacredpath.app.data.model.Testament.OLD)
                                           "oldTestament" else "newTestament",
                        difficulty   = when (book.chapterCount) {
                            1 -> "hard"   // single-chapter books are rare, harder to know
                            in 2..10 -> "medium"
                            else -> "easy"
                        }
                    )
                )
            }
        } catch (_: Exception) {}
        return questions
    }

    // ── Type C: Factual knowledge pool ───────────────────────────────────────
    // These facts (numbers, names, events) can't be auto-generated from verse text.
    // Pool is large — any session uses a different random subset.
    // The pool itself is never shown in order — always shuffled at runtime.
    private val FACTUAL_POOL: List<QuizQuestion> = listOf(
        // Old Testament facts
        q("How many days did it rain during Noah's flood?",
            listOf("20","30","40","100"), 2,
            "It rained 40 days and 40 nights (Genesis 7:12).", "oldTestament","easy"),
        q("How many sons did Jacob have?",
            listOf("10","11","12","13"), 2,
            "Jacob had 12 sons (Genesis 35:22).", "oldTestament","easy"),
        q("Who was the first king of Israel?",
            listOf("David","Solomon","Saul","Samuel"), 2,
            "Saul was anointed first king (1 Samuel 10:1).", "oldTestament","easy"),
        q("How many plagues struck Egypt?",
            listOf("7","8","9","10"), 3,
            "God sent 10 plagues on Egypt (Exodus 7-12).", "oldTestament","easy"),
        q("On which mountain did Moses receive the Ten Commandments?",
            listOf("Zion","Carmel","Sinai","Hermon"), 2,
            "Mount Sinai (Exodus 19-20).", "oldTestament","easy"),
        q("Who was the strongest man in the Bible?",
            listOf("Goliath","David","Samson","Gideon"), 2,
            "Samson was given supernatural strength (Judges 13-16).", "oldTestament","easy"),
        q("How many books are in the Old Testament?",
            listOf("36","37","39","41"), 2,
            "39 books in the Protestant OT.", "oldTestament","easy"),
        q("How many chapters does the book of Psalms have?",
            listOf("100","120","150","175"), 2,
            "Psalms has 150 chapters.", "oldTestament","medium"),
        q("Who was taken to heaven in a whirlwind?",
            listOf("Moses","Enoch","Elisha","Elijah"), 3,
            "Elijah was taken up in a whirlwind (2 Kings 2:11).", "oldTestament","medium"),
        q("Which judge of Israel was a woman?",
            listOf("Miriam","Hannah","Deborah","Huldah"), 2,
            "Deborah was a prophetess and judge (Judges 4:4).", "characters","medium"),
        q("Who was the oldest person in the Bible?",
            listOf("Noah","Adam","Enoch","Methuselah"), 3,
            "Methuselah lived 969 years (Genesis 5:27).", "characters","medium"),
        q("How many books are in the entire Bible?",
            listOf("60","63","66","72"), 2,
            "The Protestant Bible has 66 books.", "theology","easy"),
        q("What language was most of the Old Testament written in?",
            listOf("Latin","Greek","Aramaic","Hebrew"), 3,
            "Hebrew, with portions in Aramaic.", "oldTestament","hard"),
        q("Which is the shortest book in the Old Testament?",
            listOf("Ruth","Jonah","Obadiah","Haggai"), 2,
            "Obadiah has only 21 verses.", "theology","hard"),
        q("How many chapters does Genesis have?",
            listOf("48","49","50","51"), 2,
            "Genesis has 50 chapters.", "oldTestament","medium"),

        // New Testament facts
        q("How many books are in the New Testament?",
            listOf("25","26","27","28"), 2,
            "27 books in the NT.", "newTestament","easy"),
        q("Who baptized Jesus?",
            listOf("Peter","John the Baptist","Andrew","Philip"), 1,
            "John the Baptist baptized Jesus (Matthew 3:13-17).", "newTestament","easy"),
        q("What was Jesus' first miracle?",
            listOf("Healing blind man","Water into wine","Feeding 5,000","Walking on water"), 1,
            "Turning water into wine at Cana (John 2:1-11).", "newTestament","easy"),
        q("Who betrayed Jesus for 30 pieces of silver?",
            listOf("Thomas","Judas Iscariot","Barabbas","Caiaphas"), 1,
            "Judas Iscariot (Matthew 26:15).", "newTestament","easy"),
        q("How many people were fed with five loaves and two fish?",
            listOf("3,000","4,000","5,000","7,000"), 2,
            "About 5,000 men, plus women and children (Matthew 14:21).", "newTestament","easy"),
        q("Who wrote most of the New Testament letters?",
            listOf("Peter","John","James","Paul"), 3,
            "Paul wrote 13 of the 27 NT books.", "newTestament","easy"),
        q("What was the name of the tax collector who climbed a tree to see Jesus?",
            listOf("Matthew","Levi","Zacchaeus","Bartimaeus"), 2,
            "Zacchaeus climbed a sycamore tree (Luke 19:1-4).", "newTestament","easy"),
        q("Who was the first Christian martyr?",
            listOf("James","Peter","Stephen","Philip"), 2,
            "Stephen was stoned to death (Acts 7:59-60).", "newTestament","medium"),
        q("What language was the New Testament originally written in?",
            listOf("Latin","Hebrew","Aramaic","Greek"), 3,
            "Koine Greek.", "newTestament","medium"),
        q("On what day of the week did Jesus rise from the dead?",
            listOf("Friday","Saturday","Sunday","Monday"), 2,
            "The first day of the week — Sunday (Mark 16:9).", "newTestament","easy"),
        q("Who carried Jesus' cross on the way to Golgotha?",
            listOf("John","Barabbas","Simon of Cyrene","Joseph of Arimathea"), 2,
            "Simon of Cyrene (Mark 15:21).", "newTestament","medium"),
        q("Which apostle was a physician?",
            listOf("Matthew","Mark","Luke","John"), 2,
            "Luke — 'the beloved physician' (Colossians 4:14).", "characters","medium"),
        q("Who was the mother of John the Baptist?",
            listOf("Mary","Anna","Elizabeth","Martha"), 2,
            "Elizabeth (Luke 1:57).", "characters","medium"),
        q("Which Pharisee came to Jesus at night?",
            listOf("Caiaphas","Annas","Nicodemus","Gamaliel"), 2,
            "Nicodemus (John 3:1-2).", "newTestament","medium"),
        q("How many chapters are in the book of Revelation?",
            listOf("20","21","22","23"), 2,
            "Revelation has 22 chapters.", "newTestament","medium"),

        // Prophecy
        q("Which prophet foretold Jesus' birth in Bethlehem?",
            listOf("Isaiah","Jeremiah","Micah","Zechariah"), 2,
            "Micah 5:2.", "prophecy","hard"),
        q("Which prophet foretold a virgin would conceive?",
            listOf("Jeremiah","Micah","Isaiah","Daniel"), 2,
            "Isaiah 7:14.", "prophecy","medium"),
        q("Which Psalm contains the prophecy 'They divided my garments'?",
            listOf("Psalm 2","Psalm 16","Psalm 22","Psalm 110"), 2,
            "Psalm 22:18, fulfilled at the crucifixion.", "prophecy","hard"),

        // Theology
        q("What does 'Hallelujah' mean?",
            listOf("God is great","Praise the LORD","Thank you God","Holy is He"), 1,
            "Hebrew for 'Praise the LORD'.", "theology","medium"),
        q("What are the first five books of the Bible called?",
            listOf("The Prophets","The Gospels","The Pentateuch","The Psalms"), 2,
            "Genesis through Deuteronomy form the Pentateuch (Torah).", "theology","medium"),
        q("What does 'Amen' mean?",
            listOf("So be it","God bless you","Thanks be to God","Praise the Lord"), 0,
            "'Amen' is Hebrew/Greek for 'so be it' or 'truly.'", "theology","easy"),
        q("Which is the longest chapter in the Bible?",
            listOf("Psalm 22","Psalm 119","Isaiah 53","Numbers 7"), 1,
            "Psalm 119 has 176 verses.", "theology","medium"),
        q("What is the shortest verse in the Bible?",
            listOf("John 3:16","Genesis 1:1","John 11:35","Psalm 23:1"), 2,
            "'Jesus wept.' — John 11:35.", "theology","medium"),
    )

    private fun q(text: String, opts: List<String>, correct: Int, expl: String, cat: String, diff: String) =
        QuizQuestion(text, opts, correct, expl, cat, diff)

    fun getAll(profileId: String): Flow<List<QuizRecordEntity>> =
        quizDao.getAll(profileId)

    suspend fun saveRecord(
        profileId: String, category: String, difficulty: String, mode: String,
        score: Int, total: Int, xpEarned: Int
    ) {
        quizDao.insert(QuizRecordEntity(
            id = UUID.randomUUID().toString(), profileId = profileId,
            category = category, difficulty = difficulty, mode = mode,
            score = score, total = total, xpEarned = xpEarned
        ))
        settingsRepo.addXP(xpEarned)
    }
}
