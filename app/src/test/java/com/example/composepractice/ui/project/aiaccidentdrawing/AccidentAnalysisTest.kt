package com.example.composepractice.ui.project.aiaccidentdrawing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccidentAnalysisTest {

    @Test
    fun `decodes complete response`() {
        val json = """
            {
              "status": "complete",
              "clarification": null,
              "traffic_situation": {"road_type": "CROSSROADS", "priority_regulation": "PRIORITY_TO_RIGHT"},
              "liability": {"at_fault": "VEHICLE_B", "circumstance_code": "FAILED_TO_YIELD_FROM_RIGHT"},
              "summary": "Voertuig B verleende geen voorrang."
            }
        """.trimIndent()

        val result = accidentAnalysisJson.decodeFromString<AccidentAnalysis>(json)

        assertTrue(result is AccidentAnalysis.Complete)
        assertEquals(RoadType.CROSSROADS, result.trafficSituation?.roadType)
        assertEquals(PriorityRegulation.PRIORITY_TO_RIGHT, result.trafficSituation?.priorityRegulation)
        assertEquals(AtFault.VEHICLE_B, result.liability?.atFault)
        assertEquals(CircumstanceCode.FAILED_TO_YIELD_FROM_RIGHT, result.liability?.circumstanceCode)
        assertEquals("Voertuig B verleende geen voorrang.", result.summary)
    }

    @Test
    fun `decodes needs_clarification response`() {
        val json = """
            {
              "status": "needs_clarification",
              "clarification": {
                "question": "Welke kant reed voertuig A op?",
                "options": [{"id": "opt_1", "label": "Links"}, {"id": "opt_2", "label": "Rechts"}]
              },
              "traffic_situation": null,
              "liability": null,
              "summary": null
            }
        """.trimIndent()

        val result = accidentAnalysisJson.decodeFromString<AccidentAnalysis>(json)

        assertTrue(result is AccidentAnalysis.NeedsClarification)
        result as AccidentAnalysis.NeedsClarification
        assertEquals("Welke kant reed voertuig A op?", result.clarification.question)
        assertEquals(listOf("opt_1", "opt_2"), result.clarification.options.map { it.id })
        assertNull(result.trafficSituation)
        assertNull(result.summary)
    }

    @Test
    fun `unknown enum value is coerced to null`() {
        val json = """
            {"status": "complete", "traffic_situation": {"road_type": "MOUNTAIN_PASS"}}
        """.trimIndent()

        val result = accidentAnalysisJson.decodeFromString<AccidentAnalysis>(json)

        assertNull(result.trafficSituation?.roadType)
    }
}
