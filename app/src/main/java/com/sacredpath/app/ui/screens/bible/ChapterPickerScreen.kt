package com.sacredpath.app.ui.screens.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sacredpath.app.utils.BibleBookData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterPickerScreen(
    navController: NavController,
    translationId: String,
    bookId: String,
    bookName: String
) {
    // Chapter count comes from BibleBookData — no API call needed
    val chapterCount = remember(bookId) {
        BibleBookData.findById(bookId)?.chapterCount ?: 30
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bookName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            items(chapterCount) { index ->
                val chapter = index + 1
                Card(
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            navController.navigate(
                                "bible_reader/$translationId/$bookId/$bookName/$chapter"
                            )
                        },
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Box(
                        modifier        = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = chapter.toString(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
