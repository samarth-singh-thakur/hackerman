package com.hackerman.ghostwatcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.hackerman.ghostwatcher.ui.GhostWatcherApp
import com.hackerman.ghostwatcher.ui.theme.GhostWatcherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GhostWatcherTheme {
                Surface {
                    GhostWatcherApp()
                }
            }
        }
    }
}
