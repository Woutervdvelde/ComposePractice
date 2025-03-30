package com.example.composepractice.ui.project.livenotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.getSystemService
import com.example.composepractice.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Composable
fun LiveNotification() {
    val scope = rememberCoroutineScope()
    val liveNotificationBuilder = LiveNotificationBuilder(LocalContext.current)
    val alignmentState by remember { mutableStateOf(AlignmentState(alignmentSubjects.first(), 0)) }

    Scaffold { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(contentPadding)
        ) {
            Button(onClick = { scope.launch { startProgressTracking(liveNotificationBuilder, alignmentState) } }) {
                Text("Update notification")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
private suspend fun startProgressTracking(
    liveNotificationBuilder: LiveNotificationBuilder,
    initialState: AlignmentState
) {
    var state = initialState
    while (true) {
        val newProgress = state.progress + 10
        val isCompleted = newProgress > state.subject.duration

        if (isCompleted) {
            val nextSubject = alignmentSubjects.getOrNull(alignmentSubjects.indexOf(state.subject) + 1)
            if (nextSubject == null) break
            state = AlignmentState(nextSubject, 0)
        } else {
            state = state.copy(progress = newProgress)
        }
        liveNotificationBuilder.show(state)
        delay(1.seconds)
    }
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
class LiveNotificationBuilder(private val context: Context) {
    fun show(alignmentState: AlignmentState) {
        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        createNotificationChannel(notificationManager)
        notificationManager.notify(NOTIFICATION_ID, buildNotification(alignmentState))
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Alignment Live Notifications", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Displays progress of alignment subjects" }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(state: AlignmentState): Notification {
        val totalProgress = alignmentSubjects.sumOf { subject ->
            if (alignmentSubjects.indexOf(subject) < alignmentSubjects.indexOf(state.subject)) subject.duration else 0
        } + state.progress

        val style = Notification.ProgressStyle()
            .setStyledByProgress(true)
            .setProgress(totalProgress)
            .setProgressSegments(alignmentSubjects.map { subject ->
                Notification.ProgressStyle.Segment(subject.duration).setColor(subject.color.toArgb())
            })
//            .setProgressPoints(
//                listOf(
//                    Notification.ProgressStyle.Point(60).setColor(Color(255, 187, 0).toArgb()),
//                    Notification.ProgressStyle.Point(150).setColor(Color(255, 187, 0).toArgb())
//                )
//            )
            .setProgressTrackerIcon(Icon.createWithResource(context, R.drawable.framna))

        return Notification.Builder(context, CHANNEL_ID)
            .setContentTitle(state.subject.name)
            .setContentText(state.subject.description)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(style)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "live_notification"
        private const val NOTIFICATION_ID = 1
    }
}

data class AlignmentSubject(val name: String, val description: String, val duration: Int, val color: Color)

data class AlignmentState(val subject: AlignmentSubject, val progress: Int)

val alignmentSubjects = listOf(
    AlignmentSubject("Intro", "Intro to Android Alignment", 50, Color(194, 0, 0)),
    AlignmentSubject("Live Notification", "Showcase of Android 16 Live Notification", 200, Color(232, 96, 0)),
    AlignmentSubject("Standup", "Standup time", 100, Color(255, 187, 0)),
    AlignmentSubject("Outro", "Outro to Android Alignment, goodnight", 20, Color(0, 147, 65))
)

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Preview
@Composable
private fun LiveNotificationPreview() {
    LiveNotification()
}

