package com.example.composepractice.ui.project.scratchticket

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachReversed
import com.example.composepractice.R
import com.example.composepractice.ui.theme.FramnaGreen
import kotlin.math.hypot

@Composable
fun ScratchTicketScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Scratch Ticket")

        val scratchState = rememberScratchState(scratchStrokeWidth = with(LocalDensity.current) { 20.dp.toPx() })
        ScratchTicketContainer(
            modifier = Modifier.width(400.dp).height(250.dp),
            scratchState = scratchState,
            scratchContent = {
                Box(Modifier.fillMaxSize().background(FramnaGreen), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(R.drawable.framna), contentDescription = null)
                }
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().background(Color.Yellow)
            ) {
                Text("\uD83C\uDFC6", fontSize = 100.sp) // Trophy emoji
            }
        }

        Button(
            onClick = {}
        ) {
            Text("Reset ticket")
        }
    }
}

@Composable
fun ScratchTicketContainer(
    modifier: Modifier = Modifier,
    scratchState: ScratchState = rememberScratchState(),
    scratchContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectDragGestures(
                        onDrag = scratchState.onDrag,
                        onDragEnd = scratchState.onDragEnd
                    )
                }
                .then(scratchState.drawModifier)
        ) {
            scratchContent()
        }
    }
}

@Composable
fun rememberScratchState(
    scratchStrokeWidth: Float = 50f
): ScratchState {
    return remember {
        ScratchState(scratchStrokeWidth)
    }
}

class ScratchState(
    private val scratchStrokeWidth: Float
) {
    private var paths: Path = Path()
    private val currentPath: MutableList<Offset> = mutableListOf()
    private val _invalidateTrigger = mutableIntStateOf(0)

    internal val onDrag: (PointerInputChange, Offset) -> Unit = { change, dragAmount ->
        change.consume()
        currentPath.add(change.position + dragAmount)

        _invalidateTrigger.intValue++
    }

    internal val onDragEnd: () -> Unit = {
        if (currentPath.isNotEmpty()) {
            paths = Path.combine(
                operation = PathOperation.Union,
                path1 = paths,
                path2 = strokedOutline(currentPath, scratchStrokeWidth)
            )
        }
        currentPath.clear()

        _invalidateTrigger.intValue++
    }

    internal val drawModifier: Modifier
        get() = Modifier.drawWithCache {
            // Read value to trigger recomposition
            val trigger = _invalidateTrigger.intValue

            val current = strokedOutline(currentPath, scratchStrokeWidth)
            val mergedPaths = Path.combine(
                operation = PathOperation.Union,
                path1 = paths,
                path2 = current
            )

            onDrawWithContent {
                clipPath(
                    path = mergedPaths,
                    clipOp = ClipOp.Difference
                ) {
                    this@onDrawWithContent.drawContent()
                }
            }
        }

    private fun normalize(offset: Offset): Offset {
        val length = hypot(offset.x, offset.y)
        return if (length == 0f) Offset.Zero else Offset(offset.x / length, offset.y / length)
    }

    private fun strokedOutline(offsets: List<Offset>, strokeWidth: Float): Path {
        val path = Path()
        if (offsets.size < 2) return path

        val left: MutableList<Offset> = mutableListOf()
        val right: MutableList<Offset> = mutableListOf()

        for (i in 1 until offsets.size) {
            val prev = offsets.getOrNull(i - 1) ?: continue
            val current = offsets[i]
            val next = offsets.getOrNull(i + 1) ?: continue

            // Directions
            val dirIn = normalize(current - prev)
            val dirOut = normalize(next - current)

            // Normals
            val normIn = Offset(-dirIn.y, dirIn.x)
            val normOut = Offset(-dirOut.y, dirOut.x)
            val avgNormal = normalize(Offset(normIn.x + normOut.x, normIn.y + normOut.y))

            val offset = Offset(avgNormal.x * strokeWidth / 2f, avgNormal.y * strokeWidth / 2f)
            left.add(Offset(current.x + offset.x, current.y + offset.y))
            right.add(Offset(current.x - offset.x, current.y - offset.y))
        }

        if (left.size + right.size < 2) return path

        path.moveTo(left.first().x, left.first().y)
        left.drop(1).fastForEach {
            path.lineTo(it.x, it.y)
        }
        right.fastForEachReversed {
            path.lineTo(it.x, it.y)
        }
        path.close()

        return path
    }
}

