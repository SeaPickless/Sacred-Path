package com.sacredpath.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sacredpath.app.BuildConfig
import com.sacredpath.app.data.api.ApiBibleService
import com.sacredpath.app.data.db.BibleDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "sacred_path_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        ctx.dataStore

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): BibleDatabase =
        Room.databaseBuilder(ctx, BibleDatabase::class.java, "sacred_path.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys  = true
        isLenient          = true
        encodeDefaults     = true
    }

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BASIC
                else
                    HttpLoggingInterceptor.Level.NONE
            }
        )
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttp: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.scripture.api.bible/v1/")
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideApiBibleService(retrofit: Retrofit): ApiBibleService =
        retrofit.create(ApiBibleService::class.java)

    // DAOs
    @Provides fun provideBibleCacheDao(db: BibleDatabase) = db.bibleCache()
    @Provides fun provideHighlightDao(db: BibleDatabase)  = db.highlights()
    @Provides fun provideBookmarkDao(db: BibleDatabase)   = db.bookmarks()
    @Provides fun provideNoteDao(db: BibleDatabase)       = db.notes()
    @Provides fun providePrayerDao(db: BibleDatabase)     = db.prayers()
    @Provides fun provideQuizDao(db: BibleDatabase)       = db.quizRecords()
    @Provides fun provideMemorizationDao(db: BibleDatabase) = db.memorization()
    @Provides fun provideReadingHistoryDao(db: BibleDatabase) = db.readingHistory()
    @Provides fun provideSermonDao(db: BibleDatabase)     = db.sermons()
}
