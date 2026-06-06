package com.sacredpath.app.ui.screens.study

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke

// ── StudyHomeScreen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHomeScreen(navController: NavController) {
    val tools = listOf(
        Triple("devotionals",    "Devotionals",    "📅"),
        Triple("topic_explorer", "Topic Explorer", "🗺️"),
        Triple("bible_maps",     "Bible Maps",     "🌍"),
        Triple("bible_timeline", "Bible Timeline", "⏳"),
    )
    Scaffold(topBar = { TopAppBar(title = { Text("Study Tools") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Deepen Your Understanding", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            Spacer(Modifier.height(4.dp))
            tools.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (route, label, emoji) ->
                        Card(modifier = Modifier.weight(1f).clickable { navController.navigate(route) },
                            shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(emoji, fontSize = 32.sp)
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ── DevotionalsScreen ─────────────────────────────────────────────────────────
data class Devotional(val day: Int, val title: String, val verse: String, val verseText: String, val reflection: String, val prayer: String)

val DEVOTIONALS = listOf(
    Devotional(1, "In the Beginning", "Genesis 1:1", "In the beginning God created the heavens and the earth.",
        "Everything starts with God. Before time, before matter, before us — God was. Today, acknowledge that every moment of your life begins and ends in Him.",
        "Lord, remind me today that You are the author of all things, including my story."),
    Devotional(2, "The Good Shepherd", "Psalm 23:1", "The LORD is my shepherd; I shall not want.",
        "A shepherd never abandons his flock. Even in our deepest valleys, God is guiding, protecting, and providing. Rest in His care today.",
        "Shepherd of my soul, lead me in Your paths today. I trust Your guidance over my own understanding."),
    Devotional(3, "Light of the World", "John 8:12", "I am the light of the world. Whoever follows me will never walk in darkness.",
        "Jesus doesn't just point to the light — He is the light. In Him there is no darkness at all. Choose to follow Him and step out of every shadow.",
        "Jesus, shine Your light into every dark corner of my heart and mind. Lead me in truth today."),
    Devotional(4, "Renewed Strength", "Isaiah 40:31", "But those who hope in the LORD will renew their strength.",
        "When we feel empty and exhausted, God is our source of renewal. Waiting on the Lord is not passive — it is an active surrender that unleashes His power in us.",
        "Father, I am weary. Renew me today with Your Spirit. Let me soar above my circumstances."),
    Devotional(5, "The Greatest Commandment", "Matthew 22:37", "Love the Lord your God with all your heart and with all your soul and with all your mind.",
        "Love is not just a feeling — it is the foundation of all obedience to God. When our love for God is whole, everything else falls into its proper place.",
        "Lord, let my love for You grow deeper every day. Help me love You with every part of who I am."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevotionalsScreen(navController: NavController) {
    var selected by remember { mutableStateOf<Devotional?>(null) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Devotionals") },
            navigationIcon = {
                IconButton(onClick = {
                    if (selected != null) selected = null else navController.popBackStack()
                }) { Icon(Icons.Default.ArrowBack, null) }
            })
    }) { padding ->
        if (selected == null) {
            LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(DEVOTIONALS) { d ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { selected = d },
                        shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${d.day}", style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(d.title, style = MaterialTheme.typography.labelLarge)
                                Text(d.verse, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary)
                            }
                            Icon(Icons.Default.ChevronRight, null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                        }
                    }
                }
            }
        } else {
            val d = selected!!
            LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("Day ${d.day}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary)
                    Text(d.title, style = MaterialTheme.typography.displayMedium)
                }
                item {
                    Card(shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(d.verse, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary)
                            Text("\"${d.verseText}\"", style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                item { Text("Reflection", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary) }
                item { Text(d.reflection, style = MaterialTheme.typography.bodyMedium) }
                item { Text("Prayer", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary) }
                item { Text(d.prayer, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)) }
            }
        }
    }
}

// ── TopicExplorerScreen ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicExplorerScreen(navController: NavController) {
    val topics = listOf(
        Pair("Faith",        listOf("Hebrews 11:1","James 2:17","Romans 10:17","Ephesians 2:8")),
        Pair("Love",         listOf("1 Cor 13:4-7","John 3:16","Rom 8:38-39","1 John 4:8")),
        Pair("Salvation",    listOf("John 3:16","Rom 10:9","Eph 2:8-9","Acts 4:12")),
        Pair("Prayer",       listOf("Phil 4:6","Matt 6:9-13","1 Thes 5:17","James 5:16")),
        Pair("Wisdom",       listOf("Prov 3:5-6","James 1:5","Prov 1:7","Eccl 12:13")),
        Pair("Forgiveness",  listOf("Eph 4:32","1 John 1:9","Col 3:13","Matt 6:14")),
        Pair("Hope",         listOf("Rom 15:13","Jer 29:11","Ps 31:24","Heb 11:1")),
        Pair("Grace",        listOf("Eph 2:8","Titus 2:11","2 Cor 12:9","Rom 5:20")),
        Pair("Prophecy",     listOf("Isa 7:14","Micah 5:2","Ps 22:16","Dan 9:25")),
        Pair("Resurrection", listOf("1 Cor 15:20","John 11:25","Rom 6:5","Acts 4:33")),
    )
    var selected by remember { mutableStateOf<Pair<String, List<String>>?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text("Topic Explorer") },
        navigationIcon = { IconButton(onClick = { if (selected != null) selected = null else navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        if (selected == null) {
            LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topics) { t ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { selected = t }, shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(t.first, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            Text("${t.second.size} verses", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                        }
                    }
                }
            }
        } else {
            val (topic, refs) = selected!!
            LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text(topic, style = MaterialTheme.typography.displayMedium) }
                items(refs) { ref ->
                    Card(shape = RoundedCornerShape(10.dp)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.MenuBook, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Text(ref, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

// ── BibleMapsScreen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleMapsScreen(navController: NavController) {
    val maps = listOf(
        Pair("🗺️ Ancient Near East",          "Mesopotamia, Egypt & the Fertile Crescent"),
        Pair("🏔️ Exodus Route",                "Israel's journey out of Egypt"),
        Pair("⚔️ Conquest of Canaan",          "Joshua's campaigns in the Promised Land"),
        Pair("👑 Kingdom of Israel",           "United & Divided Kingdoms"),
        Pair("✈️ Paul's Missionary Journeys",  "Three journeys across the Roman world"),
        Pair("✝️ Jerusalem in Jesus' Time",    "Temple Mount, Gethsemane, Golgotha"),
    )
    Scaffold(topBar = { TopAppBar(title = { Text("Bible Maps") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp,
            bottom = 80.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(maps) { (title, desc) ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(title.take(2), fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title.drop(3), style = MaterialTheme.typography.labelLarge)
                            Text(desc, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                }
            }
            item { Text("Interactive SVG maps coming in next build",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}

// ── BibleTimelineScreen ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleTimelineScreen(navController: NavController) {
    data class TimelineEvent(val era: String, val approx: String, val event: String, val color: Color)
    val events = listOf(
        TimelineEvent("Creation",         "Before Time",   "God creates the heavens and the earth (Genesis 1)",               Color(0xFF4CAF50)),
        TimelineEvent("Patriarchs",       "2000 BC",       "Abraham's call and covenant (Genesis 12)",                         Color(0xFFFF9800)),
        TimelineEvent("Patriarchs",       "1900 BC",       "Joseph sold into Egypt (Genesis 37)",                              Color(0xFFFF9800)),
        TimelineEvent("Exodus",           "1446 BC",       "Moses leads Israel out of Egypt (Exodus 12-14)",                   Color(0xFFF44336)),
        TimelineEvent("Conquest",         "1406 BC",       "Joshua leads Israel into Canaan (Joshua 1-6)",                     Color(0xFF9C27B0)),
        TimelineEvent("United Kingdom",   "1050 BC",       "Saul becomes Israel's first king (1 Samuel 10)",                   Color(0xFF2196F3)),
        TimelineEvent("United Kingdom",   "970 BC",        "Solomon builds the Temple (1 Kings 6)",                            Color(0xFF2196F3)),
        TimelineEvent("Divided Kingdom",  "930 BC",        "Kingdom splits into Israel & Judah (1 Kings 12)",                  Color(0xFF00BCD4)),
        TimelineEvent("Exile",            "586 BC",        "Babylon destroys Jerusalem (2 Kings 25)",                          Color(0xFF607D8B)),
        TimelineEvent("Restoration",      "538 BC",        "Cyrus decrees return from exile (Ezra 1)",                         Color(0xFF8BC34A)),
        TimelineEvent("Intertestamental", "400–5 BC",      "400 years of silence between the Testaments",                      Color(0xFF9E9E9E)),
        TimelineEvent("Life of Jesus",    "5 BC – AD 30",  "Birth, ministry, death & resurrection of Jesus",                   Color(0xFFE91E63)),
        TimelineEvent("Early Church",     "AD 30",         "Pentecost — the Holy Spirit descends (Acts 2)",                    Color(0xFFFF5722)),
        TimelineEvent("Early Church",     "AD 46–58",      "Paul's missionary journeys (Acts 13-28)",                          Color(0xFFFF5722)),
        TimelineEvent("Early Church",     "AD 68–96",      "New Testament writings completed",                                 Color(0xFFFF5722)),
    )
    Scaffold(topBar = { TopAppBar(title = { Text("Bible Timeline") },
        navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        LazyColumn(contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp,
            bottom = 80.dp, start = 16.dp, end = 16.dp)) {
            items(events.size) { i ->
                val e = events[i]
                val isLast = i == events.lastIndex
                Row(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(e.color, CircleShape))
                        if (!isLast) Box(modifier = Modifier.width(2.dp).height(44.dp).background(e.color.copy(0.3f)))
                    }
                    Column(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                        Text(e.era.uppercase(), style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = androidx.compose.ui.unit.TextUnit(0.8f, androidx.compose.ui.unit.TextUnitType.Sp)),
                            color = e.color)
                        Text(e.approx, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                        Text(e.event, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
