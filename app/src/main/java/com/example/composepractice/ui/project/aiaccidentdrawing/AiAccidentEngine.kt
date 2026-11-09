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
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
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
                "  4. Shark teeth / triangles: A row of triangles drawn across a road approaching an intersection represents shark tooth markings (\"haaientanden\"). The vehicle on that road must give way, overriding priority-to-the-right; set priority_regulation to GIVE_WAY_YIELD and hold the triangle-marked vehicle at fault if it failed to yield." +
                "  5. Manoeuvres (parking, reversing, U-turns, entering from an unpaved road/driveway): Manoeuvring vehicle must yield to all other traffic." +
                "  6. Lane changes / Merging: The vehicle crossing a lane divider must yield." +
                "  7. Roundabouts: Vehicles already on the circulatory roadway have priority unless marked otherwise." +
                "- Set status to \"needs_clarification\" ONLY as an absolute last resort if crucial arrows or positions are completely missing or visually illegible." +
                "CONSISTENCY CHECK (MANDATORY, DO THIS LAST):" +
                "- Before outputting, verify that \"liability.at_fault\" and \"summary\" name the SAME vehicle as the cause of the collision." +
                "- \"summary\" must never blame a different vehicle than \"liability.at_fault\" states, and must never contradict \"liability.circumstance_code\"." +
                "- If you notice a contradiction, silently redo the reasoning until \"at_fault\" and \"summary\" agree before writing the final JSON." +
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
                "  \"summary\": null | \"Korte, feitelijke Nederlandse samenvatting (2-3 zinnen) over de toedracht en de botsing. Moet dezelfde partij als schuldige aanwijzen als 'liability.at_fault'.\"" +
                "}"
    ),
)

enum class BackendOption(val label: String) {
    CPU("CPU"),
    GPU("GPU"),
    NPU("NPU"),
}

private fun BackendOption.toBackend(context: Context): Backend = when (this) {
    BackendOption.CPU -> Backend.CPU()
    BackendOption.GPU -> Backend.GPU()
    BackendOption.NPU -> Backend.NPU(context.applicationInfo.nativeLibraryDir)
}

data class AiModel(
    val name: String,
    val url: String,
    val fileName: String,
    // Chipset-specific NPU delegates need a model file compiled ahead-of-time; null means
    // no device-agnostic NPU variant is published for this model, so NPU isn't offered for it.
    val npuUrl: String? = null,
    val npuFileName: String? = null,
)

data class LocalModel(
    val name: String,
    val path: String,
    val npuPath: String? = null,
)

data class GenerationStats(
    val tokenCount: Int,
    val tokensPerSecond: Double,
    val durationSeconds: Double,
)

data class AnalysisResponse(
    val analysis: AccidentAnalysis,
    val stats: GenerationStats,
)

private const val MODEL_EXTENSION = "litertlm"
private const val MODEL_DIRECTORY_NAME = "models"

val availableModels = listOf(
    AiModel(
        name = "Gemma 4 E2B",
        url = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true",
        fileName = "gemma-4-E2B-it.litertlm",
        // NPU (Google_Tensor_G5) fails with "Failed to allocate tensors" on this engine API;
        // the vendor doesn't document an Android/Tensor G5 setup for it. CPU/GPU only for now.
    ),
    AiModel(
        name = "Gemma 4 E4B",
        url = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm?download=true",
        fileName = "gemma-4-E4B-it.litertlm",
    ),
    AiModel(
        name = "LFM3-VL-2B",
        url = "https://huggingface.co/litert-community/LFM3-VL-2B-LiteRT/resolve/main/LFM3-VL-2B_int4_ctx4096.litertlm?download=true",
        fileName = "LFM3-VL-2B_int4_ctx4096.litertlm",
        // Not NPU-compiled by the vendor for any chipset - CPU/GPU only.
    ),
    // embeddinggemma-2 models are embedding-only (no LlmMetadata), so they fail with
    // "LlmMetadata not found in the model file" on every backend through this chat Conversation
    // API - they'd need the library's separate EmbeddingEngine, which this app doesn't use.
)

