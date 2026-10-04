package com.proteintracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val originX: Float,
    val originY: Float,
    val angle: Float,
    val speed: Float,
    val size: Float,
    val rotation: Float,
    val colorIndex: Int,
    val round: Boolean,
)

/**
 * One-shot confetti burst for the celebration dialog. Deterministic (fixed seed)
 * so the composition does not flicker on recomposition.
 */
@Composable
fun ConfettiBurst(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    particleCount: Int = 70,
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1900, easing = LinearOutSlowInEasing))
    }

    val particles = remember(particleCount) {
        val random = Random(7)
        List(particleCount) {
            Particle(
                originX = 0.15f + random.nextFloat() * 0.7f,
                originY = 0.42f + random.nextFloat() * 0.16f,
                angle = (-Math.PI * 0.9).toFloat() + random.nextFloat() * (Math.PI * 0.8).toFloat(),
                speed = 0.28f + random.nextFloat() * 0.42f,
                size = 7f + random.nextFloat() * 11f,
                rotation = random.nextFloat() * 360f,
                colorIndex = random.nextInt(maxOf(colors.size, 1)),
                round = random.nextFloat() > 0.45f,
            )
        }
    }

    Canvas(modifier = modifier) {
        val t = progress.value
        val distance = size.minDimension * 0.62f
        val fade = if (t < 0.65f) 1f else (1f - (t - 0.65f) / 0.35f).coerceIn(0f, 1f)

        particles.forEach { particle ->
            val travelled = distance * particle.speed * t
            val gravity = size.height * 0.42f * t * t
            val x = size.width * particle.originX + cos(particle.angle) * travelled
            val y = size.height * particle.originY + sin(particle.angle) * travelled + gravity
            val color = colors[particle.colorIndex % colors.size].copy(alpha = fade)

            rotate(degrees = particle.rotation * t, pivot = Offset(x, y)) {
                if (particle.round) {
                    drawCircle(
                        color = color,
                        radius = particle.size / 2f,
                        center = Offset(x, y),
                    )
                } else {
                    drawRect(
                        color = color,
                        topLeft = Offset(x - particle.size / 2f, y - particle.size / 3f),
                        size = Size(particle.size, particle.size * 0.66f),
                    )
                }
            }
        }
    }
}
