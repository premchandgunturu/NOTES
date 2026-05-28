package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlertNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("AlertReceiver", "onReceive triggered!")
        val taskId = intent.getIntExtra("TASK_ID", 0)
        android.util.Log.d("AlertReceiver", "TaskId: $taskId")
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Study Session"
        val taskSubject = intent.getStringExtra("TASK_SUBJECT") ?: "Focus"

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
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action to open MainActivity when clicking the notification
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.mipmap.ic_launcher) // App's native launcher icon for absolute safety
            .setContentTitle("Focus Alert — $taskSubject")
            .setContentText("Time to work on: $taskTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            notificationManager.notify(taskId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
