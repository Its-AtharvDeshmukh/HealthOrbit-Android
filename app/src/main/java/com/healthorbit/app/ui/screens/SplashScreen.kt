package com.healthorbit.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthorbit.app.data.local.SessionManager
import com.healthorbit.app.data.repository.AuthRepository
import com.healthorbit.app.ui.components.HealthOrbitAnimatedLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    sessionManager: SessionManager,
    authRepository: AuthRepository? = null,
    onSessionValid: () -> Unit,
    onSessionInvalid: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1200)
        val token = sessionManager.getToken()
        if (!token.isNullOrBlank()) {
            onSessionValid()
        } else {
            onSessionInvalid()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FBF9)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HealthOrbitAnimatedLogo(size = 90.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Matches .ho-footer-brand-title: Health in #0B5E55, Orbit in #E07A68
            Row {
                Text(
                    text = "Health",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B5E55),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Orbit",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE07A68),
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Health. One Orbit.",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4E6B63)
            )
        }
    }
}