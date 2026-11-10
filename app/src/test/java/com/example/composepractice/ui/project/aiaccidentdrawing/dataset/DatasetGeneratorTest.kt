package com.example.composepractice.ui.project.aiaccidentdrawing.dataset

import android.graphics.Bitmap
import com.example.composepractice.ui.project.aiaccidentdrawing.AccidentAnalysis
import com.example.composepractice.ui.project.aiaccidentdrawing.accidentAnalysisJson
import kotlinx.serialization.encodeToString
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

/**
 * Not a correctness test - this is the Phase A dataset generator from the LoRA fine-tuning plan.
 * Run with: ./gradlew :app:testDebugUnitTest --tests "*.DatasetGeneratorTest"
 * Override count/output dir with -Ddataset.count=2000 -Ddataset.outputDir=/abs/path/dataset
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class DatasetGeneratorTest {

    @Test
    fun generateDataset() {
        val count = System.getProperty("dataset.count")?.toIntOrNull() ?: 1500
        val outputDir = File(System.getProperty("dataset.outputDir") ?: "../dataset")
        val imagesDir = File(outputDir, "images").apply { mkdirs() }
        val metadataFile = File(outputDir, "metadata.jsonl")

        val random = Random(42)
        metadataFile.bufferedWriter().use { writer ->
            repeat(count) { index ->
                val scenario = randomScenario(random)
                val fileName = "%05d.png".format(index)

                val bitmap = renderSketch(scenario.strokes)
                FileOutputStream(File(imagesDir, fileName)).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val responseJson = accidentAnalysisJson.encodeToString<AccidentAnalysis>(scenario.analysis)
                val metadataLine = accidentAnalysisJson.encodeToString(
                    MetadataRow(fileName = "images/$fileName", response = responseJson),
                )
                writer.write(metadataLine)
                writer.newLine()
            }
        }

        println("Wrote $count examples to ${outputDir.absolutePath}")
    }
}

@kotlinx.serialization.Serializable
private data class MetadataRow(
    @kotlinx.serialization.SerialName("file_name") val fileName: String,
    val response: String,
)
