package com.example.composepractice.ui.project.zipper

import androidx.navigation3.runtime.entry
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
object ZipperNavigation {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {
        entry<NavRoute.Zipper> {
            ZipperScreen()
        }
    }
}