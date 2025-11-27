package com.metu.hypematch

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment

/**
 * Live Recording Screen - Host/Streamer View
 * Uses ZegoCloud UIKit for Live Streaming
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveRecordingScreen(
    sessionId: String,
    channelName: String,
    agoraToken: String,
    onStreamStarted: () -> Unit,
    onStreamEnded: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Get current user info from Firebase
    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val userId = currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
    val userName = currentUser?.displayName ?: "Guest User"
    
    DisposableEffect(Unit) {
        onStreamStarted()
        onDispose {
            // Cleanup when leaving
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Going Live", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onStreamEnded) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (activity != null) {
                AndroidView(
                    factory = { ctx ->
                        // Configure as HOST
                        val config = ZegoUIKitPrebuiltLiveStreamingConfig.host()
                        
                        // Customize config
                        config.audioVideoViewConfig.showSoundWavesInAudioMode = true
                        config.bottomMenuBarConfig.hostButtons = listOf(
                            com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
                            com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                            com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.SWITCH_CAMERA_FACING_BUTTON
                        )
                        
                        // Create fragment
                        val fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                            ZegoConfig.APP_ID,
                            ZegoConfig.APP_SIGN,
                            userId,
                            userName,
                            channelName, // Use channelName as liveID
                            config
                        )
                        
                        // Add fragment to container
                        val fragmentManager = activity.supportFragmentManager
                        val transaction = fragmentManager.beginTransaction()
                        val container = android.widget.FrameLayout(ctx).apply {
                            id = android.view.View.generateViewId()
                        }
                        transaction.add(container.id, fragment)
                        transaction.commitAllowingStateLoss()
                        
                        container
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback UI
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "⚠️ Unable to start live stream",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(onClick = onStreamEnded) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}
