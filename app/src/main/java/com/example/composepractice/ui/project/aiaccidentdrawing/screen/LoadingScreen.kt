package com.example.composepractice.ui.project.aiaccidentdrawing.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composepractice.ui.theme.ComposePracticeTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val MESSAGE_INTERVAL = 3.seconds

private val loadingMessages = listOf(
    "Uw no-claimkorting wordt gecondoleerd...",
    "Bumper wordt geïdentificeerd...",
    "Verzekeraar zoekt alvast een smoes...",
    "Lak wordt van het asfalt geschraapt...",
    "Schuldige wordt aangewezen. Hint: niet wij.",
    "Kreukelzone doet wat ze belooft...",
    "Uw premie maakt zich klaar voor lancering...",
    "Rouwkaart voor uw zijspiegel wordt opgesteld...",
    "De takelwagen ruikt geld...",
    "Garagehouder boekt alvast zijn vakantie...",
    "Schadevrije jaren worden begraven...",
    "De zijspiegel zag het niet aankomen...",
    "Airbagafdruk in uw gezicht wordt geanalyseerd...",
    "Total loss? We zeggen nog niks...",
)

@Composable
internal fun LoadingScreen() {
    val messages = remember { loadingMessages.shuffled() }
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(MESSAGE_INTERVAL)
            index = (index + 1) % messages.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            strokeWidth = 5.dp,
            color = ScreenForeground,
            trackColor = ScreenBorder,
        )
        Spacer(Modifier.height(32.dp))
        AnimatedContent(
            targetState = messages[index],
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
        ) { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = ScreenForeground,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun LoadingScreenPreview() {
    ComposePracticeTheme {
        LoadingScreen()
    }
}
