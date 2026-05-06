package com.example.grama_vaxi

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SickReportScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var animalName by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFFBFDFB),
        topBar = {
            TopAppBar(
                title = {
                    Text("Report Illness", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Emergency Instruction Tip
            Surface(
                color = Color(0xFFFFF3E0), // Subtle orange for medical attention
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Text("🚑", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Provide accurate details to help the vet understand the situation better.",
                        fontSize = 12.sp,
                        color = Color(0xFFE65100),
                        lineHeight = 16.sp
                    )
                }
            }

            // Input Section using the same style as Register Screen
            SickInput(
                label = "Animal Name",
                value = animalName,
                onValueChange = { animalName = it },
                placeholder = "e.g. Laxmi",
                icon = Icons.Default.Pets
            )

            SickInput(
                label = "Primary Issue",
                value = issue,
                onValueChange = { issue = it },
                placeholder = "e.g. High fever, Not eating",
                icon = Icons.Default.HealthAndSafety
            )

            // Symptoms input - multi-line for better reporting
            Text(
                text = "Symptoms Detail",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF555555),
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), // Taller for long descriptions
                placeholder = { Text("Describe specific symptoms...", color = Color.LightGray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.MedicalInformation, null, tint = Color(0xFF0F9D58)) },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F9D58),
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (animalName.isBlank() || issue.isBlank()) {
                        Toast.makeText(context, "Please fill in the details", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Report Submitted Successfully", Toast.LENGTH_LONG).show()
                        animalName = ""; issue = ""; symptoms = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Red color for health emergency
            ) {
                Text("Submit Sick Report", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
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
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF555555),
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.LightGray, fontSize = 14.sp) },
            leadingIcon = { Icon(icon, null, tint = Color(0xFF0F9D58), modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0F9D58),
                unfocusedBorderColor = Color(0xFFEEEEEE),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
    }
}