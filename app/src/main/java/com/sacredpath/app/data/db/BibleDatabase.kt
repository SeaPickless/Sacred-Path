package com.sacredpath.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ── Entities ──────────────────────────────────────────────────────────────────

@Entity(
    tableName = "bible_cache",
    indices = [Index(value = ["translationId", "bookId", "chapter"], unique = true)]
)
data class BibleCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val versesJson: String,  // JSON array of {number, text}
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "highlights",
    indices = [Index(value = ["profileId", "translationId", "bookId", "chapter", "verse"], unique = true)]
)
data class HighlightEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val color: String,  // gold | pink | green | blue | purple
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val label: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayer_requests")
data class PrayerEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val title: String,
    val description: String = "",
    val category: String = "personal",
    val isAnswered: Boolean = false,
    val answeredAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_records")
data class QuizRecordEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val category: String,
    val difficulty: String,
    val mode: String,
    val score: Int,
    val total: Int,
    val xpEarned: Int = 0,
    val takenAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "memorization_verses",
    indices = [Index(value = ["profileId", "translationId", "bookId", "chapter", "verse"], unique = true)]
)
data class MemorizationEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val verseText: String,
    val status: String = "new",       // new | learning | mastered
    val easeFactor: Float = 2.5f,     // SM-2
    val interval: Int = 1,            // days
    val repetitions: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val readAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sermon_notes")
data class SermonNoteEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val title: String,
    val series: String = "",
    val speaker: String = "",
    val tags: String = "[]",
    val content: String = "",
    val audioPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ── DAOs ──────────────────────────────────────────────────────────────────────

@Dao
interface BibleCacheDao {
    @Query("SELECT * FROM bible_cache WHERE translationId=:tid AND bookId=:bookId AND chapter=:chapter LIMIT 1")
    suspend fun getChapter(tid: String, bookId: String, chapter: Int): BibleCacheEntity?

    @Query("SELECT * FROM bible_cache WHERE bookId=:bookId AND chapter=:chapter LIMIT 1")
    suspend fun getChapterAnyTranslation(bookId: String, chapter: Int): BibleCacheEntity?

    @Query("SELECT * FROM bible_cache ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCachedChapter(): BibleCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(entity: BibleCacheEntity)

    @Query("SELECT DISTINCT translationId FROM bible_cache")
    suspend fun getDownloadedTranslations(): List<String>

    @Query("SELECT COUNT(*) FROM bible_cache WHERE translationId=:tid AND bookId=:bookId AND chapter=:chapter")
    suspend fun hasChapter(tid: String, bookId: String, chapter: Int): Int
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights WHERE profileId=:pid AND translationId=:tid AND bookId=:bookId AND chapter=:ch")
    fun getForChapter(pid: String, tid: String, bookId: String, ch: Int): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE profileId=:pid ORDER BY createdAt DESC")
    fun getAll(pid: String): Flow<List<HighlightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HighlightEntity)

    @Query("DELETE FROM highlights WHERE profileId=:pid AND translationId=:tid AND bookId=:bookId AND chapter=:ch AND verse=:v")
    suspend fun delete(pid: String, tid: String, bookId: String, ch: Int, v: Int)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE profileId=:pid ORDER BY createdAt DESC")
    fun getAll(pid: String): Flow<List<BookmarkEntity>>

