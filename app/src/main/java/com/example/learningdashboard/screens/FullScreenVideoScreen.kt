package com.example.learningdashboard.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.learningdashboard.CustomVideoPlayer
// Import R and stringResource
import androidx.compose.ui.res.stringResource
import com.example.learningdashboard.R

/**
 * A dedicated screen for playing a video in fullscreen.
 * This screen automatically enters landscape and immersive mode.
 *
 * @param videoUrl The URL of the video to play.
 * @param onBack Callback to navigate back (exit fullscreen).
 */
@Composable
fun FullScreenVideoScreen(
    videoUrl: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    // This effect manages system UI and orientation
    DisposableEffect(Unit) {
// ... (rest of DisposableEffect is unchanged) ...
// ...
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val window = activity.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Store original orientation
        val originalOrientation = activity.requestedOrientation

        // --- 1. Set landscape orientation ---
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // --- 2. Enter immersive mode (hide system bars) ---
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // onDispose (when user leaves this screen)
        onDispose {
            // --- 1. Restore original orientation ---
            activity.requestedOrientation = originalOrientation

            // --- 2. Exit immersive mode (show system bars) ---
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // The UI remains the same
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Black background
    ) {
        // The video player fills the entire screen
        CustomVideoPlayer(
            videoUrl = videoUrl,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            onFullScreenClick = onBack // Player's button acts as 'Back'
        )

        // Back arrow
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.video_cd_back), // <-- Changed
                tint = Color.White // Ensure icon is visible
            )
        }
    }
}