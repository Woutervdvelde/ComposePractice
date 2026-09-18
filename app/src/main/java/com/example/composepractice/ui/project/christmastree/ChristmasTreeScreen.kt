package com.example.composepractice.ui.project.christmastree

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal val dummyTree = ChristmasTreeData(
    ornaments = mapOf(
        0 to OrnamentType.CYBER_SHIELD,
        1 to OrnamentType.SUITCASE,
        4 to OrnamentType.BICYCLE,
        9 to OrnamentType.HOUSE
    )
)

@Composable
fun ChristmasTreeScreen() {
    Scaffold { scaffoldPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ChristmasTree(tree = dummyTree, modifier = Modifier)
        }
    }
}