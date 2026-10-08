package com.healthorbit.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthorbit.app.ui.theme.*

@Composable
fun HealthOrbitAmbientBackground(
    content: @Composable BoxScope.() -> Unit
) {
    // Ambient breathing animations for background blurred orbs
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_orbs")
    val orbOffset1 by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val orbOffset2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HoBgAiry)
    ) {
        // Floating ambient orbs
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-60 + orbOffset1).dp)
                .size(340.dp)
                .clip(CircleShape)
                .background(HoOrbMint)
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = (80 + orbOffset2).dp)
                .size(380.dp)
                .clip(CircleShape)
                .background(HoOrbSage)
                .blur(90.dp)
        )

        content()
    }
}

@Composable
fun HealthOrbitGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x1A0B5E55),
                ambientColor = Color(0x0D0B5E55)
            )
            .border(1.dp, HoGlassBorder, RoundedCornerShape(26.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = HoGlassCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun LivePulsingDot(color: Color = HcGreen) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}