package com.rudra.lifeledge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rudra.lifeledge.ui.screens.dashboard.DashboardScreen
import com.rudra.lifeledge.ui.screens.work.WorkScreen
import com.rudra.lifeledge.ui.screens.finance.FinanceScreen
import com.rudra.lifeledge.ui.screens.savings.SavingsScreen
import com.rudra.lifeledge.ui.screens.transfer.TransferScreen
import com.rudra.lifeledge.ui.screens.habits.HabitsScreen
import com.rudra.lifeledge.ui.screens.journal.JournalScreen
import com.rudra.lifeledge.ui.screens.goals.GoalsScreen
import com.rudra.lifeledge.ui.screens.goals.AddGoalsScreen
import com.rudra.lifeledge.ui.screens.income.IncomeScreen
import com.rudra.lifeledge.ui.screens.expense.ExpenseScreen
import com.rudra.lifeledge.ui.screens.reports.ReportsScreen
import com.rudra.lifeledge.ui.screens.settings.SettingsScreen
import com.rudra.lifeledge.ui.screens.settings.BackupScreen
import com.rudra.lifeledge.ui.screens.settings.ExportScreen
import com.rudra.lifeledge.ui.screens.more.MoreScreen

@Composable
fun LifeLedgerNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Work.route) {
            WorkScreen(navController = navController)
        }
        composable(Screen.Finance.route) {
            FinanceScreen(navController = navController)
        }
        composable("savings") {
            SavingsScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable("transfer") {
            TransferScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable("income") {
            IncomeScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable("expense") {
            ExpenseScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable(Screen.Habits.route) {
            HabitsScreen(navController = navController)
        }
        composable(Screen.Journal.route) {
            JournalScreen(navController = navController)
        }
        composable(Screen.Goals.route) {
            GoalsScreen(navController = navController)
        }
        composable("add_goal") {
            AddGoalsScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable("backup") {
            BackupScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable("export") {
            ExportScreen(navController = navController) {
                navController.popBackStack()
            }
        }
        composable("more") {
            MoreScreen(navController = navController)
        }
    }
}
