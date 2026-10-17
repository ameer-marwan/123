package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// --- 1. Five-Axis Resilience Radar Chart ---
@Composable
fun ResilienceRadarChart(
    radarValues: Map<String, Float>, // Keys: Soil, Water, Profit, Biodiversity, Climate (0.0 to 1.0)
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("resilience_radar_chart"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Multi-Dimensional Farm Resilience Radar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Evaluates agro-ecological balance across 5 resilience vectors",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                drawRadar(radarValues)
            }
        }
    }
}

private fun DrawScope.drawRadar(radarValues: Map<String, Float>) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val maxRadius = size.width.coerceAtMost(size.height) * 0.40f

    val axes = listOf(
        "Soil Health" to (radarValues["Soil"] ?: 0.82f),
        "Water Saving" to (radarValues["Water"] ?: 0.74f),
        "Profit Stability" to (radarValues["Profit"] ?: 0.79f),
        "Biodiversity" to (radarValues["Biodiversity"] ?: 0.88f),
        "Climate Shield" to (radarValues["Climate"] ?: 0.85f)
    )

    val n = axes.size
    val angleStep = (3.14159f * 2f) / n

    // Concentric Web Rings (25%, 50%, 75%, 100%)
    for (level in 1..4) {
        val r = maxRadius * (level / 4f)
        val webPath = Path()
        for (i in 0 until n) {
            val a = -3.14159f / 2f + i * angleStep
            val x = cx + cos(a) * r
            val y = cy + sin(a) * r
            if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
        }
        webPath.close()
        drawPath(webPath, color = Color(0x334CAF50), style = Stroke(width = 1.2f))
    }

    // Spokes and labels
    for (i in 0 until n) {
        val a = -3.14159f / 2f + i * angleStep
        val spokeX = cx + cos(a) * maxRadius
        val spokeY = cy + sin(a) * maxRadius
        drawLine(Color(0x554CAF50), Offset(cx, cy), Offset(spokeX, spokeY), strokeWidth = 1.5f)

        // Draw Axis label text using native canvas
        val labelX = cx + cos(a) * (maxRadius + 24f)
        val labelY = cy + sin(a) * (maxRadius + 20f)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawText("${axes[i].first} ${(axes[i].second * 100).toInt()}%", labelX, labelY, paint)
        }
    }

    // Filled Value Polygon
    val polyPath = Path()
    for (i in 0 until n) {
        val a = -3.14159f / 2f + i * angleStep
        val v = axes[i].second.coerceIn(0.1f, 1.0f)
        val x = cx + cos(a) * (maxRadius * v)
        val y = cy + sin(a) * (maxRadius * v)
        if (i == 0) polyPath.moveTo(x, y) else polyPath.lineTo(x, y)
    }
    polyPath.close()

    drawPath(
        polyPath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0x9900E676), Color(0x5500B0FF)),
            center = Offset(cx, cy),
            radius = maxRadius
        )
    )
    drawPath(polyPath, color = Color(0xFF00E676), style = Stroke(width = 3.5f))

    // Dots at vertices
    for (i in 0 until n) {
        val a = -3.14159f / 2f + i * angleStep
        val v = axes[i].second.coerceIn(0.1f, 1.0f)
        val x = cx + cos(a) * (maxRadius * v)
        val y = cy + sin(a) * (maxRadius * v)
        drawCircle(Color(0xFF00E676), radius = 5.5f, center = Offset(x, y))
        drawCircle(Color.White, radius = 2.5f, center = Offset(x, y))
    }
}

