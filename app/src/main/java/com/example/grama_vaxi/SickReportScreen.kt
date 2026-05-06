package com.example.grama_vaxi

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SickReportScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var animalName by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFFBFDFB),
        topBar = {
            TopAppBar(
                title = { Text("Health Emergency", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp) // Updated to match Register Screen padding
                .verticalScroll(rememberScrollState())
        ) {
            if (!isSubmitted) {
                Spacer(modifier = Modifier.height(16.dp))

                // Alert Banner matching the Register Screen style
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🚨", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Reporting this will alert the local veterinary officer immediately.",
                            fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium
                        )
                    }
                }

                // --- INPUT FIELDS (Now matching ModernRegisterInput style) ---
                SickInput("Animal Name / Tag ID", animalName, { animalName = it }, "e.g. Buffalo-04", Icons.Default.Pets)
                SickInput("Primary Issue", issue, { issue = it }, "e.g. High fever, Swelling", Icons.Default.Warning)

                // Detailed Symptoms with updated label styling
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text("Detailed Symptoms", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Describe behavioral changes...", fontSize = 14.sp, color = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD32F2F), // Red for emergency
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (animalName.isBlank() || issue.isBlank()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                        } else {
                            isSubmitting = true
                            val reportId = UUID.randomUUID().toString()
                            val data = mapOf(
                                "id" to reportId,
                                "animalName" to animalName,
                                "issue" to issue,
                                "symptoms" to symptoms,
                                "status" to "Pending Vet",
                                "timestamp" to SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                            )

                            db.collection("sick_reports").document(reportId).set(data)
                                .addOnSuccessListener {
                                    isSubmitting = false
                                    isSubmitted = true
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), // Keep Emergency Red
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("SEND EMERGENCY REPORT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                // --- SUCCESS STATE ---
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(80.dp))
                    Text("Report Sent to Vet", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Case ID: #${animalName.take(4).uppercase()}", color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("IMMEDIATE NEXT STEPS:", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            Spacer(modifier = Modifier.height(10.dp))
                            StepItem("1", "Isolate $animalName from other livestock.")
                            StepItem("2", "Provide clean water but avoid heavy feed.")
                            StepItem("3", "Keep your phone nearby; the vet will call shortly.")
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(num: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("$num.", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun SickInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        // Label styling matched exactly to your Register screen
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.LightGray) },
            leadingIcon = { Icon(icon, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD32F2F),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            singleLine = true
        )
    }
}