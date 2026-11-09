package com.example.composepractice.ui.project.aiaccidentdrawing.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composepractice.ui.project.aiaccidentdrawing.AccidentAnalysis
import com.example.composepractice.ui.project.aiaccidentdrawing.AtFault
import com.example.composepractice.ui.project.aiaccidentdrawing.Clarification
import com.example.composepractice.ui.project.aiaccidentdrawing.ClarificationOption
import com.example.composepractice.ui.project.aiaccidentdrawing.CircumstanceCode
import com.example.composepractice.ui.project.aiaccidentdrawing.GenerationStats
import com.example.composepractice.ui.project.aiaccidentdrawing.Liability
import com.example.composepractice.ui.project.aiaccidentdrawing.PriorityRegulation
import com.example.composepractice.ui.project.aiaccidentdrawing.RoadType
import com.example.composepractice.ui.project.aiaccidentdrawing.TrafficSituation
import com.example.composepractice.ui.theme.ComposePracticeTheme

private val previewShape = RoundedCornerShape(20.dp)

@Composable
internal fun ResultScreen(
    sketch: ImageBitmap,
    analysis: AccidentAnalysis,
    stats: GenerationStats,
    onAnswer: (ClarificationOption) -> Unit,
    onCustomAnswer: (String) -> Unit,
    onRedraw: () -> Unit,
) {
    ResultScaffold(title = "Analysis", sketch = sketch, stats = stats, onRedraw = onRedraw) {
        if (analysis is AccidentAnalysis.NeedsClarification) {
            InfoCard(
                title = analysis.clarification.question,
                emphasized = true,
            ) {
                analysis.clarification.options.forEach { option ->
                    OutlinedButton(
                        onClick = { onAnswer(option) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenForeground),
                        border = BorderStroke(1.dp, ScreenBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(option.label)
                    }
                }
                ClarificationOtherAnswer(onSubmit = onCustomAnswer)
            }
        }

        analysis.liability?.let { liability ->
            InfoCard(title = "Liability") {
                liability.atFault?.let { FaultBadge(it) }
                liability.circumstanceCode?.let { InfoRow("Circumstance", it.text()) }
            }
        }

        analysis.summary?.let { summary ->
            InfoCard(title = "Summary") {
                Text(summary, style = MaterialTheme.typography.bodyLarge)
            }
        }

        analysis.trafficSituation?.let { situation ->
            InfoCard(title = "Traffic situation") {
                situation.roadType?.let { InfoRow("Road type", it.text()) }
                situation.priorityRegulation?.let { InfoRow("Priority", it.text()) }
            }
        }
    }
}

@Composable
internal fun ResultErrorScreen(
    sketch: ImageBitmap,
    message: String,
    onRedraw: () -> Unit,
) {
    ResultScaffold(title = "Couldn't read the analysis", sketch = sketch, onRedraw = onRedraw) {
        InfoCard(
            title = "Details",
            emphasized = true,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = ScreenForeground,
            )
        }
    }
}

