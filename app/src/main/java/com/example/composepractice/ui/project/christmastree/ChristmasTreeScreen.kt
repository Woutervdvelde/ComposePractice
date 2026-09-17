package com.example.composepractice.ui.project.christmastree

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal val dummyTree = ChristmasTreeData(
    ornaments = mapOf(
        0 to OrnamentType.CYBER_SHIELD,
        1 to OrnamentType.SUITCASE,
        4 to OrnamentType.BICYCLE,
        10 to OrnamentType.HOUSE
    )
)

@Composable
fun ChristmasTreeScreen() {
    Scaffold { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            ChristmasTree(tree = dummyTree, modifier = Modifier)
        }
    }
}