// --- 2. Multi-Year Profit Comparison Bar Chart ---
@Composable
fun MultiYearProfitBarChart(
    netProfitsPerYear: List<Double>,
    grossPerYear: List<Double>,
    expensesPerYear: List<Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("profit_comparison_bar_chart"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "5-Year Financial Projections (Acre-Scaled)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Gross Revenue vs. Operating Expenses vs. Net Profit ($ USD)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = Color(0xFF29B6F6), label = "Gross")
                LegendItem(color = Color(0xFFFF7043), label = "Expenses")
                LegendItem(color = Color(0xFF00E676), label = "Net Margin")
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val w = size.width
                val h = size.height
                val padBottom = 30f
                val chartHeight = h - padBottom
                val maxVal = (grossPerYear.maxOrNull() ?: 350000.0).coerceAtLeast(100000.0).toFloat()
                val groupCount = netProfitsPerYear.size.coerceAtLeast(1)
                val groupWidth = w / groupCount

                for (i in 0 until groupCount) {
                    val gross = grossPerYear.getOrElse(i) { 240000.0 }.toFloat()
                    val exp = expensesPerYear.getOrElse(i) { 150000.0 }.toFloat()
                    val net = netProfitsPerYear.getOrElse(i) { 90000.0 }.toFloat()

                    val gx = i * groupWidth + groupWidth * 0.15f
                    val barW = groupWidth * 0.22f

                    // Gross bar
                    val grossH = (gross / maxVal) * chartHeight
                    drawRoundRect(
                        color = Color(0xFF29B6F6),
                        topLeft = Offset(gx, chartHeight - grossH),
                        size = Size(barW, grossH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    // Expense bar
                    val expH = (exp / maxVal) * chartHeight
                    drawRoundRect(
                        color = Color(0xFFFF7043),
                        topLeft = Offset(gx + barW + 4f, chartHeight - expH),
                        size = Size(barW, expH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    // Net bar
                    val netH = (net / maxVal) * chartHeight
                    drawRoundRect(
                        color = Color(0xFF00E676),
                        topLeft = Offset(gx + (barW + 4f) * 2f, chartHeight - netH),
                        size = Size(barW, netH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )

                    // Year label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText("Yr ${i + 1}", gx + barW * 1.5f + 4f, h - 4f, paint)
                    }
                }
            }
        }
    }
}

// --- 3. Soil Organic Matter & Health Recovery Trajectory Curve ---
@Composable
fun SoilTrajectoryChart(
    soilTrajectoryScores: List<Int>, // 5 years scores (e.g. [68, 73, 79, 85, 91])
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("soil_trajectory_chart"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Soil Health & Organic Matter Recovery Curve",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "5-Year biological regeneration under continuous bio-armor & no-till",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height
                val padY = 24f
                val usableH = h - padY * 2f
                val points = soilTrajectoryScores.ifEmpty { listOf(65, 71, 78, 84, 90) }
                val stepX = w / (points.size - 1).coerceAtLeast(1)

                val linePath = Path()
                val fillPath = Path()

                points.forEachIndexed { idx, score ->
                    val x = idx * stepX
                    // Map score 50..100 to y
                    val normalized = ((score - 50f) / 50f).coerceIn(0f, 1f)
                    val y = padY + (1f - normalized) * usableH

                    if (idx == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = (idx - 1) * stepX
                        val prevScore = points[idx - 1]
                        val prevNorm = ((prevScore - 50f) / 50f).coerceIn(0f, 1f)
                        val prevY = padY + (1f - prevNorm) * usableH

                        val midX = (prevX + x) / 2f
                        linePath.cubicTo(midX, prevY, midX, y, x, y)
                        fillPath.cubicTo(midX, prevY, midX, y, x, y)
                    }
                }
                fillPath.lineTo(w, h)
                fillPath.close()

                // Draw gradient under curve
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x6600E676), Color(0x0500E676)),
                        startY = 0f,
                        endY = h
                    )
                )

                // Draw glowing curve line
                drawPath(
                    linePath,
                    color = Color(0xFF00E676),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Draw node points and values
                points.forEachIndexed { idx, score ->
                    val x = idx * stepX
                    val normalized = ((score - 50f) / 50f).coerceIn(0f, 1f)
                    val y = padY + (1f - normalized) * usableH

                    drawCircle(Color(0xFF00E676), radius = 6f, center = Offset(x, y))
                    drawCircle(Color.White, radius = 3f, center = Offset(x, y))

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 26f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText("${score} pts", x, y - 14f, paint)
                    }
                }
            }
        }
    }
}

// --- 4. Seasonal Water Consumption Area Chart ---
@Composable
fun WaterAreaChart(
    baselineWaterMm: List<Double>,
    optimizedWaterMm: List<Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("water_area_chart"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Seasonal Water Footprint (mm/month)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Conventional Monoculture vs. FieldShift Regenerative Rotation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = Color(0xFFEF5350), label = "Conventional Monoculture")
                LegendItem(color = Color(0xFF29B6F6), label = "FieldShift Optimized")
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val w = size.width
                val h = size.height
                val months = listOf("Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct")
                val n = months.size
                val stepX = w / (n - 1)
                val maxMm = 160f

                // Draw baseline area
                val baseData = baselineWaterMm.ifEmpty { listOf(45.0, 75.0, 140.0, 155.0, 120.0, 80.0, 35.0) }
                val optData = optimizedWaterMm.ifEmpty { listOf(35.0, 55.0, 95.0, 105.0, 85.0, 50.0, 25.0) }

                fun drawCurve(data: List<Double>, lineColor: Color, fillColor: Color) {
                    val p = Path()
                    val f = Path()
                    data.forEachIndexed { i, mm ->
                        val x = i * stepX
                        val y = h - 25f - (mm.toFloat() / maxMm) * (h - 40f)
                        if (i == 0) {
                            p.moveTo(x, y)
                            f.moveTo(x, h - 25f)
                            f.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevMm = data[i - 1]
                            val prevY = h - 25f - (prevMm.toFloat() / maxMm) * (h - 40f)
                            val midX = (prevX + x) / 2f
                            p.cubicTo(midX, prevY, midX, y, x, y)
                            f.cubicTo(midX, prevY, midX, y, x, y)
                        }
                    }
                    f.lineTo(w, h - 25f)
                    f.close()
                    drawPath(f, fillColor)
                    drawPath(p, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
                }

                drawCurve(baseData, Color(0xFFEF5350), Color(0x33EF5350))
                drawCurve(optData, Color(0xFF29B6F6), Color(0x5529B6F6))

                // Month labels
                months.forEachIndexed { idx, m ->
                    val x = idx * stepX
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(m, x, h - 2f, paint)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
