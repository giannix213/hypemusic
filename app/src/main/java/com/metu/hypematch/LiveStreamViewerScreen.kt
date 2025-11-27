package com.metu.hypematch

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment
import com.zegocloud.uikit.service.defines.ZegoUIKitUser

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
    val userId = authManager.getUserId() ?: "viewer_${System.currentTimeMillis()}"
    val username = authManager.getUsername() ?: "Viewer"
    
    DisposableEffect(Unit) {
        onDispose {
            onExit()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            // Configure as AUDIENCE using ZegoCloud UIKit Prebuilt
            val config = ZegoUIKitPrebuiltLiveStreamingConfig.audience()
            
            // Customize configuration
            config.apply {
                // Video configuration for portrait mode
                videoConfig.apply {
                    resolution = com.zegocloud.uikit.service.defines.ZegoVideoConfigPreset.PRESET_720P
                }
                
                // Bottom menu buttons for audience
                bottomMenuBarConfig.audienceButtons = listOf(
                    com.zegocloud.uikit.prebuilt.livestreaming.ZegoMenuBarButtonName.CHAT_BUTTON,
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
