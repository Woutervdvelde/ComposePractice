package com.example.composepractice.ui.project.aiaccidentdrawing.dataset

import androidx.compose.ui.geometry.Offset
import com.example.composepractice.ui.project.aiaccidentdrawing.AccidentAnalysis
import com.example.composepractice.ui.project.aiaccidentdrawing.AtFault
import com.example.composepractice.ui.project.aiaccidentdrawing.CircumstanceCode
import com.example.composepractice.ui.project.aiaccidentdrawing.Liability
import com.example.composepractice.ui.project.aiaccidentdrawing.PriorityRegulation
import com.example.composepractice.ui.project.aiaccidentdrawing.RoadType
import com.example.composepractice.ui.project.aiaccidentdrawing.TrafficSituation
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.DrawType
import com.example.composepractice.ui.project.aiaccidentdrawing.screen.DrawnStroke
import kotlin.random.Random

internal const val CANVAS_SIZE = 512f

internal data class SyntheticScenario(
    val strokes: List<DrawnStroke>,
    val analysis: AccidentAnalysis.Complete,
)

/**
 * Deterministically-labeled synthetic sketches, one per call, covering the 7 rules in
 * `system_instruction.txt`. The geometry is a simplified, schematic approximation (not
 * geometrically rigorous compass directions) - transforms are applied uniformly to every
 * stroke in a scenario, so relative correctness (and therefore the ground-truth label) is
 * preserved regardless of the random mirroring/orientation applied for visual diversity.
 */
internal fun randomScenario(random: Random): SyntheticScenario {
    val templates = listOf<(Random) -> SyntheticScenario>(
        ::rearEndScenario,
        ::turningVsStraightScenario,
        ::priorityToRightScenario,
        ::sharkTeethScenario,
        ::manoeuvreScenario,
        ::laneChangeScenario,
        ::mergingHighwayScenario,
        ::roundaboutScenario,
        ::overtakingScenario,
        ::openingDoorScenario,
    )
    val scenario = templates.random(random).invoke(random)
    return scenario.randomlyTransformed(random)
}

// region Geometry helpers

private fun pt(x: Float, y: Float) = Offset(x, y)

private fun Offset.jitter(random: Random, amount: Float = 8f): Offset =
    Offset(x + random.nextJitter(amount), y + random.nextJitter(amount))

private fun Random.nextJitter(amount: Float): Float = (nextFloat() * 2f - 1f) * amount

private fun road(random: Random, points: List<Offset>): DrawnStroke =
    DrawnStroke(DrawType.Road, points.map { it.jitter(random, 4f) })

private fun vehicle(random: Random, type: DrawType, points: List<Offset>): DrawnStroke =
    DrawnStroke(type, points.map { it.jitter(random, 4f) })

/** A small downward-pointing shark-tooth triangle, drawn as a single closed road-colored stroke. */
private fun sharkTooth(random: Random, centerX: Float, centerY: Float): DrawnStroke = road(
    random,
    listOf(
        pt(centerX - 14f, centerY - 10f),
        pt(centerX, centerY + 10f),
        pt(centerX + 14f, centerY - 10f),
    ),
)

private fun DrawType.toAtFault(): AtFault = when (this) {
    DrawType.A -> AtFault.VEHICLE_A
    DrawType.B -> AtFault.VEHICLE_B
    DrawType.Road -> error("Road is not a vehicle")
}

private fun DrawType.dutchLabel(): String = when (this) {
    DrawType.A -> "Voertuig A"
    DrawType.B -> "Voertuig B"
    DrawType.Road -> error("Road is not a vehicle")
}

private fun randomRoles(random: Random): Pair<DrawType, DrawType> =
    if (random.nextBoolean()) DrawType.A to DrawType.B else DrawType.B to DrawType.A

private fun summary(random: Random, atFault: DrawType, otherVehicle: DrawType, variants: List<String>): String {
    val template = variants.random(random)
    return template
        .replace("{fault}", atFault.dutchLabel())
        .replace("{other}", otherVehicle.dutchLabel())
}

/** Applies a random mirror/axis-swap to every stroke, preserving relative (and label) correctness. */
private fun SyntheticScenario.randomlyTransformed(random: Random): SyntheticScenario {
    val mirrorX = random.nextBoolean()
    val mirrorY = random.nextBoolean()
    val swapXY = random.nextBoolean()
    fun Offset.transformed(): Offset {
        var px = x
        var py = y
        if (swapXY) {
            val tmp = px
            px = py
            py = tmp
        }
        if (mirrorX) px = CANVAS_SIZE - px
        if (mirrorY) py = CANVAS_SIZE - py
        return Offset(px, py)
    }
    return copy(strokes = strokes.map { stroke -> stroke.copy(points = stroke.points.map { it.transformed() }) })
}

