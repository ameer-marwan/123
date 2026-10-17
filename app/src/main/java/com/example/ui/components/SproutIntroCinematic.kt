package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SproutIntroCinematic(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0f to 1f animation sequence over 7.5 seconds
    val introProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        introProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
        )
        onFinished()
    }

    val progress = introProgress.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040A07))
            .clickable { onFinished() }
            .testTag("sprout_intro_cinematic")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCinematicSequence(progress)
        }

        // Top bar with skip button and narrative caption
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp)
        ) {
            val phaseLabel = when {
                progress < 0.25f -> "STAGE 1: Radicle Germination in Stratified Soil"
                progress < 0.50f -> "STAGE 2: Root Network Expansion & Biomass Emergence"
                progress < 0.75f -> "STAGE 3: Cellular Chloroplast Activation & Bio-Nodes"
                else -> "STAGE 4: Planetary Orbital Synthesis (NASA & Google)"
            }

            Text(
                text = phaseLabel,
                color = Color(0xFF69F0AE),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(Color(0xCC081A10), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            ElevatedButton(
                onClick = onFinished,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0xDD112D1E),
                    contentColor = Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .testTag("skip_intro_button")
            ) {
                Icon(Icons.Default.FastForward, contentDescription = "Skip Intro", modifier = Modifier.width(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Skip Intro", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom progress bar and title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 24.dp, end = 24.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = Color(0xFF00E676),
                trackColor = Color(0xFF1E3A2B)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF69F0AE))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FieldShift • Orbital Earth & Soil Regeneration",
                    color = Color(0xFFC8E6C9),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun DrawScope.drawCinematicSequence(progress: Float) {
    val w = size.width
    val h = size.height
    val soilHorizonY = h * 0.58f

    if (progress < 0.70f) {
        // --- STAGE 1 & 2: Soil Stratification & Sprouting Plant ---

        // Stratified Soil Gradient
        val soilBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF2A1C12), // Humus topsoil
                Color(0xFF3E2723), // Subsoil
                Color(0xFF1B110B), // Bedrock base
                Color(0xFF0D0805)
            ),
            startY = soilHorizonY,
            endY = h
        )
        drawRect(soilBrush, topLeft = Offset(0f, soilHorizonY), size = Size(w, h - soilHorizonY))

        // Atmosphere / Sky gradient
        val skyBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF030A06),
                Color(0xFF091F14),
                Color(0xFF0F3320)
            ),
            startY = 0f,
            endY = soilHorizonY
        )
        drawRect(skyBrush, topLeft = Offset.Zero, size = Size(w, soilHorizonY))

        // Soil Horizon Line
        drawLine(
            color = Color(0xFF4CAF50),
            start = Offset(0f, soilHorizonY),
            end = Offset(w, soilHorizonY),
            strokeWidth = 3f
        )

        val seedX = w * 0.5f
        val seedY = soilHorizonY + 40f

        // Seed Core
        val seedGlow = Brush.radialGradient(
            colors = listOf(Color(0xFF76FF03), Color(0xFF2E7D32), Color.Transparent),
            center = Offset(seedX, seedY),
            radius = 28f
        )
        drawCircle(seedGlow, radius = 24f, center = Offset(seedX, seedY))

        // Roots branching downward
        val rootProgress = (progress / 0.50f).coerceIn(0f, 1f)
        val rootColor = Color(0xFFC8E6C9)

        fun drawBranch(startX: Float, startY: Float, endX: Float, endY: Float, p: Float) {
            val currX = startX + (endX - startX) * p
            val currY = startY + (endY - startY) * p
            drawLine(
                color = rootColor,
                start = Offset(startX, startY),
                end = Offset(currX, currY),
                strokeWidth = 3.5f * (1f - p * 0.5f),
                cap = StrokeCap.Round
            )
            // Bioluminescent root node tip
            if (p > 0.05f) {
                drawCircle(Color(0xFF00E676), radius = 3.5f, center = Offset(currX, currY))
            }
        }

        // Primary taproot
        drawBranch(seedX, seedY + 8f, seedX, seedY + 180f, rootProgress)
        // Lateral roots
        drawBranch(seedX, seedY + 50f, seedX - 90f, seedY + 130f, (rootProgress - 0.2f).coerceIn(0f, 1f))
        drawBranch(seedX, seedY + 80f, seedX + 110f, seedY + 160f, (rootProgress - 0.3f).coerceIn(0f, 1f))
        drawBranch(seedX - 45f, seedY + 90f, seedX - 130f, seedY + 220f, (rootProgress - 0.4f).coerceIn(0f, 1f))
        drawBranch(seedX + 55f, seedY + 120f, seedX + 120f, seedY + 250f, (rootProgress - 0.45f).coerceIn(0f, 1f))

        // Stem & Foliage growing upward
        val stemProgress = ((progress - 0.15f) / 0.55f).coerceIn(0f, 1f)
        if (stemProgress > 0f) {
            val stemHeight = 240f * stemProgress
            val stemTopY = soilHorizonY - stemHeight

            // Curved biological stem
            val stemPath = Path().apply {
                moveTo(seedX, soilHorizonY)
                cubicTo(
                    seedX - 15f, soilHorizonY - stemHeight * 0.4f,
                    seedX + 15f, soilHorizonY - stemHeight * 0.7f,
                    seedX, stemTopY
                )
            }
            drawPath(
                path = stemPath,
                brush = Brush.verticalGradient(listOf(Color(0xFFB9F6CA), Color(0xFF00E676), Color(0xFF1B5E20))),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // Leaves uncurling
            if (stemProgress > 0.4f) {
                val leafProgress = ((stemProgress - 0.4f) / 0.6f).coerceIn(0f, 1f)
                val leafSize = 75f * leafProgress

                // Left Leaf
                val leftLeafCenter = Offset(seedX - leafSize * 0.9f, stemTopY + 30f)
                val leftPath = Path().apply {
                    moveTo(seedX, stemTopY + 40f)
                    quadraticTo(leftLeafCenter.x - 20f, leftLeafCenter.y - 40f, leftLeafCenter.x - leafSize, leftLeafCenter.y)
                    quadraticTo(leftLeafCenter.x - 10f, leftLeafCenter.y + 40f, seedX, stemTopY + 40f)
                }
                drawPath(leftPath, Brush.radialGradient(listOf(Color(0xFF69F0AE), Color(0xFF1B5E20)), center = leftLeafCenter, radius = leafSize))
                drawPath(leftPath, Color(0xFF00E676), style = Stroke(width = 2f))

                // Right Leaf
                val rightLeafCenter = Offset(seedX + leafSize * 0.9f, stemTopY + 20f)
                val rightPath = Path().apply {
                    moveTo(seedX, stemTopY + 35f)
                    quadraticTo(rightLeafCenter.x + 20f, rightLeafCenter.y - 40f, rightLeafCenter.x + leafSize, rightLeafCenter.y)
                    quadraticTo(rightLeafCenter.x + 10f, rightLeafCenter.y + 40f, seedX, stemTopY + 35f)
                }
                drawPath(rightPath, Brush.radialGradient(listOf(Color(0xFF69F0AE), Color(0xFF1B5E20)), center = rightLeafCenter, radius = leafSize))
                drawPath(rightPath, Color(0xFF00E676), style = Stroke(width = 2f))

                // Glowing Data-Stream Nodes orbiting the leaves
                val orbitAngle = progress * 14f
                val orbitRadius = 90f * leafProgress
                for (i in 0..4) {
                    val a = orbitAngle + i * (3.14159f * 2f / 5f)
                    val nx = seedX + cos(a) * orbitRadius
                    val ny = (stemTopY + 25f) + sin(a) * (orbitRadius * 0.45f)
                    drawCircle(Color(0xFF29B6F6), radius = 4f, center = Offset(nx, ny))
                    drawCircle(Color(0x6600E676), radius = 8f, center = Offset(nx, ny))
                }
            }
        }
    } else {
        // --- STAGE 3 & 4: Seamless Zoom into Leaf Cell Structure & Transformation into Earth Globe ---
        val globeProgress = ((progress - 0.70f) / 0.30f).coerceIn(0f, 1f)

        // Deep cosmos background
        drawRect(Color(0xFF020905), size = size)

        val globeCenterX = w * 0.5f
        val globeCenterY = h * 0.48f
        val globeRadius = (w * 0.38f) * (0.8f + globeProgress * 0.2f)

        // Atmospheric glow behind globe
        val atmosGlow = Brush.radialGradient(
            colors = listOf(Color(0x8829B6F6), Color(0x3300E676), Color.Transparent),
            center = Offset(globeCenterX, globeCenterY),
            radius = globeRadius * 1.35f
        )
        drawCircle(atmosGlow, radius = globeRadius * 1.35f, center = Offset(globeCenterX, globeCenterY))

        // Globe ocean sphere
        val oceanBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF1B4E6B), Color(0xFF0D283A), Color(0xFF05111B)),
            center = Offset(globeCenterX - globeRadius * 0.3f, globeCenterY - globeRadius * 0.3f),
            radius = globeRadius * 1.1f
        )
        drawCircle(oceanBrush, radius = globeRadius, center = Offset(globeCenterX, globeCenterY))

        // Continents and agricultural green zones on globe
        val spinAngle = globeProgress * 3.5f
        for (lonIdx in -3..3) {
            val cx = globeCenterX + (lonIdx * 45f + sin(spinAngle + lonIdx) * 20f)
            if (abs(cx - globeCenterX) < globeRadius * 0.85f) {
                drawCircle(
                    color = Color(0x992E7D32),
                    radius = globeRadius * 0.28f,
                    center = Offset(cx, globeCenterY + cos(spinAngle + lonIdx) * 35f)
                )
                // NDVI high vegetation hotspot
                drawCircle(
                    color = Color(0xDD00E676),
                    radius = globeRadius * 0.12f,
                    center = Offset(cx, globeCenterY + cos(spinAngle + lonIdx) * 35f)
                )
            }
        }

        // Latitude / Longitude coordinate rings
        drawCircle(
            color = Color(0x4480CBC4),
            radius = globeRadius,
            center = Offset(globeCenterX, globeCenterY),
            style = Stroke(width = 2.5f)
        )
        drawLine(
            color = Color(0x3380CBC4),
            start = Offset(globeCenterX - globeRadius, globeCenterY),
            end = Offset(globeCenterX + globeRadius, globeCenterY),
            strokeWidth = 1.5f
        )

        // Orbital Satellite Tracking Vector
        val satAngle = spinAngle * 2.2f
        val satX = globeCenterX + cos(satAngle) * (globeRadius * 1.25f)
        val satY = globeCenterY + sin(satAngle) * (globeRadius * 0.55f)
        // Orbit ring
        drawOval(
            color = Color(0x5500E676),
            topLeft = Offset(globeCenterX - globeRadius * 1.25f, globeCenterY - globeRadius * 0.55f),
            size = Size(globeRadius * 2.5f, globeRadius * 1.1f),
            style = Stroke(width = 1.5f)
        )
        // Satellite beacon
        drawCircle(Color(0xFFFFEA00), radius = 5.5f, center = Offset(satX, satY))
        drawCircle(Color(0x66FFEA00), radius = 12f, center = Offset(satX, satY))

        // Data telemetry burst line from satellite to field
        drawLine(
            color = Color(0x9929B6F6),
            start = Offset(satX, satY),
            end = Offset(globeCenterX + 25f, globeCenterY - 15f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}
