package com.example.composepractice.ui.project.christmastree

enum class OrnamentType {
    HOUSE, BICYCLE, HEART, CYBER_SHIELD, MITTENS, PAW, CAR, SUITCASE, SPORT_SHOE, STREET_LANTERN, STAR;
}

data class ChristmasTreeData(
    /** Map representing a position in the tree and [OrnamentType] */
    val ornaments: Map<Int, OrnamentType>,
)