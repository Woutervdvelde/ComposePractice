package com.example.composepractice.ui.project.scratchticketv2

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
object ScratchTicketV2Navigation : BaseNavigation {

    @IntoSet
    @Provides
    override fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {
        entry<NavRoute.ScratchTicketV2> {
            ScratchTicketV2()
        }
    }
}

interface BaseNavigation {
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller
}