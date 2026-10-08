package com.healthorbit.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Exact Hex Constants from your CSS styles
val HoTealRing = Color(0xFF0D7A6E)       // .ho-foot-ring-1
val HoCoralRing = Color(0xFFE07A68)      // .ho-foot-ring-2
val HoNucleus = Color(0xFF0B5E55)        // .ho-foot-nucleus
val HoMintGlow = Color(0xFF42B89D)       // box-shadow: 0 0 10px #42b89d

@Composable
fun HealthOrbitAnimatedLogo(
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HealthOrbitSpin")

    // hoFootSpinSlow 12s linear infinite
    val ring1Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring1SlowClockwise"
    )

    // hoFootSpinFast 8s linear infinite reverse
    val ring2Rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring2FastCounterClockwise"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val centerOffset = center
            val totalRadius = this.size.minDimension / 2f

            // Ratio calculation based on CSS (34px box, 8px nucleus, -4px outer inset)
            val nucleusRadius = totalRadius * 0.24f
            val ring1Radius = totalRadius * 0.68f
            val ring2Radius = totalRadius * 0.88f
            val strokeW = totalRadius * 0.085f

            // 1. Nucleus Aura Shadow (box-shadow: 0 0 10px #42b89d)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HoMintGlow.copy(alpha = 0.55f),
                        HoMintGlow.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = nucleusRadius * 2.8f
                ),
                radius = nucleusRadius * 2.8f,
                center = centerOffset
            )

            // 2. Solid Nucleus (#0b5e55)
            drawCircle(
                color = HoNucleus,
                radius = nucleusRadius,
                center = centerOffset
            )

            // 3. Ring 1 (.ho-foot-ring-1: #0d7a6e dashed, 12s rotation)
            rotate(degrees = ring1Rotation, pivot = centerOffset) {
                drawCircle(
                    color = HoTealRing,
                    radius = ring1Radius,
                    center = centerOffset,
                    style = Stroke(
                        width = strokeW,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(totalRadius * 0.22f, totalRadius * 0.14f),
                            phase = 0f
                        )
                    )
                )
            }

            // 4. Ring 2 (.ho-foot-ring-2: #e07a68 dashed, 8s reverse rotation)
            rotate(degrees = ring2Rotation, pivot = centerOffset) {
                drawCircle(
                    color = HoCoralRing,
                    radius = ring2Radius,
                    center = centerOffset,
                    style = Stroke(
                        width = strokeW,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(totalRadius * 0.24f, totalRadius * 0.16f),
                            phase = 0f
                        )
                    )
                )
            }
        }
    }
}