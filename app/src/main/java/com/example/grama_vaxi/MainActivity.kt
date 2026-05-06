package com.example.grama_vaxi

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private var loginDone: (() -> Unit)? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseLogin(account.idToken!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("6624763231-dhuq73lc7g9lc7navfmk99fk9edrl0lp.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        setContent {
            var loggedIn by remember { mutableStateOf(auth.currentUser != null) }
            var currentScreen by remember { mutableStateOf("dashboard") }

            // TRIGGER EVERY TIME THE APP OPENS
            LaunchedEffect(loggedIn) {
                if (loggedIn) {
                    setupBackgroundWork() // 12-hour cycle
                    triggerImmediateCheck(this@MainActivity) // Immediate launch check
                }
            }

            loginDone = {
                loggedIn = true
                currentScreen = "dashboard"
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    if (!loggedIn) {
                        LoginScreen(onGoogleClick = { startGoogleLogin() })
                    } else {
                        when (currentScreen) {
                            "dashboard" -> HomeScreen(
                                onRegisterClick = { currentScreen = "register" },
                                onRecordsClick = { currentScreen = "records" },
                                onVaccineClick = { currentScreen = "vaccine" },
                                onSickClick = { currentScreen = "sick" },
                                onProfileClick = { currentScreen = "profile" }
                            )
                            "register" -> RegisterAnimalScreen(onBackClick = { currentScreen = "dashboard" })
                            "records" -> AnimalRecordsScreen(onBackClick = { currentScreen = "dashboard" }, onEditClick = {})
                            "vaccine" -> VaccineAlertsScreen(onBackClick = { currentScreen = "dashboard" })
                            "sick" -> SickReportScreen(onBackClick = { currentScreen = "dashboard" })
                            "profile" -> ProfileScreen(onBackClick = { currentScreen = "dashboard" })
                        }
                    }
                }
            }
        }
    }

    private fun setupBackgroundWork() {
        val workManager = WorkManager.getInstance(this)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val periodicRequest = PeriodicWorkRequestBuilder<VaccineNotificationWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints).build()

        workManager.enqueueUniquePeriodicWork("VaccineAlertCheck", ExistingPeriodicWorkPolicy.KEEP, periodicRequest)
    }

    private fun triggerImmediateCheck(context: Context) {
        val immediateRequest = OneTimeWorkRequestBuilder<VaccineNotificationWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueue(immediateRequest)
    }

    private fun startGoogleLogin() { launcher.launch(googleClient.signInIntent) }

    private fun firebaseLogin(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) { loginDone?.invoke() }
        }
    }
}

@Composable
fun LoginScreen(onGoogleClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF4CAF50), Color(0xFFF1F8E9))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(130.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🐄", fontSize = 64.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Grama-Vaxi", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text("Smart Livestock Management", color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp)

            Spacer(modifier = Modifier.height(64.dp))

            Card(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                onClick = onGoogleClick
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Continue with Google", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Secure access for verified farmers", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}