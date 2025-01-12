package com.example.buildplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

/**
 * Gradle Task to convert SVG files to Kotlin ImageVector files.
 * Generates a similar file as Material Icons uses.
 *
 * @param inputDirectory Path to the input directory containing SVG file(s). Will convert every .svg file in this directory.
 * @param outputDirectory Path to the output directory, the generated Kotlin file(s) will be placed here.
 * @param packageName The package name for the generated Kotlin file(s).
 * @param baseObjectPackage The full package path of the object the icon should extend from.
 *
 * # Example usage:
 * ```kt
 * // CustomIcon.kt
 * package com.example.composepractice
 *
 * object CustomIcons
 *
 * ```
 *
 * ```console
 * ./gradlew convertSvgToImageVector
 * --inputDirectory=/path/to/svg
 * ```
 * run with custom outputDirectory, packagename and baseObjectPackage:
 * ```console
 * ./gradlew convertSvgToImageVector
 * --outputDirectory=/path/to/output
 * --packageName=com.example.composepractice.ui.project.icons
 * --baseObjectPackage=com.example.composepractice.ui.project.icons.CustomIcons
 * ```
 *
 * For the example the input directory contained: avatar_full.svg. The following file would be generated:
 * ```kt
 * // AvatarFull.kt
 * package com.example.composepractice.ui.project.icons // < packageName
 *
 * import com.example.composepractice.ui.project.icons.CustomIcons // < baseObjectPackage
 * import ...
 *
 * val CustomIcons.AvatarFull: ImageVector
 *      get() {
 *          ...
 *      }
 *
 * private var _AvatarFull: ImageVector? = null
 * ```
 */
open class ConvertSvgToImageVector : DefaultTask() {

    @Input
    @Option(
        option = "inputDirectory",
        description = "Path to the input directory containing SVG file(s)"
    )
    lateinit var inputDirectory: String

    @Input
    @Option(option = "outputDirectory", description = "Path to the output directory")
    lateinit var outputDirectory: String

    @Input
    @Option(option = "packageName", description = "The package name for the generated Kotlin file")
    lateinit var packageName: String

    @Input
    @Option(
        option = "baseObjectPackage",
        description = "The full package path of the object the icon should extend from"
    )
    lateinit var baseObjectPackage: String

    @TaskAction
    fun convert() {
        validateInputs()
        val inputDir = File(inputDirectory)
        val outputDir = File(outputDirectory)
        if (!outputDir.exists()) outputDir.mkdirs()

        val results = inputDir.listFiles { file -> file.extension == "svg" }?.map { svgFile ->
            try {
                processSvgFile(svgFile, packageName, baseObjectPackage, outputDir)
            } catch (e: Exception) {
                logger.error("Failed to process SVG file ${svgFile.name}: ${e.message}", e)
                ConversionResult(svgFile.name, success = false, errorMessage = e.message)
            }
        } ?: emptyList()

        summarizeResults(results)
    }

    private fun validateInputs() {
        if (inputDirectory.isBlank()) throw IllegalArgumentException("inputDirectory is required")
        if (!File(inputDirectory).exists()) throw IllegalArgumentException("inputDirectory does not exist: $inputDirectory")
        if (packageName.isBlank()) throw IllegalArgumentException("packageName is required")
        if (baseObjectPackage.isBlank()) throw IllegalArgumentException("baseObjectPackage is required")
    }

    /**
     * Handles everything for converting a single SVG file to a kotlin ImageVector file and saving it.
     * @param svgFile SVG file to be converted.
     * @param packageName packageName to be used in the output file.
     * @param baseObjectPackage full package path of the object to be extended from. Will be used for import and extending.
     * @param outputDir Directory the output kotlin file will be saved in.
     */
    private fun processSvgFile(
        svgFile: File,
        packageName: String,
        baseObjectPackage: String,
        outputDir: File
    ): ConversionResult {
        val svgContent = svgFile.readText()
        val dimensions = parseSvgToDimensions(svgContent)
        val paths = parseSvgToPaths(svgContent)
        val dimensionsWarning = checkSvgDimensions(dimensions)

        val iconName = svgFile
            .nameWithoutExtension
            .replace('_', ' ')
            .split(' ')
            .joinToString("") { it.replaceFirstChar { it.uppercase() } }

        val kotlinCode = generateKotlinCode(
            packageName,
            baseObjectPackage,
            iconName,
            dimensions,
            paths,
            dimensionsWarning
        )
        File(outputDir, "$iconName.kt").writeText(kotlinCode)

        logger.lifecycle("Converted ${svgFile.name} to $iconName.kt")
        return ConversionResult(iconName, success = true, warningMessage = dimensionsWarning)
    }

