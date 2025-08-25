package com.example.composepractice.ui.project.particlesystem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.example.composepractice.R
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
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

data class ParticleVelocity(
    val acceleration: Offset,
    val initial: Offset,
    val minimum: Offset,
    val maximum: Offset
)

data class ParticleRotation(
    val speed: Float,
    val initial: Float,
    val minimum: Float,
    val maximum: Float
)

/**
 * @param lifeSpan Duration a particle will live in seconds
 */
class Particle(
    val resource: ParticleResource,
    val color: Color,
    val startPosition: Offset,
    val velocity: ParticleVelocity,
    val rotation: ParticleRotation,
    val lifeSpan: Float,
    var alive: Boolean = true,
) {
    private val _invalidateTrigger = mutableIntStateOf(0)
    private var currentPosition = startPosition
    private var currentRotation = rotation.initial
    private var currentAcceleration = velocity.initial
    private var currentLifeSpan = lifeSpan
    private var currentVelocity = velocity.initial

    fun update(deltaTime: Float) {
        currentPosition = currentPosition.copy(
            x = currentPosition.x + currentVelocity.x * deltaTime,
            y = currentPosition.y + currentVelocity.y * deltaTime
        )
        currentRotation += rotation.speed
        currentVelocity += currentAcceleration
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
                velocity = ParticleVelocity(
                    acceleration = Offset(0f, 0f),
                    initial = generateRandomOffset(min = Offset(.1f, .1f), max = Offset(.9f, .9f)),
                    minimum = Offset(.1f, .1f),
                    maximum = Offset(1f, 1f)
                ),
                rotation = ParticleRotation(
                    speed = 0f,
                    initial = 0f,
                    minimum = 0f,
                    maximum = 0f
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