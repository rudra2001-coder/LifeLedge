package com.rudra.lifeledge.ui.screens.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Achievement(
    val id: Int,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val isUnlocked: Boolean,
    val progress: Float = 0f,
    val unlockedDate: String? = null
)

@Composable
fun AchievementsScreen() {
    val achievements = remember {
        listOf(
            Achievement(1, "First Step", "Complete your first habit", Icons.Default.FirstPage, Color(0xFF22C55E), true, 1f, "Jan 15"),
            Achievement(2, "Week Warrior", "Maintain a 7-day streak", Icons.Default.LocalFireDepartment, Color(0xFFF59E0B), true, 1f, "Jan 22"),
            Achievement(3, "Money Saver", "Save \$100 for the first time", Icons.Default.Savings, Color(0xFF3B82F6), true, 1f, "Feb 1"),
            Achievement(4, "Month Master", "Complete a full month of habits", Icons.Default.EmojiEvents, Color(0xFF8B5CF6), false, 0.7f),
            Achievement(5, "Early Bird", "Log 5 days before 7 AM", Icons.Default.WbSunny, Color(0xFFEC4899), true, 1f, "Jan 20"),
            Achievement(6, "Night Owl", "Log activity after 10 PM", Icons.Default.NightsStay, Color(0xFF6366F1), false, 0.4f),
            Achievement(7, "Finance Guru", "Track expenses for 30 days", Icons.Default.AccountBalance, Color(0xFF10B981), false, 0.6f),
            Achievement(8, "Work Champion", "Log 20 work days", Icons.Default.Work, Color(0xFFEF4444), true, 1f, "Feb 15"),
            Achievement(9, "Zen Master", "Complete 50 meditation sessions", Icons.Default.SelfImprovement, Color(0xFF06B6D4), false, 0.3f),
            Achievement(10, "Perfect Week", "Complete all habits for 7 days", Icons.Default.Star, Color(0xFFF59E0B), false, 0.85f),
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Unlocked", "In Progress")

    val filteredAchievements = when (selectedTab) {
        0 -> achievements
        1 -> achievements.filter { it.isUnlocked }
        2 -> achievements.filter { !it.isUnlocked }
        else -> achievements
    }

    val unlockedCount = achievements.count { it.isUnlocked }
    val totalPoints = achievements.filter { it.isUnlocked }.sumOf { (it.progress * 100).toInt() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$unlockedCount/${achievements.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Unlocked",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$totalPoints",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Points",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAchievements) { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (achievement.isUnlocked)
                    achievement.color.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        achievement.icon,
                        contentDescription = null,
                        tint = if (achievement.isUnlocked)
                            achievement.color
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!achievement.isUnlocked && achievement.progress > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { achievement.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "${(achievement.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (achievement.isUnlocked && achievement.unlockedDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Unlocked ${achievement.unlockedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (achievement.isUnlocked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
