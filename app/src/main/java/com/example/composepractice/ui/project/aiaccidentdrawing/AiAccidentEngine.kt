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

private const val SYSTEM_INSTRUCTION_ASSET = "system_instruction.txt"

private fun loadSystemInstruction(context: Context): String =
    context.assets.open(SYSTEM_INSTRUCTION_ASSET).bufferedReader().use { it.readText() }

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
    private val conversationConfig by lazy {
        ConversationConfig(systemInstruction = Contents.of(loadSystemInstruction(appContext)))
    }

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