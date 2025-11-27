package com.metu.hypematch

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment

/**
 * Live Streaming Screen - Host/Streamer
 * Uses ZegoCloud UIKit Prebuilt Live Streaming Kit
 */
@Composable
fun LiveRecordingScreen(
    sessionId: String,
    channelName: String,
    agoraToken: String,
    onStreamStarted: () -> Unit,
    onStreamEnded: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val userId = authManager.getUserId() ?: "user_${System.currentTimeMillis()}"
    
    // Obtener username de Firebase
    var username by remember { mutableStateOf("User") }
    LaunchedEffect(userId) {
        try {
            val profile = firebaseManager.getUserProfile(userId)
            username = profile?.username ?: "User"
        } catch (e: Exception) {
            android.util.Log.e("LiveRecordingScreen", "Error getting username: ${e.message}")
        }
    }
    
    LaunchedEffect(Unit) {
        onStreamStarted()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            onStreamEnded()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            // Configure as HOST using ZegoCloud UIKit Prebuilt
            val config = ZegoUIKitPrebuiltLiveStreamingConfig.host()
            
            // Customize configuration
            config.turnOnCameraWhenJoining = true
            config.turnOnMicrophoneWhenJoining = true
            
            // Create ZegoCloud UIKit Prebuilt fragment
            val fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                ZegoConfig.APP_ID,
                ZegoConfig.APP_SIGN,
                userId,
                username,
                channelName,
                config
            )
            
            // Create container for fragment
            val fragmentContainer = android.widget.FrameLayout(ctx).apply {
                id = android.view.View.generateViewId()
            }
            
            // Add fragment to container
            if (ctx is androidx.fragment.app.FragmentActivity) {
                ctx.supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, fragment)
                    .commit()
            }
            
            fragmentContainer
        }
    )
}
