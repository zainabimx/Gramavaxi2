package com.example.grama_vaxi

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRegisterClick: () -> Unit,
    onRecordsClick: () -> Unit,
    onVaccineClick: () -> Unit,
    onSickClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLanguageToggle: () -> Unit,
    isKannada: Boolean
) {

    val context =
        LocalContext.current

    val prefs =

        context.getSharedPreferences(
            "gramavaxi_prefs",
            Context.MODE_PRIVATE
        )

    val db =
        FirebaseFirestore.getInstance()

    val auth =
        FirebaseAuth.getInstance()

    val userId =
        auth.currentUser?.uid ?: "guest_user"

    var displayName by remember {
        mutableStateOf("Farmer")
    }

    var villageName by remember {
        mutableStateOf("Village")
    }

    var translatedName by remember {
        mutableStateOf(displayName)
    }

    var translatedVillage by remember {
        mutableStateOf(villageName)
    }

    // FETCH USER DATA
    LaunchedEffect(userId) {

        if (userId != "guest_user") {

            db.collection("farmers")
                .document(userId)

                .addSnapshotListener { snapshot, _ ->

                    if (
                        snapshot != null &&
                        snapshot.exists()
                    ) {

                        displayName =
                            snapshot.getString("name")
                                ?: "Farmer"

                        villageName =
                            snapshot.getString("village")
                                ?: "Village"
                    }
                }
        }
    }

    // ✅ SEND ENGLISH NOTIFICATION
    // ONLY FIRST APP OPEN

    LaunchedEffect(Unit) {

        // ✅ FORCE ENGLISH AT APP START

        prefs.edit()

            .putBoolean(
                "is_kannada",
                false
            )

            .apply()

        val hasSentStartupNotification =

            prefs.getBoolean(
                "startup_notification_sent",
                false
            )

        if (!hasSentStartupNotification) {

            prefs.edit()

                .putBoolean(
                    "startup_notification_sent",
                    true
                )

                .apply()

            val workRequest =

                OneTimeWorkRequestBuilder<
                        VaccineNotificationWorker>()
                    .build()

            WorkManager
                .getInstance(context)

                .enqueueUniqueWork(

                    "vaccine_notification_work",

                    ExistingWorkPolicy.REPLACE,

                    workRequest
                )
        }
    }
    // TRANSLATE PROFILE INFO
    LaunchedEffect(
        isKannada,
        displayName,
        villageName
    ) {

        if (isKannada) {

            translatedName =

                try {

                    KannadaTranslator
                        .translateToKannada(
                            displayName
                        )

                } catch (e: Exception) {

                    displayName
                }

            translatedVillage =

                try {

                    KannadaTranslator
                        .translateToKannada(
                            villageName
                        )

                } catch (e: Exception) {

                    villageName
                }

        } else {

            translatedName =
                displayName

            translatedVillage =
                villageName
        }
    }

    Scaffold(

        containerColor = Color(0xFFFBFDFB),

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text(

                        if (isKannada)
                            "ಗ್ರಾಮ-ವ್ಯಾಕ್ಸಿ"
                        else
                            "Grama-Vaxi",

                        fontWeight =
                            FontWeight.ExtraBold,

                        fontSize = 26.sp,

                        color = Color(0xFF1B5E20)
                    )
                },

                actions = {

                    Button(

                        onClick = {

                            val newKannadaState =
                                !isKannada

                            // ✅ SAVE LANGUAGE
                            prefs.edit()

                                .putBoolean(
                                    "is_kannada",
                                    newKannadaState
                                )

                                .apply()

                            // ✅ UPDATE UI
                            onLanguageToggle()

                            // ✅ ONLY SEND NOTIFICATION
                            // WHEN SWITCHING TO KANNADA

                            if (newKannadaState) {

                                val workRequest =

                                    OneTimeWorkRequestBuilder<
                                            VaccineNotificationWorker>()
                                        .build()

                                WorkManager
                                    .getInstance(context)

                                    .enqueueUniqueWork(

                                        "vaccine_notification_work",

                                        ExistingWorkPolicy.REPLACE,

                                        workRequest
                                    )
                            }
                        },

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),

                        shape = RoundedCornerShape(14.dp),

                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 6.dp
                        )
                    ) {

                        Text(

                            if (isKannada)
                                "EN"
                            else
                                "ಕನ್ನಡ",

                            color = Color(0xFF2E7D32),

                            fontWeight =
                                FontWeight.Bold,

                            fontSize = 14.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)

        ) {

            // PROFILE CARD
            Surface(

                onClick = onProfileClick,

                color = Color.Transparent,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)

            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(

                        modifier = Modifier
                            .size(68.dp)

                            .clip(CircleShape)

                            .background(

                                Brush.linearGradient(

                                    listOf(
                                        Color(0xFF0F9D58),
                                        Color(0xFF8BC34A)
                                    )
                                )
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            "👨‍🌾",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(14.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(

                            translatedName,

                            fontSize = 22.sp,

                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(

                            if (isKannada)
                                "ಗ್ರಾಮ: $translatedVillage"
                            else
                                "Village: $villageName",

                            fontSize = 14.sp,

                            color = Color.Gray
                        )
                    }

                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        null,
                        tint = Color.LightGray
                    )
                }
            }

            // ALERT CARD
            Card(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .padding(vertical = 6.dp),

                shape = RoundedCornerShape(24.dp)

            ) {

                Box(

                    modifier = Modifier
                        .fillMaxSize()

                        .background(

                            Brush.linearGradient(

                                listOf(
                                    Color(0xFF0F9D58),
                                    Color(0xFF0B8043)
                                )
                            )
                        )

                        .padding(20.dp)
                ) {

                    Column {

                        Surface(

                            color = Color(0xFFF4B400),

                            shape = RoundedCornerShape(10.dp)

                        ) {

                            Text(

                                if (isKannada)
                                    "ಎಚ್ಚರಿಕೆ"
                                else
                                    "ALERT",

                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),

                                fontSize = 11.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        Text(

                            if (isKannada)
                                "ಲಸಿಕೆ ಕಾಲ ಆರಂಭವಾಗಿದೆ"
                            else
                                "Vaccine Season Started",

                            color = Color.White,

                            fontSize = 23.sp,

                            fontWeight =
                                FontWeight.Black
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(

                            if (isKannada)
                                "ನಿಮ್ಮ ಪಶುಗಳನ್ನು ರಕ್ಷಿಸಿ."
                            else
                                "Protect your livestock today.",

                            color = Color.White,

                            fontSize = 15.sp,

                            fontWeight =
                                FontWeight.Medium
                        )
                    }
                }
            }

            // SERVICES TITLE
            Text(

                if (isKannada)
                    "ಸೇವೆಗಳು"
                else
                    "Services",

                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 12.dp
                ),

                fontSize = 22.sp,

                fontWeight =
                    FontWeight.Bold
            )

            val items = listOf(

                GridItem(
                    if (isKannada) "ನೋಂದಣಿ" else "Register",
                    if (isKannada) "ಪ್ರಾಣಿ ಸೇರಿಸಿ" else "Add Animal",
                    "🐄",
                    Color(0xFFE8F5E9),
                    onRegisterClick
                ),

                GridItem(
                    if (isKannada) "ದಾಖಲೆಗಳು" else "Records",
                    if (isKannada) "ಇತಿಹಾಸ ನೋಡಿ" else "View History",
                    "📋",
                    Color(0xFFFFF8E1),
                    onRecordsClick
                ),

                GridItem(
                    if (isKannada) "ಲಸಿಕೆ" else "Vaccine",
                    if (isKannada) "ಸ್ಥಿತಿ ಪರಿಶೀಲಿಸಿ" else "Check Status",
                    "💉",
                    Color(0xFFE3F2FD),
                    onVaccineClick
                ),

                GridItem(
                    if (isKannada) "ಆರೋಗ್ಯ" else "Health",
                    if (isKannada) "ತುರ್ತು ಪರಿಸ್ಥಿತಿ" else "Emergency",
                    "🚑",
                    Color(0xFFFFEBEE),
                    onSickClick
                )
            )

            LazyVerticalGrid(

                columns = GridCells.Fixed(2),

                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                horizontalArrangement =
                    Arrangement.spacedBy(14.dp),

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)

            ) {

                items(items) { item ->

                    LaunchCard(item)
                }
            }
        }
    }
}

data class GridItem(
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun LaunchCard(item: GridItem) {
    Card(
        onClick = item.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = item.color),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(item.icon, fontSize = 24.sp)
            }

            Column {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}