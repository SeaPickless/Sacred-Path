package com.sacredpath.app.ui.screens.bible

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sacredpath.app.utils.BibleBookData
import kotlinx.coroutines.launch
import java.util.Locale

// Highlight color map
val HIGHLIGHT_COLORS = mapOf(
    "gold"   to Color(0xFFF9E4A0),
    "pink"   to Color(0xFFF9C0CC),
    "green"  to Color(0xFFC6E8C6),
    "blue"   to Color(0xFFB8D8F0),
    "purple" to Color(0xFFD4C0F0),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderScreen(
    navController: NavController,
    translationId: String,
    bookId: String,
    bookName: String,
    chapter: Int,
    highlightVerse: Int = -1,
    viewModel: BibleReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedVerse by remember { mutableStateOf<Int?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }

    // TTS setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var speakingVerse by remember { mutableStateOf(0) }
    var ttsSpeed by remember { mutableStateOf(1.0f) }
    var showTtsBar by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    LaunchedEffect(bookId, chapter, translationId) {
        viewModel.load(translationId, bookId, bookName, chapter)
    }

    // Scroll to highlighted verse
    LaunchedEffect(highlightVerse, state.verses) {
        if (highlightVerse > 0 && state.verses.isNotEmpty()) {
            val idx = state.verses.indexOfFirst { it.number == highlightVerse }
            if (idx >= 0) listState.animateScrollToItem(idx + 2) // +2 for header items
        }
    }

    fun speakFrom(startVerse: Int) {
        tts?.let { engine ->
            engine.stop()
            val versesToSpeak = state.verses.dropWhile { it.number < startVerse }
            var idx = 0

            fun speakNext() {
                if (idx >= versesToSpeak.size) {
                    isSpeaking = false; speakingVerse = 0; return
                }
                val v = versesToSpeak[idx]
                speakingVerse = v.number
                engine.speak(
                    "Verse ${v.number}. ${v.text}",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "verse_${v.number}"
                )
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { idx++; speakNext() }
                    override fun onError(utteranceId: String?) { isSpeaking = false; speakingVerse = 0 }
                })
            }

            engine.setSpeechRate(ttsSpeed)
            isSpeaking = true
            showTtsBar = true
            speakNext()
        }
    }

    val maxChapter = BibleBookData.findById(bookId)?.chapterCount ?: 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("$bookName $chapter", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "KJV",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showTtsBar = !showTtsBar; if (!isSpeaking) speakFrom(1) }) {
                        Icon(Icons.Default.VolumeUp, "Read aloud")
                    }
                    IconButton(onClick = { navController.navigate("bible_search/$translationId") }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (showTtsBar) {
                TtsBar(
                    isSpeaking   = isSpeaking,
                    speakingVerse= speakingVerse,
                    speed        = ttsSpeed,
                    onPlay       = { if (!isSpeaking) speakFrom(speakingVerse.takeIf { it > 0 } ?: 1) },
                    onPause      = { tts?.stop(); isSpeaking = false },
                    onStop       = { tts?.stop(); isSpeaking = false; speakingVerse = 0; showTtsBar = false },
                    onSpeedCycle = {
                        ttsSpeed = when (ttsSpeed) {
                            0.75f -> 1.0f; 1.0f -> 1.25f; 1.25f -> 1.5f; 1.5f -> 2.0f; else -> 0.75f
                        }
                        if (isSpeaking) { tts?.stop(); speakFrom(speakingVerse.coerceAtLeast(1)) }
                    },
                    onPrev = {
                        if (chapter > 1) navController.navigate("bible_reader/$translationId/$bookId/$bookName/${chapter - 1}") {
                            popUpTo("bible_reader/$translationId/$bookId/$bookName/$chapter") { inclusive = true }
                        }
                    },
                    onNext = {
                        if (chapter < maxChapter) navController.navigate("bible_reader/$translationId/$bookId/$bookName/${chapter + 1}") {
                            popUpTo("bible_reader/$translationId/$bookId/$bookName/$chapter") { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                state.loading -> FullScreenLoading("Loading $bookName $chapter…")
                state.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("⚠️", fontSize = 48.sp)
                        Text(state.error!!, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { viewModel.load(translationId, bookId, bookName, chapter) }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Chapter heading
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    bookName,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Chapter $chapter",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // Verses
                        items(state.verses, key = { it.number }) { verse ->
                            val isSelected   = selectedVerse == verse.number
                            val isSpeakingThis = speakingVerse == verse.number
                            val hlColor      = state.highlights[verse.number]?.let { HIGHLIGHT_COLORS[it] }
                            val hasNote      = state.notes.containsKey(verse.number)
                            val isBookmarked = state.bookmarked.contains(verse.number)

                            VerseRow(
                                verse        = verse,
                                isSelected   = isSelected,
                                isSpeaking   = isSpeakingThis,
                                highlightColor = hlColor,
                                hasNote      = hasNote,
                                isBookmarked = isBookmarked,
                                onClick      = { selectedVerse = if (selectedVerse == verse.number) null else verse.number }
                            )
                        }

                        // Chapter navigation
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (chapter > 1) {
                                    OutlinedButton(onClick = {
                                        navController.navigate("bible_reader/$translationId/$bookId/$bookName/${chapter - 1}") {
                                            popUpTo("bible_reader/$translationId/$bookId/$bookName/$chapter") { inclusive = true }
                                        }
                                    }) {
                                        Icon(Icons.Default.ChevronLeft, null, Modifier.size(16.dp))
                                        Text("Prev")
                                    }
                                } else { Spacer(Modifier.size(1.dp)) }

                                if (chapter < maxChapter) {
                                    OutlinedButton(onClick = {
                                        navController.navigate("bible_reader/$translationId/$bookId/$bookName/${chapter + 1}") {
                                            popUpTo("bible_reader/$translationId/$bookId/$bookName/$chapter") { inclusive = true }
                                        }
                                    }) {
                                        Text("Next")
                                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // ── Verse action bar ────────────────────────────────────
                    if (selectedVerse != null) {
                        VerseActionBar(
                            verse          = selectedVerse!!,
                            highlights     = state.highlights,
                            isBookmarked   = state.bookmarked.contains(selectedVerse!!),
                            verseText      = state.verses.find { it.number == selectedVerse }?.text ?: "",
                            bookName       = bookName,
                            chapter        = chapter,
                            context        = context,
                            onHighlight    = { color -> viewModel.toggleHighlight(selectedVerse!!, color); selectedVerse = null },
                            onBookmark     = { viewModel.toggleBookmark(selectedVerse!!); selectedVerse = null },
                            onNote         = {
                                noteText = state.notes[selectedVerse!!] ?: ""
                                showNoteDialog = true
                            },
                            onDismiss      = { selectedVerse = null },
                            modifier       = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    // Note dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title  = { Text("Note — $bookName $chapter:${selectedVerse}") },
            text   = {
                OutlinedTextField(
                    value         = noteText,
                    onValueChange = { noteText = it },
                    placeholder   = { Text("Write your note…") },
                    minLines      = 4,
                    modifier      = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedVerse?.let { viewModel.saveNote(it, noteText) }
                    showNoteDialog = false; selectedVerse = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun VerseRow(
    verse: CachedVerse,
    isSelected: Boolean,
    isSpeaking: Boolean,
    highlightColor: Color?,
    hasNote: Boolean,
    isBookmarked: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSpeaking   -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
        isSelected   -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        highlightColor != null -> highlightColor
        else         -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(6.dp))
            .then(if (isSpeaking) Modifier.border(1.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(6.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text     = verse.number.toString(),
            style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color    = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.width(24.dp).padding(top = 3.dp)
        )
        Text(
            text     = verse.text,
            style    = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (isBookmarked || hasNote) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (isBookmarked) Icon(Icons.Default.Bookmark, null,
                    tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                if (hasNote) Icon(Icons.Default.Edit, null,
                    tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun VerseActionBar(
    verse: Int,
    highlights: Map<Int, String>,
    isBookmarked: Boolean,
    verseText: String,
    bookName: String,
    chapter: Int,
    context: android.content.Context,
    onHighlight: (String) -> Unit,
    onBookmark: () -> Unit,
    onNote: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Highlight dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HIGHLIGHT_COLORS.forEach { (name, color) ->
                    val isActive = highlights[verse] == name
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 34.dp else 28.dp)
                            .background(color, CircleShape)
                            .border(if (isActive) 2.5.dp else 1.dp,
                                if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.3f),
                                CircleShape)
                            .clickable { onHighlight(name) }
                    )
                }
            }
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ActionBtn(
                    icon  = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = if (isBookmarked) "Saved" else "Bookmark",
                    onClick = onBookmark
                )
                ActionBtn(Icons.Outlined.Edit, "Note", onNote)
                ActionBtn(Icons.Default.ContentCopy, "Copy") {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("verse",
                        "$bookName $chapter:$verse — $verseText"))
                    onDismiss()
                }
                ActionBtn(Icons.Default.Share, "Share") {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "\"$verseText\" — $bookName $chapter:$verse\n\nShared via Sacred Path")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Verse"))
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun ActionBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun TtsBar(
    isSpeaking: Boolean,
    speakingVerse: Int,
    speed: Float,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSpeedCycle: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (speakingVerse > 0) {
                Text(
                    text  = "Reading verse $speakingVerse…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 6.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Speed badge
                OutlinedButton(onClick = onSpeedCycle, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("${speed}×", style = MaterialTheme.typography.labelSmall)
                }
                // Prev chapter
                IconButton(onClick = onPrev) { Icon(Icons.Default.SkipPrevious, "Prev chapter") }
                // Play / Pause
                FloatingActionButton(
                    onClick          = if (isSpeaking) onPause else onPlay,
                    containerColor   = MaterialTheme.colorScheme.primary,
                    contentColor     = MaterialTheme.colorScheme.onPrimary,
                    modifier         = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Next chapter
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Next chapter") }
                // Stop
                IconButton(onClick = onStop) { Icon(Icons.Default.Close, "Stop") }
            }
        }
    }
}
