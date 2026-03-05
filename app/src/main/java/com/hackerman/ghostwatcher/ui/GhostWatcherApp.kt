package com.hackerman.ghostwatcher.ui

import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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

private const val GHOSTWATCHER_URL = "http://127.0.0.1:8000/ghostwatcher"

@Composable
fun GhostWatcherApp() {
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var status by remember { mutableStateOf("Tap START to summon the fsociety ghost feed") }
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

            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        status = "Intercepting feed from $GHOSTWATCHER_URL"
                        playReelLikeBeat()
                        val result = fetchGhostWatcherFrame()
                        result.fold(
                            onSuccess = {
                                imageBytes = it
                                status = "Signal locked. Ghost frame rendered."
                            },
                            onFailure = {
                                status = "Feed error: ${it.message}"
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
                Text("START", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
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
