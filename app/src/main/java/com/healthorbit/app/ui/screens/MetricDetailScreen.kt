package com.healthorbit.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthorbit.app.data.repository.WearableRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailScreen(
    metricType: String,
    wearableRepository: WearableRepository,
    onBack: () -> Unit
) {
    val metricTitle = remember(metricType) {
        metricType.replace("_", " ").split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(metricTitle, fontWeight = FontWeight.Bold, fontSize = 19.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Daily Trend Analysis", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Activity Intensity", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(18.dp))

                    // Dynamic bar chart simulation
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val barValues = listOf(0.4f, 0.65f, 0.35f, 0.85f, 0.55f, 0.95f, 0.7f)
                        val barWidth = 24.dp.toPx()
                        val spacing = (size.width - (barValues.size * barWidth)) / (barValues.size - 1)

                        barValues.forEachIndexed { i, factor ->
                            val x = i * (barWidth + spacing)
                            val barHeight = size.height * factor
                            val y = size.height - barHeight

                            drawRoundRect(
                                color = if (i == barValues.lastIndex) Color(0xFF0F766E) else Color(0xFFCCFBF1),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}