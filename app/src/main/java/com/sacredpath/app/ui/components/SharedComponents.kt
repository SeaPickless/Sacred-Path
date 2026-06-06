package com.sacredpath.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sacredpath.app.data.model.DailyVerse
import com.sacredpath.app.data.model.Testament
import androidx.compose.foundation.BorderStroke

// ── XP Progress Bar ───────────────────────────────────────────────────────────
@Composable
fun XPProgressBar(
    xp: Int,
    minXP: Int,
    maxXP: Int,
    color: Color,
    modifier: Modifier = Modifier,
    height: Int = 10
) {
    val progress = if (maxXP > minXP)
        ((xp - minXP).toFloat() / (maxXP - minXP)).coerceIn(0f, 1f)
    else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "xp_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height.dp))
                .background(color)
        )
    }
}

// ── Streak Badge ──────────────────────────────────────────────────────────────
@Composable
fun StreakBadge(streak: Int) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFFF6B2B).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color(0xFFFF6B2B).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("🔥", fontSize = 16.sp)
            Text(
                text  = streak.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFF6B2B)
            )
        }
    }
}

// ── Verse Card (Verse of the Day) ─────────────────────────────────────────────
@Composable
fun VerseCard(
    verse: DailyVerse,
    onNavigate: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .then(if (onNavigate != null) Modifier.clickable { onNavigate() } else Modifier),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Quote mark
            Text(
                text  = "\u201C",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 24.sp
            )

            // Verse text
            Text(
                text  = verse.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = verse.reference,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontStyle = FontStyle.Italic
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Testament badge
                    Surface(
                        shape = CircleShape,
                        color = if (verse.testament == Testament.OLD)
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (verse.testament == Testament.OLD) "📜 OT" else "✝️ NT",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (verse.testament == Testament.OLD)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (onShare != null) {
                        IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        trailing?.invoke()
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(
            text      = title,
            style     = MaterialTheme.typography.headlineMedium,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Text(
            text      = subtitle,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

// ── Loading indicator ─────────────────────────────────────────────────────────
@Composable
fun FullScreenLoading(message: String = "Loading…") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}
