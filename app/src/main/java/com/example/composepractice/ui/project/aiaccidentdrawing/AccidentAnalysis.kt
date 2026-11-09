package com.example.composepractice.ui.project.aiaccidentdrawing

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("status")
sealed interface AccidentAnalysis {
    val trafficSituation: TrafficSituation?
    val liability: Liability?
    val summary: String?

    @Serializable
    @SerialName("complete")
    data class Complete(
        @SerialName("traffic_situation") override val trafficSituation: TrafficSituation? = null,
        override val liability: Liability? = null,
        override val summary: String? = null,
    ) : AccidentAnalysis

    @Serializable
    @SerialName("needs_clarification")
    data class NeedsClarification(
        val clarification: Clarification,
        @SerialName("traffic_situation") override val trafficSituation: TrafficSituation? = null,
        override val liability: Liability? = null,
        override val summary: String? = null,
    ) : AccidentAnalysis
}

@Serializable
data class Clarification(
    val question: String,
    val options: List<ClarificationOption>,
)

@Serializable
data class ClarificationOption(
    val id: String,
    val label: String,
)

@Serializable
data class TrafficSituation(
    @SerialName("road_type") val roadType: RoadType? = null,
    @SerialName("priority_regulation") val priorityRegulation: PriorityRegulation? = null,
)

@Serializable
data class Liability(
    @SerialName("at_fault") val atFault: AtFault? = null,
    @SerialName("circumstance_code") val circumstanceCode: CircumstanceCode? = null,
)

@Serializable
enum class RoadType {
    STRAIGHT_ROAD,
    T_JUNCTION,
    CROSSROADS,
    ROUNDABOUT,
    HIGHWAY_MOTORWAY,
    HIGHWAY_ON_OFF_RAMP,
    PARKING_LOT,
    DRIVEWAY_PRIVATE_PROPERTY,
    BEND_CURVE,
}

@Serializable
enum class PriorityRegulation {
    PRIORITY_ROAD,
    GIVE_WAY_YIELD,
    STOP_SIGN,
    TRAFFIC_LIGHTS,
    PRIORITY_TO_RIGHT,
    ROUNDABOUT_RULES,
    UNKNOWN_UNREGULATED,
}

@Serializable
enum class AtFault {
    VEHICLE_A,
    VEHICLE_B,
    SHARED,
    UNDETERMINED,
}

@Serializable
enum class CircumstanceCode {
    REAR_END_COLLISION,
    FAILED_TO_YIELD_FROM_RIGHT,
    FAILED_TO_YIELD_AT_SIGN_OR_MARKING,
    TURNING_ACROSS_OPPOSING_TRAFFIC,
    IMPROPER_LANE_CHANGE,
    REVERSING,
    ENTERING_OR_LEAVING_PARKING,
    ENTERING_ROUNDABOUT,
    OVERTAKING_COLLISION,
    OPENING_CAR_DOOR,
    MERGING_HIGHWAY,
}

@OptIn(ExperimentalSerializationApi::class)
val accidentAnalysisJson = Json {
    // "clarification": null on a complete response is simply dropped
    ignoreUnknownKeys = true
    // An enum value the model made up becomes null instead of throwing
    coerceInputValues = true
    explicitNulls = false
}
