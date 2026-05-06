package com.example.grama_vaxi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore

// --- 1. DATA MODELS ---
data class CampVaccine(
    val title: String = "",
    val campDate: String = "",
    val description: String = "",
    val targetType: String = "",
    val severity: String = ""
)

data class Animal(
    val id: String = "",
    val animalName: String = "",
    val animalType: String = "",
    val age: String = "",
    val ownerName: String = "",
    val imageUri: String = ""
)
// ... (keep imports and data classes the same)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalRecordsScreen(onBackClick: () -> Unit, onEditClick: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val animalList = remember { mutableStateListOf<Animal>() }
    val globalVaccines = remember { mutableStateListOf<CampVaccine>() }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        isLoading = true
        // Ensure collection name matches your Firestore (camps or vaccine_alerts)
        db.collection("vaccine_alerts").get().addOnSuccessListener { vaxResult ->
            globalVaccines.clear()
            for (doc in vaxResult) {
                globalVaccines.add(doc.toObject(CampVaccine::class.java))
            }

            db.collection("animals").get().addOnSuccessListener { animalResult ->
                animalList.clear()
                for (doc in animalResult) {
                    animalList.add(Animal(
                        id = doc.id,
                        animalName = doc.getString("animalName") ?: "",
                        animalType = doc.getString("animalType") ?: "",
                        age = doc.getString("age") ?: "",
                        ownerName = doc.getString("ownerName") ?: "",
                        imageUri = doc.getString("imageUri") ?: ""
                    ))
                }
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("LIVESTOCK RECORDS", fontWeight = FontWeight.Black, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2E7D32)) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(animalList) { animal ->
                        AnimalRecordCard(
                            animal = animal,
                            availableVaccines = globalVaccines,
                            onEdit = { onEditClick(animal.id) }, // This sends the ID up to MainActivity
                            onDelete = {
                                db.collection("animals").document(animal.id).delete().addOnSuccessListener { loadData() }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalRecordCard(
    animal: Animal,
    availableVaccines: List<CampVaccine>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Image Box
                Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0))) {
                    Image(
                        painter = rememberAsyncImagePainter(animal.imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(animal.animalName.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Type: ${animal.animalType}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Age: ${animal.age}", fontSize = 12.sp, color = Color.Gray)
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color(0xFF2196F3)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF1744)) }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- FIXED MATCHING LOGIC ---
            // .trim() removes hidden spaces, ignoreCase=true matches "Cow" with "cow"
            val matchingAlerts = availableVaccines.filter {
                it.targetType.trim().equals(animal.animalType.trim(), ignoreCase = true)
            }

            if (matchingAlerts.isNotEmpty()) {
                Text("HEALTH ALERTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                matchingAlerts.forEach { vax ->
                    val isHighSeverity = vax.severity.trim().equals("High", ignoreCase = true)
                    Surface(
                        color = if (isHighSeverity) Color(0xFFFFF1F1) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if(isHighSeverity) Icons.Default.Warning else Icons.Default.Info,
                                    null,
                                    tint = if(isHighSeverity) Color.Red else Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(vax.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("Date: ${vax.campDate}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            } else {
                Text("No active drives for ${animal.animalType}", fontSize = 11.sp, color = Color.LightGray)
            }
        }
    }
}