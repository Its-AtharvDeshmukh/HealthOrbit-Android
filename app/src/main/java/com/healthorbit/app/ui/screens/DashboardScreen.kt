package com.healthorbit.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.healthorbit.app.ui.DashboardViewModel
import com.healthorbit.app.ui.SyncUiState
import com.healthorbit.app.ui.components.HealthOrbitAnimatedLogo

private const val HEALTHORBIT_WEBSITE_URL = "https://healthorbit.onrender.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    onNavigateToMetric: (String) -> Unit
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()
    val lastSync by viewModel.lastSyncFormatted.collectAsState()
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()

    val steps by viewModel.steps.collectAsState()
    val heartRate by viewModel.heartRate.collectAsState()
    val bloodPressure by viewModel.bloodPressure.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val distanceKm by viewModel.distanceKm.collectAsState()
    val spo2 by viewModel.spo2.collectAsState()
    val sleepDuration by viewModel.sleepDuration.collectAsState()
    val deepSleep by viewModel.deepSleep.collectAsState()
    val bodyTemp by viewModel.bodyTemp.collectAsState()
    val stressHrv by viewModel.stressHrv.collectAsState()

    var animatedContentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animatedContentVisible = true
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(viewModel.getRequiredPermissions())) {
            viewModel.onPermissionsGranted()
        } else {
            viewModel.onPermissionsDenied()
        }
    }

    LaunchedEffect(syncState) {
        when (syncState) {
            is SyncUiState.Success -> {
                val s = syncState as SyncUiState.Success
                Toast.makeText(context, "Telemetry Updated (${s.inserted} records)", Toast.LENGTH_SHORT).show()
            }
            is SyncUiState.Error -> {
                val e = syncState as SyncUiState.Error
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    fun openWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(HEALTHORBIT_WEBSITE_URL))
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dynamic Animated Multi-Ring Orbit Logo
                        HealthOrbitAnimatedLogo(size = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HealthOrbit",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Apple Health & Connect Companion",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { openWebsite() }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Website",
                            tint = Color(0xFF0F766E)
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = animatedContentVisible,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 60 },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        ) {
            // LazyVerticalGrid as root scrollable container resolves scrolling
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
            ) {
                // 1. Triple Activity Rings Hero Banner
                item(span = { GridItemSpan(2) }) {
                    ActivityRingHeroCard(
                        steps = steps,
                        calories = calories,
                        distance = distanceKm,
                        lastSync = lastSync,
                        syncState = syncState,
                        onSyncClick = {
                            if (!isPermissionGranted) {
                                permissionLauncher.launch(viewModel.getRequiredPermissions())
                            } else {
                                viewModel.syncNow()
                            }
                        }
                    )
                }

                // 2. Direct Website Banner Portal
                item(span = { GridItemSpan(2) }) {
                    WebPortalBanner(onOpenUrl = { openWebsite() })
                }

                // 3. Section Divider & Status Badge
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Telemetry Metrics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isPermissionGranted) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPermissionGranted) "Connect Linked" else "Permission Required",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPermissionGranted) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                    }
                }

                // 4. Biometric Telemetry Cards
                item {
                    ModernMetricCard(
                        title = "Steps",
                        value = steps,
                        unit = "steps",
                        icon = Icons.Default.DirectionsRun,
                        primaryColor = Color(0xFFFA5252),
                        surfaceTint = Color(0xFFFFF5F5),
                        sparklinePoints = listOf(0.2f, 0.4f, 0.35f, 0.7f, 0.55f, 0.9f)
                    ) { onNavigateToMetric("steps") }
                }

                item {
                    ModernMetricCard(
                        title = "Heart Rate",
                        value = heartRate,
                        unit = "BPM",
                        icon = Icons.Default.MonitorHeart,
                        primaryColor = Color(0xFFFF2A6D),
                        surfaceTint = Color(0xFFFFF0F5),
                        sparklinePoints = listOf(0.4f, 0.3f, 0.8f, 0.2f, 0.9f, 0.5f)
                    ) { onNavigateToMetric("heart_rate") }
                }

                item {
                    ModernMetricCard(
                        title = "Blood Pressure",
                        value = bloodPressure,
                        unit = "mmHg",
                        icon = Icons.Default.Speed,
                        primaryColor = Color(0xFF7950F2),
                        surfaceTint = Color(0xFFF8F0FC),
                        sparklinePoints = listOf(0.5f, 0.6f, 0.55f, 0.65f, 0.6f, 0.62f)
                    ) { onNavigateToMetric("blood_pressure_systolic") }
                }

                item {
                    ModernMetricCard(
                        title = "Blood Oxygen",
                        value = spo2,
                        unit = "",
                        icon = Icons.Default.WaterDrop,
                        primaryColor = Color(0xFF228BE6),
                        surfaceTint = Color(0xFFE7F5FF),
                        sparklinePoints = listOf(0.85f, 0.9f, 0.88f, 0.94f, 0.92f, 0.96f)
                    ) { onNavigateToMetric("spo2") }
                }

                item {
                    ModernMetricCard(
                        title = "Sleep Total",
                        value = sleepDuration,
                        unit = "",
                        icon = Icons.Default.Bedtime,
                        primaryColor = Color(0xFF4C6EF5),
                        surfaceTint = Color(0xFFEDF2FF),
                        sparklinePoints = listOf(0.3f, 0.5f, 0.6f, 0.7f, 0.65f, 0.8f)
                    ) { onNavigateToMetric("sleep_duration") }
                }

                item {
                    ModernMetricCard(
                        title = "Deep Sleep",
                        value = deepSleep,
                        unit = "",
                        icon = Icons.Default.FitnessCenter,
                        primaryColor = Color(0xFF15AABF),
                        surfaceTint = Color(0xFFE3FAFC),
                        sparklinePoints = listOf(0.2f, 0.35f, 0.25f, 0.45f, 0.4f, 0.5f)
                    ) { onNavigateToMetric("deep_sleep") }
                }

                item {
                    ModernMetricCard(
                        title = "Active Energy",
                        value = calories,
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        primaryColor = Color(0xFFFD7E14),
                        surfaceTint = Color(0xFFFFF4E6),
                        sparklinePoints = listOf(0.1f, 0.3f, 0.45f, 0.6f, 0.75f, 0.85f)
                    ) { onNavigateToMetric("calories") }
                }

                item {
                    ModernMetricCard(
                        title = "Distance",
                        value = distanceKm,
                        unit = "",
                        icon = Icons.Default.DirectionsRun,
                        primaryColor = Color(0xFF12B886),
                        surfaceTint = Color(0xFFE6FCF5),
                        sparklinePoints = listOf(0.2f, 0.3f, 0.4f, 0.5f, 0.7f, 0.85f)
                    ) { onNavigateToMetric("distance") }
                }

                item {
                    ModernMetricCard(
                        title = "Body Temp",
                        value = bodyTemp,
                        unit = "",
                        icon = Icons.Default.Thermostat,
                        primaryColor = Color(0xFFE64980),
                        surfaceTint = Color(0xFFFFF0F6),
                        sparklinePoints = listOf(0.5f, 0.5f, 0.52f, 0.49f, 0.51f, 0.5f)
                    ) { onNavigateToMetric("body_temperature") }
                }

                item {
                    ModernMetricCard(
                        title = "Stress HRV",
                        value = stressHrv,
                        unit = "",
                        icon = Icons.Default.Security,
                        primaryColor = Color(0xFF82C91E),
                        surfaceTint = Color(0xFFF4FCE3),
                        sparklinePoints = listOf(0.6f, 0.45f, 0.55f, 0.35f, 0.65f, 0.5f)
                    ) { onNavigateToMetric("stress_hrv") }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: Apple Health Style Multi-Ring Hero Card
// -------------------------------------------------------------
@Composable
fun ActivityRingHeroCard(
    steps: String,
    calories: String,
    distance: String,
    lastSync: String,
    syncState: SyncUiState,
    onSyncClick: () -> Unit
) {
    val stepsFloat = (steps.toDoubleOrNull() ?: 0.0) / 10000.0
    val calFloat = (calories.toDoubleOrNull() ?: 0.0) / 600.0
    val distFloat = (distance.replace("km", "").trim().toDoubleOrNull() ?: 0.0) / 8.0

    val animatedSteps by animateFloatAsState(
        targetValue = stepsFloat.toFloat().coerceIn(0f, 1.3f),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "stepsRing"
    )
    val animatedCal by animateFloatAsState(
        targetValue = calFloat.toFloat().coerceIn(0f, 1.3f),
        animationSpec = tween(1200, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "calRing"
    )
    val animatedDist by animateFloatAsState(
        targetValue = distFloat.toFloat().coerceIn(0f, 1.3f),
        animationSpec = tween(1200, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "distRing"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x0A000000)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Activity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Last synced: $lastSync",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Button(
                    onClick = onSyncClick,
                    enabled = syncState !is SyncUiState.Syncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F766E),
                        disabledContainerColor = Color(0xFF0F766E).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    if (syncState is SyncUiState.Syncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Syncing", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Triple Concentric Ring Graphic
                Box(
                    modifier = Modifier.size(115.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 9.dp.toPx()

                        // Outer: Move/Calories
                        drawArc(
                            color = Color(0xFFFFD5D5),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeW, cap = StrokeCap.Round),
                            size = Size(size.width - strokeW, size.height - strokeW),
                            topLeft = Offset(strokeW / 2, strokeW / 2)
                        )
                        drawArc(
                            color = Color(0xFFFA5252),
                            startAngle = -90f,
                            sweepAngle = (animatedCal * 360f).coerceAtMost(360f),
                            useCenter = false,
                            style = Stroke(strokeW, cap = StrokeCap.Round),
                            size = Size(size.width - strokeW, size.height - strokeW),
                            topLeft = Offset(strokeW / 2, strokeW / 2)
                        )

                        // Middle: Steps
                        val inset1 = strokeW * 1.35f
                        drawArc(
                            color = Color(0xFFC3FAEF),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeW, cap = StrokeCap.Round),
                            size = Size(size.width - (inset1 * 2), size.height - (inset1 * 2)),
                            topLeft = Offset(inset1, inset1)
                        )
                        drawArc(
                            color = Color(0xFF12B886),
                            startAngle = -90f,
                            sweepAngle = (animatedSteps * 360f).coerceAtMost(360f),
                            useCenter = false,
                            style = Stroke(strokeW, cap = StrokeCap.Round),
                            size = Size(size.width - (inset1 * 2), size.height - (inset1 * 2)),
                            topLeft = Offset(inset1, inset1)
                        )

                        // Inner: Distance
                        val inset2 = strokeW * 2.7f
                        drawArc(
                            color = Color(0xFFD0EBFF),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeW, cap = StrokeCap.Round),
                            size = Size(size.width - (inset2 * 2), size.height - (inset2 * 2)),
                            topLeft = Offset(inset2, inset2)
                        )
                        drawArc(
                            color = Color(0xFF228BE6),
                            startAngle = -90f,
                            sweepAngle = (animatedDist * 360f).coerceAtMost(360f),
                            useCenter = false,
                            style = Stroke(strokeW, cap = StrokeCap.Round),
                            size = Size(size.width - (inset2 * 2), size.height - (inset2 * 2)),
                            topLeft = Offset(inset2, inset2)
                        )
                    }
                }

                // Legend labels
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendItem(color = Color(0xFFFA5252), label = "Move", value = "$calories kcal")
                    LegendItem(color = Color(0xFF12B886), label = "Steps", value = "$steps steps")
                    LegendItem(color = Color(0xFF228BE6), label = "Distance", value = "$distance")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: Modern Web Portal Banner Card
// -------------------------------------------------------------
@Composable
fun WebPortalBanner(onOpenUrl: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portalPulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenUrl() }
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF042F2E), Color(0xFF0F766E), Color(0xFF14B8A6))
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "HealthOrbit Cloud Portal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "healthorbit.onrender.com",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Visit Site",
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: Modern Metric Tile (Sparkline + Apple Health Tone)
// -------------------------------------------------------------
@Composable
fun ModernMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    primaryColor: Color,
    surfaceTint: Color,
    sparklinePoints: List<Float>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x08000000)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(surfaceTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Value + Unit (Formatted as strings safely)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Mini-Sparkline
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            ) {
                if (sparklinePoints.size >= 2) {
                    val path = Path()
                    val stepX = size.width / (sparklinePoints.size - 1)
                    sparklinePoints.forEachIndexed { index, p ->
                        val x = index * stepX
                        val y = size.height - (p * size.height)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}