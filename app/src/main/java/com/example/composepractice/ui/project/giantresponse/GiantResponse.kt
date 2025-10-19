package com.example.composepractice.ui.project.giantresponse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import kotlin.time.Duration.Companion.nanoseconds

@Composable
fun GiantResponseScreen() {
    Scaffold(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) { contentPadding ->
        var doRequest by remember { mutableStateOf(false) }

        LaunchedEffect(doRequest) {
            if (doRequest) {
                RetrofitClient.loadApiData()
                doRequest = false
            }
        }

        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Button(
                onClick = { doRequest = true }
            ) {
                Text("Load Giant Response")
            }
        }
    }
}

object RetrofitClient {

    // IMPORTANT: Use 10.0.2.2 for the Android Emulator to reach the host machine's localhost (127.0.0.1).
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // 1. Configure Moshi with Polymorphic Adapter
    private val moshi: Moshi = Moshi.Builder()
        // Register the Polymorphic Adapter for the Destination sealed interface
        .add(
            PolymorphicJsonAdapterFactory.of(Destination::class.java, "type")
                .withSubtype(WebDestination::class.java, "web")
                .withSubtype(DeeplinkDestination::class.java, "deeplink")
                .withSubtype(QisDestination::class.java, "qis")
                .withSubtype(MultipleDestination::class.java, "multiple")
        )
        // Add the KotlinJsonAdapterFactory for general Kotlin data class support
        // This MUST be the last adapter added.
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // 2. Configure OkHttpClient with a logger for debugging
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    // 3. Initialize Retrofit
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // 4. Create the service instance
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Executes the API call and measures the duration of the network request and the JSON parsing.
     */
    suspend fun loadApiData() {
        // Timer for the overall network request (includes sending and receiving the response body)
        val networkStartTime = System.nanoTime()

        try {
            // Execute the suspend function defined in ApiService.
            // This is the network part (network transfer + OkHttp buffering).
            val response = apiService.getGiantResponse()

            // Timer for the start of parsing (deserialization).
            // This happens when .body() is first accessed.
            val parsingStartTime = System.nanoTime()

            if (response.isSuccessful) {
                // Accessing .body() triggers the Moshi parsing process, hence this is where
                // we want to measure the duration from parsingStartTime.
                val giantResponse = response.body()

                // Timer for the end of parsing.
                val parsingEndTime = System.nanoTime()

                // Calculate durations. Network duration is kept in milliseconds for high-level comparison.
                val networkDurationMs = (parsingStartTime - networkStartTime).nanoseconds.inWholeMilliseconds

                // Parsing duration is calculated in microseconds (us) for better granularity.
                val parsingDurationUs = (parsingEndTime - parsingStartTime).nanoseconds.inWholeMicroseconds

                if (giantResponse != null) {
                    // Success! Data is now a fully parsed GiantResponse object
                    println("\n--- Performance Metrics ---")
                    println("Network Call/Transfer/Buffering Duration: $networkDurationMs ms")
                    println("JSON Parsing Duration: $parsingDurationUs \u03BCs") // \u03BCs is the microsecond symbol
                    println("---------------------------\n")

                    println("Successfully parsed response.")
                    println("Total Packages: ${giantResponse.packages.size}")
                    println("Total Single Policies: ${giantResponse.singlepolicies.size}")
                    println("Total policies in all packages: ${giantResponse.packages.sumOf { it.policies.size }}")

                    // Example: Checking a deeply nested polymorphic field
                    val firstPolicy = giantResponse.packages.firstOrNull()?.policies?.firstOrNull()
                    val destinationType =
                        firstPolicy?.insuranceDetails?.topTasks?.firstOrNull()?.destination

                    println("First policy's destination type is: ${destinationType?.javaClass?.simpleName}")

                    // You can safely cast the sealed interface based on its type:
                    if (destinationType is WebDestination) {
                        println("Web URL: ${destinationType.url}")
                    }

                } else {
                    println("API call successful, but response body was null.")
                }
            } else {
                // Handle non-2xx status codes (e.g., 404, 500)
                println("API Request failed with code: ${response.code()}")
            }
        } catch (e: HttpException) {
            // Handle HTTP exceptions (e.g., failed parsing, timeouts)
            println("HTTP Exception: ${e.message()}")
        } catch (e: IOException) {
            // Handle network exceptions (e.g., no internet connection)
            println("Network Error: ${e.message}")
        }
    }
}