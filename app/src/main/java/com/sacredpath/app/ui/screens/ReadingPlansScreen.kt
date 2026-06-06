package com.sacredpath.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class ReadingPlan(
    val id: String,
    val name: String,
    val days: Int,
    val description: String,
    val emoji: String
)

val READING_PLANS = listOf(
    ReadingPlan("chronological",  "Chronological Bible",          365, "Read the Bible in the order events occurred",         "📅"),
    ReadingPlan("nt_90",          "New Testament in 90 Days",      90, "Complete the New Testament in 3 months",              "✝️"),
    ReadingPlan("ot_90",          "Old Testament in 90 Days",      90, "Read all 39 OT books in 3 months",                    "📜"),
    ReadingPlan("psalms_30",      "Psalms & Proverbs in 30 Days",  30, "Wisdom literature in a month",                        "🎶"),
    ReadingPlan("gospels_28",     "The Four Gospels in 28 Days",   28, "Matthew, Mark, Luke, and John",                       "🕊️"),
    ReadingPlan("genesis_rev",    "Genesis to Revelation",        365, "Classic cover-to-cover reading plan",                 "🌍"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPlansScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Plans") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 80.dp, start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Choose a plan and read through God's Word systematically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(READING_PLANS) { plan ->
                Card(
                    modifier  = Modifier.fillMaxWidth().clickable { /* open plan detail */ },
                    shape     = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(plan.emoji, fontSize = 32.sp)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(plan.name, style = MaterialTheme.typography.labelLarge)
                            Text(plan.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(
                                "${plan.days} days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}
