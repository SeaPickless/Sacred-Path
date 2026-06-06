package com.sacredpath.app.ui.screens.bible

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sacredpath.app.data.model.BibleBook
import com.sacredpath.app.data.model.Testament
import com.sacredpath.app.utils.BibleBookData

// BookPickerScreen — shows all 66 books split by OT / NT
// Book IDs always come from BibleBookData (e.g. GEN, JHN, 1CO, SNG) — never derived

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPickerScreen(
    navController: NavController,
    translationId: String
) {
    var query by remember { mutableStateOf("") }
    val allBooks = BibleBookData.ALL
    val filtered = remember(query) {
        if (query.isBlank()) allBooks
        else allBooks.filter { it.name.contains(query, ignoreCase = true) }
    }
    val otFiltered = filtered.filter { it.testament == Testament.OLD }
    val ntFiltered = filtered.filter { it.testament == Testament.NEW }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a Book") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("Find a book…") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                // Old Testament
                if (otFiltered.isNotEmpty()) {
                    item {
                        TestamentHeader("Old Testament", otFiltered.size)
                    }
                    items(otFiltered, key = { it.id }) { book ->
                        BookRow(book) {
                            navController.navigate(
                                "chapter_picker/$translationId/${book.id}/${book.name}"
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp)
                    }
                }
                // New Testament
                if (ntFiltered.isNotEmpty()) {
                    item {
                        TestamentHeader("New Testament", ntFiltered.size)
                    }
                    items(ntFiltered, key = { it.id }) { book ->
                        BookRow(book) {
                            navController.navigate(
                                "chapter_picker/$translationId/${book.id}/${book.name}"
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TestamentHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text  = "$count books",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun BookRow(book: BibleBook, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Abbreviation badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text  = book.abbreviation,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize   = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(book.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${book.chapterCount} chapters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        Icon(
            Icons.Default.ChevronRight, null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}
