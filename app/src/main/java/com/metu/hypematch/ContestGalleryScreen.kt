package com.metu.hypematch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ContestGalleryScreen(
    contest: Contest,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    
    var selectedTab by remember { mutableStateOf(0) }
    var myVideos by remember { mutableStateOf<List<ContestEntry>>(emptyList()) }
    var allVideos by remember { mutableStateOf<List<ContestEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showTikTokPlayer by remember { mutableStateOf(false) }
    var selectedVideoIndex by remember { mutableStateOf(0) }
    
    val userId = authManager.getUserId() ?: ""
    
    LaunchedEffect(contest.name) {
        isLoading = true
        try {
            val entries = firebaseManager.getAllContestEntries()
            val contestEntries = entries.filter { it.contestId == contest.name }
            allVideos = contestEntries
            myVideos = contestEntries.filter { it.userId == userId }
        } catch (e: Exception) {
            android.util.Log.e("ContestGallery", "Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    if (showTikTokPlayer) {
        val videosToPlay = if (selectedTab == 0) myVideos else allVideos
        TikTokStyleVideoPlayer(
            videos = videosToPlay,
            initialIndex = selectedVideoIndex,
            onBack = { showTikTokPlayer = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PopArtColors.Black)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Volver", tint = PopArtColors.Yellow)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Galería de Videos", fontSize = 20.sp, fontWeight = FontWeight.Black, color = PopArtColors.Yellow)
                    Text(contest.name, fontSize = 14.sp, color = PopArtColors.White.copy(alpha = 0.7f))
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Mis Videos (${myVideos.size})", fontSize = 14.sp, fontWeight = FontWeight.Black,
                        color = if (selectedTab == 0) PopArtColors.Black else PopArtColors.White)
                }
                
                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Todos (${allVideos.size})", fontSize = 14.sp, fontWeight = FontWeight.Black,
                        color = if (selectedTab == 1) PopArtColors.Black else PopArtColors.White)
                }
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PopArtColors.Yellow)
                }
            } else {
                val videosToShow = if (selectedTab == 0) myVideos else allVideos
                
                if (videosToShow.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎬", fontSize = 64.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (selectedTab == 0) "No has subido videos aún" else "No hay videos en este concurso",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PopArtColors.White, textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(videosToShow.size) { index ->
                            VideoEntryCard(
                                entry = videosToShow[index],
                                isMyVideo = videosToShow[index].userId == userId,
                                onClick = {
                                    selectedVideoIndex = index
                                    showTikTokPlayer = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoEntryCard(entry: ContestEntry, isMyVideo: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = if (isMyVideo) PopArtColors.Yellow else PopArtColors.White),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(80.dp).background(PopArtColors.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Play", tint = PopArtColors.White, modifier = Modifier.size(32.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(entry.username, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PopArtColors.Black)
                    if (isMyVideo) {
                        Box(modifier = Modifier.background(PopArtColors.Pink, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("TÚ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
                Text(entry.title, fontSize = 14.sp, color = PopArtColors.Black.copy(alpha = 0.7f), maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, "Likes", tint = PopArtColors.Pink, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(entry.likes.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PopArtColors.Black)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, "Views", tint = PopArtColors.Cyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(entry.views.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PopArtColors.Black)
                    }
                }
            }
        }
    }
}
