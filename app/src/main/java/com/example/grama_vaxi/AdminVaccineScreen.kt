package com.example.grama_vaxi

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AdminVaccineScreen(
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var animalName by remember { mutableStateOf("") }
    var vaccineName by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(
                rememberScrollState()
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⬅ Back to Dashboard")
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            text = "🛠 Admin Vaccine Panel",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F9D58)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = animalName,
            onValueChange = {
                animalName = it
            },
            label = {
                Text("Animal Name")
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = vaccineName,
            onValueChange = {
                vaccineName = it
            },
            label = {
                Text("Vaccine Name")
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = dueDate,
            onValueChange = {
                dueDate = it
            },
            label = {
                Text("Due Date")
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        OutlinedTextField(
            value = status,
            onValueChange = {
                status = it
            },
            label = {
                Text("Status")
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(
            onClick = {

                val vaccine =
                    hashMapOf(
                        "animalName" to animalName,
                        "vaccineName" to vaccineName,
                        "dueDate" to dueDate,
                        "status" to status
                    )

                db.collection("vaccines")
                    .add(vaccine)
                    .addOnSuccessListener {

                        Toast.makeText(
                            context,
                            "Saved Successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        animalName = ""
                        vaccineName = ""
                        dueDate = ""
                        status = ""
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            context,
                            "Failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape =
                RoundedCornerShape(14.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF0F9D58)
                )
        ) {
            Text("Save Vaccine Alert")
        }
    }
}