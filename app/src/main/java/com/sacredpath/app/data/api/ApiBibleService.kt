package com.sacredpath.app.data.api

import com.sacredpath.app.data.model.ApiBibleBooksResponse
import com.sacredpath.app.data.model.ApiBibleChapterResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiBibleService {

    @GET("bibles/{bibleId}/books")
    suspend fun getBooks(
        @Path("bibleId") bibleId: String,
        @Header("api-key") apiKey: String
    ): ApiBibleBooksResponse

    @GET("bibles/{bibleId}/chapters/{chapterId}")
    suspend fun getChapter(
        @Path("bibleId") bibleId: String,
        @Path("chapterId") chapterId: String,  // e.g. GEN.1, MAT.5
        @Header("api-key") apiKey: String,
        @Query("content-type") contentType: String = "text",
        @Query("include-notes") includeNotes: Boolean = false,
        @Query("include-titles") includeTitles: Boolean = false,
        @Query("include-chapter-numbers") includeChapterNumbers: Boolean = false,
        @Query("include-verse-numbers") includeVerseNumbers: Boolean = true
    ): ApiBibleChapterResponse
}
