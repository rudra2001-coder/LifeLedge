package com.rudra.lifeledge.di

import com.rudra.lifeledge.data.local.database.LifeLedgerDatabase
import com.rudra.lifeledge.data.repository.*
import com.rudra.lifeledge.ui.screens.dashboard.DashboardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { LifeLedgerDatabase.getDatabase(androidContext()) }
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
}

val repositoryModule = module {
    single { WorkRepository(get(), get(), get()) }
    single { FinanceRepository(get(), get(), get(), get(), get(), get(), get()) }
    single { HabitRepository(get(), get()) }
    single { JournalRepository(get(), get()) }
    single { GoalRepository(get(), get()) }
    single { SettingsRepository(get(), get(), get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
}

val appModules = listOf(databaseModule, repositoryModule)
