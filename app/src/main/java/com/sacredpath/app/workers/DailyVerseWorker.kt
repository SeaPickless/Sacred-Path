package com.sacredpath.app.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.sacredpath.app.MainActivity
import com.sacredpath.app.R
import com.sacredpath.app.data.repository.DailyVerseRepository
import com.sacredpath.app.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyVerseWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dailyVerseRepo: DailyVerseRepository,
    private val settingsRepo: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID   = "sacred_path_daily"
        const val WORK_NAME    = "daily_verse_notification"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyVerseWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        // Schedule for 8 AM each day
        private fun calculateInitialDelay(): Long {
            val now    = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 8)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepo.settingsFlow.first()
            // Force pick a fresh random verse for today
            val verse = dailyVerseRepo.refreshVerse(settings.translationId)
            showNotification(verse.reference, verse.text.take(100))
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(reference: String, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Daily Verse",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Your daily Bible verse" }
            )
        }

        val intent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📖 Verse of the Day — $reference")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
