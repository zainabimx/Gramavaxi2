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

        val db =
            FirebaseFirestore.getInstance()

        // ✅ READ GLOBAL LANGUAGE
        val prefs =

            applicationContext
                .getSharedPreferences(
                    "gramavaxi_prefs",
                    Context.MODE_PRIVATE
                )

        val isKannada =
            prefs.getBoolean(
                "is_kannada",
                false
            )

        val sdf =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        // VALID DATES
        val validDates =
            mutableListOf<String>()

        for (i in 0..3) {

            val cal =
                Calendar.getInstance()

            cal.add(
                Calendar.DAY_OF_YEAR,
                i
            )

            validDates.add(
                sdf.format(cal.time)
            )
        }

        return try {

            val snapshot =
                db.collection("vaccine_alerts")
                    .get()
                    .await()

            for (doc in snapshot.documents) {

                val firebaseTitle =
                    doc.getString("title")
                        ?: "Vaccine Alert"

                val firebaseDate =
                    doc.getString("campDate")
                        ?: ""

                val firebaseLocation =
                    doc.getString("location")
                        ?: "Village Center"

                // NORMALIZE DATE
                val normalizedFirebase =

                    try {

                        val dateObj =
                            sdf.parse(firebaseDate)

                        if (dateObj != null)
                            sdf.format(dateObj)
                        else
                            ""

                    } catch (e: Exception) {

                        ""
                    }

                if (
                    normalizedFirebase.isNotEmpty()
                    && normalizedFirebase in validDates
                ) {

                    val campDateObj =
                        sdf.parse(normalizedFirebase)
                            ?: continue

                    val diff =
                        calculateDaysDiff(campDateObj)

                    // ✅ TRANSLATE TITLE
                    val title =

                        if (isKannada) {

                            KannadaTranslator
                                .translateToKannada(
                                    firebaseTitle
                                )

                        } else {

                            firebaseTitle
                        }

                    // ✅ TRANSLATE LOCATION
                    val location =

                        if (isKannada) {

                            KannadaTranslator
                                .translateToKannada(
                                    firebaseLocation
                                )

                        } else {

                            firebaseLocation
                        }

                    // DAY MESSAGE
                    val dayMessage =

                        if (isKannada) {

                            when (diff.toInt()) {

                                0 ->
                                    "ಇಂದು ಲಸಿಕೆ"

                                1 ->
                                    "ನಾಳೆ ಲಸಿಕೆ"

                                2 ->
                                    "2 ದಿನಗಳಲ್ಲಿ ಲಸಿಕೆ"

                                3 ->
                                    "3 ದಿನಗಳಲ್ಲಿ ಲಸಿಕೆ"

                                else ->
                                    "ಮುಂಬರುವ ಲಸಿಕೆ"
                            }

                        } else {

                            when (diff.toInt()) {

                                0 ->
                                    "Vaccination Today"

                                1 ->
                                    "Vaccination Tomorrow"

                                2 ->
                                    "Vaccination in 2 Days"

                                3 ->
                                    "Vaccination in 3 Days"

                                else ->
                                    "Upcoming Vaccination"
                            }
                        }

                    // FINAL MESSAGE
                    val finalMessage =

                        if (isKannada) {

                            """
                            $dayMessage
                            📍 $location
                            📅 $firebaseDate
                            """.trimIndent()

                        } else {

                            """
                            $dayMessage
                            📍 $location
                            📅 $firebaseDate
                            """.trimIndent()
                        }

                    // ✅ SHOW NOTIFICATION
                    showNotification(

                        title = title,

                        message = finalMessage,

                        notificationId =
                            doc.id.hashCode(),

                        isKannada = isKannada
                    )

                    // SMALL DELAY
                    delay(1000)
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

    // DATE DIFFERENCE
    private fun calculateDaysDiff(
        campDate: Date
    ): Long {

        val calCamp =
            Calendar.getInstance().apply {

                time = campDate

                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        val calNow =
            Calendar.getInstance().apply {

                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        return (
                calCamp.timeInMillis
                        - calNow.timeInMillis
                ) / (1000 * 60 * 60 * 24)
    }

    // NOTIFICATION
    private fun showNotification(

        title: String,

        message: String,

        notificationId: Int,

        isKannada: Boolean
    ) {

        val manager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channelId =
            "vaxi_individual_alerts_v9"

        // CHANNEL
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel = NotificationChannel(

                channelId,

                if (isKannada)
                    "ಲಸಿಕೆ ಎಚ್ಚರಿಕೆಗಳು"
                else
                    "Vaccine Reminders",

                NotificationManager.IMPORTANCE_HIGH

            ).apply {

                description =

                    if (isKannada)
                        "ಪಶು ಲಸಿಕೆ ಶಿಬಿರ ಎಚ್ಚರಿಕೆಗಳು"
                    else
                        "Alerts for livestock vaccine camps"

                enableLights(true)

                enableVibration(true)

                setBypassDnd(true)
            }

            manager.createNotificationChannel(channel)
        }

        // BUILD NOTIFICATION
        val notification =
            NotificationCompat.Builder(
                applicationContext,
                channelId
            )

                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )

                .setContentTitle(
                    "📢 $title"
                )

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

                .setDefaults(
                    Notification.DEFAULT_ALL
                )

                .setAutoCancel(true)

                .build()

        manager.notify(
            notificationId,
            notification
        )
    }
}