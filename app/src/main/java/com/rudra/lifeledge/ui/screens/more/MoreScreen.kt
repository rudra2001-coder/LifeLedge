package com.rudra.lifeledge.ui.screens.more

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rudra.lifeledge.ui.navigation.Screen
import com.rudra.lifeledge.ui.theme.*
import kotlinx.coroutines.delay

// Enhanced data class with more properties
data class MoreMenuItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val iconOutline: ImageVector? = null,
    val color: Color,
    val gradient: List<Color>? = null,
    val badge: String? = null,
    val route: String? = null,
    val onClick: (() -> Unit)? = null
)

data class MoreSection(
    val title: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val items: List<MoreMenuItem>
)

// Updated sections with gradients and badges
val financialSection = MoreSection(
    title = "Finance Hub",
    icon = Icons.Outlined.AccountBalance,
    gradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
    items = listOf(
        MoreMenuItem(
            id = "income",
            title = "Add Income",
            subtitle = "Record your earnings",
            icon = Icons.Filled.TrendingUp,
            iconOutline = Icons.Outlined.TrendingUp,
            color = Color(0xFF10B981),
            badge = "NEW",
            route = Screen.Income.route
        ),
        MoreMenuItem(
            id = "expense",
            title = "Add Expense",
            subtitle = "Track your spending",
            icon = Icons.Filled.TrendingDown,
            iconOutline = Icons.Outlined.TrendingDown,
            color = Color(0xFFEF4444),
            badge = "5 today",
            route = Screen.Expense.route
        ),
        MoreMenuItem(
            id = "savings",
            title = "Add Savings",
            subtitle = "Build your wealth",
            icon = Icons.Filled.Savings,
            iconOutline = Icons.Outlined.Savings,
            color = Color(0xFFF59E0B),
            route = Screen.AddSavings.route
        ),
        MoreMenuItem(
            id = "manage_savings",
            title = "Manage Savings",
            subtitle = "View goals progress",
            icon = Icons.Filled.Savings,
            iconOutline = Icons.Outlined.Savings,
            color = Color(0xFF8B5CF6),
            badge = "80%",
            route = Screen.Savings.route
        ),
        MoreMenuItem(
            id = "transfer",
            title = "Transfer",
            subtitle = "Move money between accounts",
            icon = Icons.Filled.SwapHoriz,
            iconOutline = Icons.Outlined.SwapHoriz,
            color = Color(0xFF6366F1),
            route = Screen.Transfer.route
        ),
        MoreMenuItem(
            id = "cards",
            title = "Cards & Wallets",
            subtitle = "Digital & physical cards",
            icon = Icons.Filled.CreditCard,
            iconOutline = Icons.Outlined.CreditCard,
            color = Color(0xFFEC4899),
            route = Screen.Cards.route
        ),
        MoreMenuItem(
            id = "accounts",
            title = "Accounts",
            subtitle = "Bank & investment accounts",
            icon = Icons.Filled.AccountBalance,
            iconOutline = Icons.Outlined.AccountBalance,
            color = Color(0xFF3B82F6),
            route = Screen.Finance.route
        ),
        MoreMenuItem(
            id = "budgets",
            title = "Budgets",
            subtitle = "Set spending limits",
            icon = Icons.Filled.PieChart,
            iconOutline = Icons.Outlined.PieChart,
            color = Color(0xFFF97316),
            route = Screen.Finance.route
        ),
        MoreMenuItem(
            id = "recurring",
            title = "Recurring",
            subtitle = "Auto transactions",
            icon = Icons.Filled.Repeat,
            iconOutline = Icons.Outlined.Repeat,
            color = Color(0xFF8B5CF6),
            route = Screen.RecurringTransactions.route
        ),
        MoreMenuItem(
            id = "loans",
            title = "Loans & EMI",
            subtitle = "Track debts & payments",
            icon = Icons.Filled.CreditCard,
            iconOutline = Icons.Outlined.CreditCard,
            color = Color(0xFFEF4444),
            route = Screen.Finance.route
        )
    )
)

