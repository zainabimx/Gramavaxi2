package com.example.grama_vaxi

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAnimalScreen(
    onBackClick: () -> Unit,
    editAnimalId: String? = null // Receives ID from Records Screen
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // Form State
    var animalName by remember { mutableStateOf("") }
    var animalType by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // --- 1. PRE-FILL PREVIOUS INFO ---
    LaunchedEffect(editAnimalId) {
        if (editAnimalId != null) {
            db.collection("animals").document(editAnimalId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        animalName = doc.getString("animalName") ?: ""
                        animalType = doc.getString("animalType") ?: ""
                        age = doc.getString("age") ?: ""
                        ownerName = doc.getString("ownerName") ?: ""
                        val uriStr = doc.getString("imageUri")
                        if (!uriStr.isNullOrEmpty()) {
                            selectedImageUri = Uri.parse(uriStr)
                        }
                    }
                }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    Scaffold(
        containerColor = Color(0xFFFBFDFB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editAnimalId == null) "New Registration" else "Update Animal Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
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
                .verticalScroll(rememberScrollState())
        ) {

            // Status Banner
            Surface(
                color = if (editAnimalId == null) Color(0xFFE8F5E9) else Color(0xFFE3F2FD),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (editAnimalId == null) "🐄" else "✏️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (editAnimalId == null) "Provide accurate details for better health tracking."
                        else "Update the info below and save to refresh records.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Photo Section
            Text("Animal Photo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5F5F5))
                    .border(1.dp, Color(0xFF0F9D58).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color(0xFF0F9D58))
                        Text("Photo", fontSize = 11.sp, color = Color(0xFF0F9D58))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Inputs
            ModernRegisterInput(
                "Animal Name",
                animalName,
                { animalName = it },
                "e.g. Ganga",
                Icons.Default.Pets
            )
            ModernRegisterInput(
                "Livestock Type",
                animalType,
                { animalType = it },
                "e.g. Cow, Buffalo",
                Icons.Default.Badge
            )
            ModernRegisterInput("Age (Years)", age, { age = it }, "e.g. 3", Icons.Default.Numbers)
            ModernRegisterInput(
                "Owner Name",
                ownerName,
                { ownerName = it },
                "Farmer's name",
                Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(30.dp))

            // --- 2. SAVE OR UPDATE ACTION ---
            Button(
                onClick = {
                    if (animalName.isBlank() || animalType.isBlank()) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    isSaving = true

                    val animalData = hashMapOf(
                        "animalName" to animalName,
                        "animalType" to animalType,
                        "age" to age,
                        "ownerName" to ownerName,
                        "imageUri" to (selectedImageUri?.toString() ?: "")
                    )

                    if (editAnimalId != null) {
                        // UPDATE PATH
                        db.collection("animals").document(editAnimalId)
                            .set(animalData, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(context, "Update Successful!", Toast.LENGTH_SHORT)
                                    .show()
                                onBackClick()
                            }
                    } else {
                        // NEW REGISTRATION PATH
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val cal = Calendar.getInstance()
                        val registrationDate = sdf.format(cal.time)
                        cal.add(Calendar.MONTH, 6)
                        val nextVaccination = sdf.format(cal.time)

                        animalData["lastVaccinated"] = registrationDate
                        animalData["nextVaccinationDue"] = nextVaccination
                        animalData["vaccinationStatus"] = "Up-to-Date"
                        db.collection("animals").add(animalData).addOnSuccessListener {
                            Toast.makeText(context, "Registration Complete!", Toast.LENGTH_SHORT)
                                .show()
                            onBackClick()
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        if (editAnimalId == null) "Complete Registration" else "Save Changes",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- 3. REUSABLE INPUT COMPONENT (Fixes the red errors) ---
@Composable
fun ModernRegisterInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.LightGray) },
            leadingIcon = {
                Icon(
                    icon,
                    null,
                    tint = Color(0xFF0F9D58),
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0F9D58),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            singleLine = true
        )
    }
}