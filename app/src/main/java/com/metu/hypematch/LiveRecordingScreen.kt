package com.metu.hypematch

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment
import com.zegocloud.uikit.service.defines.ZegoUIKitUser

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
    val userId = authManager.getUserId() ?: "user_${System.currentTimeMillis()}"
    val username = authManager.getUsername() ?: "User"
    
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
            config.apply {
                // Enable camera and microphone by default
                turnOnCameraWhenJoining = true
                turnOnMicrophoneWhenJoining = true
                
                // Video configuration for portrait mode
                videoConfig.apply {
                    resolution = com.zegocloud.uikit.service.defines.ZegoVideoConfigPreset.PRESET_720P
                }
                
                // Bottom menu buttons for host
                bottomMenuBarConfig.hostButtons = listOf(
                    com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
                    com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                    com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON,
                    com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.LEAVE_BUTTON
                )
            }
            
            // Create ZegoCloud UIKit Prebuilt fragment
            val fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                ZegoConfig.APP_ID,
                ZegoConfig.APP_SIGN,
                ZegoUIKitUser(userId, username),
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
