package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FieldLocation
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MapProjection {
    GLOBE_3D,
    MAP_2D
}

enum class MapLayer {
    STANDARD,
    SATELLITE,
    NDVI_THERMAL
}

@Composable
fun GeospatialGlobeView(
    currentLocation: FieldLocation,
    projection: MapProjection,
    activeLayer: MapLayer,
    onCoordinateSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "earth_orbit")
    val orbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06110B))
            .testTag("geospatial_globe_view")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(projection, activeLayer) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        if (projection == MapProjection.GLOBE_3D) {
                            val cx = w / 2f
                            val cy = h / 2f
                            val r = w.coerceAtMost(h) * 0.40f
                            val dx = tapOffset.x - cx
                            val dy = tapOffset.y - cy
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist <= r) {
                                val lat = ((cy - tapOffset.y) / r * 85.0).coerceIn(-85.0, 85.0)
                                val lon = (dx / r * 180.0).coerceIn(-180.0, 180.0)
                                onCoordinateSelected(lat, lon)
                            }
                        } else {
                            val lon = ((tapOffset.x / w) * 360.0 - 180.0).coerceIn(-180.0, 180.0)
                            val lat = (90.0 - (tapOffset.y / h) * 180.0).coerceIn(-85.0, 85.0)
                            onCoordinateSelected(lat, lon)
                        }
                    }
                }
        ) {
            if (projection == MapProjection.GLOBE_3D) {
                draw3dGlobe(currentLocation, activeLayer, orbitPhase)
            } else {
                draw2dMap(currentLocation, activeLayer)
            }
        }

        // Selected Pin Telemetry Overlay Tag
        Surface(
            color = Color(0xCC091C13),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${currentLocation.name} • ${String.format("%.2f°N, %.2f°E", currentLocation.latitude, currentLocation.longitude)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun DrawScope.draw3dGlobe(
    location: FieldLocation,
    layer: MapLayer,
    orbitPhase: Float
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val radius = w.coerceAtMost(h) * 0.40f

    // Space / Atmosphere back glow
    val glowBrush = Brush.radialGradient(
        colors = listOf(
            when (layer) {
                MapLayer.NDVI_THERMAL -> Color(0x66FF5252)
                MapLayer.SATELLITE -> Color(0x6640C4FF)
                MapLayer.STANDARD -> Color(0x6669F0AE)
            },
            Color.Transparent
        ),
        center = Offset(cx, cy),
        radius = radius * 1.35f
    )
    drawCircle(glowBrush, radius = radius * 1.35f, center = Offset(cx, cy))

    // Sphere base fill
    val globeBaseBrush = Brush.radialGradient(
        colors = when (layer) {
            MapLayer.SATELLITE -> listOf(Color(0xFF1B4965), Color(0xFF0B2538), Color(0xFF030D14))
            MapLayer.NDVI_THERMAL -> listOf(Color(0xFF2C1810), Color(0xFF190C07), Color(0xFF080402))
            MapLayer.STANDARD -> listOf(Color(0xFF1A3326), Color(0xFF0D1E16), Color(0xFF050D09))
        },
        center = Offset(cx - radius * 0.35f, cy - radius * 0.35f),
        radius = radius * 1.2f
    )
    drawCircle(globeBaseBrush, radius = radius, center = Offset(cx, cy))

    // Graticule: Latitude & Longitude lines
    for (latDeg in listOf(-60f, -30f, 0f, 30f, 60f)) {
        val yOffset = radius * sin(Math.toRadians(latDeg.toDouble())).toFloat()
        val rAtLat = radius * cos(Math.toRadians(latDeg.toDouble())).toFloat()
        drawOval(
            color = Color(0x33A7F3D0),
            topLeft = Offset(cx - rAtLat, cy - yOffset - rAtLat * 0.22f),
            size = Size(rAtLat * 2f, rAtLat * 0.44f),
            style = Stroke(width = 1f)
        )
    }

    // Continents & Agricultural Hubs
    val continents = listOf(
        Pair(0.1f, -0.2f), Pair(-0.35f, 0.15f), Pair(0.45f, 0.25f),
        Pair(-0.2f, -0.4f), Pair(0.25f, -0.45f), Pair(-0.5f, -0.1f)
    )
    continents.forEach { (nx, ny) ->
        val continentColor = when (layer) {
            MapLayer.SATELLITE -> Color(0xFF2E7D32)
            MapLayer.NDVI_THERMAL -> Color(0xFFFFB300)
            MapLayer.STANDARD -> Color(0xFF388E3C)
        }
        drawCircle(
            color = continentColor,
            radius = radius * 0.24f,
            center = Offset(cx + nx * radius, cy + ny * radius)
        )
        // High NDVI or thermal hotspot inside
        val hotspotColor = when (layer) {
            MapLayer.NDVI_THERMAL -> Color(0xFFFF3D00)
            MapLayer.SATELLITE -> Color(0xFF00E676)
            MapLayer.STANDARD -> Color(0xFF81C784)
        }
        drawCircle(
            color = hotspotColor,
            radius = radius * 0.12f,
            center = Offset(cx + nx * radius, cy + ny * radius)
        )
    }

    // Outer spherical rim border
    drawCircle(
        color = Color(0x8869F0AE),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 2.5f)
    )

    // NASA Satellite orbital ring
    val orbitMajor = radius * 1.35f
    val orbitMinor = radius * 0.65f
    drawOval(
        color = Color(0x4429B6F6),
        topLeft = Offset(cx - orbitMajor, cy - orbitMinor),
        size = Size(orbitMajor * 2f, orbitMinor * 2f),
        style = Stroke(width = 1.5f)
    )

    // Orbiting NASA MODIS/SMAP satellite
    val satX = cx + cos(orbitPhase) * orbitMajor
    val satY = cy + sin(orbitPhase) * orbitMinor
    drawCircle(Color(0xFFFFD54F), radius = 6f, center = Offset(satX, satY))
    drawCircle(Color(0x66FFD54F), radius = 14f, center = Offset(satX, satY))

    // Interactive Field Pin on Globe
    val pinLatRad = Math.toRadians(location.latitude)
    val pinLonRad = Math.toRadians(location.longitude)
    val pinX = cx + (sin(pinLonRad) * cos(pinLatRad) * radius * 0.85).toFloat()
    val pinY = cy - (sin(pinLatRad) * radius * 0.85).toFloat()

    // Pin marker beacon
    drawCircle(Color(0xFFFF1744), radius = 8f, center = Offset(pinX, pinY))
    drawCircle(Color.White, radius = 3.5f, center = Offset(pinX, pinY))
    drawCircle(Color(0x55FF1744), radius = 18f, center = Offset(pinX, pinY))

    // Laser telemetry ray linking satellite to field
    drawLine(
        color = Color(0x8800E676),
        start = Offset(satX, satY),
        end = Offset(pinX, pinY),
        strokeWidth = 1.8f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.draw2dMap(
    location: FieldLocation,
    layer: MapLayer
) {
    val w = size.width
    val h = size.height

    // Background 2D grid
    val bgBrush = when (layer) {
        MapLayer.SATELLITE -> Brush.verticalGradient(listOf(Color(0xFF0A1821), Color(0xFF030A0E)))
        MapLayer.NDVI_THERMAL -> Brush.verticalGradient(listOf(Color(0xFF1E0E08), Color(0xFF0A0402)))
        MapLayer.STANDARD -> Brush.verticalGradient(listOf(Color(0xFF081810), Color(0xFF030C07)))
    }
    drawRect(bgBrush, size = size)

    // Grid coordinates
    for (i in 1..7) {
        val x = (w / 8f) * i
        drawLine(Color(0x2269F0AE), Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
    }
    for (i in 1..5) {
        val y = (h / 6f) * i
        drawLine(Color(0x2269F0AE), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
    }

    // Stylized continent outlines
    val continentColor = when (layer) {
        MapLayer.SATELLITE -> Color(0xFF1E5235)
        MapLayer.NDVI_THERMAL -> Color(0xFFD84315)
        MapLayer.STANDARD -> Color(0xFF2E7D32)
    }

    // North America / South America
    drawOval(continentColor, topLeft = Offset(w * 0.12f, h * 0.18f), size = Size(w * 0.22f, h * 0.32f))
    drawOval(continentColor, topLeft = Offset(w * 0.22f, h * 0.52f), size = Size(w * 0.16f, h * 0.38f))

    // Europe / Africa
    drawOval(continentColor, topLeft = Offset(w * 0.44f, h * 0.16f), size = Size(w * 0.16f, h * 0.22f))
    drawOval(continentColor, topLeft = Offset(w * 0.46f, h * 0.38f), size = Size(w * 0.18f, h * 0.42f))

    // Asia / Australia
    drawOval(continentColor, topLeft = Offset(w * 0.62f, h * 0.12f), size = Size(w * 0.32f, h * 0.42f))
    drawOval(continentColor, topLeft = Offset(w * 0.74f, h * 0.62f), size = Size(w * 0.16f, h * 0.22f))

    // Pin on 2D map
    val pinX = (((location.longitude + 180.0) / 360.0) * w).toFloat()
    val pinY = (((90.0 - location.latitude) / 180.0) * h).toFloat()

    drawCircle(Color(0xFFFF1744), radius = 9f, center = Offset(pinX, pinY))
    drawCircle(Color.White, radius = 4f, center = Offset(pinX, pinY))
    drawCircle(Color(0x66FF1744), radius = 22f, center = Offset(pinX, pinY))
}
