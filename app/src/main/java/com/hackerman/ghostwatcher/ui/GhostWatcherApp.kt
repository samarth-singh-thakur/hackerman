package com.hackerman.ghostwatcher.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private val GHOSTWATCHER_URL: String
    get() = if (isEmulator()) "http://192.168.1.9:8000/ghostwatcher " else "http://192.168.1.9:8000/ghostwatcher "

private fun isEmulator(): Boolean {
    return Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk" == Build.PRODUCT
}

@Composable
fun GhostWatcherApp() {
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var status by remember { mutableStateOf("SYSTEM READY // Awaiting authorization...") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val glitch = rememberInfiniteTransition(label = "glitch")
    val pulse by glitch.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val shimmer by glitch.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF030604), Color(0xFF08110B), Color(0xFF000000))
                )
            )
            .padding(20.dp)
    ) {
        // Close button in top-left when image is displayed
        if (imageBytes != null) {
            IconButton(
                onClick = {
                    imageBytes = null
                    status = "SYSTEM READY // Awaiting authorization..."
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(48.dp)
                    .background(Color(0xFF11D46E), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "MR. ROBOT // GHOSTWATCHER",
                color = Color(0xFF72FFA9),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(shimmer)
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = status,
                color = Color(0xFFB6FFD2),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(24.dp))

            // Only show HACK button when no image is displayed and not loading
            if (imageBytes == null && !loading) {
                Button(
                onClick = {
                    scope.launch {
                        loading = true
                        
                        // Cinematic hacking sequence with faster delays
                        status = ">>> Initializing SSH connection..."
                        delay(400)
                        playReelLikeBeat()
                        
                        status = ">>> SSH tunnel established [192.168.1.9:22]"
                        delay(350)
                        
                        status = ">>> Bypassing firewall protocols..."
                        delay(300)
                        
                        status = ">>> Installing malicious script [payload.sh]"
                        delay(450)
                        
                        status = ">>> Executing remote code injection..."
                        delay(350)
                        
                        status = ">>> Scanning network topology..."
                        delay(300)
                        
                        status = ">>> TARGET ACQUIRED // Intercepting feed..."
                        delay(250)
                        
                        val result = fetchGhostWatcherFrame()
                        result.fold(
                            onSuccess = {
                                imageBytes = it
                                status = "✓ BREACH SUCCESSFUL // Ghost frame extracted"
                            },
                            onFailure = {
                                status = "✗ OPERATION FAILED // ${it.message}"
                            }
                        )
                        loading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF11D46E),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .scale(pulse)
                    .fillMaxWidth(0.55f)
                    .height(54.dp)
                ) {
                    Text("HACK", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }

            Spacer(Modifier.height(22.dp))

            when {
                loading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                imageBytes != null -> AsyncImage(
                    model = imageBytes,
                    contentDescription = "GhostWatcher feed image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .alpha(shimmer)
                )
            }
        }
    }
}

private suspend fun fetchGhostWatcherFrame(): Result<ByteArray> = withContext(Dispatchers.IO) {
    runCatching {
        val client = OkHttpClient()
        val request = Request.Builder().url(GHOSTWATCHER_URL).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            response.body?.bytes() ?: error("Empty response body")
        }
    }
}

private suspend fun playReelLikeBeat() = withContext(Dispatchers.Default) {
    val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
    tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180)
    delay(160)
    tone.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 220)
    delay(200)
    tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 260)
    delay(260)
    tone.release()
}
