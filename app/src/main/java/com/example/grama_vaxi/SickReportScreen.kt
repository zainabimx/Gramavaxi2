package com.example.grama_vaxi

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SickReportScreen(
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val scope = rememberCoroutineScope()

    // Form States
    var animalType by remember {
        mutableStateOf("")
    }

    var issue by remember {
        mutableStateOf("")
    }

    var symptoms by remember {
        mutableStateOf("")
    }

    var isSubmitted by remember {
        mutableStateOf(false)
    }

    var isSubmitting by remember {
        mutableStateOf(false)
    }

    // AI States
    var aiAdvice by remember {
        mutableStateOf("")
    }

    var isAiLoading by remember {
        mutableStateOf(false)
    }

    Scaffold(

        containerColor = Color(0xFFFBFDFB),

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        "Health Emergency",
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())

        ) {

            if (!isSubmitted) {

                Spacer(modifier = Modifier.height(16.dp))

                // Alert Banner
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {

                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            "🚨",
                            fontSize = 24.sp
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Text(
                            "This alerts the local vet officer instantly.",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                // Animal Type
                SickInput(
                    label = "Animal Type",
                    value = animalType,
                    onValueChange = {
                        animalType = it
                    },
                    placeholder = "e.g. Goat",
                    icon = Icons.Default.Pets
                )

                // Primary Issue
                SickInput(
                    label = "Primary Issue",
                    value = issue,
                    onValueChange = {
                        issue = it
                    },
                    placeholder = "e.g. Fever",
                    icon = Icons.Default.Warning
                )

                // Symptoms
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {

                    Text(
                        "Detailed Symptoms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )

                    OutlinedTextField(

                        value = symptoms,

                        onValueChange = {
                            symptoms = it
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),

                        placeholder = {
                            Text(
                                "Describe behavioral changes..."
                            )
                        },

                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // AI Advice Button
                Button(

                    onClick = {

                        if (
                            animalType.isBlank()
                            || issue.isBlank()
                            || symptoms.length < 10
                        ) {

                            Toast.makeText(
                                context,
                                "Please fill all details properly",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            isAiLoading = true
                            aiAdvice = ""

                            scope.launch(Dispatchers.Main) {

                                aiAdvice =
                                    GramaVaxiAI.getEmergencyAdvice(
                                        animal = animalType,
                                        issue = issue,
                                        symptoms = symptoms
                                    )

                                isAiLoading = false
                            }
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0)
                    )
                ) {

                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        "Get AI Advice",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // AI Advice Card
                AnimatedVisibility(
                    visible =
                        aiAdvice.isNotEmpty()
                                || isAiLoading
                ) {

                    Card(

                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),

                        shape = RoundedCornerShape(16.dp),

                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.AutoAwesome,
                                    null,
                                    tint = Color(0xFF1565C0)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text(
                                    "AI Smart Advice",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }

                            if (isAiLoading) {

                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )

                            } else {

                                Text(
                                    aiAdvice,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Submit Report
                Button(

                    onClick = {

                        if (
                            animalType.isBlank()
                            || issue.isBlank()
                        ) {

                            Toast.makeText(
                                context,
                                "Please fill fields",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            isSubmitting = true

                            val report = mapOf(

                                "animalType" to animalType,

                                "issue" to issue,

                                "symptoms" to symptoms,

                                "status" to "Critical",

                                "timestamp" to
                                        SimpleDateFormat(
                                            "dd MMM, hh:mm a",
                                            Locale.getDefault()
                                        ).format(Date())
                            )

                            db.collection("sick_reports")
                                .add(report)

                                .addOnSuccessListener {

                                    isSubmitting = false
                                    isSubmitted = true
                                }

                                .addOnFailureListener {

                                    isSubmitting = false

                                    Toast.makeText(
                                        context,
                                        "Upload Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),

                    enabled = !isSubmitting
                ) {

                    if (isSubmitting) {

                        CircularProgressIndicator(
                            color = Color.White
                        )

                    } else {

                        Text(
                            "SEND EMERGENCY REPORT",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            } else {

                // Success Screen
                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(80.dp)
                    )

                    Text(
                        "Report Sent to Vet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Text(
                        "Animal: $animalType",
                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    if (aiAdvice.isNotEmpty()) {

                        Text(
                            "FOLLOW AI GUIDANCE:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Card(

                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            ),

                            shape = RoundedCornerShape(16.dp),

                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                aiAdvice,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(30.dp)
                    )

                    OutlinedButton(

                        onClick = onBackClick,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {

                        Text("Return to Dashboard")
                    }
                }
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

    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {

        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )

        OutlinedTextField(

            value = value,

            onValueChange = onValueChange,

            modifier = Modifier.fillMaxWidth(),

            placeholder = {
                Text(placeholder)
            },

            leadingIcon = {

                Icon(
                    icon,
                    null,
                    tint = Color(0xFFD32F2F)
                )
            },

            shape = RoundedCornerShape(12.dp)
        )
    }
}