class AiAccidentEngine(
    context: Context,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val appContext = context.applicationContext
    private val modelDirectory = File(appContext.noBackupFilesDir, MODEL_DIRECTORY_NAME)

    private var conversation: Conversation? = null
    private lateinit var engine: Engine

    @OptIn(ExperimentalApi::class)
    fun initialize(
        model: LocalModel,
        backendOption: BackendOption = BackendOption.GPU,
    ) {
        if (conversation != null) return
        val modelPath = when (backendOption) {
            BackendOption.NPU -> checkNotNull(model.npuPath) { "No NPU-compiled model downloaded for ${model.name}" }
            BackendOption.CPU, BackendOption.GPU -> model.path
        }
        ExperimentalFlags.enableBenchmark = true
        val backend = backendOption.toBackend(appContext)
        engine = Engine(
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                visionBackend = backend,
                // NPU delegates need a writable scratch directory to allocate compiled tensors into.
                cacheDir = appContext.cacheDir.absolutePath,
            ),
        )
        engine.initialize()
        conversation = engine.createConversation(
            conversationConfig = conversationConfig,
        )
    }

    fun listDownloadedModels(): List<LocalModel> {
        // NPU files are a variant of a model, not a model of their own: fold them into npuPath.
        val npuFileNames = availableModels.mapNotNull { it.npuFileName }.toSet()
        return modelDirectory.listFiles { file -> file.isFile && file.extension == MODEL_EXTENSION }
            .orEmpty()
            .filter { it.name !in npuFileNames }
            .map { file ->
                val model = availableModels.find { it.fileName == file.name }
                val npuPath = model?.npuFileName
                    ?.let { npuFileName -> File(modelDirectory, npuFileName) }
                    ?.takeIf { it.isFile }
                    ?.absolutePath
                LocalModel(
                    name = model?.name ?: file.nameWithoutExtension,
                    path = file.absolutePath,
                    npuPath = npuPath,
                )
            }
            .sortedBy { it.name }
    }

    fun listUndownloadedModels(): List<AiModel> {
        val modelNames = listDownloadedModels().map { it.name }
        return availableModels.filter { it.name !in modelNames }
    }

    suspend fun downloadModel(
        model: AiModel,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): File = downloadFile(model.url, model.fileName, onProgress)

    suspend fun downloadNpuModel(
        model: AiModel,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): File {
        val npuUrl = checkNotNull(model.npuUrl) { "${model.name} has no NPU-compiled variant" }
        val npuFileName = checkNotNull(model.npuFileName)
        return downloadFile(npuUrl, npuFileName, onProgress)
    }

    private suspend fun downloadFile(
        url: String,
        fileName: String,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val modelFile = File(modelDirectory, fileName)
        if (modelFile.exists()) return@withContext modelFile

        check(modelDirectory.isDirectory || modelDirectory.mkdirs()) { "Could not create model directory" }
        val partialFile = File(modelDirectory, "$fileName.part")

        val request = Request.Builder().url(url).build()
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

    @OptIn(ExperimentalApi::class)
    fun sendImage(bitmap: Bitmap): Flow<AnalysisResponse> = callbackFlow {
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
                        val stats = try {
                            val benchmark = activeConversation.getBenchmarkInfo()
                            val decodeSeconds = if (benchmark.lastDecodeTokensPerSecond > 0) {
                                benchmark.lastDecodeTokenCount / benchmark.lastDecodeTokensPerSecond
                            } else {
                                0.0
                            }
                            GenerationStats(
                                tokenCount = benchmark.lastDecodeTokenCount,
                                tokensPerSecond = benchmark.lastDecodeTokensPerSecond,
                                durationSeconds = benchmark.timeToFirstTokenInSecond + decodeSeconds,
                            )
                        } catch (e: Exception) {
                            println(e)
                            GenerationStats(tokenCount = 0, tokensPerSecond = 0.0, durationSeconds = 0.0)
                        }
                        trySend(AnalysisResponse(analysis, stats))
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