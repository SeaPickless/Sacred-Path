package com.sacredpath.app.ui.screens.kids

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke

data class KidsStory(val emoji: String, val title: String, val subtitle: String, val ref: String, val color: Color)
data class KidsQuestion(val q: String, val opts: List<String>, val correct: Int)

val KIDS_STORIES = listOf(
    KidsStory("🌍","Creation",         "God makes the world",       "Genesis 1",  Color(0xFF4CAF50)),
    KidsStory("🚢","Noah's Ark",       "A big boat & lots of animals","Genesis 6-9",Color(0xFF2196F3)),
    KidsStory("🌈","God's Promise",    "The rainbow covenant",      "Genesis 9",  Color(0xFFFF9800)),
    KidsStory("🦁","Daniel & Lions",   "God keeps Daniel safe",     "Daniel 6",   Color(0xFFF44336)),
    KidsStory("⭐","Baby Jesus",       "The night Jesus was born",  "Luke 2",     Color(0xFF9C27B0)),
    KidsStory("🐑","The Lost Sheep",   "Jesus looks for us",        "Luke 15",    Color(0xFF8BC34A)),
    KidsStory("🍞","Feeding 5,000",    "A boy shares his lunch",    "John 6",     Color(0xFFFFC107)),
    KidsStory("🦋","Jesus is Alive!",  "The Easter story",          "Luke 24",    Color(0xFFE91E63)),
)

val KIDS_QUIZ = listOf(
    KidsQuestion("Who made the world?",               listOf("God","Noah","Adam","Moses"),     0),
    KidsQuestion("Who built the big boat?",           listOf("Moses","David","Noah","Abraham"),2),
    KidsQuestion("How many disciples did Jesus have?",listOf("10","11","12","13"),             2),
    KidsQuestion("Who was swallowed by a big fish?",  listOf("Elijah","Daniel","Jonah","Ezra"),2),
    KidsQuestion("Where was Jesus born?",             listOf("Jerusalem","Bethlehem","Nazareth","Jericho"),1),
    KidsQuestion("What did Jesus turn water into?",   listOf("Juice","Milk","Wine","Tea"),     2),
    KidsQuestion("Who was Daniel thrown to?",         listOf("Bears","Lions","Wolves","Tigers"),1),
    KidsQuestion("How many days did God take to make everything?",listOf("3","5","6","7"),     2),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsHomeScreen(navController: NavController) {
    var showQuiz   by remember { mutableStateOf(false) }
    var qIndex     by remember { mutableStateOf(0) }
    var score      by remember { mutableStateOf(0) }
    var answered   by remember { mutableStateOf(false) }
    var selected   by remember { mutableStateOf(-1) }
    var showStory  by remember { mutableStateOf<KidsStory?>(null) }
    var showExit   by remember { mutableStateOf(false) }
    var pin        by remember { mutableStateOf("") }

    val kidsBg     = Color(0xFFFFF8E1)
    val kidsOrange = Color(0xFFFF6B35)

    if (showQuiz) {
        val q = KIDS_QUIZ[qIndex]
        Scaffold(
            containerColor = kidsBg,
            topBar = {
                TopAppBar(
                    title = { Text("🧠 Bible Quiz!", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = { IconButton(onClick = { showQuiz = false }) { Icon(Icons.Default.ArrowBack, null) } },
                    actions = { Text("⭐ $score", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = kidsOrange,
                        titleContentColor = Color.White, navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Question ${qIndex + 1} of ${KIDS_QUIZ.size}",
                    style = MaterialTheme.typography.labelSmall, color = kidsOrange)
                Text(q.q, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp)
                q.opts.forEachIndexed { idx, opt ->
                    val bg = when {
                        !answered            -> Color.White
                        idx == q.correct     -> Color(0xFFC8F0C8)
                        idx == selected      -> Color(0xFFF0C8C8)
                        else                 -> Color.White
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !answered) {
                            selected = idx; answered = true
                            if (idx == q.correct) score++
                        },
                        shape  = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = bg),
                        border = BorderStroke(2.dp, Color(0xFFFFB74D))
                    ) {
                        Box(modifier = Modifier.padding(14.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(opt, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                if (answered) {
                    Button(
                        onClick = {
                            if (qIndex + 1 >= KIDS_QUIZ.size) {
                                qIndex = 0; score = 0; answered = false; selected = -1; showQuiz = false
                            } else { qIndex++; answered = false; selected = -1 }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = kidsOrange)
                    ) {
                        Text(if (qIndex + 1 >= KIDS_QUIZ.size) "🎉 Finish!" else "Next →",
                            fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
        return
    }

    if (showStory != null) {
        val s = showStory!!
        Scaffold(
            containerColor = kidsBg,
            topBar = {
                TopAppBar(
                    title = { Text("${s.emoji} ${s.title}", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { showStory = null }) { Icon(Icons.Default.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = s.color,
                        titleContentColor = Color.White, navigationIconContentColor = Color.White)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).padding(24.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(s.emoji, fontSize = 80.sp)
                Text(s.title, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                Text(s.subtitle, fontSize = 18.sp, color = Color(0xFF5D4037))
                Text("From ${s.ref}", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    "This story is from ${s.ref}. Open the Bible tab in Sacred Path to read the full story with your family!",
                    fontSize = 16.sp, color = Color(0xFF3E2723)
                )
                Button(onClick = { showStory = null },
                    colors = ButtonDefaults.buttonColors(containerColor = s.color)) {
                    Text("Back to Stories 📚", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
        return
    }

    // Main kids home
    Scaffold(
        containerColor = kidsBg,
        topBar = {
            TopAppBar(
                title = { Text("📖 Kids Bible!", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { showExit = true }) {
                        Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = kidsOrange,
                    titleContentColor = Color.White, actionIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quiz banner
            Card(
                modifier = Modifier.fillMaxWidth().clickable { showQuiz = true; qIndex = 0; score = 0 },
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
                border = BorderStroke(2.dp, Color(0xFFFF9800))
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("🧠", fontSize = 40.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bible Quiz!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFE65100))
                        Text("Test your Bible knowledge!", color = Color(0xFFBF360C))
                    }
                    Text("→", fontSize = 24.sp, color = Color(0xFFE65100))
                }
            }

            // Stories
            Text("Bible Stories", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFD84315))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                KIDS_STORIES.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { story ->
                            Card(
                                modifier = Modifier.weight(1f).clickable { showStory = story },
                                shape  = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = story.color.copy(0.15f)),
                                border = BorderStroke(2.dp, story.color)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(story.emoji, fontSize = 36.sp)
                                    Text(story.title, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                        color = Color(0xFF3E2723))
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    // PIN exit dialog
    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false; pin = "" },
            title = { Text("🔐 Parent PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter PIN to exit Kids Mode")
                    OutlinedTextField(
                        value         = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true, placeholder = { Text("• • • •") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pin == "1234") { navController.popBackStack(); showExit = false }
                    pin = ""
                }) { Text("Exit") }
            },
            dismissButton = {
                TextButton(onClick = { showExit = false; pin = "" }) { Text("Cancel") }
            }
        )
    }
}