val generalSection = MoreSection(
    title = "Life Tools",
    icon = Icons.Outlined.GridView,
    gradient = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
    items = listOf(
        MoreMenuItem(
            id = "work",
            title = "Work Center",
            subtitle = "Track hours & productivity",
            icon = Icons.Filled.Work,
            iconOutline = Icons.Outlined.Work,
            color = Color(0xFF3B82F6),
            route = Screen.Work.route
        ),
        MoreMenuItem(
            id = "habits",
            title = "Habits",
            subtitle = "Build daily routines",
            icon = Icons.Filled.CheckCircle,
            iconOutline = Icons.Outlined.CheckCircle,
            color = Color(0xFF22C55E),
            badge = "3 active",
            route = Screen.Habits.route
        ),
        MoreMenuItem(
            id = "journal",
            title = "Journal",
            subtitle = "Reflect & write thoughts",
            icon = Icons.Filled.Book,
            iconOutline = Icons.Outlined.Book,
            color = Color(0xFF8B5CF6),
            route = Screen.Journal.route
        ),
        MoreMenuItem(
            id = "goals",
            title = "Goals",
            subtitle = "Set & achieve milestones",
            icon = Icons.Filled.Flag,
            iconOutline = Icons.Outlined.Flag,
            color = Color(0xFFF59E0B),
            route = Screen.Goals.route
        ),
        MoreMenuItem(
            id = "reports",
            title = "Reports",
            subtitle = "Analytics & insights",
            icon = Icons.Filled.Analytics,
            iconOutline = Icons.Outlined.Analytics,
            color = Color(0xFF06B6D4),
            route = Screen.Reports.route
        ),
        MoreMenuItem(
            id = "calendar",
            title = "Calendar",
            subtitle = "View all events",
            icon = Icons.Filled.CalendarMonth,
            iconOutline = Icons.Outlined.CalendarMonth,
            color = Color(0xFF14B8A6),
            route = Screen.Calendar.route
        )
    )
)

val settingsSection = MoreSection(
    title = "System",
    icon = Icons.Outlined.Settings,
    gradient = listOf(Color(0xFF6B7280), Color(0xFF4B5563)),
    items = listOf(
        MoreMenuItem(
            id = "settings",
            title = "Settings",
            subtitle = "App preferences & theme",
            icon = Icons.Filled.Settings,
            iconOutline = Icons.Outlined.Settings,
            color = Color(0xFF64748B),
            route = Screen.Settings.route
        ),
        MoreMenuItem(
            id = "backup",
            title = "Backup & Restore",
            subtitle = "Secure your data",
            icon = Icons.Filled.Backup,
            iconOutline = Icons.Outlined.Backup,
            color = Color(0xFF64748B),
            route = Screen.Backup.route
        ),
        MoreMenuItem(
            id = "export",
            title = "Export Data",
            subtitle = "JSON, CSV, PDF formats",
            icon = Icons.Filled.Download,
            iconOutline = Icons.Outlined.Download,
            color = Color(0xFF64748B),
            route = Screen.Export.route
        ),
        MoreMenuItem(
            id = "about",
            title = "About",
            subtitle = "v2.0.0 | Privacy policy",
            icon = Icons.Filled.Info,
            iconOutline = Icons.Outlined.Info,
            color = Color(0xFF64748B),
            route = Screen.Settings.route
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    var selectedItemId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Explore",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                QuickActionsSection(navController)
            }

            item {
                WelcomeCard()
            }

            items(listOf(financialSection, generalSection, settingsSection)) { section ->
                AnimatedSection(
                    section = section,
                    navController = navController,
                    selectedItemId = selectedItemId,
                    onItemClick = { selectedItemId = it }
                )
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Welcome back! 👋",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Track your finances and stay organized",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    var hoveredIndex by remember { mutableStateOf(-1) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    Triple(Icons.Default.TrendingUp, "Income", Screen.Income.route),
                    Triple(Icons.Default.TrendingDown, "Expense", Screen.Expense.route),
                    Triple(Icons.Default.Savings, "Savings", Screen.AddSavings.route),
                    Triple(Icons.Default.SwapHoriz, "Transfer", Screen.Transfer.route)
                ).forEachIndexed { index, (icon, label, route) ->
                    AnimatedQuickActionButton(
                        icon = icon,
                        label = label,
                        isHovered = hoveredIndex == index,
                        onClick = { navController.navigate(route) },
                        onHover = { hoveredIndex = if (it) index else -1 }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedQuickActionButton(
    icon: ImageVector,
    label: String,
    isHovered: Boolean,
    onClick: () -> Unit,
    onHover: (Boolean) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun AnimatedSection(
    section: MoreSection,
    navController: NavController,
    selectedItemId: String?,
    onItemClick: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.horizontalGradient(section.gradient),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        section.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Section Items with Animation
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    section.items.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { item ->
                                AnimatedSectionItem(
                                    item = item,
                                    isSelected = selectedItemId == item.id,
                                    onClick = {
                                        onItemClick(item.id)
                                        item.route?.let { navController.navigate(it) }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSectionItem(
    item: MoreMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                item.color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                item.color.copy(alpha = 0.2f),
                                item.color.copy(alpha = 0.05f)
                            ),
                            center = Offset(20f, 20f),
                            radius = 30f
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected && item.iconOutline != null)
                        item.iconOutline
                    else
                        item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    item.badge?.let { badge ->
                        Badge(
                            containerColor = item.color,
                            modifier = Modifier
                        ) {
                            Text(
                                badge,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    item.subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Press animation handling
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            pressed = false
        }
    }
}