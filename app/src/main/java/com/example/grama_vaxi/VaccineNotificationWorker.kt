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
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class VaccineNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val db = FirebaseFirestore.getInstance()

        val sdf = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        // 🔥 Today
        val todayCalendar = Calendar.getInstance()

        // 🔥 Valid dates:
        // today + 0,1,2,3
        val validDates = mutableListOf<String>()

        for (i in 0..3) {

            val cal =
                todayCalendar.clone() as Calendar

            cal.add(
                Calendar.DAY_OF_YEAR,
                i
            )

            validDates.add(
                sdf.format(cal.time)
                    .replace("-0", "-")
            )
        }

        return try {

            val snapshot =
                db.collection("vaccine_alerts")
                    .get()
                    .await()

            for (doc in snapshot.documents) {

                val title =
                    doc.getString("title")
                        ?: "Vaccine Alert"

                val firebaseDate =
                    doc.getString("campDate")
                        ?: ""

                val location =
                    doc.getString("location")
                        ?: "Village Center"

                // Normalize Firebase date
                val normalizedFirebase =
                    firebaseDate.replace("-0", "-")

                Log.d(
                    "VAXI_DEBUG",
                    "Firebase Date: $normalizedFirebase"
                )

                Log.d(
                    "VAXI_DEBUG",
                    "Valid Dates: $validDates"
                )

                // 🔥 Match any of the 4 days
                if (normalizedFirebase in validDates) {

                    // 🔥 Calculate days left
                    val campDateObj = sdf.parse(firebaseDate) ?: Date()

                    val calCamp = Calendar.getInstance().apply { time = campDateObj }
                    val calNow = Calendar.getInstance()

                    // Reset time components to compare calendar days accurately
                    val campMs = Calendar.getInstance().apply {
                        set(calCamp.get(Calendar.YEAR), calCamp.get(Calendar.MONTH), calCamp.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val todayMs = Calendar.getInstance().apply {
                        set(calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH), calNow.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val diff = (campMs - todayMs) / (1000 * 60 * 60 * 24)

                    val dayMessage =
                        when (diff.toInt()) {

                            3 ->
                                "Vaccination in 3 days"

                            2 ->
                                "Vaccination in 2 days"

                            1 ->
                                "Vaccination Tomorrow"

                            0 ->
                                "Vaccination Today"

                            else ->
                                "Upcoming Vaccination"
                        }

                    showNotification(
                        title,
                        "$dayMessage\n📍 $location\n📅 $firebaseDate"
                    )
                }
            }

            Result.success()

        } catch (e: Exception) {

            Log.e(
                "WORKER_ERROR",
                "Firebase error: ${e.message}"
            )

            Result.retry()
        }
    }

    private fun showNotification(
        title: String,
        message: String
    ) {

        val manager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channelId = "vaxi_final_v5"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    channelId,
                    "Vaccine Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {

                    description =
                        "Urgent alerts for livestock vaccination"

                    enableLights(true)

                    enableVibration(true)

                    setBypassDnd(true)

                    lockscreenVisibility =
                        Notification.VISIBILITY_PUBLIC
                }

            manager.createNotificationChannel(channel)
        }

        val notificationBuilder =
            NotificationCompat.Builder(
                applicationContext,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle("📢 $title")
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_MAX
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)

        val notificationId =
            System.currentTimeMillis().toInt()

        manager.notify(
            notificationId,
            notificationBuilder.build()
        )
    }
}