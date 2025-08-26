package com.example.composepractice.ui.project.particlesystem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.example.composepractice.R
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
internal fun ParticleSystemScreen() {
    val state = rememberParticleSystemState(
        maxParticles = 10,
        particleResources = listOf(
            ParticleResource.Resource(R.drawable.snowflakes_01),
            ParticleResource.Resource(R.drawable.snowflakes_02),
        )
    )

    ParticleSystem(
        particleSystemState = state,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}

@Composable
fun ParticleSystem(
    modifier: Modifier = Modifier,
    particleSystemState: ParticleSystemState = rememberParticleSystemState(),
) {
    LaunchedEffect(Unit) {
        var lastFrameTime = withFrameNanos { it }

        while (isActive) {
            val frameTime = withFrameNanos { it }
            val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000f // Nano seconds to seconds
            lastFrameTime = frameTime

            particleSystemState.update(deltaTime)
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                var lastPosition = Offset.Zero
                var lastVelocity = Offset.Zero

                detectDragGestures(
                    onDragEnd = {
                        particleSystemState.addSingleParticle(
                            Particle(
                                resource = ParticleResource.Resource(R.drawable.framna),
                                color = Color.White,
                                startPosition = lastPosition,
                                velocity = lastVelocity,
                                rotation = ParticleRotation(
                                    speed = 0f,
                                    initial = 0f,
                                ),
                                lifeSpan = 10f,
                            )
                        )
                    },
                    onDrag = { change, dragAmount ->
                        lastVelocity = dragAmount
                        lastPosition = change.position
                    }
                )
            }
    ) {
        particleSystemState.draw(this)
    }
}

sealed interface ParticleResource {
    data class Resource(
        @DrawableRes val resource: Int
    ) : ParticleResource

    data class Path(
        val path: androidx.compose.ui.graphics.Path
    )
}

data class ParticleRotation(
    val speed: Float,
    val initial: Float,
)

/**
 * @param lifeSpan Duration a particle will live in seconds
 */
class Particle(
    val resource: ParticleResource,
    val color: Color,
    val startPosition: Offset,
    val velocity: Offset,
    val rotation: ParticleRotation,
    val lifeSpan: Float,
    var alive: Boolean = true,
) {
    private val _invalidateTrigger = mutableIntStateOf(0)
    private var currentPosition = startPosition
    private var currentRotation = rotation.initial
    private var currentLifeSpan = lifeSpan

    fun update(deltaTime: Float) {
        currentPosition = currentPosition.copy(
            x = currentPosition.x + velocity.x * deltaTime,
            y = currentPosition.y + velocity.y * deltaTime
        )
        currentRotation += rotation.speed
        currentLifeSpan -= deltaTime

        if (currentLifeSpan <= 0f) {
            alive = false
        }
        _invalidateTrigger.intValue++
    }

    fun draw(drawScope: DrawScope) = with(drawScope) {
        // Read value to trigger update
        val trigger = _invalidateTrigger.intValue

        drawCircle(
            Color.White,
            radius = 10f,
            center = currentPosition
        )
    }
}

class ParticleSystemState(
    val maxParticles: Int = 10,
    val particleResources: List<ParticleResource>,
) {
    private var particles = mutableStateListOf<Particle>()

    init {
        addParticles(maxParticles)
    }

    private fun generateRandomOffset(min: Offset, max: Offset): Offset =
        Offset(
            x = Random.nextFloat() * (max.x - min.x) + min.x,
            y = Random.nextFloat() * (max.y - min.y) + min.y
        )

    private fun addParticles(amount: Int) {
        for (i in 0 until amount) {
            addParticle()
        }
    }

    private fun addParticle() {
        particles.add(
            Particle(
                resource = particleResources[particles.size % particleResources.size],
                color = Color.White,
                startPosition = Offset(
                    x = 0f,
                    y = 0f
                ),
                velocity = generateRandomOffset(min = Offset(10f, 10f), max = Offset(50f, 50f)),
                rotation = ParticleRotation(
                    speed = 0f,
                    initial = 0f,
                ),
                lifeSpan = 10f,
            )
        )
    }

    fun update(deltaTime: Float) {
        var deaths = 0
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.update(deltaTime)
            if (!particle.alive) {
                iterator.remove()
                deaths++
            }
        }

        addParticles(deaths)
    }

    fun draw(drawScope: DrawScope) {
        particles.forEach { particle ->
            particle.draw(drawScope)
        }
    }

    fun addSingleParticle(particle: Particle) {
        particles.add(particle)
    }
}

@Composable
fun rememberParticleSystemState(
    maxParticles: Int = 10,
    particleResources: List<ParticleResource> = listOf(),
): ParticleSystemState {
    return remember(maxParticles, particleResources) {
        ParticleSystemState(
            maxParticles = maxParticles,
            particleResources = particleResources
        )
    }
}