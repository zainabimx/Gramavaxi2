package com.example.grama_vaxi

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRegisterClick: () -> Unit,
    onRecordsClick: () -> Unit,
    onVaccineClick: () -> Unit,
    onSickClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    // ════ BACKEND CONNECTIVITY ════
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: "guest_user"

    var displayName by remember { mutableStateOf("Farmer") }
    var villageName by remember { mutableStateOf("Verified Account") }

    // ════ REAL-TIME DATA SYNC ════
    LaunchedEffect(userId) {
        if (userId != "guest_user") {
            db.collection("farmers").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    if (snapshot != null && snapshot.exists()) {
                        displayName = snapshot.getString("name") ?: "Farmer"
                        val village = snapshot.getString("village") ?: ""
                        if (village.isNotEmpty()) {
                            villageName = "Village: $village"
                        }
                    }
                }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFBFDFB),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(
                        text = "Grama-Vaxi",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1B5E20)
                    )
                },
                actions = {
                    IconButton(onClick = { /* Notification action */ }) {
                        Icon(Icons.Default.Notifications, null, tint = Color(0xFF0F9D58))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {

            // ════ PROFILE HEADER ════
            Surface(
                onClick = onProfileClick,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF0F9D58), Color(0xFF8BC34A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.size(54.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🌾", fontSize = 28.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = villageName,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.LightGray)
                }
            }

            // ════ BANNER ════
            Card(
                modifier = Modifier.fillMaxWidth().height(130.dp).padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFF0F9D58), Color(0xFF0B8043))))
                        .padding(20.dp)
                ) {
                    Column {
                        Surface(color = Color(0xFFF4B400), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "ALERT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Vaccine Season Started",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Protect your livestock today.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Text(
                text = "Services",
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )

            // ════ GRID ITEMS ════
            val items = listOf(
                GridItem("Register", "Add Animal", "🐄", Color(0xFFE8F5E9), onRegisterClick),
                GridItem("Records", "View History", "📋", Color(0xFFFFF8E1), onRecordsClick),
                GridItem("Vaccine", "Check Status", "💉", Color(0xFFE3F2FD), onVaccineClick),
                GridItem("Health", "Emergency", "🚑", Color(0xFFFFEBEE), onSickClick)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    LaunchCard(item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class GridItem(val title: String, val sub: String, val icon: String, val bg: Color, val action: () -> Unit)

@Composable
fun LaunchCard(item: GridItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { item.action() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(item.bg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(item.icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF333333))
            Text(item.sub, color = Color.Gray, fontSize = 12.sp)
        }
    }
}