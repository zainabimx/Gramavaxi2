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
    editAnimalId: String? = null,
    isKannada: Boolean
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // FORM STATE
    var animalName by remember {
        mutableStateOf("")
    }

    var animalType by remember {
        mutableStateOf("")
    }

    var age by remember {
        mutableStateOf("")
    }

    var ownerName by remember {
        mutableStateOf("")
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    // EDIT MODE LOADING
    LaunchedEffect(editAnimalId) {

        if (editAnimalId != null) {

            db.collection("animals")
                .document(editAnimalId)
                .get()

                .addOnSuccessListener { doc ->

                    if (doc.exists()) {

                        animalName =
                            doc.getString("animalName")
                                ?: ""

                        animalType =
                            doc.getString("animalType")
                                ?: ""

                        age =
                            doc.getString("age")
                                ?: ""

                        ownerName =
                            doc.getString("ownerName")
                                ?: ""

                        doc.getString("imageUri")
                            ?.let {

                                if (it.isNotEmpty()) {

                                    selectedImageUri =
                                        Uri.parse(it)
                                }
                            }
                    }
                }

                .addOnFailureListener {

                    Toast.makeText(

                        context,

                        if (isKannada)
                            "ಡೇಟಾ ಲೋಡ್ ದೋಷ"
                        else
                            "Error loading data",

                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    val photoLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            selectedImageUri = uri
        }

    Scaffold(

        containerColor = Color(0xFFFBFDFB),

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        if (editAnimalId == null) {

                            if (isKannada)
                                "ಹೊಸ ನೋಂದಣಿ"
                            else
                                "New Registration"

                        } else {

                            if (isKannada)
                                "ಪ್ರಾಣಿ ವಿವರಗಳನ್ನು ನವೀಕರಿಸಿ"
                            else
                                "Update Animal Details"
                        },

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

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())

        ) {

            // CONTEXT BANNER
            Surface(

                color =
                    if (editAnimalId == null)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFE3F2FD),

                shape = RoundedCornerShape(16.dp),

                modifier = Modifier.padding(vertical = 16.dp)

            ) {

                Row(

                    modifier = Modifier.padding(12.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(

                        if (editAnimalId == null)
                            "🐄"
                        else
                            "✏️",

                        fontSize = 24.sp
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text(

                        if (editAnimalId == null) {

                            if (isKannada)
                                "ಉತ್ತಮ ಆರೋಗ್ಯ ಟ್ರ್ಯಾಕಿಂಗ್‌ಗಾಗಿ ಸರಿಯಾದ ವಿವರಗಳನ್ನು ನೀಡಿ."
                            else
                                "Provide accurate details for better health tracking."

                        } else {

                            if (isKannada)
                                "ಮಾಹಿತಿಯನ್ನು ನವೀಕರಿಸಿ ಮತ್ತು ಉಳಿಸಿ."
                            else
                                "Update the info below and save to refresh records."
                        },

                        fontSize = 12.sp,

                        color = Color.DarkGray
                    )
                }
            }

            // PHOTO SECTION
            Text(

                if (isKannada)
                    "ಪ್ರಾಣಿ ಫೋಟೋ"
                else
                    "Animal Photo",

                fontWeight = FontWeight.Bold,

                fontSize = 14.sp,

                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Box(

                modifier = Modifier
                    .size(120.dp)

                    .align(
                        Alignment.CenterHorizontally
                    )

                    .clip(
                        RoundedCornerShape(20.dp)
                    )

                    .background(Color(0xFFF5F5F5))

                    .border(
                        1.dp,
                        Color(0xFF0F9D58).copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )

                    .clickable {
                        photoLauncher.launch("image/*")
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                if (selectedImageUri != null) {

                    Image(

                        painter =
                            rememberAsyncImagePainter(
                                selectedImageUri
                            ),

                        contentDescription =
                            "Animal Photo",

                        modifier = Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )

                } else {

                    Column(

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Icon(
                            Icons.Default.AddAPhoto,
                            null,
                            tint = Color(0xFF0F9D58)
                        )

                        Text(

                            if (isKannada)
                                "ಫೋಟೋ ಸೇರಿಸಿ"
                            else
                                "Add Photo",

                            fontSize = 11.sp,

                            color = Color(0xFF0F9D58)
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // INPUT FIELDS
            ModernRegisterInput(

                label =
                    if (isKannada)
                        "ಪ್ರಾಣಿ ಹೆಸರು"
                    else
                        "Animal Name",

                value = animalName,

                onValueChange = {
                    animalName = it
                },

                placeholder =
                    if (isKannada)
                        "ಉದಾ: ಗಂಗಾ"
                    else
                        "e.g. Ganga",

                icon = Icons.Default.Pets
            )

            ModernRegisterInput(

                label =
                    if (isKannada)
                        "ಪಶು ಪ್ರಕಾರ"
                    else
                        "Livestock Type",

                value = animalType,

                onValueChange = {
                    animalType = it
                },

                placeholder =
                    if (isKannada)
                        "ಉದಾ: ಹಸು"
                    else
                        "e.g. Cow, Buffalo",

                icon = Icons.Default.Badge
            )

            ModernRegisterInput(

                label =
                    if (isKannada)
                        "ವಯಸ್ಸು (ವರ್ಷ)"
                    else
                        "Age (Years)",

                value = age,

                onValueChange = {
                    age = it
                },

                placeholder =
                    if (isKannada)
                        "ಉದಾ: 3"
                    else
                        "e.g. 3",

                icon = Icons.Default.Numbers
            )

            ModernRegisterInput(

                label =
                    if (isKannada)
                        "ಮಾಲೀಕರ ಹೆಸರು"
                    else
                        "Owner Name",

                value = ownerName,

                onValueChange = {
                    ownerName = it
                },

                placeholder =
                    if (isKannada)
                        "ರೈತರ ಹೆಸರು"
                    else
                        "Farmer's name",

                icon = Icons.Default.Person
            )

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            // SAVE BUTTON
            Button(

                onClick = {

                    if (
                        animalName.isBlank()
                        || animalType.isBlank()
                    ) {

                        Toast.makeText(

                            context,

                            if (isKannada)
                                "ಅಗತ್ಯ ವಿವರಗಳನ್ನು ಭರ್ತಿ ಮಾಡಿ"
                            else
                                "Please fill required fields",

                            Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    isSaving = true

                    val animalData =
                        hashMapOf(

                            "animalName" to animalName,

                            "animalType" to animalType,

                            "age" to age,

                            "ownerName" to ownerName,

                            "imageUri" to
                                    (
                                            selectedImageUri
                                                ?.toString()
                                                ?: ""
                                            )
                        )

                    if (editAnimalId != null) {

                        db.collection("animals")
                            .document(editAnimalId)

                            .set(
                                animalData,
                                SetOptions.merge()
                            )

                            .addOnSuccessListener {

                                isSaving = false

                                Toast.makeText(

                                    context,

                                    if (isKannada)
                                        "ನವೀಕರಣ ಯಶಸ್ವಿ!"
                                    else
                                        "Update Successful!",

                                    Toast.LENGTH_SHORT
                                ).show()

                                onBackClick()
                            }

                    } else {

                        val sdf =
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            )

                        val cal =
                            Calendar.getInstance()

                        val registrationDate =
                            sdf.format(cal.time)

                        cal.add(Calendar.MONTH, 6)

                        val nextVaccination =
                            sdf.format(cal.time)

                        animalData["lastVaccinated"] =
                            registrationDate

                        animalData["nextVaccinationDue"] =
                            nextVaccination

                        animalData["vaccinationStatus"] =
                            "Up-to-Date"

                        db.collection("animals")
                            .add(animalData)

                            .addOnSuccessListener {

                                isSaving = false

                                Toast.makeText(

                                    context,

                                    if (isKannada)
                                        "ನೋಂದಣಿ ಪೂರ್ಣಗೊಂಡಿದೆ!"
                                    else
                                        "Registration Complete!",

                                    Toast.LENGTH_SHORT
                                ).show()

                                onBackClick()
                            }
                    }
                },

                enabled = !isSaving,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F9D58)
                )
            ) {

                if (isSaving) {

                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                } else {

                    Text(

                        text =

                            if (editAnimalId == null) {

                                if (isKannada)
                                    "ನೋಂದಣಿ ಪೂರ್ಣಗೊಳಿಸಿ"
                                else
                                    "Complete Registration"

                            } else {

                                if (isKannada)
                                    "ಬದಲಾವಣೆಗಳನ್ನು ಉಳಿಸಿ"
                                else
                                    "Save Changes"
                            },

                        fontWeight =
                            FontWeight.Bold,

                        fontSize = 16.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Composable
fun ModernRegisterInput(

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
                FontWeight.Medium,

            color = Color.DarkGray
        )

        Spacer(
            modifier = Modifier.height(4.dp)
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

                    tint = Color(0xFF0F9D58),

                    modifier = Modifier.size(20.dp)
                )
            },

            shape = RoundedCornerShape(12.dp),

            colors = OutlinedTextFieldDefaults.colors(

                focusedBorderColor =
                    Color(0xFF0F9D58),

                unfocusedBorderColor =
                    Color(0xFFE0E0E0)
            ),

            singleLine = true
        )
    }
}