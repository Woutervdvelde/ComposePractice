package com.example.composepractice.ui.project.gradient

import com.example.composepractice.navigation.EntryProviderInstaller
import com.example.composepractice.navigation.NavRoute
import com.example.composepractice.navigation.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object GradientAnimationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {
        entry<NavRoute.GradientAnimation> {
            GradientAnimation()
        }
    }
}