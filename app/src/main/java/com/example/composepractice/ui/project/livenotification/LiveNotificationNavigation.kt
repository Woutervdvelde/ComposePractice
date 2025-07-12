package com.example.composepractice.ui.project.livenotification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import com.example.composepractice.navigation.EntryProviderInstaller
import com.example.composepractice.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

object LiveNotificationRoute

@Module
@InstallIn(ActivityRetainedComponent::class)
object LiveNotificationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {
        entry<LiveNotificationRoute> {
            Scaffold { contentPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                    Text("Live Notification")
                    Button(
                        onClick = { navigator.goBack() }
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}