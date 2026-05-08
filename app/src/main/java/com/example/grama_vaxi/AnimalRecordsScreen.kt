package com.example.grama_vaxi

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

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
    val imageUri: String = "",
    val lastVaccinated: String = "Never",
    val history: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalRecordsScreen(
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit,
    isKannada: Boolean
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val animalList =
        remember {
            mutableStateListOf<Animal>()
        }

    val globalVaccines =
        remember {
            mutableStateListOf<CampVaccine>()
        }

    var isLoading by remember {
        mutableStateOf(true)
    }

    fun loadData() {

        isLoading = true

        db.collection("vaccine_alerts")
            .get()

            .addOnSuccessListener { vaxResult ->

                globalVaccines.clear()

                vaxResult.forEach {

                    globalVaccines.add(
                        it.toObject(
                            CampVaccine::class.java
                        )
                    )
                }

                db.collection("animals")
                    .get()

                    .addOnSuccessListener { animalResult ->

                        animalList.clear()

                        animalResult.forEach { doc ->

                            val historyData =
                                doc.get("history")
                                        as? List<String>
                                    ?: emptyList()

                            animalList.add(

                                Animal(

                                    id = doc.id,

                                    animalName =
                                        doc.getString(
                                            "animalName"
                                        ) ?: "",

                                    animalType =
                                        doc.getString(
                                            "animalType"
                                        ) ?: "",

                                    age =
                                        doc.getString(
                                            "age"
                                        ) ?: "",

                                    ownerName =
                                        doc.getString(
                                            "ownerName"
                                        ) ?: "",

                                    imageUri =
                                        doc.getString(
                                            "imageUri"
                                        ) ?: "",

                                    lastVaccinated =
                                        doc.getString(
                                            "lastVaccinated"
                                        ) ?: "Never",

                                    history =
                                        historyData
                                )
                            )
                        }

                        isLoading = false
                    }
            }
    }

    val onVaccinateConfirm =
        { animalId: String, vaccineDetails: String ->

            val today =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date())

            val docRef =
                db.collection("animals")
                    .document(animalId)

            db.runTransaction { transaction ->

                val snapshot =
                    transaction.get(docRef)

                val currentHistory =
                    snapshot.get("history")
                            as? List<String>
                        ?: emptyList()

                val updatedHistory =
                    currentHistory +
                            "$vaccineDetails (Completed: $today)"

                transaction.update(
                    docRef,
                    "lastVaccinated",
                    today
                )

                transaction.update(
                    docRef,
                    "history",
                    updatedHistory
                )

            }.addOnSuccessListener {

                Toast.makeText(

                    context,

                    if (isKannada)
                        "ಆರೋಗ್ಯ ದಾಖಲೆ ನವೀಕರಿಸಲಾಗಿದೆ!"
                    else
                        "Health Record Updated!",

                    Toast.LENGTH_SHORT
                ).show()

                loadData()
            }
        }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text(

                        if (isKannada)
                            "ಪಶು ದಾಖಲೆಗಳು"
                        else
                            "LIVESTOCK RECORDS",

                        fontWeight =
                            FontWeight.Black,

                        fontSize = 18.sp
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            null
                        )
                    }
                }
            )
        },

        containerColor = Color(0xFFF8F9FA)

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)

        ) {

            if (isLoading) {

                Box(
                    Modifier.fillMaxSize(),
                    Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color = Color(0xFF2E7D32)
                    )
                }

            } else {

                LazyColumn(

                    verticalArrangement =
                        Arrangement.spacedBy(16.dp),

                    contentPadding =
                        PaddingValues(bottom = 20.dp)

                ) {

                    items(animalList) { animal ->

                        AnimalRecordCard(

                            animal = animal,

                            availableVaccines =
                                globalVaccines,

                            isKannada =
                                isKannada,

                            onEdit = {

                                onEditClick(
                                    animal.id
                                )
                            },

                            onDelete = {

                                db.collection("animals")
                                    .document(animal.id)
                                    .delete()

                                    .addOnSuccessListener {
                                        loadData()
                                    }
                            },

                            onVaccinateConfirm =
                                { details ->

                                    onVaccinateConfirm(
                                        animal.id,
                                        details
                                    )
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

    availableVaccines:
    List<CampVaccine>,

    isKannada: Boolean,

    onEdit: () -> Unit,

    onDelete: () -> Unit,

    onVaccinateConfirm:
        (String) -> Unit
) {

    var translatedAnimalName by remember {
        mutableStateOf(animal.animalName)
    }

    var translatedAnimalType by remember {
        mutableStateOf(animal.animalType)
    }

    var translatedOwnerName by remember {
        mutableStateOf(animal.ownerName)
    }

    var translatedAge by remember {
        mutableStateOf(animal.age)
    }

    var translatedHistory by remember {
        mutableStateOf(animal.history)
    }

    LaunchedEffect(
        isKannada,
        animal.animalName,
        animal.animalType,
        animal.ownerName,
        animal.age,
        animal.history
    ) {

        if (isKannada) {

            translatedAnimalName =
                try {
                    KannadaTranslator
                        .translateToKannada(
                            animal.animalName
                        )
                } catch (e: Exception) {
                    animal.animalName
                }

            translatedAnimalType =
                try {
                    KannadaTranslator
                        .translateToKannada(
                            animal.animalType
                        )
                } catch (e: Exception) {
                    animal.animalType
                }

            translatedOwnerName =
                try {
                    KannadaTranslator
                        .translateToKannada(
                            animal.ownerName
                        )
                } catch (e: Exception) {
                    animal.ownerName
                }

            translatedAge =
                try {
                    KannadaTranslator
                        .translateToKannada(
                            "${animal.age} years"
                        )
                } catch (e: Exception) {
                    animal.age
                }

            translatedHistory =
                animal.history.map {

                    try {
                        KannadaTranslator
                            .translateToKannada(it)
                    } catch (e: Exception) {
                        it
                    }
                }

        } else {

            translatedAnimalName =
                animal.animalName

            translatedAnimalType =
                animal.animalType

            translatedOwnerName =
                animal.ownerName

            translatedAge =
                animal.age

            translatedHistory =
                animal.history
        }
    }

    Card(

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.Top
            ) {

                Box(

                    modifier = Modifier
                        .size(90.dp)

                        .clip(
                            RoundedCornerShape(12.dp)
                        )

                        .background(
                            Color(0xFFF0F0F0)
                        )
                ) {

                    Image(

                        painter =
                            rememberAsyncImagePainter(
                                animal.imageUri
                            ),

                        contentDescription = null,

                        modifier = Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(

                        translatedAnimalName.uppercase(),

                        fontWeight =
                            FontWeight.ExtraBold,

                        fontSize = 18.sp
                    )

                    Text(

                        "$translatedAnimalType • $translatedAge",

                        color = Color(0xFF2E7D32),

                        fontWeight =
                            FontWeight.Bold,

                        fontSize = 14.sp
                    )

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically,

                        modifier = Modifier.padding(top = 4.dp)
                    ) {

                        Icon(

                            Icons.Default.Person,

                            null,

                            tint = Color.Gray,

                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(
                            Modifier.width(4.dp)
                        )

                        Text(

                            if (isKannada)
                                "ಮಾಲೀಕ: $translatedOwnerName"
                            else
                                "Owner: ${animal.ownerName}",

                            fontSize = 12.sp,

                            color = Color.Gray
                        )
                    }
                }

                Row {

                    IconButton(
                        onClick = onEdit
                    ) {

                        Icon(
                            Icons.Default.Edit,
                            null,
                            tint = Color(0xFF2196F3)
                        )
                    }

                    IconButton(
                        onClick = onDelete
                    ) {

                        Icon(
                            Icons.Default.Delete,
                            null,
                            tint = Color(0xFFFF1744)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            val matchingAlerts =
                availableVaccines.filter {

                    it.targetType.trim()
                        .equals(
                            animal.animalType.trim(),
                            ignoreCase = true
                        )
                }

            if (matchingAlerts.isNotEmpty()) {

                matchingAlerts.forEach { vax ->

                    val alreadyDone =
                        animal.history.any {

                            it.contains(vax.title)
                                    && it.contains(vax.campDate)
                        }

                    if (!alreadyDone) {

                        VaccinationAlertBox(

                            vax = vax,

                            isKannada =
                                isKannada,

                            onConfirm = {

                                onVaccinateConfirm(
                                    "${vax.title} (Camp: ${vax.campDate})"
                                )
                            }
                        )
                    }
                }
            }

            if (translatedHistory.isNotEmpty()) {

                Spacer(
                    Modifier.height(12.dp)
                )

                Text(

                    if (isKannada)
                        "ಡಿಜಿಟಲ್ ಆರೋಗ್ಯ ಕಾರ್ಡ್"
                    else
                        "DIGITAL HEALTH CARD",

                    fontSize = 11.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color = Color.Gray
                )

                Column(

                    modifier = Modifier
                        .fillMaxWidth()

                        .padding(top = 8.dp)

                        .background(
                            Color(0xFFF1F8E9),
                            RoundedCornerShape(10.dp)
                        )

                        .padding(8.dp)
                ) {

                    translatedHistory
                        .reversed()

                        .forEach { record ->

                            Row(

                                verticalAlignment =
                                    Alignment.CenterVertically,

                                modifier = Modifier.padding(
                                    vertical = 2.dp
                                )
                            ) {

                                Icon(

                                    Icons.Default.Done,

                                    null,

                                    tint = Color(0xFF2E7D32),

                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(
                                    Modifier.width(8.dp)
                                )

                                Text(
                                    record,
                                    fontSize = 12.sp
                                )
                            }
                        }
                }
            }
        }
    }
}

@Composable
fun VaccinationAlertBox(

    vax: CampVaccine,

    isKannada: Boolean,

    onConfirm: () -> Unit
) {

    var translatedTitle by remember {
        mutableStateOf(vax.title)
    }

    var translatedDescription by remember {
        mutableStateOf(vax.description)
    }

    LaunchedEffect(
        isKannada,
        vax.title,
        vax.description
    ) {

        if (isKannada) {

            translatedTitle =
                try {
                    KannadaTranslator
                        .translateToKannada(
                            vax.title
                        )
                } catch (e: Exception) {
                    vax.title
                }

            translatedDescription =
                try {
                    KannadaTranslator
                        .translateToKannada(
                            vax.description
                        )
                } catch (e: Exception) {
                    vax.description
                }

        } else {

            translatedTitle =
                vax.title

            translatedDescription =
                vax.description
        }
    }

    val isHighSeverity =
        vax.severity.trim()
            .equals(
                "High",
                ignoreCase = true
            )

    Surface(

        color =
            if (isHighSeverity)
                Color(0xFFFFF1F1)
            else
                Color(0xFFE8F5E9),

        shape = RoundedCornerShape(12.dp),

        border = BorderStroke(

            2.dp,

            if (isHighSeverity)
                Color.Red.copy(alpha = 0.5f)
            else
                Color(0xFF2E7D32).copy(alpha = 0.5f)
        ),

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)

    ) {

        Row(

            modifier = Modifier.padding(12.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(

                        imageVector =

                            if (isHighSeverity)
                                Icons.Default.Warning
                            else
                                Icons.Default.Info,

                        contentDescription = null,

                        tint =

                            if (isHighSeverity)
                                Color.Red
                            else
                                Color(0xFF2E7D32),

                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        Modifier.width(6.dp)
                    )

                    Text(

                        if (isKannada)
                            "ತುರ್ತು ಎಚ್ಚರಿಕೆ"
                        else
                            "URGENT ALERT",

                        fontSize = 10.sp,

                        fontWeight =
                            FontWeight.Black,

                        color =

                            if (isHighSeverity)
                                Color.Red
                            else
                                Color(0xFF2E7D32)
                    )
                }

                Text(

                    translatedTitle,

                    fontWeight =
                        FontWeight.ExtraBold,

                    fontSize = 15.sp
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    if (isKannada)
                        "ಕ್ಯಾಂಪ್ ದಿನಾಂಕ: ${vax.campDate}"
                    else
                        "Camp Date: ${vax.campDate}",

                    fontSize = 12.sp,

                    color = Color.DarkGray
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(

                    translatedDescription,

                    fontSize = 12.sp,

                    color = Color.Gray,

                    lineHeight = 18.sp
                )
            }

            IconButton(

                onClick = onConfirm,

                modifier = Modifier
                    .size(44.dp)

                    .background(
                        Color.White,
                        RoundedCornerShape(10.dp)
                    )
            ) {

                Icon(

                    Icons.Default.CheckCircle,

                    null,

                    tint = Color(0xFF2E7D32),

                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}