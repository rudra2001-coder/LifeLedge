package com.rudra.lifeledge.di

import com.rudra.lifeledge.data.local.database.LifeLedgerDatabase
import com.rudra.lifeledge.data.repository.*
import com.rudra.lifeledge.data.backup.BackupManager
import com.rudra.lifeledge.ui.screens.dashboard.DashboardViewModel
import com.rudra.lifeledge.ui.screens.goals.GoalsViewModel
import com.rudra.lifeledge.ui.screens.goals.AddGoalViewModel
import com.rudra.lifeledge.ui.screens.habits.HabitsViewModel
import com.rudra.lifeledge.ui.screens.journal.JournalViewModel
import com.rudra.lifeledge.ui.screens.savings.SavingsViewModel
import com.rudra.lifeledge.ui.screens.savings.AddSavingsViewModel
import com.rudra.lifeledge.ui.screens.income.IncomeViewModel
import com.rudra.lifeledge.ui.screens.work.WorkViewModel
import com.rudra.lifeledge.ui.screens.backup.BackupViewModel
import com.rudra.lifeledge.ui.screens.expense.ExpenseViewModel
import com.rudra.lifeledge.ui.screens.finance.FinanceViewModel
import com.rudra.lifeledge.ui.screens.transfer.TransferViewModel
import com.rudra.lifeledge.ui.screens.settings.ExportViewModel
import com.rudra.lifeledge.ui.screens.reports.ReportsViewModel
import com.rudra.lifeledge.ui.screens.recurring.RecurringTransactionsViewModel
import com.rudra.lifeledge.ui.screens.cards.CardsViewModel
import com.rudra.lifeledge.ui.screens.calendar.CalendarViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { LifeLedgerDatabase.getDatabase(androidContext()) }
    single { get<LifeLedgerDatabase>().workLogDao() }
    single { get<LifeLedgerDatabase>().workDayDao() }
    single { get<LifeLedgerDatabase>().overtimeLogDao() }
    single { get<LifeLedgerDatabase>().workSessionDao() }
    single { get<LifeLedgerDatabase>().accountDao() }
    single { get<LifeLedgerDatabase>().transactionDao() }
    single { get<LifeLedgerDatabase>().categoryDao() }
    single { get<LifeLedgerDatabase>().recurringTransactionDao() }
    single { get<LifeLedgerDatabase>().loanDao() }
    single { get<LifeLedgerDatabase>().emiPaymentDao() }
    single { get<LifeLedgerDatabase>().creditCardDao() }
    single { get<LifeLedgerDatabase>().habitDao() }
    single { get<LifeLedgerDatabase>().habitCompletionDao() }
    single { get<LifeLedgerDatabase>().journalEntryDao() }
    single { get<LifeLedgerDatabase>().dailyLogDao() }
    single { get<LifeLedgerDatabase>().goalDao() }
    single { get<LifeLedgerDatabase>().healthMetricDao() }
    single { get<LifeLedgerDatabase>().settingDao() }
    single { get<LifeLedgerDatabase>().backupLogDao() }
    single { get<LifeLedgerDatabase>().smartAdviceLogDao() }
    single { get<LifeLedgerDatabase>().savingGoalDao() }
    single { get<LifeLedgerDatabase>().savingTransactionDao() }
    single { get<LifeLedgerDatabase>().cardDao() }
    single { get<LifeLedgerDatabase>().activityLogDao() }
    single { get<LifeLedgerDatabase>().monthlySummaryDao() }
    single { get<LifeLedgerDatabase>().dailySummaryDao() }
    single { get<LifeLedgerDatabase>().categorySummaryDao() }
    single { get<LifeLedgerDatabase>().accountSummaryDao() }
    single { get<LifeLedgerDatabase>().behaviorPatternDao() }
    single { get<LifeLedgerDatabase>().spendingStreakDao() }
}

val repositoryModule = module {
    single { WorkRepository(get(), get(), get(), get()) }
    single { FinanceRepository(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { HabitRepository(get(), get()) }
    single { JournalRepository(get(), get()) }
    single { GoalRepository(get(), get()) }
    single { SettingsRepository(get(), get(), get()) }
    single { SavingRepository(get(), get()) }
    single { CardRepository(get()) }
    single { ActivityLogRepository(get()) }
    single { BackupManager(androidContext()) }
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
    viewModel { GoalsViewModel(get()) }
    viewModel { AddGoalViewModel(get()) }
    viewModel { HabitsViewModel(get()) }
    viewModel { JournalViewModel(get()) }
    viewModel { IncomeViewModel(get()) }
    viewModel { WorkViewModel(get()) }
    viewModel { SavingsViewModel(get(), get()) }
    viewModel { AddSavingsViewModel(get()) }
    viewModel { BackupViewModel(get(), get()) }
    viewModel { ExpenseViewModel(get()) }
    viewModel { FinanceViewModel(get()) }
    viewModel { TransferViewModel(get()) }
    viewModel { ExportViewModel() }
    viewModel { ReportsViewModel(get()) }
    viewModel { RecurringTransactionsViewModel(get()) }
    viewModel { CardsViewModel(get()) }
    viewModel { CalendarViewModel(get(), get(), get()) }
}

val appModules = listOf(databaseModule, repositoryModule)
