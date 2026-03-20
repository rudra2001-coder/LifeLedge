package com.rudra.lifeledge.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.rudra.lifeledge.MainActivity
import com.rudra.lifeledge.R
import com.rudra.lifeledge.core.finance.FinanceEngine
import com.rudra.lifeledge.core.finance.event.FinanceAlert
import com.rudra.lifeledge.core.finance.insight.ExplainableInsight
import com.rudra.lifeledge.core.finance.insight.InsightType
import com.rudra.lifeledge.core.finance.insight.Priority
import com.rudra.lifeledge.core.finance.model.RunwayCalculation
import com.rudra.lifeledge.core.finance.model.Urgency
import com.rudra.lifeledge.core.finance.state.DashboardStatus
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class FinanceNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "finance_alerts"
        const val CHANNEL_NAME = "Finance Alerts"
        const val CHANNEL_DESCRIPTION = "Smart financial insights and alerts"
        
        const val NOTIFICATION_ID_DAILY = 1001
        const val NOTIFICATION_ID_ALERT = 1002
        const val NOTIFICATION_ID_RUNWAY = 1003
        const val NOTIFICATION_ID_GOAL = 1004
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyInsightNotification(insight: ExplainableInsight) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_insight", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (insight.priority) {
            Priority.CRITICAL -> NotificationCompat.PRIORITY_HIGH
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            Priority.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(insight.title)
            .setContentText(insight.shortMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(insight.message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, notification)
        } catch (e: SecurityException) {
            // Handle missing permission
        }
    }

    fun showAlertNotification(alert: FinanceAlert) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_alerts", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(alert.title)
            .setContentText(alert.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, notification)
        } catch (e: SecurityException) {
            // Handle missing permission
        }
    }

    fun showRunwayWarningNotification(runway: RunwayCalculation) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_dashboard", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priority = when (runway.urgency) {
            Urgency.SAFE -> NotificationCompat.PRIORITY_LOW
            Urgency.MONITOR -> NotificationCompat.PRIORITY_DEFAULT
            Urgency.WARNING -> NotificationCompat.PRIORITY_HIGH
            Urgency.CRITICAL -> NotificationCompat.PRIORITY_HIGH
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Balance Warning")
            .setContentText(runway.message)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_RUNWAY, notification)
        } catch (e: SecurityException) {
            // Handle missing permission
        }
    }

    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}

class FinanceNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // This would need FinanceEngine injected via DI in production
        // For now, this is a placeholder that schedules notifications

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "finance_daily_notification"

        fun scheduleDaily(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<FinanceNotificationWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun scheduleWeekly(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<FinanceNotificationWorker>(
                7, TimeUnit.DAYS
            )
                .setInitialDelay(calculateWeeklyInitialDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "${WORK_NAME}_weekly",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        private fun calculateInitialDelay(): Long {
            val now = java.util.Calendar.getInstance()
            val targetHour = 9 // 9 AM

            val delay = if (now.get(java.util.Calendar.HOUR_OF_DAY) >= targetHour) {
                // Schedule for next day
                (24 - now.get(java.util.Calendar.HOUR_OF_DAY) + targetHour) * 60 * 60 * 1000L
            } else {
                (targetHour - now.get(java.util.Calendar.HOUR_OF_DAY)) * 60 * 60 * 1000L
            }

            return delay
        }

        private fun calculateWeeklyInitialDelay(): Long {
            return calculateInitialDelay() + (6 * 24 * 60 * 60 * 1000L) // 7 days
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

class InsightPrioritizationEngine {

    fun prioritizeInsights(insights: List<ExplainableInsight>): ExplainableInsight? {
        if (insights.isEmpty()) return null

        return insights.maxByOrNull { insight ->
            calculatePriorityScore(insight)
        }
    }

    private fun calculatePriorityScore(insight: ExplainableInsight): Int {
        var score = 0

        // Base priority from insight type
        score += when (insight.type) {
            InsightType.SAVINGS_ALERT -> 100
            InsightType.BUDGET_STATUS -> 90
            InsightType.TODAY_SPENDING -> 80
            InsightType.CATEGORY_ALERT -> 70
            InsightType.WEEKLY_TREND -> 60
            InsightType.SAVINGS_CELEBRATION -> 40
            InsightType.MONTHLY_SUMMARY -> 30
            InsightType.GOAL_PROGRESS -> 50
        }

        // Priority level boost
        score += when (insight.priority) {
            Priority.CRITICAL -> 50
            Priority.HIGH -> 35
            Priority.MEDIUM -> 20
            Priority.LOW -> 5
        }

        // Actionable insights get priority
        if (insight.actionable) score += 15

        // Financial impact scoring
        val data = insight.data
        data["potential_savings"]?.let { savings ->
            if (savings is Double && savings > 1000) score += 20
        }
        data["percent_difference"]?.let { diff ->
            if (diff is Double && kotlin.math.abs(diff) > 50) score += 15
        }

        return score
    }

    fun getTopInsights(insights: List<ExplainableInsight>, limit: Int = 3): List<ExplainableInsight> {
        return insights
            .sortedByDescending { calculatePriorityScore(it) }
            .take(limit)
    }

    fun shouldShowNotification(insight: ExplainableInsight): Boolean {
        return when (insight.priority) {
            Priority.CRITICAL -> true
            Priority.HIGH -> true
            Priority.MEDIUM -> insight.actionable
            Priority.LOW -> false
        }
    }

    fun getNotificationPriority(insight: ExplainableInsight): Int {
        return when (insight.priority) {
            Priority.CRITICAL -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            Priority.LOW -> NotificationCompat.PRIORITY_LOW
        }
    }
}
