package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class AlertNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationType = intent.getStringExtra("NOTIFICATION_TYPE")
        
        val channelId = "consistency_hub_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create the NotificationChannel on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Study Objective Reminders"
            val descriptionText = "Notifications for scheduled study sessions and objectives"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (notificationType == "WEEKLY_PERFORMANCE") {
            // Fetch logs for summary
            val logs = runBlocking {
                com.example.data.AppDatabase.getDatabase(context).appDao().getAllStudyLogsList()
            }
            val lastWeekLogs = logs.filter { log ->
                val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                log.timestamp >= calendar.timeInMillis
            }
            val totalHours = lastWeekLogs.sumOf { it.durationHours }
            val topics = lastWeekLogs.map { it.topic }.distinct().joinToString(", ")
            
            builder.setContentTitle("Weekly Performance Review")
                .setContentText("Total: $totalHours hours. Accomplishments: $topics")
                .setStyle(NotificationCompat.BigTextStyle().bigText("Total: $totalHours hours. Accomplishments: $topics"))
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                999,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
            notificationManager.notify(999, builder.build())
        } else {
            val taskId = intent.getIntExtra("TASK_ID", 0)
            val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Study Session"
            val taskSubject = intent.getStringExtra("TASK_SUBJECT") ?: "Focus"
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.setContentTitle("Focus Alert — $taskSubject")
                .setContentText("Time to work on: $taskTitle")
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
            
            notificationManager.notify(taskId, builder.build())
        }
    }
}