// endregion

// region Templates

private fun rearEndScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val strokes = listOf(
        road(random, listOf(pt(40f, 256f), pt(472f, 256f))),
        vehicle(random, priorityVehicle, listOf(pt(190f, 256f), pt(250f, 256f))),
        vehicle(random, atFaultVehicle, listOf(pt(430f, 256f), pt(262f, 256f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.STRAIGHT_ROAD, priorityRegulation = null),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.REAR_END_COLLISION),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} reed achterop {other} en hield onvoldoende afstand, waardoor de botsing ontstond.",
                "{fault} kon niet op tijd stoppen en botste achterop {other}.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun turningVsStraightScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val strokes = listOf(
        road(random, listOf(pt(40f, 256f), pt(472f, 256f))),
        road(random, listOf(pt(256f, 40f), pt(256f, 472f))),
        vehicle(random, priorityVehicle, listOf(pt(472f, 256f), pt(60f, 256f))),
        vehicle(random, atFaultVehicle, listOf(pt(40f, 256f), pt(256f, 256f), pt(256f, 460f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.CROSSROADS, priorityRegulation = null),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.TURNING_ACROSS_OPPOSING_TRAFFIC),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} sloeg af en kruiste het pad van {other}, dat rechtdoor reed en voorrang had.",
                "{fault} verleende geen voorrang aan het tegemoetkomende, rechtdoor rijdende {other} tijdens het afslaan.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun priorityToRightScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val strokes = listOf(
        road(random, listOf(pt(40f, 256f), pt(472f, 256f))),
        road(random, listOf(pt(256f, 40f), pt(256f, 472f))),
        vehicle(random, priorityVehicle, listOf(pt(256f, 40f), pt(256f, 230f))),
        vehicle(random, atFaultVehicle, listOf(pt(40f, 256f), pt(256f, 256f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.CROSSROADS, priorityRegulation = PriorityRegulation.PRIORITY_TO_RIGHT),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.FAILED_TO_YIELD_FROM_RIGHT),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "Op dit onbeveiligde kruispunt verleende {fault} geen voorrang van rechts aan {other}.",
                "{fault} negeerde de voorrangsregel van rechts en botste met {other}.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun sharkTeethScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val roadType = if (random.nextBoolean()) RoadType.CROSSROADS else RoadType.T_JUNCTION
    val strokes = listOf(
        road(random, listOf(pt(40f, 256f), pt(472f, 256f))),
        road(random, listOf(pt(256f, 40f), pt(256f, 472f))),
        sharkTooth(random, 200f, 256f),
        sharkTooth(random, 230f, 256f),
        vehicle(random, priorityVehicle, listOf(pt(256f, 40f), pt(256f, 230f))),
        vehicle(random, atFaultVehicle, listOf(pt(40f, 256f), pt(256f, 256f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = roadType, priorityRegulation = PriorityRegulation.GIVE_WAY_YIELD),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.FAILED_TO_YIELD_AT_SIGN_OR_MARKING),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} negeerde de haaientanden en verleende geen voorrang aan {other}.",
                "Ondanks de haaientandmarkering reed {fault} door zonder voorrang te verlenen aan {other}.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun manoeuvreScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val circumstance = if (random.nextBoolean()) {
        CircumstanceCode.REVERSING
    } else {
        CircumstanceCode.ENTERING_OR_LEAVING_PARKING
    }
    val strokes = listOf(
        road(random, listOf(pt(40f, 180f), pt(472f, 180f), pt(472f, 380f), pt(40f, 380f), pt(40f, 180f))),
        vehicle(random, priorityVehicle, listOf(pt(60f, 280f), pt(450f, 280f))),
        vehicle(random, atFaultVehicle, listOf(pt(300f, 180f), pt(300f, 280f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.PARKING_LOT, priorityRegulation = null),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = circumstance),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} voerde een manoeuvre uit op de parkeerplaats en verleende geen voorrang aan {other}.",
                "Tijdens het achteruitrijden/parkeren verleende {fault} geen voorrang aan het doorgaande {other}.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun laneChangeScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val roadType = if (random.nextBoolean()) RoadType.STRAIGHT_ROAD else RoadType.HIGHWAY_MOTORWAY
    val strokes = listOf(
        road(random, listOf(pt(40f, 220f), pt(472f, 220f))),
        road(random, listOf(pt(40f, 300f), pt(472f, 300f))),
        vehicle(random, priorityVehicle, listOf(pt(60f, 220f), pt(460f, 220f))),
        vehicle(random, atFaultVehicle, listOf(pt(60f, 300f), pt(300f, 300f), pt(340f, 220f), pt(460f, 220f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = roadType, priorityRegulation = null),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.IMPROPER_LANE_CHANGE),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} wisselde onverwacht van rijstrook zonder voorrang te verlenen aan {other}.",
                "Door een onveilige rijstrookwissel van {fault} ontstond de botsing met {other}.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun mergingHighwayScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val strokes = listOf(
        road(random, listOf(pt(40f, 256f), pt(472f, 256f))),
        road(random, listOf(pt(40f, 400f), pt(220f, 280f))),
        vehicle(random, priorityVehicle, listOf(pt(60f, 256f), pt(460f, 256f))),
        vehicle(random, atFaultVehicle, listOf(pt(60f, 400f), pt(230f, 270f), pt(400f, 256f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.HIGHWAY_ON_OFF_RAMP, priorityRegulation = null),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.MERGING_HIGHWAY),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} voegde in vanaf de oprit zonder voorrang te verlenen aan {other} op de hoofdrijbaan.",
                "Bij het invoegen verleende {fault} geen voorrang aan het doorgaande {other}.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun roundaboutScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val ringPoints = (0..8).map { i ->
        val angle = (i / 8.0) * 2 * Math.PI
        pt(256f + 150f * Math.cos(angle).toFloat(), 256f + 150f * Math.sin(angle).toFloat())
    }
    val strokes = listOf(
        road(random, ringPoints),
        vehicle(random, atFaultVehicle, listOf(pt(256f, 40f), pt(256f, 110f))),
        vehicle(random, priorityVehicle, listOf(pt(190f, 150f), pt(256f, 106f), pt(320f, 150f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.ROUNDABOUT, priorityRegulation = PriorityRegulation.ROUNDABOUT_RULES),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.ENTERING_ROUNDABOUT),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} reed de rotonde op zonder voorrang te verlenen aan {other}, dat al op de rotonde reed.",
                "{other} reed al op de rotonde toen {fault} invoegde zonder voorrang te verlenen.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun overtakingScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val strokes = listOf(
        road(random, listOf(pt(40f, 300f), pt(200f, 280f), pt(320f, 230f), pt(472f, 200f))),
        vehicle(random, priorityVehicle, listOf(pt(472f, 200f), pt(320f, 230f), pt(200f, 280f))),
        vehicle(random, atFaultVehicle, listOf(pt(60f, 310f), pt(200f, 260f), pt(330f, 210f), pt(460f, 190f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.BEND_CURVE, priorityRegulation = null),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.OVERTAKING_COLLISION),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} haalde in een bocht in en botste frontaal met het tegemoetkomende {other}.",
                "Door onverantwoord inhalen van {fault} ontstond een aanrijding met {other}.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

private fun openingDoorScenario(random: Random): SyntheticScenario {
    val (priorityVehicle, atFaultVehicle) = randomRoles(random)
    val strokes = listOf(
        road(random, listOf(pt(40f, 256f), pt(472f, 256f))),
        vehicle(random, atFaultVehicle, listOf(pt(260f, 220f), pt(260f, 180f))),
        vehicle(random, priorityVehicle, listOf(pt(60f, 256f), pt(230f, 256f), pt(260f, 210f), pt(460f, 200f))),
    )
    val analysis = AccidentAnalysis.Complete(
        trafficSituation = TrafficSituation(roadType = RoadType.STRAIGHT_ROAD, priorityRegulation = null),
        liability = Liability(atFault = atFaultVehicle.toAtFault(), circumstanceCode = CircumstanceCode.OPENING_CAR_DOOR),
        summary = summary(
            random, atFaultVehicle, priorityVehicle,
            listOf(
                "{fault} opende het portier zonder uit te kijken, waardoor {other} moest uitwijken en in botsing kwam.",
                "Door het onverwacht openen van het portier van {fault} moest {other} uitwijken en raakte betrokken bij een botsing.",
            ),
        ),
    )
    return SyntheticScenario(strokes, analysis)
}

// endregion
