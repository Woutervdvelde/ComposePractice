package com.example.composepractice.di

import com.example.composepractice.navigation.Navigator
import com.example.composepractice.ui.project.home.HomeRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {

    @Provides
    @ActivityRetainedScoped
    fun provideNavigator(): Navigator =  Navigator(startDestination = HomeRoute)
}