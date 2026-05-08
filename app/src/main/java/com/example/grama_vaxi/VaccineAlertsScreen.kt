package com.example.grama_vaxi

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

// DATA CLASS
data class VaccineAlerts(
    val title: String = "",
    val description: String = "",
    val severity: String = "ALERT",
    val location: String = "",
    val campDate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineAlertsScreen(
    onBackClick: () -> Unit,
    isKannada: Boolean
) {

    val db =
        FirebaseFirestore.getInstance()

    var alerts by remember {

        mutableStateOf(
            listOf<VaccineAlerts>()
        )
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    // ✅ ONLY FETCH ALERTS
    // ❌ NO NOTIFICATIONS HERE
    LaunchedEffect(Unit) {

        db.collection("vaccine_alerts")

            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {

                    alerts =

                        snapshot.toObjects(
                            VaccineAlerts::class.java
                        )
                }

                isLoading = false
            }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        if (isKannada)
                            "ಮುಂಬರುವ ಶಿಬಿರಗಳು"
                        else
                            "Upcoming Camps",

                        fontWeight =
                            FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        if (isLoading) {

            Box(

                modifier = Modifier.fillMaxSize(),

                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator(
                    color = Color(0xFF0F9D58)
                )
            }

        } else {

            LazyColumn(

                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)

            ) {

                items(alerts) { alert ->

                    AlertCard(
                        alert = alert,
                        isKannada = isKannada
                    )
                }
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: VaccineAlerts,
    isKannada: Boolean
) {

    var translatedTitle by remember {
        mutableStateOf(alert.title)
    }

    var translatedDescription by remember {
        mutableStateOf(alert.description)
    }

    var translatedLocation by remember {
        mutableStateOf(alert.location)
    }

    // ✅ TRANSLATE CONTENT
    LaunchedEffect(
        isKannada,
        alert.title,
        alert.description,
        alert.location
    ) {

        if (isKannada) {

            translatedTitle =

                try {

                    KannadaTranslator
                        .translateToKannada(
                            alert.title
                        )

                } catch (e: Exception) {

                    alert.title
                }

            translatedDescription =

                try {

                    KannadaTranslator
                        .translateToKannada(
                            alert.description
                        )

                } catch (e: Exception) {

                    alert.description
                }

            translatedLocation =

                try {

                    KannadaTranslator
                        .translateToKannada(
                            alert.location
                        )

                } catch (e: Exception) {

                    alert.location
                }

        } else {

            translatedTitle =
                alert.title

            translatedDescription =
                alert.description

            translatedLocation =
                alert.location
        }
    }

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // TITLE
            Text(

                text = translatedTitle,

                fontSize = 18.sp,

                fontWeight =
                    FontWeight.Bold,

                color = Color(0xFF1B5E20)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            // LOCATION
            Text(

                text =

                    if (isKannada)
                        "📍 ಸ್ಥಳ: $translatedLocation"
                    else
                        "📍 Location: ${alert.location}",

                fontSize = 14.sp,

                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            // DATE
            Text(

                text =

                    if (isKannada)
                        "📅 ದಿನಾಂಕ: ${alert.campDate}"
                    else
                        "📅 Date: ${alert.campDate}",

                fontSize = 14.sp,

                color = Color.Black,

                fontWeight =
                    FontWeight.SemiBold
            )

            HorizontalDivider(

                modifier = Modifier.padding(
                    vertical = 10.dp
                ),

                thickness = 0.8.dp
            )

            // DESCRIPTION
            Text(

                text = translatedDescription,

                fontSize = 14.sp,

                lineHeight = 20.sp,

                color = Color.DarkGray
            )
        }
    }
}