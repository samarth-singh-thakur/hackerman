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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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
    
    // Terminal logs state
    val terminalLogs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    
    // Blinking cursor state
    var cursorVisible by remember { mutableStateOf(true) }
    
    // System messages
    val systemMessages = listOf(
        "sys.kernel: process monitor active",
        "net.tcp: connection pool initialized",
        "crypto.aes: encryption module loaded",
        "mem.alloc: buffer cache optimized",
        "io.disk: read/write operations nominal",
        "net.firewall: packet filter enabled",
        "sys.daemon: background services running",
        "auth.token: session key refreshed"
    )

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
    
    // Scan grid animation
    val scanOffset by glitch.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )
    
    // Cursor blink effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            cursorVisible = !cursorVisible
        }
    }
    
    // Ambient terminal logs
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1500, 4000))
            val msg = systemMessages.random()
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            terminalLogs.add("[$timestamp] $msg")
            if (terminalLogs.size > 8) {
                terminalLogs.removeAt(0)
            }
            // Auto-scroll to bottom
            if (terminalLogs.isNotEmpty()) {
                listState.animateScrollToItem(terminalLogs.size - 1)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF030604), Color(0xFF08110B), Color(0xFF000000))
                )
            )
    ) {
        // Scan grid animation background - hide when photo is shown
        if (imageBytes == null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = 40.dp.toPx()
                val scanLineY = size.height * scanOffset
                
                // Horizontal grid lines
                for (i in 0..(size.height / gridSpacing).toInt()) {
                    val y = i * gridSpacing
                    val alpha = if (kotlin.math.abs(y - scanLineY) < 60) 0.3f else 0.08f
                    drawLine(
                        color = Color(0xFF11D46E).copy(alpha = alpha),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
                
                // Vertical grid lines
                for (i in 0..(size.width / gridSpacing).toInt()) {
                    val x = i * gridSpacing
                    drawLine(
                        color = Color(0xFF11D46E).copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
                
                // Scan line
                drawLine(
                    color = Color(0xFF11D46E).copy(alpha = 0.6f),
                    start = Offset(0f, scanLineY),
                    end = Offset(size.width, scanLineY),
                    strokeWidth = 2f
                )
            }
            
            // Terminal logs in background
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .alpha(0.25f)
            ) {
                items(terminalLogs) { log ->
                    Text(
                        text = log,
                        color = Color(0xFF72FFA9),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
        
        // Top right UI elements - always visible
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signal bars
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((8 + index * 3).dp)
                            .background(
                                Color(0xFF11D46E).copy(alpha = if (index < 3) 1f else 0.4f)
                            )
                    )
                }
            }
            
            // Latency
            Text(
                text = "${Random.nextInt(12, 28)}ms",
                color = Color(0xFF72FFA9),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            
            // Encryption label
            Text(
                text = "AES-256",
                color = Color(0xFF11D46E),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color(0xFF11D46E).copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        // Main content with padding
        Box(modifier = Modifier.padding(20.dp).fillMaxSize()) {
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
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top spacer to push content down
            Spacer(Modifier.weight(0.3f))
            
            // Main content area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
            // Hide title and status when photo is shown
            if (imageBytes == null) {
                Text(
                    text = "MR. ROBOT // GHOSTWATCHER",
                    color = Color(0xFF72FFA9),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.alpha(shimmer)
                )
                Spacer(Modifier.height(18.dp))
                
                // Status with blinking cursor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = status,
                        color = Color(0xFFB6FFD2),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (!loading) {
                        Text(
                            text = "█",
                            color = Color(0xFF11D46E),
                            fontSize = 14.sp,
                            modifier = Modifier.alpha(if (cursorVisible) 1f else 0f)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Only show HACK button when no image is displayed and not loading
            if (imageBytes == null && !loading) {
                Button(
                onClick = {
                    scope.launch {
                        loading = true
                        
                        // Start API call immediately in parallel
                        var apiResult: Result<ByteArray>? = null
                        val apiJob = launch(Dispatchers.IO) {
                            apiResult = fetchGhostWatcherFrame()
                        }
                        
                        // Fast terminal-like sequence while API is loading
                        val messages = listOf(
                            "REMOTE HOST CONNECTED",
                            "DEVICE ID: A1:3F:8D:72",
                            "Searching peripherals...",
                            "[✓] microphone",
                            "[✓] network adapter",
                            "[✓] camera device detected",
                            ">>> Initializing SSH connection...",
                            ">>> SSH tunnel established [192.168.1.9:22]",
                            ">>> Bypassing firewall protocols...",
                            ">>> Installing malicious script [payload.sh]",
                            ">>> Executing remote code injection...",
                            ">>> Scanning network topology...",
                            ">>> TARGET ACQUIRED // Intercepting feed..."
                        )
                        
                        // Show messages rapidly (terminal-like)
                        launch {
                            playReelLikeBeat()
                            for (msg in messages) {
                                status = msg
                                delay(80) // Fast terminal output
                            }
                            // Keep showing last message until API completes
                            while (apiJob.isActive) {
                                delay(100)
                            }
                        }
                        
                        // Wait for API result
                        apiJob.join()
                        apiResult?.fold(
                            onSuccess = { bytes ->
                                imageBytes = bytes
                                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                                status = "REMOTE SNAPSHOT ACQUIRED\nTimestamp: $timestamp"
                            },
                            onFailure = { error ->
                                status = "✗ OPERATION FAILED // ${error.message}"
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

            when {
                loading -> {
                    Spacer(Modifier.height(22.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                imageBytes != null -> {
                    // Show timestamp just above photo
                    Text(
                        text = status,
                        color = Color(0xFF72FFA9),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.alpha(0.8f)
                    )
                    Spacer(Modifier.height(12.dp))
                    // Image with rounded corners
                    AsyncImage(
                        model = imageBytes,
                        contentDescription = "GhostWatcher feed image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
                else -> {
                    Spacer(Modifier.height(22.dp))
                }
            }
            }
            
            // Bottom spacer
            Spacer(Modifier.weight(0.3f))
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
