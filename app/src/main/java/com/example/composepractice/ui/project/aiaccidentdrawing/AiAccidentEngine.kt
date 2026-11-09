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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

private val conversationConfig = ConversationConfig(
    systemInstruction = Contents.of("You are an accident assistant that helps by translating a top down drawing of a traffic accident to human readable text"),
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
                visionBackend = backend
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

    fun sendImage(bitmap: Bitmap): Flow<String> = callbackFlow {
        val activeConversation = checkNotNull(conversation) { "Initialize a model first" }
        var done = false
        activeConversation.sendMessageAsync(
            Contents.of(Content.ImageBytes(bitmap.toByteArray())),
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
}

fun Bitmap.toByteArray(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100,
): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(format, quality, stream)
    return stream.toByteArray()
}