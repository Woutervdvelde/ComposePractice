package com.example.composepractice.ui.project.aiaccidentdrawing.screen

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composepractice.ui.project.aiaccidentdrawing.AccidentAnalysis
import com.example.composepractice.ui.project.aiaccidentdrawing.AtFault
import com.example.composepractice.ui.project.aiaccidentdrawing.ClarificationOption

private val previewShape = RoundedCornerShape(20.dp)

@Composable
internal fun ResultScreen(
    sketch: ImageBitmap,
    analysis: AccidentAnalysis,
    onAnswer: (ClarificationOption) -> Unit,
    onRedraw: () -> Unit,
) {
    ResultScaffold(title = "Analysis", sketch = sketch, onRedraw = onRedraw) {
        if (analysis is AccidentAnalysis.NeedsClarification) {
            InfoCard(
                title = analysis.clarification.question,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                analysis.clarification.options.forEach { option ->
                    OutlinedButton(
                        onClick = { onAnswer(option) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(option.label)
                    }
                }
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
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun ResultScaffold(
    title: String,
    sketch: ImageBitmap,
    onRedraw: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
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
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, previewShape),
        )

        content()

        OutlinedButton(
            onClick = onRedraw,
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
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
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
        AtFault.SHARED -> "Shared fault" to MaterialTheme.colorScheme.tertiary
        AtFault.UNDETERMINED -> "Undetermined" to MaterialTheme.colorScheme.outline
    }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
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
