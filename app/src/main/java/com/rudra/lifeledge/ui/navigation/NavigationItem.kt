package com.rudra.lifeledge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val description: String? = null
) {
    object Onboarding : NavigationItem("onboarding", "Onboarding", Icons.Default.Dashboard)
    
    object Dashboard : NavigationItem(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Dashboard,
        description = "Your daily summary"
    )

    object Calendar : NavigationItem(
        route = "calendar",
        title = "Calendar",
        icon = Icons.Default.CalendarToday,
        description = "View your work logs"
    )

    object Analytics : NavigationItem(
        route = "analytics",
        title = "Analytics",
        icon = Icons.Default.Analytics,
        description = "Visualize your progress"
    )

    object Settings : NavigationItem(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings,
        description = "Customize the app"
    )

    object MonthlyReport : NavigationItem(
        route = "monthly_report",
        title = "Monthly Report",
        icon = Icons.Default.PieChart,
        description = "Detailed monthly summaries"
    )

    object WorkTimer : NavigationItem(
        route = "work_timer",
        title = "Work Timer",
        icon = Icons.Default.Timer,
        description = "Track your work sessions"
    )

    object Finance : NavigationItem(
        route = "finance",
        title = "Finance",
        icon = Icons.Default.AccountBalance,
        description = "Manage your finances"
    )

    object Work : NavigationItem(
        route = "work",
        title = "Work Center",
        icon = Icons.Default.Work,
        description = "Track hours & productivity"
    )

    object Goals : NavigationItem(
        route = "goals",
        title = "Goals",
        icon = Icons.Default.Flag,
        description = "Set & achieve milestones"
    )

    object Expense : NavigationItem(
        route = "expense",
        title = "Expense Log",
        icon = Icons.Default.AttachMoney,
        description = "Log your expenses"
    )
    
    object Income : NavigationItem(
        route = "income",
        title = "Income",
        icon = Icons.Default.AttachMoney,
        description = "Track your income"
    )

    object Health : NavigationItem(
        route = "health",
        title = "Health Metrics",
        icon = Icons.Default.Favorite,
        description = "Monitor your well-being"
    )

    object Focus : NavigationItem(
        route = "focus",
        title = "Focus Sessions",
        icon = Icons.Default.FilterCenterFocus,
        description = "Improve your concentration"
    )

    object Habit : NavigationItem(
        route = "habit",
        title = "Habit Tracker",
        icon = Icons.Default.CheckCircle,
        description = "Build positive habits"
    )

    object Achievements : NavigationItem(
        route = "achievements",
        title = "Achievements",
        icon = Icons.Default.EmojiEvents,
        description = "Celebrate your milestones"
    )

    object Journal : NavigationItem(
        route = "journal",
        title = "Daily Journal",
        icon = Icons.Default.Book,
        description = "Reflect on your day"
    )

    object MindfulBreak : NavigationItem(
        route = "mindful_break",
        title = "Mindful Break",
        icon = Icons.Default.SelfImprovement,
        description = "Relax and recharge"
    )

    object Wisdom : NavigationItem(
        route = "wisdom",
        title = "Wisdom Library",
        icon = Icons.AutoMirrored.Filled.LibraryBooks,
        description = "Get inspired"
    )

    object Calculation : NavigationItem(
        route = "calculation",
        title = "Calculation",
        icon = Icons.Default.Calculate,
        description = "Meal and overtime rates"
    )

    object Backup : NavigationItem(
        route = "backup",
        title = "Backup & Restore",
        icon = Icons.Default.Backup,
        description = "Secure your data"
    )

    object AllFunsion : NavigationItem(
        route = "all_funsion",
        title = "All Features",
        icon = Icons.Default.AddRoad,
        description = "Explore all app features"
    )

    object UserProfile : NavigationItem(
        route = "user_profile",
        title = "User Profile",
        icon = Icons.Default.Person,
        description = "Manage your profile"
    )

    object AddEntry : NavigationItem(
        route = "add_entry",
        title = "Add Entry",
        icon = Icons.Default.Add,
        description = "Log your work"
    )

    object Reports : NavigationItem(
        route = "reports",
        title = "Reports",
        icon = Icons.Default.Assessment,
        description = "Generate work reports"
    )
    
    object FinancialStatement : NavigationItem(
        route = "financial_statement",
        title = "Financial Statement",
        icon = Icons.Default.Assessment,
        description = "View all financial transactions"
    )
    
    object Savings : NavigationItem(
        route = "savings",
        title = "Savings",
        icon = Icons.Default.Savings,
        description = "Manage your savings"
    )
    
    object AddSavings : NavigationItem(
        route = "add_savings",
        title = "Add Savings",
        icon = Icons.Default.Add,
        description = "Add a new savings goal"
    )
    
    object Loans : NavigationItem(
        route = "loans",
        title = "Loans",
        icon = Icons.Default.RealEstateAgent,
        description = "Manage your loans"
    )
    
    object EMI : NavigationItem(
        route = "emi",
        title = "EMI",
        icon = Icons.Default.Payment,
        description = "Manage your EMIs"
    )
    
    object CreditCard : NavigationItem(
        route = "cards",
        title = "Credit Card",
        icon = Icons.Default.CreditCard,
        description = "Manage your credit cards"
    )
    
    object Transfer : NavigationItem(
        route = "transfer",
        title = "Transfer",
        icon = Icons.Default.SwapHoriz,
        description = "Move money between accounts"
    )
    
    object Team : NavigationItem(
        route = "team",
        title = "Team",
        icon = Icons.Default.Group,
        description = "Manage your teams"
    )
    
    object Overtime : NavigationItem(
        route = "overtime",
        title = "Overtime",
        icon = Icons.Default.Group,
        description = "Over time calculation"
    )
    
    object Scheduler : NavigationItem(
        route = "scheduler",
        title = "Scheduler",
        icon = Icons.Default.Schedule,
        description = "Schedule your tasks"
    )

    object More : NavigationItem(
        route = "more",
        title = "More",
        icon = Icons.Default.MoreVert,
        description = "More options"
    )
}
