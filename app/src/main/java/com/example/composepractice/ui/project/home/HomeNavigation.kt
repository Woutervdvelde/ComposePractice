package com.example.composepractice.ui.project.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composepractice.navigation.EntryProviderInstaller
import com.example.composepractice.navigation.NavRoute
import com.example.composepractice.navigation.Navigator
import com.example.composepractice.ui.util.ColorGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object HomeModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {
        entry<NavRoute.Home> {
            Scaffold { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        // Don't show home route itself
                        items(NavRoute.allRoutes(except = listOf(NavRoute.Home))) { route ->
                            NavRouteCard(
                                route = route,
                                onClick = {
                                    navigator.goTo(route)
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
private fun NavRouteCard(route: NavRoute, onClick: () -> Unit) {
    val background = ColorGenerator.generateBackgroundColor(route.title)
    val foreground = ColorGenerator.getForegroundColor(background)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            4.dp,
            Alignment.CenterVertically
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxSize()
            .background(background)
            .aspectRatio(1.5f)
            .clickable(onClick = onClick)
    ) {
        Text(route.emoji, fontSize = 24.sp)
        Text(route.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = foreground)
    }
}