package com.example.composepractice.ui.project.giantresponse

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET

// --- 1. Destination Polymorphism Setup (Sealed Interface with Moshi) ---

/**
 * Defines the contract for all destination types (Web, Deeplink, Qis, Multiple).
 * Moshi will use a custom adapter (configured in RetrofitClient) to determine
 * the concrete class based on the 'type' field.
 */

// --- 1. Destination Polymorphism Setup ---

/**
 * Enum class defining the exact string values used for the 'type' discriminator field in the JSON.
 */
enum class DestinationType {
    @Json(name = "web") WEB,
    @Json(name = "deeplink") DEEPLINK,
    @Json(name = "qis") QIS,
    @Json(name = "multiple") MULTIPLE
}

/**
 * Defines the contract for all destination types (Web, Deeplink, Qis, Multiple).
 * The base class now uses the enum for the discriminator field, improving safety.
 */
sealed class Destination(@Json(name = "type") val type: DestinationType)

// 1.1. Web Destination
@JsonClass(generateAdapter = true)
data class WebDestination(
    val url: String,
    val sso: Boolean,
    val userData: Boolean,
) : Destination(DestinationType.WEB)

// 1.2. Deeplink Destination
@JsonClass(generateAdapter = true)
data class DeeplinkDestination(
    val deeplink: String,
) : Destination(DestinationType.DEEPLINK)

// 1.3. Qis Destination (Supports three QisTypes: requestNew, mutate, claim)
@JsonClass(generateAdapter = true)
data class QisDestination(
    val qisType: String,
    val qisData: QisDataContainer
) : Destination(DestinationType.QIS)

@JsonClass(generateAdapter = true)
data class QisDataContainer(
    val productExtId: String? = null,
    val policyId: String? = null,
    val productName: String? = null,
    val changeType: String? = null,
    val extraInstructions: Map<String, String>? = null
)

// 1.4. Multiple Destination (Contains nested Destinations)
@JsonClass(generateAdapter = true)
data class MultipleDestination(
    val destinations: List<MultipleOption>,
) : Destination(DestinationType.MULTIPLE)

@JsonClass(generateAdapter = true)
data class MultipleOption(
    val title: String,
    val description: String,
    val icon: String? = null,
    val destination: Destination, // Recursive Destination field
)

// --- 2. Core Policy Details and Sub-Structures ---

@JsonClass(generateAdapter = true)
data class PolicyField(
    val name: String,
    val value: String
)

@JsonClass(generateAdapter = true)
data class PolicyDetail(
    val name: String,
    val picto: String? = null,
    val actionLabel: String? = null,
    val actionDestination: Destination? = null,
    val fields: List<PolicyField>
)

@JsonClass(generateAdapter = true)
data class TopTask(
    val icon: String,
    val name: String,
    val destination: Destination // Destination field
)

@JsonClass(generateAdapter = true)
data class PolicyAction(
    val icon: String,
    val name: String,
    val destination: Destination // Destination field
)

@JsonClass(generateAdapter = true)
data class Service(
    val icon: String,
    val name: String,
    val subtitle: String,
    val destination: Destination // Destination field
)

@JsonClass(generateAdapter = true)
data class PolicyDetails(
    val picto: String,
    val subTitle1: String,
    val subTitle2: String? = null,
    val topTasks: List<TopTask>,
    val actions: List<PolicyAction>,
    val services: List<Service>? = null,
    @Json(name = "policyDetais") // Handle the typo in the JSON schema 'policyDetais' -> 'policyDetails'
    val policyDetails: List<PolicyDetail>
)

@JsonClass(generateAdapter = true)
data class InsuranceListItem(
    val icon: String,
    val title: String,
    val subTitle: String,
    val label: String? = null,
    val labelType: String? = null,
    val price: String
)

@JsonClass(generateAdapter = true)
data class MutationAction(
    val text: MutationActionText,
    val destination: Destination // Destination field
)

@JsonClass(generateAdapter = true)
data class MutationActionText(
    val title: String,
    val description: String
)

// --- 3. Core Policy Structure ---

@JsonClass(generateAdapter = true)
data class Policy(
    val source: String,
    val productExtId: String,
    val policyId: Int,
    val policyStatus: String,
    val dateStart: String,
    val dateEnd: String,
    val productName: String,
    val insuranceType: String,
    // The JSON structure for this field is { "action": { ... } }
    val mutationActions: Map<String, MutationAction>,
    val insuranceListItem: InsuranceListItem,
    val insuranceDetails: PolicyDetails
)

// --- 4. Package and Intermediary ---

@JsonClass(generateAdapter = true)
data class PaymentInformation(
    val bankAccountNumber: String,
    val paymentPeriod: String,
    val paymentType: String,
    val termAmount: String,
    val termAmountDiscount: String
)

@JsonClass(generateAdapter = true)
data class Intermediary(
    val name: String,
    val formattedNameAndContactDetails: String,
    val telephoneNumber: String
)

@JsonClass(generateAdapter = true)
data class Package(
    val packageId: Int,
    val paymentInformation: PaymentInformation,
    val intermediary: Intermediary,
    val policies: List<Policy>
)

// --- 5. Top-Level Response Structure ---

@JsonClass(generateAdapter = true)
data class GiantResponse(
    val packages: List<Package>,
    val singlepolicies: List<Policy>
)

// --- 6. Retrofit Service Interface ---

/**
 * Retrofit interface defining the API call.
 */
interface ApiService {
    @GET("/api/data") // Endpoint from your server.js
    suspend fun getGiantResponse(): Response<GiantResponse>
}
