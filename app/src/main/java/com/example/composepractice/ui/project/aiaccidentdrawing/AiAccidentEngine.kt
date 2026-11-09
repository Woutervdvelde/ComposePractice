package com.example.composepractice.ui.project.aiaccidentdrawing

import android.content.Context
import android.graphics.Bitmap
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
private val conversationConfig = ConversationConfig(
    systemInstruction = Contents.of(
        "You are a decisive European Traffic Accident Report (Europees Schadeformulier) visual analyst." +
                "Analyze the top-down accident sketch and return a single, raw JSON object." +
                "CRITICAL FORMATTING INSTRUCTIONS:" +
                "- Return ONLY valid raw JSON." +
                "- DO NOT wrap the output in markdown codeblocks (NEVER use ```json or ```)." +
                "- Start your response directly with { and end directly with }." +
                "- No explanations, preambles, or conversational commentary." +
                "LANGUAGE SPECIFICATIONS:" +
                "- Structural keys and ENUM values: STRICTLY ENGLISH." +
                "- Dynamic user-facing text fields (\"question\", \"label\", \"summary\"): STRICTLY DUTCH." +
                "BEHAVIOR & DEFAULT ASSUMPTIONS (BE DECISIVE):" +
                "- Do NOT hesitate or ask unnecessary questions. Interpret the sketch assertively using standard European traffic regulations:" +
                "  1. Rear-end collision: Trailing vehicle failed to maintain distance or stop in time." +
                "  2. Turning vs straight traffic: Vehicle continuing straight on the same road has priority over turning vehicles." +
                "  3. Unmarked intersections: Default to priority-to-the-right rule (\"voorrang van rechts\") if no shark teeth or signs appear." +
                "  4. Manoeuvres (parking, reversing, U-turns, entering from an unpaved road/driveway): Manoeuvring vehicle must yield to all other traffic." +
                "  5. Lane changes / Merging: The vehicle crossing a lane divider must yield." +
                "  6. Roundabouts: Vehicles already on the circulatory roadway have priority unless marked otherwise." +
                "- Set status to \"needs_clarification\" ONLY as an absolute last resort if crucial arrows or positions are completely missing or visually illegible." +
                "JSON SCHEMA:" +
                "{" +
                "  \"status\": \"complete\" | \"needs_clarification\"," +
                "  \"clarification\": null | {" +
                "    \"question\": \"Korte, duidelijke vraag in het Nederlands (< 50 tekens)\"," +
                "    \"options\": [" +
                "      {\"id\": \"opt_1\", \"label\": \"Korte knoptekst in het Nederlands (< 30 tekens)\"}," +
                "      {\"id\": \"opt_2\", \"label\": \"Korte knoptekst in het Nederlands (< 30 tekens)\"}" +
                "    ]" +
                "  }," +
                "  \"traffic_situation\": null | {" +
                "    \"road_type\": \"STRAIGHT_ROAD\" | \"T_JUNCTION\" | \"CROSSROADS\" | \"ROUNDABOUT\" | \"HIGHWAY_MOTORWAY\" | \"HIGHWAY_ON_OFF_RAMP\" | \"PARKING_LOT\" | \"DRIVEWAY_PRIVATE_PROPERTY\" | \"BEND_CURVE\"," +
                "    \"priority_regulation\": \"PRIORITY_ROAD\" | \"GIVE_WAY_YIELD\" | \"STOP_SIGN\" | \"TRAFFIC_LIGHTS\" | \"PRIORITY_TO_RIGHT\" | \"ROUNDABOUT_RULES\" | \"UNKNOWN_UNREGULATED\"" +
                "  }," +
                "  \"liability\": null | {" +
                "    \"at_fault\": \"VEHICLE_A\" | \"VEHICLE_B\" | \"SHARED\" | \"UNDETERMINED\"," +
                "    \"circumstance_code\": " +
                "      \"REAR_END_COLLISION\" |" +
                "      \"FAILED_TO_YIELD_FROM_RIGHT\" |" +
                "      \"FAILED_TO_YIELD_AT_SIGN_OR_MARKING\" |" +
                "      \"TURNING_ACROSS_OPPOSING_TRAFFIC\" |" +
                "      \"IMPROPER_LANE_CHANGE\" |" +
                "      \"REVERSING\" |" +
                "      \"ENTERING_OR_LEAVING_PARKING\" |" +
                "      \"ENTERING_ROUNDABOUT\" |" +
                "      \"OVERTAKING_COLLISION\" |" +
                "      \"OPENING_CAR_DOOR\" |" +
                "      \"MERGING_HIGHWAY\"" +
                "  }," +
                "  \"summary\": null | \"Korte, feitelijke Nederlandse samenvatting (2-3 zinnen) over de toedracht en de botsing.\"" +
                "}"
    ),
)

