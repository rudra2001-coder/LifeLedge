package com.rudra.lifeledge.ui.navigation

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rudra.lifeledge.ui.screens.placeholder.*
import com.rudra.lifeledge.ui.screens.analytics.AnalyticsScreen
import com.rudra.lifeledge.ui.screens.achievements.AchievementsScreen
import com.rudra.lifeledge.ui.screens.focus.FocusScreen
import com.rudra.lifeledge.ui.screens.health.HealthMetricsScreen
import com.rudra.lifeledge.ui.screens.profile.ProfileScreen
import com.rudra.lifeledge.ui.screens.profile.ProfileSetupScreen
import com.rudra.lifeledge.ui.screens.work.WorkTimerScreen
import com.rudra.lifeledge.ui.screens.backup.BackupScreen
import com.rudra.lifeledge.ui.screens.calendar.CalendarScreen
import com.rudra.lifeledge.ui.screens.cards.CardsScreen
import com.rudra.lifeledge.ui.screens.dashboard.DashboardScreen
import com.rudra.lifeledge.ui.screens.expense.ExpenseScreen
import com.rudra.lifeledge.ui.screens.habits.HabitsScreen
import com.rudra.lifeledge.ui.screens.income.IncomeScreen
import com.rudra.lifeledge.ui.screens.journal.JournalScreen
import com.rudra.lifeledge.ui.screens.onboarding.OnboardingScreen
import com.rudra.lifeledge.ui.screens.reports.ReportsScreen
import com.rudra.lifeledge.ui.screens.savings.SavingsScreen
import com.rudra.lifeledge.ui.screens.settings.SettingsScreen
import com.rudra.lifeledge.ui.screens.transfer.TransferScreen
import com.rudra.lifeledge.ui.screens.more.MoreScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE) }
    val isOnboardingCompleted = remember { sharedPreferences.getBoolean("onboarding_completed", false) }
    val startDestination = if (isOnboardingCompleted) NavigationItem.Dashboard.route else NavigationItem.Onboarding.route

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBars = currentRoute != NavigationItem.Onboarding.route

    val navigationItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.AddEntry,
        NavigationItem.Reports,
        NavigationItem.Journal,
        NavigationItem.WorkTimer,
        NavigationItem.Focus,
        NavigationItem.MindfulBreak,
        NavigationItem.Habit,
        NavigationItem.Expense,
        NavigationItem.Income,
        NavigationItem.Health,
        NavigationItem.Achievements,
        NavigationItem.Calendar,
        NavigationItem.Analytics,
        NavigationItem.MonthlyReport,
        NavigationItem.Calculation,
        NavigationItem.FinancialStatement,
        NavigationItem.Savings,
        NavigationItem.Loans,
        NavigationItem.EMI,
        NavigationItem.CreditCard,
        NavigationItem.Transfer,
        NavigationItem.Backup,
        NavigationItem.Settings,
        NavigationItem.Team,
        NavigationItem.Overtime,
        NavigationItem.Scheduler,
        NavigationItem.UserProfile
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                navigationItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = item.route == currentRoute,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (shouldShowBars) {
                    TopAppBar(
                        title = { Text("LifeLedger") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (shouldShowBars) {
                    AppBottomNavigation(
                        navController = navController,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(NavigationItem.Onboarding.route) {
                    OnboardingScreen(
                        onOnboardingFinished = {
                            sharedPreferences.edit().putBoolean("onboarding_completed", true).apply()
                            navController.navigate(NavigationItem.Dashboard.route) {
                                popUpTo(NavigationItem.Onboarding.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }
                composable(
                    route = NavigationItem.Dashboard.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    DashboardScreen(
                        navController = navController
                    )
                }

                composable(
                    route = NavigationItem.Calendar.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    CalendarScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.Analytics.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.Settings.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    SettingsScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.More.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    MoreScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.MonthlyReport.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    MonthlyReportScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.WorkTimer.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    WorkTimerScreen()
                }

                composable(
                    route = NavigationItem.Expense.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    ExpenseScreen(navController = navController, onNavigateBack = { navController.popBackStack() })
                }
                
                composable(
                    route = NavigationItem.Income.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    IncomeScreen(navController = navController, onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.Health.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    HealthMetricsScreen()
                }

                composable(
                    route = NavigationItem.Focus.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    FocusScreen()
                }

                composable(
                    route = NavigationItem.Habit.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    HabitsScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.Achievements.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    AchievementsScreen()
                }

                composable(
                    route = NavigationItem.Journal.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    JournalScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.MindfulBreak.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    MindfulBreakScreen()
                }

                composable(
                    route = NavigationItem.Wisdom.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    WisdomScreen()
                }

                composable(
                    route = NavigationItem.Calculation.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    CalculationScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.Backup.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    BackupScreen(navController = navController, onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.AllFunsion.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    AllFunsionScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.AddEntry.route + "?workLogId={workLogId}",
                    arguments = listOf(navArgument("workLogId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }),
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    AddEntryScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.Reports.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    ReportsScreen(navController = navController)
                }
                
                composable(
                    route = NavigationItem.FinancialStatement.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    FinancialStatementScreen()
                }
                
                composable(
                    route = NavigationItem.Savings.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    SavingsScreen(navController = navController, onNavigateBack = { navController.popBackStack() })
                }
                
                composable(
                    route = NavigationItem.Loans.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    LoansScreen()
                }
                
                composable(
                    route = NavigationItem.EMI.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    EmiScreen()
                }
                
                composable(
                    route = NavigationItem.CreditCard.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    CardsScreen(onNavigateBack = { navController.popBackStack() })
                }
                
                composable(
                    route = NavigationItem.Transfer.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    TransferScreen(navController = navController, onNavigateBack = { navController.popBackStack() })
                }
                
                composable(
                    route = NavigationItem.Team.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    TeamScreen(onNavigateBack = { navController.popBackStack() })
                }
                
                composable(
                    route = NavigationItem.Overtime.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    OvertimeScreen()
                }
                
                composable(
                    route = NavigationItem.Scheduler.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    SchedulerScreen()
                }
                
                composable(NavigationItem.UserProfile.route) {
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSetup = { navController.navigate("profile_setup") }
                    )
                }
                
                composable("profile_setup") {
                    ProfileSetupScreen(
                        onProfileSaved = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterTransition() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitTransition() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopEnterTransition() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopExitTransition() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400)
    )

@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Calendar,
        NavigationItem.Analytics,
        NavigationItem.More,
        NavigationItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
