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
    onBackClick: () -> Unit,
    isKannada: Boolean
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val scope = rememberCoroutineScope()

    // FORM STATES
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

    // AI STATES
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

                        if (isKannada)
                            "ಆರೋಗ್ಯ ತುರ್ತು ಪರಿಸ್ಥಿತಿ"
                        else
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
                .verticalScroll(
                    rememberScrollState()
                )

        ) {

            if (!isSubmitted) {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // ALERT BANNER
                Surface(

                    color = Color(0xFFFFF3E0),

                    shape = RoundedCornerShape(16.dp),

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {

                    Row(

                        modifier = Modifier.padding(12.dp),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            "🚨",
                            fontSize = 24.sp
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Text(

                            if (isKannada)
                                "ಇದು ಸ್ಥಳೀಯ ಪಶುವೈದ್ಯಾಧಿಕಾರಿಗೆ ತಕ್ಷಣ ಮಾಹಿತಿ ಕಳುಹಿಸುತ್ತದೆ."
                            else
                                "This alerts the local vet officer instantly.",

                            fontSize = 12.sp,

                            color = Color.DarkGray
                        )
                    }
                }

                // ANIMAL TYPE
                SickInput(

                    label =

                        if (isKannada)
                            "ಪ್ರಾಣಿಯ ಪ್ರಕಾರ"
                        else
                            "Animal Type",

                    value = animalType,

                    onValueChange = {
                        animalType = it
                    },

                    placeholder =

                        if (isKannada)
                            "ಉದಾ: ಹಸು"
                        else
                            "e.g. Cow",

                    icon = Icons.Default.Pets
                )

                // PRIMARY ISSUE
                SickInput(

                    label =

                        if (isKannada)
                            "ಮುಖ್ಯ ಸಮಸ್ಯೆ"
                        else
                            "Primary Issue",

                    value = issue,

                    onValueChange = {
                        issue = it
                    },

                    placeholder =

                        if (isKannada)
                            "ಉದಾ: ಆಹಾರ ತಿನ್ನುವುದಿಲ್ಲ"
                        else
                            "e.g. Loss of appetite",

                    icon = Icons.Default.Warning
                )

                // SYMPTOMS
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {

                    Text(

                        if (isKannada)
                            "ವಿಸ್ತೃತ ಲಕ್ಷಣಗಳು"
                        else
                            "Detailed Symptoms",

                        fontSize = 13.sp,

                        fontWeight =
                            FontWeight.Medium,

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

                                if (isKannada)
                                    "ಉದಾ: ಬಾಯಿಯಲ್ಲಿ ಗಾಯ, ಜ್ವರ, ಕಾಲಿನ ನೋವು..."
                                else
                                    "e.g. Mouth wounds, fever, foot pain..."
                            )
                        },

                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // AI BUTTON
                Button(

                    onClick = {

                        if (
                            animalType.isBlank()
                            || issue.isBlank()
                            || symptoms.length < 8
                        ) {

                            Toast.makeText(

                                context,

                                if (isKannada)
                                    "ದಯವಿಟ್ಟು ಎಲ್ಲಾ ವಿವರಗಳನ್ನು ನಮೂದಿಸಿ"
                                else
                                    "Please enter all details",

                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            isAiLoading = true
                            aiAdvice = ""

                            scope.launch(Dispatchers.Main) {

                                try {

                                    // ✅ GET PROFESSIONAL ENGLISH ADVICE

                                    val englishAdvice =

                                        GramaVaxiAI
                                            .getEmergencyAdvice(

                                                animal =
                                                    animalType,

                                                issue =
                                                    issue,

                                                symptoms =
                                                    symptoms,

                                                isKannada =
                                                    false
                                            )

                                    // ✅ TRANSLATE TO KANNADA

                                    aiAdvice =

                                        if (isKannada) {

                                            KannadaTranslator
                                                .translateToKannada(
                                                    englishAdvice
                                                )

                                        } else {

                                            englishAdvice
                                        }

                                } catch (e: Exception) {

                                    aiAdvice =

                                        if (isKannada)

                                            "AI ಸಲಹೆ ಪಡೆಯಲು ಸಾಧ್ಯವಾಗಲಿಲ್ಲ."

                                        else

                                            "Unable to fetch AI advice."
                                }

                                isAiLoading = false
                            }
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),

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

                        if (isKannada)
                            "AI ಸಲಹೆ ಪಡೆಯಿರಿ"
                        else
                            "Get AI Advice",

                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                // AI RESULT CARD
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

                                    if (isKannada)
                                        "AI ಪಶುವೈದ್ಯ ಸಲಹೆ"
                                    else
                                        "AI Veterinary Advice",

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

                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }
                    }
                }

                // SUBMIT BUTTON
                Button(

                    onClick = {

                        if (
                            animalType.isBlank()
                            || issue.isBlank()
                        ) {

                            Toast.makeText(

                                context,

                                if (isKannada)
                                    "ದಯವಿಟ್ಟು ಎಲ್ಲಾ ವಿವರಗಳನ್ನು ನಮೂದಿಸಿ"
                                else
                                    "Please fill all fields",

                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            isSubmitting = true

                            val report = mapOf(

                                "animalType" to animalType,

                                "issue" to issue,

                                "symptoms" to symptoms,

                                "aiAdvice" to aiAdvice,

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

                                        if (isKannada)
                                            "ವರದಿ ಕಳುಹಿಸಲು ವಿಫಲವಾಗಿದೆ"
                                        else
                                            "Failed to send report",

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

                            if (isKannada)
                                "ತುರ್ತು ವರದಿ ಕಳುಹಿಸಿ"
                            else
                                "SEND EMERGENCY REPORT",

                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

            } else {

                // SUCCESS SCREEN
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

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(

                        if (isKannada)
                            "ವರದಿ ಯಶಸ್ವಿಯಾಗಿ ಕಳುಹಿಸಲಾಗಿದೆ"
                        else
                            "Report Sent Successfully",

                        fontWeight = FontWeight.Bold,

                        fontSize = 22.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(

                        if (isKannada)
                            "ಪ್ರಾಣಿ: $animalType"
                        else
                            "Animal: $animalType",

                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    if (aiAdvice.isNotEmpty()) {

                        Text(

                            if (isKannada)
                                "AI ಸಲಹೆ:"
                            else
                                "AI Advice:",

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

                        Text(

                            if (isKannada)
                                "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್‌ಗೆ ಹಿಂತಿರುಗಿ"
                            else
                                "Return to Dashboard"
                        )
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