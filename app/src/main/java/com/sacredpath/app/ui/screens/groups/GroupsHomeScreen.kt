package com.sacredpath.app.ui.screens.groups

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke

data class LocalGroup(val code: String, val name: String, val role: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsHomeScreen(navController: NavController) {
    var groups    by remember { mutableStateOf(listOf<LocalGroup>()) }
    var showCreate by remember { mutableStateOf(false) }
    var showJoin   by remember { mutableStateOf(false) }
    var newName    by remember { mutableStateOf("") }
    var joinCode   by remember { mutableStateOf("") }
    var selected   by remember { mutableStateOf<LocalGroup?>(null) }
    var prayer     by remember { mutableStateOf("") }
    var prayers    by remember { mutableStateOf(listOf<String>()) }

    val words = listOf("DOVE","LION","LAMB","FISH","VINE","ROCK","STAR","GATE","WELL","SEED")

    fun generateCode() = "${words.random()}-${(1000..9999).random()}"

    if (selected != null) {
        val g = selected!!
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(g.name) },
                    navigationIcon = { IconButton(onClick = { selected = null }) { Icon(Icons.Default.ArrowBack, null) } },
                    actions = { TextButton(onClick = { groups = groups - g; selected = null }) { Text("Leave") } }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Code share card
                Card(shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(0.4f))) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Text("Code: ", style = MaterialTheme.typography.bodyMedium)
                        Text(g.code, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                        Text("Tap to share", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                    }
                }
                Text("🙏 Shared Prayer Board", style = MaterialTheme.typography.headlineMedium)
                Text("Prayer requests visible to everyone in your group.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (prayers.isEmpty()) {
                        item { Text("No prayers yet. Be the first to add one!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f)) }
                    }
                    items(prayers) { p ->
                        Card(shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(0.3f))) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(p, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = prayer, onValueChange = { prayer = it }, placeholder = { Text("Add a prayer request…") },
                        modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = { if (prayer.isNotBlank()) { prayers = prayers + prayer; prayer = "" } },
                        modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary, CircleShape)) {
                        Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Church Groups") },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp,
            bottom = 100.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Study Scripture together with your community.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f)) }
            if (groups.isEmpty()) {
                item { com.sacredpath.app.ui.components.EmptyState("👥","No groups yet","Create a group or join one with a code.") }
            }
            items(groups) { g ->
                Card(modifier = Modifier.fillMaxWidth().clickable { selected = g; prayers = listOf() },
                    shape = RoundedCornerShape(14.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(g.name.first().uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(g.name, style = MaterialTheme.typography.labelLarge)
                            Text("${g.code} · ${g.role}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showCreate = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Create Group")
                    }
                    OutlinedButton(onClick = { showJoin = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Join Group")
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(onDismissRequest = { showCreate = false; newName = "" },
            title = { Text("Create a Group") },
            text = { OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Group name") }, singleLine = true) },
            confirmButton = { TextButton(onClick = {
                if (newName.isNotBlank()) { groups = groups + LocalGroup(generateCode(), newName.trim(), "admin"); newName = ""; showCreate = false }
            }) { Text("Create & Share") } },
            dismissButton = { TextButton(onClick = { showCreate = false; newName = "" }) { Text("Cancel") } })
    }

    if (showJoin) {
        AlertDialog(onDismissRequest = { showJoin = false; joinCode = "" },
            title = { Text("Join a Group") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ask your group leader for their group code.")
                OutlinedTextField(value = joinCode, onValueChange = { joinCode = it.uppercase() }, label = { Text("Code (e.g. DOVE-1234)") }, singleLine = true)
            }},
            confirmButton = { TextButton(onClick = {
                if (joinCode.isNotBlank()) { groups = groups + LocalGroup(joinCode.uppercase(), "Group $joinCode", "member"); joinCode = ""; showJoin = false }
            }) { Text("Join") } },
            dismissButton = { TextButton(onClick = { showJoin = false; joinCode = "" }) { Text("Cancel") } })
    }
}
