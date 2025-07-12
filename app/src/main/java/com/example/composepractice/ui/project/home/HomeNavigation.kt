package com.example.composepractice.ui.project.home

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
import com.example.composepractice.ui.project.livenotification.LiveNotificationRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

object HomeRoute

@Module
@InstallIn(ActivityRetainedComponent::class)
object HomeModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {
        entry<HomeRoute> {
            Scaffold { contentPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                    Text("Home")
                    Button(
                        onClick = { navigator.goTo(LiveNotificationRoute) }
                    ) {
                        Text("Go to Live Notification")
                    }
                }
            }
        }
    }
}