    /**
     * Generates the kotlin file content based on the provided input.
     * @param packageName packageName of the file.
     * @param baseObjectPackage full package path of the object to be extended from. Will be used for import and extending.
     * @param iconName name of the final icon.
     * @param dimensions dimensions of the ImageVector.
     * @param paths list of ImageVector builder path methods.
     * @param dimensionsWarning optional warning when not recommended dimensions are used.
     */
    private fun generateKotlinCode(
        packageName: String,
        baseObjectPackage: String,
        iconName: String,
        dimensions: ImageVectorDimension,
        paths: List<String>,
        dimensionsWarning: String? = null
    ): String {
        val baseObject = baseObjectPackage.substringAfterLast('.')
        val pathsString =
            paths.joinToString("\n") { "path (fill = SolidColor(Color(0xFF000000))) {\n$it\n\t\t\t}" }

        return """
package $packageName

import $baseObjectPackage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
${dimensionsWarning?.let { "\n// $it" } ?: ""}
val $baseObject.$iconName: ImageVector
    get() {
        if (_$iconName != null)
            return _$iconName!!

        _$iconName = ImageVector.Builder(
            name = "$iconName",
            defaultWidth = ${dimensions.defaultWidth}.dp,
            defaultHeight = ${dimensions.defaultHeight}.dp,
            viewportWidth = ${dimensions.viewportWidth}f,
            viewportHeight = ${dimensions.viewportHeight}f
        ).apply {
            $pathsString
        }.build()

        return _$iconName!!
    }

private var _$iconName: ImageVector? = null
"""
    }

    /**
     * Extracts the width, height and viewBox from the SVG file.
     * When a value can not be found the [ConvertSvgToImageVector.DEFAULT_WIDTH] and [ConvertSvgToImageVector.DEFAULT_HEIGHT] will be used.
     * @param svgContent The content of the SVG file.
     * @return The extracted width, height and viewBox width and height.
     */
    private fun parseSvgToDimensions(svgContent: String): ImageVectorDimension {
        val widthRegex = """width="(\d+)"""".toRegex()
        val heightRegex = """height="(\d+)"""".toRegex()
        val viewBoxRegex = """viewBox="([^"]+)"""".toRegex()

        val width = widthRegex.find(svgContent)?.groups?.get(1)?.value?.toInt()
        val height = heightRegex.find(svgContent)?.groups?.get(1)?.value?.toInt()
        val viewBox = viewBoxRegex.find(svgContent)?.groups?.get(1)?.value
        val viewBoxWidth = viewBox?.split(' ')?.get(2)?.toInt()
        val viewBoxHeight = viewBox?.split(' ')?.get(3)?.toInt()

        return ImageVectorDimension(
            defaultWidth = width ?: DEFAULT_WIDTH,
            defaultHeight = height ?: DEFAULT_HEIGHT,
            viewportWidth = viewBoxWidth ?: DEFAULT_WIDTH,
            viewportHeight = viewBoxHeight ?: DEFAULT_HEIGHT
        )
    }

    /**
     * Checks if the dimensions are not the recommended [DEFAULT_WIDTH] and [DEFAULT_HEIGHT].
     * @param dimensions The dimensions to check.
     * @return A warning message if the dimensions are not recommended, otherwise null.
     */
    private fun checkSvgDimensions(dimensions: ImageVectorDimension): String? {
        val defaultWidth = DEFAULT_WIDTH
        val defaultHeight = DEFAULT_HEIGHT
        val warning =
            "Warning: The dimensions are not the recommended $defaultWidth by $defaultHeight, please verify with the design team"

        return if (
            dimensions.defaultWidth != defaultWidth
            || dimensions.defaultHeight != defaultHeight
            || dimensions.viewportWidth != defaultWidth
            || dimensions.viewportHeight != defaultHeight
        )
            warning
        else
            null
    }

