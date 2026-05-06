package com.example.grama_vaxi

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Get the Unique ID of the logged-in Google User
    val user = auth.currentUser
    val userId = user?.uid ?: "guest_user" // Fallback if not logged in

    // State variables
    var name by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "") } // Read-only from Google
    var isLoading by remember { mutableStateOf(true) }

    // ════ FETCH DATA BASED ON GOOGLE ID ════
    LaunchedEffect(userId) {
        if (userId != "guest_user") {
            db.collection("farmers").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name = document.getString("name") ?: ""
                        village = document.getString("village") ?: ""
                        phone = document.getString("phone") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFFBFDFB),
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0F9D58))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ════ PROFILE AVATAR ════
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍🌾", fontSize = 48.sp)
                }

                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ════ INPUT FIELDS ════
                ProfileInput("Full Name", name, { name = it }, "Enter your name", Icons.Default.Person)
                ProfileInput("Village", village, { village = it }, "Enter your village", Icons.Default.Home)
                ProfileInput("Phone Number", phone, { phone = it }, "+91 XXXXX XXXXX", Icons.Default.Phone)

                Spacer(modifier = Modifier.height(40.dp))

                // ════ SAVE / UPDATE BUTTON ════
                Button(
                    onClick = {
                        if (name.isBlank() || village.isBlank() || phone.isBlank()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else {
                            val farmerData = hashMapOf(
                                "name" to name,
                                "village" to village,
                                "phone" to phone,
                                "email" to email,
                                "updatedAt" to System.currentTimeMillis()
                            )

                            db.collection("farmers").document(userId).set(farmerData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Update Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// ════ REUSABLE INPUT COMPONENT ════
@Composable
fun ProfileInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.LightGray) },
            leadingIcon = { Icon(icon, null, tint = Color(0xFF0F9D58)) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0F9D58),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}