    @Query("SELECT COUNT(*) FROM bookmarks WHERE profileId=:pid AND translationId=:tid AND bookId=:bookId AND chapter=:ch AND verse=:v")
    suspend fun isBookmarked(pid: String, tid: String, bookId: String, ch: Int, v: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id=:id")
    suspend fun delete(id: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE profileId=:pid ORDER BY updatedAt DESC")
    fun getAll(pid: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE profileId=:pid AND bookId=:bookId AND chapter=:ch AND verse=:v LIMIT 1")
    suspend fun getForVerse(pid: String, bookId: String, ch: Int, v: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteEntity)

    @Query("DELETE FROM notes WHERE id=:id")
    suspend fun delete(id: String)
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_requests WHERE profileId=:pid AND isAnswered=0 ORDER BY createdAt DESC")
    fun getActive(pid: String): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayer_requests WHERE profileId=:pid AND isAnswered=1 ORDER BY answeredAt DESC")
    fun getAnswered(pid: String): Flow<List<PrayerEntity>>

    @Query("SELECT COUNT(*) FROM prayer_requests WHERE profileId=:pid")
    suspend fun getTotal(pid: String): Int

    @Query("SELECT COUNT(*) FROM prayer_requests WHERE profileId=:pid AND isAnswered=1")
    suspend fun getAnsweredCount(pid: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PrayerEntity)

    @Query("UPDATE prayer_requests SET isAnswered=1, answeredAt=:at WHERE id=:id")
    suspend fun markAnswered(id: String, at: Long = System.currentTimeMillis())

    @Query("DELETE FROM prayer_requests WHERE id=:id")
    suspend fun delete(id: String)

    @Update
    suspend fun update(entity: PrayerEntity)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_records WHERE profileId=:pid ORDER BY takenAt DESC")
    fun getAll(pid: String): Flow<List<QuizRecordEntity>>

    @Insert
    suspend fun insert(entity: QuizRecordEntity)

    @Query("SELECT MAX(CAST(score AS REAL)/total*100) FROM quiz_records WHERE profileId=:pid AND category=:cat")
    suspend fun getPersonalBest(pid: String, cat: String): Float?
}

@Dao
interface MemorizationDao {
    @Query("SELECT * FROM memorization_verses WHERE profileId=:pid ORDER BY createdAt ASC")
    fun getAll(pid: String): Flow<List<MemorizationEntity>>

    @Query("SELECT * FROM memorization_verses WHERE profileId=:pid AND nextReviewAt<=:now ORDER BY nextReviewAt ASC LIMIT :limit")
    suspend fun getDueNow(pid: String, now: Long = System.currentTimeMillis(), limit: Int = 20): List<MemorizationEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: MemorizationEntity)

    @Update
    suspend fun update(entity: MemorizationEntity)

    @Query("DELETE FROM memorization_verses WHERE id=:id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM memorization_verses WHERE profileId=:pid AND status=:status")
    suspend fun getCountByStatus(pid: String, status: String): Int
}

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history WHERE profileId=:pid ORDER BY readAt DESC LIMIT :limit")
    suspend fun getRecent(pid: String, limit: Int = 10): List<ReadingHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history WHERE profileId=:pid AND id NOT IN (SELECT id FROM reading_history WHERE profileId=:pid ORDER BY readAt DESC LIMIT 10)")
    suspend fun trimToTen(pid: String)
}

@Dao
interface SermonDao {
    @Query("SELECT * FROM sermon_notes WHERE profileId=:pid ORDER BY updatedAt DESC")
    fun getAll(pid: String): Flow<List<SermonNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SermonNoteEntity)

    @Update
    suspend fun update(entity: SermonNoteEntity)

    @Query("DELETE FROM sermon_notes WHERE id=:id")
    suspend fun delete(id: String)
}

// ── Database ──────────────────────────────────────────────────────────────────

@Database(
    entities = [
        BibleCacheEntity::class,
        HighlightEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        PrayerEntity::class,
        QuizRecordEntity::class,
        MemorizationEntity::class,
        ReadingHistoryEntity::class,
        SermonNoteEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class BibleDatabase : RoomDatabase() {
    abstract fun bibleCache(): BibleCacheDao
    abstract fun highlights(): HighlightDao
    abstract fun bookmarks(): BookmarkDao
    abstract fun notes(): NoteDao
    abstract fun prayers(): PrayerDao
    abstract fun quizRecords(): QuizDao
    abstract fun memorization(): MemorizationDao
    abstract fun readingHistory(): ReadingHistoryDao
    abstract fun sermons(): SermonDao
}
