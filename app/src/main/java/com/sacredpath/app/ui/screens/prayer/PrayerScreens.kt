package com.sacredpath.app.ui.screens.prayer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.sacredpath.app.data.db.*
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import androidx.compose.foundation.BorderStroke

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val prayerDao: PrayerDao,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    val activeProfileId = settingsRepo.settingsFlow.map { it.activeProfileId }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val activePrayers: StateFlow<List<PrayerEntity>> = settingsRepo.settingsFlow
        .flatMapLatest { prayerDao.getActive(it.activeProfileId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val answeredPrayers: StateFlow<List<PrayerEntity>> = settingsRepo.settingsFlow
        .flatMapLatest { prayerDao.getAnswered(it.activeProfileId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addPrayer(title: String, description: String, category: String) {
        viewModelScope.launch {
            val pid = settingsRepo.settingsFlow.first().activeProfileId
            prayerDao.insert(
                PrayerEntity(
                    id          = UUID.randomUUID().toString(),
                    profileId   = pid,
                    title       = title,
                    description = description,
                    category    = category
                )
            )
            settingsRepo.addXP(3)
        }
    }

    fun markAnswered(id: String) = viewModelScope.launch {
        prayerDao.markAnswered(id)
        settingsRepo.addXP(5)
    }

    fun deletePrayer(id: String) = viewModelScope.launch { prayerDao.delete(id) }
}

val PRAYER_CATEGORIES = listOf("personal","family","health","nation","gratitude","church")
val CATEGORY_COLORS = mapOf(
    "personal"  to Color(0xFF7B2D3E),
    "family"    to Color(0xFF2196F3),
    "health"    to Color(0xFF4CAF50),
    "nation"    to Color(0xFFFF9800),
    "gratitude" to Color(0xFFC9A84C),
    "church"    to Color(0xFF9C27B0),
)

// ── PrayerHomeScreen ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerHomeScreen(
    navController: NavController,
    viewModel: PrayerViewModel = hiltViewModel()
) {
    val prayers  by viewModel.activePrayers.collectAsState()
    var showAdd  by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Prayer Journal") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, "Add Prayer")
            }
        }
    ) { padding ->
        if (prayers.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                com.sacredpath.app.ui.components.EmptyState(
                    "🙏", "No prayers yet",
                    "Tap + to add your first prayer request."
                )
                TextButton(onClick = { navController.navigate("answered_prayers") }) {
                    Text("View Answered Prayers")
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 100.dp, start = 16.dp, end = 16.dp
            )) {
                item {
                    TextButton(
                        onClick  = { navController.navigate("answered_prayers") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(6.dp))
                        Text("View Answered Prayers", color = Color(0xFF4CAF50))
                    }
                }
                items(prayers, key = { it.id }) { prayer ->
                    PrayerCard(
                        prayer       = prayer,
                        onAnswered   = { viewModel.markAnswered(prayer.id) },
                        onDelete     = { viewModel.deletePrayer(prayer.id) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAdd) {
        AddPrayerBottomSheet(
            onDismiss = { showAdd = false },
            onSave    = { title, desc, cat ->
                viewModel.addPrayer(title, desc, cat)
                showAdd = false
            }
        )
    }
}

@Composable
private fun PrayerCard(
    prayer: PrayerEntity,
    onAnswered: () -> Unit,
    onDelete: () -> Unit
) {
    val catColor = CATEGORY_COLORS[prayer.category] ?: MaterialTheme.colorScheme.primary

    Card(shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Surface(shape = RoundedCornerShape(20.dp), color = catColor.copy(alpha = 0.12f)) {
                    Text(
                        prayer.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = catColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp))
                }
            }
            Text(prayer.title, style = MaterialTheme.typography.labelLarge)
            if (prayer.description.isNotBlank())
                Text(prayer.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2)

            OutlinedButton(
                onClick  = onAnswered,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp),
                    tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(6.dp))
                Text("Mark as Answered")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPrayerBottomSheet(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title    by remember { mutableStateOf("") }
    var desc     by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("personal") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("New Prayer Request", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = desc, onValueChange = { desc = it },
                label = { Text("Details (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Text("Category", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRAYER_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick  = { category = cat },
                        label    = { Text(cat.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            Button(
                onClick  = { if (title.isNotBlank()) onSave(title, desc, category) },
                modifier = Modifier.fillMaxWidth(),
                enabled  = title.isNotBlank()
            ) { Text("Add Prayer") }
        }
    }
}

// ── AddPrayerScreen (navigation-based) ────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPrayerScreen(navController: NavController, existingId: String? = null,
    viewModel: PrayerViewModel = hiltViewModel()) {
    Scaffold(topBar = { TopAppBar(title = { Text(if (existingId != null) "Edit Prayer" else "Add Prayer") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        var title by remember { mutableStateOf("") }
        var desc  by remember { mutableStateOf("") }
        var cat   by remember { mutableStateOf("personal") }
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(onClick = { viewModel.addPrayer(title, desc, cat); navController.popBackStack() }, modifier = Modifier.fillMaxWidth(), enabled = title.isNotBlank()) { Text("Save") }
        }
    }
}

// ── AnsweredPrayersScreen ─────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnsweredPrayersScreen(navController: NavController, viewModel: PrayerViewModel = hiltViewModel()) {
    val answered by viewModel.answeredPrayers.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Answered Prayers") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        if (answered.isEmpty()) {
            com.sacredpath.app.ui.components.EmptyState("✅", "No answered prayers yet",
                "Mark prayers as answered and they appear here.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 80.dp, start = 16.dp, end = 16.dp)) {
                items(answered, key = { it.id }) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF4CAF50).copy(alpha = 0.4f))) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp).padding(top = 2.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.title, style = MaterialTheme.typography.labelLarge)
                                if (p.description.isNotBlank()) Text(p.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}
