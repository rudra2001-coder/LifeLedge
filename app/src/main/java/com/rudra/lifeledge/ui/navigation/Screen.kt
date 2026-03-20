package com.rudra.lifeledge.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Work : Screen("work")
    object Finance : Screen("finance")
    object Habits : Screen("habits")
    object Journal : Screen("journal")
    object Goals : Screen("goals")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object AddTransaction : Screen("add_transaction")
    object AddHabit : Screen("add_habit")
    object AddJournalEntry : Screen("add_journal_entry")
    object AddGoal : Screen("add_goal")
    object WorkDayDetail : Screen("work_day_detail/{date}") {
        fun createRoute(date: String) = "work_day_detail/$date"
    }
    object TransactionDetail : Screen("transaction_detail/{id}") {
        fun createRoute(id: Long) = "transaction_detail/$id"
    }
    object HabitDetail : Screen("habit_detail/{id}") {
        fun createRoute(id: Long) = "habit_detail/$id"
    }
    object GoalDetail : Screen("goal_detail/{id}") {
        fun createRoute(id: Long) = "goal_detail/$id"
    }
    object Savings : Screen("savings")
    object AddSavings : Screen("add_savings")
    object Transfer : Screen("transfer")
    object Income : Screen("income")
    object Expense : Screen("expense")
    object Backup : Screen("backup")
    object Export : Screen("export")
    object More : Screen("more")
    object Goal : Screen("add_goal")
    object RecurringTransactions : Screen("recurring_transactions")
    object Cards : Screen("cards")
    object Calendar : Screen("calendar")
    object Onboarding : Screen("onboarding")
}
