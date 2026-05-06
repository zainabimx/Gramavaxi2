package com.example.grama_vaxi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Data class matching your Firestore structure
data class VaccineAlerts(
    val title: String = "",
    val description: String = "",
    val severity: String = "ALERT",
    val location: String = "",
    val campDate: String = "" // Must be YYYY-MM-DD
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineAlertsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var alerts by remember { mutableStateOf(listOf<VaccineAlerts>()) }
    var isLoading by remember { mutableStateOf(true) }

    // This prevents the notification from firing repeatedly while looking at the screen
    val notifiedItems = remember { mutableStateSetOf<String>() }

    LaunchedEffect(Unit) {
        db.collection("vaccine_alerts")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val fetchedAlerts = snapshot.toObjects(VaccineAlerts::class.java)
                    alerts = fetchedAlerts

                    val today = LocalDate.now()
                    for (alert in fetchedAlerts) {
                        try {
                            val targetDate = LocalDate.parse(alert.campDate)
                            val daysRemaining = ChronoUnit.DAYS.between(today, targetDate)

                            // Logic: Trigger if camp is in exactly 3 days
                            if (daysRemaining == 3L && !notifiedItems.contains(alert.title)) {
                                sendLoudSystemNotification(
                                    context,
                                    "VACCINE CAMP IN 3 DAYS!",
                                    "${alert.title} at ${alert.location}"
                                )
                                notifiedItems.add(alert.title)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming Camps", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0F9D58))
            }
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp)) {
                items(alerts) { alert ->
                    AlertCard(alert)
                }
            }
        }
    }
}

/**
 * Sends a high-priority notification with sound and heads-up display.
 */
fun sendLoudSystemNotification(context: Context, title: String, message: String) {
    val channelId = "vaccine_camp_alerts"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // 1. Create the High Importance Channel (Necessary for sound/popup)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Urgent Vaccine Alerts",
            NotificationManager.IMPORTANCE_HIGH // Enables popup and sound
        ).apply {
            description = "Alerts for vaccination camps 3 days in advance"
            enableVibration(true)
            enableLights(true)
            lightColor = android.graphics.Color.RED
        }
        notificationManager.createNotificationChannel(channel)
    }

    // 2. Build the notification with High Priority
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Use your own icon here
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)
        .setDefaults(NotificationCompat.DEFAULT_ALL) // Standard loud sound and vibration

    // 3. Show it
    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
}

@Composable
fun AlertCard(alert: VaccineAlerts) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = alert.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            Spacer(Modifier.height(6.dp))
            Text(text = "📍 Location: ${alert.location}", fontSize = 14.sp, color = Color.Gray)
            Text(text = "📅 Date: ${alert.campDate}", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(Modifier.padding(vertical = 10.dp), thickness = 0.8.dp)
            Text(text = alert.description, fontSize = 14.sp, lineHeight = 20.sp, color = Color.DarkGray)
        }
    }
}