data class AiModel(
    val name: String,
    val url: String,
    val fileName: String,
)

data class LocalModel(
    val name: String,
    val path: String,
)

private const val MODEL_EXTENSION = "litertlm"
private const val MODEL_DIRECTORY_NAME = "models"

val availableModels = listOf(
    AiModel(
        name = "Gemma 4 E2B",
        url = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true",
        fileName = "gemma-4-E2B-it.litertlm",
    ),
    AiModel(
        name = "Gemma 4 E4B",
        url = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm?download=true",
        fileName = "gemma-4-E4B-it.litertlm",
    ),
)

class AiAccidentEngine(
    context: Context,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val modelDirectory = File(context.applicationContext.noBackupFilesDir, MODEL_DIRECTORY_NAME)

    private var conversation: Conversation? = null
    private lateinit var engine: Engine

    fun initialize(
        modelPath: String,
        backend: Backend = Backend.GPU(),
    ) {
        if (conversation != null) return
        engine = Engine(
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                visionBackend = backend,
            ),
        )
        engine.initialize()
        conversation = engine.createConversation(
            conversationConfig = conversationConfig,
        )
    }

    fun initialize(
        model: LocalModel,
        backend: Backend = Backend.GPU(),
    ) = initialize(model.path, backend)

    fun listDownloadedModels(): List<LocalModel> =
        modelDirectory.listFiles { file -> file.isFile && file.extension == MODEL_EXTENSION }
            .orEmpty()
            .map { file ->
                LocalModel(
                    name = availableModels.find { it.fileName == file.name }?.name
                        ?: file.nameWithoutExtension,
                    path = file.absolutePath,
                )
            }
            .sortedBy { it.name }

    fun listUndownloadedModels(): List<AiModel> {
        val modelNames = listDownloadedModels().map { it.name }
        return availableModels.filter { it.name !in modelNames }
    }

    suspend fun downloadModel(
        model: AiModel,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): File = withContext(Dispatchers.IO) {
        val modelFile = File(modelDirectory, model.fileName)
        if (modelFile.exists()) return@withContext modelFile

        check(modelDirectory.isDirectory || modelDirectory.mkdirs()) { "Could not create model directory" }
        val partialFile = File(modelDirectory, "${model.fileName}.part")

        val request = Request.Builder().url(model.url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Model download failed: HTTP ${response.code}")
            val body = response.body
            val totalBytes = body.contentLength()
            if (totalBytes > 0 && modelDirectory.usableSpace < totalBytes) throw IOException("Not enough storage for model")

            body.byteStream().use { input ->
                partialFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloadedBytes = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        downloadedBytes += count
                        onProgress(downloadedBytes, totalBytes)
                    }
                }
            }
        }

        check(partialFile.renameTo(modelFile)) { "Could not save model" }
        modelFile
    }

    fun sendMessage(message: String): Flow<String> = callbackFlow {
        val activeConversation = checkNotNull(conversation) { "Initialize a model first" }
        var done = false
        activeConversation.sendMessageAsync(
            Contents.of(Content.Text(message)),
            object : MessageCallback {
                override fun onMessage(message: Message) {
                    message.contents.contents.filterIsInstance<Content.Text>().forEach { trySend(it.text) }
                }

                override fun onDone() {
                    done = true
                    close()
                }

                override fun onError(throwable: Throwable) {
                    done = true
                    close(throwable)
                }
            },
        )
        awaitClose { if (!done) activeConversation.cancelProcess() }
    }

    fun sendImage(bitmap: Bitmap): Flow<AccidentAnalysis> = callbackFlow {
        val activeConversation = checkNotNull(conversation) { "Initialize a model first" }
        var response = ""
        var done = false
        activeConversation.sendMessageAsync(
            Contents.of(Content.ImageBytes(bitmap.toByteArray())),
            object : MessageCallback {
                override fun onMessage(message: Message) {
                    message.contents.contents.filterIsInstance<Content.Text>().forEach { 
                        response += it.text
                    }
                }

                override fun onDone() {
                    done = true
                    try {
                        val analysis = accidentAnalysisJson.decodeFromString<AccidentAnalysis>(response)
                        trySend(analysis)
                    } catch (e: Exception) {
                        println(e)
                    } finally {
                        close()
                    }
                }

                override fun onError(throwable: Throwable) {
                    done = true
                    close(throwable)
                }
            },
        )
        awaitClose { if (!done) activeConversation.cancelProcess() }
    }
}

fun Bitmap.toByteArray(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(format, quality, stream)
    return stream.toByteArray()
}