package com.metu.hypematch

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment

/**
 * Live Stream Viewer Screen - Audience
 * Uses ZegoCloud UIKit Prebuilt Live Streaming Kit
 */
@Composable
fun LiveStreamViewerScreen(
    sessionId: String,
    channelName: String,
    agoraToken: String,
    streamerName: String,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val userId = authManager.getUserId() ?: "viewer_${System.currentTimeMillis()}"
    
    // Obtener username de Firebase
    var username by remember { mutableStateOf("Viewer") }
    LaunchedEffect(userId) {
        try {
            val profile = firebaseManager.getUserProfile(userId)
            username = profile?.username ?: "Viewer"
        } catch (e: Exception) {
            android.util.Log.e("LiveStreamViewerScreen", "Error getting username: ${e.message}")
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            onExit()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            // Configure as AUDIENCE using ZegoCloud UIKit Prebuilt
            val config = ZegoUIKitPrebuiltLiveStreamingConfig.audience()
            
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