@Composable
private fun ResultScaffold(
    title: String,
    sketch: ImageBitmap,
    stats: GenerationStats? = null,
    onRedraw: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = ScreenForeground,
        )

        Image(
            bitmap = sketch,
            contentDescription = "Your sketch",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(sketch.width.toFloat() / sketch.height)
                .clip(previewShape)
                .background(Color.White)
                .border(1.dp, ScreenBorder, previewShape),
        )

        stats?.let {
            Text(
                text = it.summaryText(),
                style = MaterialTheme.typography.labelSmall,
                color = ScreenForegroundMuted,
            )
        }

        content()

        OutlinedButton(
            onClick = onRedraw,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenForeground),
            border = BorderStroke(1.dp, ScreenBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Back to drawing")
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    emphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenBackground, contentColor = ScreenForeground),
        border = BorderStroke(if (emphasized) 2.dp else 1.dp, if (emphasized) ScreenForeground else ScreenBorder),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun ClarificationOtherAnswer(onSubmit: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    if (!expanded) {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenForeground),
            border = BorderStroke(1.dp, ScreenBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Anders")
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Beschrijf de situatie...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ScreenForeground,
                unfocusedTextColor = ScreenForeground,
                focusedBorderColor = ScreenForeground,
                unfocusedBorderColor = ScreenBorder,
                cursorColor = ScreenForeground,
                focusedPlaceholderColor = ScreenForegroundMuted,
                unfocusedPlaceholderColor = ScreenForegroundMuted,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    expanded = false
                    text = ""
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenForegroundMuted),
                border = BorderStroke(1.dp, ScreenBorder),
                modifier = Modifier.weight(1f),
            ) {
                Text("Annuleren")
            }
            Button(
                onClick = {
                    val answer = text.trim()
                    if (answer.isNotEmpty()) {
                        onSubmit(answer)
                        expanded = false
                        text = ""
                    }
                },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ScreenForeground,
                    contentColor = ScreenBackground,
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text("Versturen")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ScreenForegroundMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ScreenForeground,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FaultBadge(atFault: AtFault) {
    val (label, color) = when (atFault) {
        AtFault.VEHICLE_A -> "${DrawType.A.label} at fault" to DrawType.A.color
        AtFault.VEHICLE_B -> "${DrawType.B.label} at fault" to DrawType.B.color
        AtFault.SHARED -> "Shared fault" to ScreenForeground
        AtFault.UNDETERMINED -> "Undetermined" to ScreenForegroundMuted
    }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ColorDot(color)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

private fun Enum<*>.text(): String =
    name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun GenerationStats.summaryText(): String =
    "$tokenCount tokens · %.1f tok/s · %.1fs".format(tokensPerSecond, durationSeconds)

private fun samplePreviewSketch(): ImageBitmap {
    val width = 320
    val height = 200
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = android.graphics.Paint().apply {
        strokeWidth = 8f
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        isAntiAlias = true
    }
    paint.color = android.graphics.Color.BLACK
    canvas.drawLine(20f, height - 20f, width - 20f, height - 20f, paint)
    paint.color = android.graphics.Color.RED
    canvas.drawLine(40f, height - 30f, width / 2f, 40f, paint)
    paint.color = android.graphics.Color.BLUE
    canvas.drawLine((width - 40).toFloat(), height - 30f, width / 2f, 40f, paint)
    return bitmap.asImageBitmap()
}

@Preview
@Composable
private fun ResultScreenCompletePreview() {
    ComposePracticeTheme {
        ResultScreen(
            sketch = samplePreviewSketch(),
            analysis = AccidentAnalysis.Complete(
                trafficSituation = TrafficSituation(
                    roadType = RoadType.CROSSROADS,
                    priorityRegulation = PriorityRegulation.PRIORITY_TO_RIGHT,
                ),
                liability = Liability(
                    atFault = AtFault.VEHICLE_B,
                    circumstanceCode = CircumstanceCode.FAILED_TO_YIELD_FROM_RIGHT,
                ),
                summary = "Voertuig B reed het kruispunt op zonder voorrang te verlenen aan voertuig A dat van rechts kwam.",
            ),
            stats = GenerationStats(tokenCount = 184, tokensPerSecond = 27.3, durationSeconds = 6.7),
            onAnswer = {},
            onCustomAnswer = {},
            onRedraw = {},
        )
    }
}

@Preview
@Composable
private fun ResultScreenClarificationPreview() {
    ComposePracticeTheme {
        ResultScreen(
            sketch = samplePreviewSketch(),
            analysis = AccidentAnalysis.NeedsClarification(
                clarification = Clarification(
                    question = "Wie had er voorrang op het kruispunt?",
                    options = listOf(
                        ClarificationOption(id = "opt_1", label = "Persoon A"),
                        ClarificationOption(id = "opt_2", label = "Persoon B"),
                    ),
                ),
            ),
            stats = GenerationStats(tokenCount = 96, tokensPerSecond = 31.8, durationSeconds = 3.2),
            onAnswer = {},
            onCustomAnswer = {},
            onRedraw = {},
        )
    }
}

@Preview
@Composable
private fun ResultErrorScreenPreview() {
    ComposePracticeTheme {
        ResultErrorScreen(
            sketch = samplePreviewSketch(),
            message = "Unexpected character at offset 42: expected '}' but was EOF",
            onRedraw = {},
        )
    }
}
