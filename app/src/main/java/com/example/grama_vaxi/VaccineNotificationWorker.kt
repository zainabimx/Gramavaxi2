package com.example.grama_vaxi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class VaccineNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // 1. Generate target dates (Today + 3 days)
        val validDates = mutableListOf<String>()
        for (i in 0..3) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i)
            validDates.add(sdf.format(cal.time))
        }

        return try {
            val snapshot = db.collection("vaccine_alerts").get().await()

            for (doc in snapshot.documents) {
                val title = doc.getString("title") ?: "Vaccine Alert"
                val firebaseDate = doc.getString("campDate") ?: ""
                val location = doc.getString("location") ?: "Village Center"

                // Normalize date
                val normalizedFirebase = try {
                    val dateObj = sdf.parse(firebaseDate)
                    if (dateObj != null) sdf.format(dateObj) else ""
                } catch (e: Exception) { "" }

                if (normalizedFirebase.isNotEmpty() && normalizedFirebase in validDates) {
                    val campDateObj = sdf.parse(normalizedFirebase)!!
                    val diff = calculateDaysDiff(campDateObj)

                    val dayMessage = when (diff.toInt()) {
                        0 -> "Vaccination Today"
                        1 -> "Vaccination Tomorrow"
                        2 -> "Vaccination in 2 Days"
                        3 -> "Vaccination in 3 Days"
                        else -> "Upcoming Vaccination"
                    }

                    // 2. Trigger notification with a unique ID (doc.id.hashCode())
                    // This ensures individual entries in the notification tray
                    showNotification(
                        title = title,
                        message = "$dayMessage\n📍 $location\n📅 $firebaseDate",
                        notificationId = doc.id.hashCode()
                    )

                    // 3. 🔥 Crucial: Delay for a "multi-ring" effect
                    // This prevents the system from bundling the sounds together
                    delay(1000)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("WORKER_ERROR", "Firebase error: ${e.message}")
            Result.retry()
        }
    }

    private fun calculateDaysDiff(campDate: Date): Long {
        val calCamp = Calendar.getInstance().apply {
            time = campDate
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val calNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return (calCamp.timeInMillis - calNow.timeInMillis) / (1000 * 60 * 60 * 24)
    }

    private fun showNotification(title: String, message: String, notificationId: Int) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use a high-priority channel
        val channelId = "vaxi_individual_alerts_v8"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Vaccine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for each livestock camp"
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true) // Allows ringing even in DND mode
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📢 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Required for high-visibility pop-up
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        // notify() with a unique ID ensures it creates a new notification (and sound)
        manager.notify(notificationId, notification)
    }
}