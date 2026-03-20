package com.rudra.lifeledge.ui.screens.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Welcome to LifeLedge",
        description = "Your all-in-one personal finance and life management companion. Track your finances, habits, work, and more in one beautiful app.",
        icon = Icons.Default.Lightbulb
    ),
    OnboardingPage(
        title = "Smart Finance Tracking",
        description = "Track income, expenses, savings, and goals. Get intelligent insights and recommendations to improve your financial health.",
        icon = Icons.Default.AccountBalance
    ),
    OnboardingPage(
        title = "Habit Building",
        description = "Build positive habits and track your daily routines. Stay motivated with progress tracking and reminders.",
        icon = Icons.Default.DirectionsRun
    ),
    OnboardingPage(
        title = "Work & Time Management",
        description = "Log your work hours, track overtime, and analyze your productivity patterns over time.",
        icon = Icons.Default.Work
    ),
    OnboardingPage(
        title = "Personal Journal",
        description = "Reflect on your day with a personal journal. Track your thoughts, gratitude, and personal growth.",
        icon = Icons.Default.MenuBook
    ),
    OnboardingPage(
        title = "Goals & Savings",
        description = "Set financial goals and track your savings progress. Visualize your journey to financial freedom.",
        icon = Icons.Default.Savings
    ),
    OnboardingPage(
        title = "Reports & Insights",
        description = "Get detailed reports and analytics on all aspects of your life. Make data-driven decisions.",
        icon = Icons.Default.Timeline
    ),
    OnboardingPage(
        title = "You're All Set!",
        description = "Start your journey to a more organized and fulfilling life. Your data is secure and private.",
        icon = Icons.Default.CheckCircle
    )
)