    /**
     * Extracts the path data from the SVG file and converts it into their respective ImageVector builder methods.
     * @param svgContent The content of the SVG file.
     * @return A list of ImageVector builder methods. Each string inside the list should be placed inside its own path { }
     */
    private fun parseSvgToPaths(svgContent: String): List<String> {
        // Match SVG <path> tag
        val regex = """<path\s+[^>]*d=["']([^"']+)["'][^>]*>""".toRegex()
        val matches = regex.findAll(svgContent).map { it.groupValues[1] }.toList()

        if (matches.isEmpty())
            throw IllegalArgumentException("No path data found in SVG")

        val response = mutableListOf<String>()

        matches.forEach { match ->
            val path = mutableListOf<String>()
            val commandRegex = """([MCQLZHVSAmcqlzhvsa])([^MCQLZHVSAmcqlzhvsa]*)""".toRegex()
            val commandMatches = commandRegex.findAll(match)

            for (m in commandMatches) {
                val commandType = m.groupValues[1]
                val arguments = m.groupValues[2].trim()
                val argsList = arguments.split(" ").filter { it.isNotEmpty() }
                val command = Command.fromLetter(commandType)

                // When command is unknown or there are not enough arguments, throw error
                if (command == null || command.arguments > argsList.size)
                    throw IllegalArgumentException("Invalid command: $commandType with arguments: $arguments")

                path.add(command.toCode(argsList))
            }
            response.add(path.joinToString("\n") { "\t\t\t\t$it" })
        }

        return response
    }

    private fun summarizeResults(results: List<ConversionResult>) {
        val total = results.size
        val success = results.filter { it.success }.size
        val warnings = results.filter { it.success && it.warningMessage != null }
        val failed = results.filter { !it.success }

        logger.lifecycle("\nSuccessfully converted $success out of $total SVG files")
        if (warnings.isNotEmpty())
            logger.warn("${warnings.size} files converted with a warning:\n${warnings.map { "\t${it.fileName} - ${it.warningMessage}" }.joinToString("\n")}")
        if (failed.isNotEmpty())
            logger.error("Encountered ${failed.size} errors during conversion: \n${failed.map { "\t${it.fileName} - ${it.errorMessage}" }.joinToString("\n")}")
    }

    /**
     * Enums representing every possible SVG command mapped to the correct kotlin ImageVector method.
     * @property letter The letter used in a SVG <path> data.
     * @property arguments The amount of arguments this command should have.
     * @property toCode Maps the SVG command with arguments to the correct kotlin ImageVector method.
     */
    private enum class Command(
        val letter: String,
        val arguments: Int,
        val toCode: (List<String>) -> String
    ) {
        MoveTo("M", 2, { args -> "moveTo(${args[0]}f, ${args[1]}f)" }),
        MoveToRelative("m", 2, { args -> "moveToRelative(${args[0]}f, ${args[1]}f)" }),
        LineTo("L", 2, { args -> "lineTo(${args[0]}f, ${args[1]}f)" }),
        LineToRelative("l", 2, { args -> "lineToRelative(${args[0]}f, ${args[1]}f)" }),
        HorizontalLineTo("H", 1, { args -> "horizontalLineTo(${args[0]}f)" }),
        HorizontalLineToRelative("h", 1, { args -> "horizontalLineToRelative(${args[0]}f)" }),
        VerticalLineTo("V", 1, { args -> "verticalLineTo(${args[0]}f)" }),
        VerticalLineToRelative("v", 1, { args -> "verticalLineToRelative(${args[0]}f)" }),
        CurveTo("C", 6, { args -> "curveTo(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f, ${args[4]}f, ${args[5]}f)" }),
        CurveToRelative("c", 6, { args -> "curveToRelative(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f, ${args[4]}f, ${args[5]}f)" }),
        ReflectiveCurveTo("S", 4, { args -> "quadTo(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f)" }),
        ReflectiveCurveToRelative("s", 4, { args -> "quadToRelative(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f)" }),
        QuadTo("Q", 4, { args -> "quadTo(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f)" }),
        QuadToRelative("q", 4, { args -> "quadToRelative(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f)" }),
        ReflectiveQuadTo("T", 2, { args -> "reflectiveQuadTo(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f)" }),
        ReflectiveQuadToRelative("t", 2, { args -> "reflectiveQuadToRelative(${args[0]}f, ${args[1]}f, ${args[2]}f, ${args[3]}f)" }),
        Close("z", 0, { "close()" }),
        CloseRelative("Z", 0, { "close()" });

        companion object {
            fun fromLetter(letter: String): Command? {
                return values().find { it.letter == letter }
            }
        }
    }

    private data class ImageVectorDimension(
        val defaultWidth: Int,
        val defaultHeight: Int,
        val viewportWidth: Int,
        val viewportHeight: Int
    )

    /**
     * Result data from the conversion containing everything needed to summarize the conversion.
     * @property fileName The name of the SVG file (without extension)
     * @property success Indicates if the conversion was successful
     * @property warningMessage Warning message that was found during conversion
     * @property errorMessage Error message that was thrown during conversion
     */
    private data class ConversionResult(
        val fileName: String,
        val success: Boolean,
        val warningMessage: String? = null,
        val errorMessage: String? = null
    )

    companion object {
        const val DEFAULT_WIDTH = 24
        const val DEFAULT_HEIGHT = 24
    }
}