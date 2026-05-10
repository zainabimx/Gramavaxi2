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
    onBackClick: () -> Unit,
    isKannada: Boolean
) {

    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()

    val auth = FirebaseAuth.getInstance()

    val user = auth.currentUser

    val userId =
        user?.uid ?: "guest_user"

    // STATES
    var name by remember {
        mutableStateOf("")
    }

    var village by remember {
        mutableStateOf("")
    }

    var phone by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf(
            user?.email ?: ""
        )
    }

    // TRANSLATED STATES
    var translatedName by remember {
        mutableStateOf("")
    }

    var translatedVillage by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    // FETCH DATA
    LaunchedEffect(userId, isKannada) {

        if (userId != "guest_user") {

            db.collection("farmers")
                .document(userId)
                .get()

                .addOnSuccessListener { document ->

                    if (
                        document != null
                        && document.exists()
                    ) {

                        name =
                            document.getString("name")
                                ?: ""

                        village =
                            document.getString("village")
                                ?: ""

                        phone =
                            document.getString("phone")
                                ?: ""

                        // TRANSLATE TO KANNADA
                        if (isKannada) {

                            translatedName =
                                name

                            translatedVillage =
                                village

                        } else {

                            translatedName =
                                name

                            translatedVillage =
                                village
                        }
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

    // TRANSLATION EFFECT
    LaunchedEffect(isKannada, name, village) {

        if (isKannada) {

            translatedName =
                KannadaTranslator
                    .translateToKannada(name)

            translatedVillage =
                KannadaTranslator
                    .translateToKannada(village)

        } else {

            translatedName = name

            translatedVillage = village
        }
    }

    Scaffold(

        containerColor = Color(0xFFFBFDFB),

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        if (isKannada)
                            "ನನ್ನ ಪ್ರೊಫೈಲ್"
                        else
                            "My Profile",

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

    ) { paddingValues ->

        if (isLoading) {

            Box(

                modifier = Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                CircularProgressIndicator(
                    color = Color(0xFF0F9D58)
                )
            }

        } else {

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                // PROFILE AVATAR
                Box(

                    modifier = Modifier
                        .size(100.dp)

                        .clip(CircleShape)

                        .background(
                            Color(0xFFE8F5E9)
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        "👨‍🌾",
                        fontSize = 48.sp
                    )
                }

                Text(

                    text = email,

                    fontSize = 14.sp,

                    color = Color.Gray,

                    modifier = Modifier.padding(
                        top = 8.dp
                    )
                )

                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                // NAME
                ProfileInput(

                    label =
                        if (isKannada)
                            "ಪೂರ್ಣ ಹೆಸರು"
                        else
                            "Full Name",

                    value =
                        if (isKannada)
                            translatedName
                        else
                            name,

                    onValueChange = {
                        name = it
                    },

                    placeholder =
                        if (isKannada)
                            "ನಿಮ್ಮ ಹೆಸರನ್ನು ನಮೂದಿಸಿ"
                        else
                            "Enter your name",

                    icon = Icons.Default.Person
                )

                // VILLAGE
                ProfileInput(

                    label =
                        if (isKannada)
                            "ಗ್ರಾಮ"
                        else
                            "Village",

                    value =
                        if (isKannada)
                            translatedVillage
                        else
                            village,

                    onValueChange = {
                        village = it
                    },

                    placeholder =
                        if (isKannada)
                            "ನಿಮ್ಮ ಗ್ರಾಮದ ಹೆಸರು"
                        else
                            "Enter your village",

                    icon = Icons.Default.Home
                )

                // PHONE
                ProfileInput(

                    label =
                        if (isKannada)
                            "ದೂರವಾಣಿ ಸಂಖ್ಯೆ"
                        else
                            "Phone Number",

                    value = phone,

                    onValueChange = {
                        phone = it
                    },

                    placeholder =
                        "+91 XXXXX XXXXX",

                    icon = Icons.Default.Phone
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                // SAVE BUTTON
                Button(

                    onClick = {

                        if (
                            name.isBlank()
                            || village.isBlank()
                            || phone.isBlank()
                        ) {

                            Toast.makeText(

                                context,

                                if (isKannada)
                                    "ದಯವಿಟ್ಟು ಎಲ್ಲಾ ವಿವರಗಳನ್ನು ಭರ್ತಿ ಮಾಡಿ"
                                else
                                    "Please fill all fields",

                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            val farmerData =
                                hashMapOf(

                                    "name" to name,

                                    "village" to village,

                                    "phone" to phone,

                                    "email" to email,

                                    "updatedAt" to
                                            System.currentTimeMillis()
                                )

                            db.collection("farmers")
                                .document(userId)
                                .set(farmerData)

                                .addOnSuccessListener {

                                    Toast.makeText(

                                        context,

                                        if (isKannada)
                                            "ಪ್ರೊಫೈಲ್ ಯಶಸ್ವಿಯಾಗಿ ನವೀಕರಿಸಲಾಗಿದೆ!"
                                        else
                                            "Profile Updated Successfully!",

                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                .addOnFailureListener { e ->

                                    Toast.makeText(

                                        context,

                                        if (isKannada)
                                            "ದೋಷ: ${e.message}"
                                        else
                                            "Error: ${e.message}",

                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape = RoundedCornerShape(16.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F9D58)
                    )
                ) {

                    Icon(
                        Icons.Default.Check,
                        contentDescription = null
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(

                        if (isKannada)
                            "ಪ್ರೊಫೈಲ್ ಉಳಿಸಿ"
                        else
                            "Save & Update Profile",

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}

// INPUT COMPONENT
@Composable
fun ProfileInput(

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

            fontWeight =
                FontWeight.Bold,

            color = Color.DarkGray,

            modifier = Modifier.padding(
                start = 4.dp,
                bottom = 4.dp
            )
        )

        OutlinedTextField(

            value = value,

            onValueChange = onValueChange,

            modifier = Modifier.fillMaxWidth(),

            placeholder = {

                Text(

                    placeholder,

                    fontSize = 14.sp,

                    color = Color.LightGray
                )
            },

            leadingIcon = {

                Icon(
                    icon,
                    null,
                    tint = Color(0xFF0F9D58)
                )
            },

            shape = RoundedCornerShape(12.dp),

            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor =
                    Color(0xFF0F9D58),

                unfocusedBorderColor =
                    Color(0xFFE0E0E0)
            )
        )
    }
}