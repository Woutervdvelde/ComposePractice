package com.example.composepractice.ui.util

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.composepractice.ui.ext.requireActivity

/**
 * Locks screen rotation to [orientation].
 * Releases the lock when this composable leaves the composition.
 */
@Composable
fun LockScreenOrientation(orientation: Orientation) {
    val context = LocalContext.current
    val activity = context.requireActivity()
    DisposableEffect(context) {
        when (orientation) {
            Orientation.PORTRAIT -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Orientation.LANDSCAPE -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

enum class Orientation {
    PORTRAIT, LANDSCAPE
}