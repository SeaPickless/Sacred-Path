package com.sacredpath.app.ui.screens.sermons

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.db.SermonDao
import com.sacredpath.app.data.db.SermonNoteEntity
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SermonsViewModel @Inject constructor(
    private val sermonDao: SermonDao,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val sermons: StateFlow<List<SermonNoteEntity>> = settingsRepo.settingsFlow
        .flatMapLatest { sermonDao.getAll(it.activeProfileId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun save(profileId: String, title: String, speaker: String, series: String, content: String) {
        viewModelScope.launch {
            val pid = settingsRepo.settingsFlow.first().activeProfileId
            sermonDao.insert(SermonNoteEntity(
                id = UUID.randomUUID().toString(), profileId = pid,
                title = title.ifBlank { "Untitled" }, speaker = speaker,
                series = series, content = content
            ))
            settingsRepo.addXP(5)
        }
    }

    fun delete(id: String) = viewModelScope.launch { sermonDao.delete(id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SermonsListScreen(
    navController: NavController,
    viewModel: SermonsViewModel = hiltViewModel()
) {
    val sermons by viewModel.sermons.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sermons") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("sermon_editor") }) {
                Icon(Icons.Default.Add, "New sermon note")
            }
        }
    ) { padding ->
        if (sermons.isEmpty()) {
            com.sacredpath.app.ui.components.EmptyState(
                "🎤", "No sermon notes yet",
                "Tap + to capture your next sermon.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 100.dp, start = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sermons, key = { it.id }) { note ->
                    Card(
                        modifier  = Modifier.fillMaxWidth()
                            .clickable { navController.navigate("sermon_editor?sermonId=${note.id}") },
                        shape     = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.Top,
                                modifier              = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    note.title,
                                    style    = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick  = { viewModel.delete(note.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete, null,
                                        tint     = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (note.speaker.isNotBlank())
                                Text(
                                    note.speaker,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            if (note.series.isNotBlank())
                                Text(
                                    "Series: ${note.series}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                                )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SermonEditorScreen(
    navController: NavController,
    sermonId: String? = null,
    viewModel: SermonsViewModel = hiltViewModel()
) {
    var title   by remember { mutableStateOf("") }
    var speaker by remember { mutableStateOf("") }
    var series  by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sermonId != null) "Edit Note" else "New Sermon Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.save("", title, speaker, series, content)
                        navController.popBackStack()
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                label         = { Text("Sermon Title") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value         = speaker,
                    onValueChange = { speaker = it },
                    label         = { Text("Speaker") },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = series,
                    onValueChange = { series = it },
                    label         = { Text("Series") },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true
                )
            }
            OutlinedTextField(
                value         = content,
                onValueChange = { content = it },
                label         = { Text("Notes") },
                placeholder   = { Text("Your sermon notes…\n\n• Key points\n• Scripture references\n• Personal reflections") },
                modifier      = Modifier.fillMaxWidth().defaultMinSize(minHeight = 300.dp),
                minLines      = 10
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}
