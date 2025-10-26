package com.example.composepractice.navigation

import androidx.navigation3.runtime.NavKey

sealed class NavRoute(
    val title: String,
    val emoji: String = emojis.random(),
) : NavKey {
    data object Home : NavRoute(
        title = "Home",
    )

    data object Counter : NavRoute(
        title = "Counter",
        emoji = "\uD83E\uDDEE"
    )

    data object LiveNotification : NavRoute(
        title = "Live notification",
        emoji = "\uD83D\uDCAC"
    )

    data object Rive : NavRoute(
        title = "Rive test",
        emoji = "\uD83E\uDE84"
    )

    data object ScratchTicket : NavRoute(
        title = "Scratch ticket",
        emoji = "\uD83C\uDFAB"
    )

    data object ScratchTicketV2 : NavRoute(
        title = "Scratch ticket v2",
        emoji = "\uD83C\uDFAB"
    )

    data object Lock : NavRoute(
        title = "Lock",
        emoji = "\uD83D\uDD12"
    )

    data object GiantResponse : NavRoute(
        title = "Giant Response",
    )

    data object Zipper : NavRoute(
        title = "Zipper - Hackathon Animation 2025",
        emoji = "\uD83E\uDD10"
    )

    companion object {
        fun allRoutes(except: List<NavRoute>): List<NavRoute> =
            NavRoute::class.sealedSubclasses.mapNotNull { kClass ->
                return@mapNotNull if (except.any { it::class == kClass })
                    null
                else kClass.objectInstance
            }
    }
}


// Some random emojis for when non is supplied
private val emojis = listOf(
    "\uD83D\uDE80",
    "\uD83D\uDE81",
    "\uD83D\uDE82",
    "\uD83D\uDE83",
    "\uD83D\uDE84",
    "\uD83D\uDE85",
    "\uD83D\uDE86",
    "\uD83D\uDE87",
    "\uD83D\uDE88",
    "\uD83D\uDE89",
    "\uD83D\uDE8A",
    "\uD83D\uDE8B",
    "\uD83D\uDE8C",
)