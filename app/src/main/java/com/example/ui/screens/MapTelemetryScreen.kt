package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgriculturalHubs
import com.example.data.model.FieldLocation
import com.example.data.model.NasaTelemetry
import com.example.ui.components.GeospatialGlobeView
import com.example.ui.components.MapLayer
import com.example.ui.components.MapProjection
import com.example.ui.localization.LocalizationProvider
import com.example.viewmodel.FieldShiftUiState
import com.example.viewmodel.FieldShiftViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapTelemetryScreen(
    viewModel: FieldShiftViewModel,
    uiState: FieldShiftUiState,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(uiState.activeLanguage)
    var showHubDropdown by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("map_telemetry_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Location Search & GPS Bar ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { searchInput = it },
                            placeholder = { Text(strings.searchRegion, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("region_search_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                // GPS Locator action (Simulates current farm coordinates or samples California)
                                viewModel.selectLocation(
                                    AgriculturalHubs.PRESETS.firstOrNull { it.id == "california" }
                                        ?: AgriculturalHubs.PRESETS.first()
                                )
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .size(48.dp)
                                .testTag("gps_location_button")
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = strings.useGps, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Hubs selector button
                    Box {
                        Surface(
                            onClick = { showHubDropdown = true },
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Parcel: ${uiState.currentFarmName} • ${uiState.selectedLocation.name}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("▼ Change", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        DropdownMenu(
                            expanded = showHubDropdown,
                            onDismissRequest = { showHubDropdown = false }
                        ) {
                            AgriculturalHubs.PRESETS.forEach { hub ->
                                DropdownMenuItem(
                                    text = { Text("${hub.name} (${hub.country})") },
                                    onClick = {
                                        viewModel.selectLocation(hub)
                                        showHubDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Hybrid 3D Globe / 2D Map Canvas ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Projection & Layer Switchers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 3D vs 2D
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = uiState.mapProjection == MapProjection.GLOBE_3D,
                                onClick = { viewModel.setMapProjection(MapProjection.GLOBE_3D) },
                                label = { Text(strings.view3dGlobe, fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                            FilterChip(
                                selected = uiState.mapProjection == MapProjection.MAP_2D,
                                onClick = { viewModel.setMapProjection(MapProjection.MAP_2D) },
                                label = { Text(strings.view2dMap, fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }

                        IconButton(
                            onClick = { viewModel.refreshTelemetry() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (uiState.isLoadingTelemetry) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Telemetry")
                            }
                        }
                    }

                    // Layer Chips (Standard, Satellite, NDVI/Thermal)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.mapLayer == MapLayer.NDVI_THERMAL,
                            onClick = { viewModel.setMapLayer(MapLayer.NDVI_THERMAL) },
                            label = { Text(strings.layerNdvi, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFE65100))
                        )
                        FilterChip(
                            selected = uiState.mapLayer == MapLayer.SATELLITE,
                            onClick = { viewModel.setMapLayer(MapLayer.SATELLITE) },
                            label = { Text(strings.layerSatellite, fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = uiState.mapLayer == MapLayer.STANDARD,
                            onClick = { viewModel.setMapLayer(MapLayer.STANDARD) },
                            label = { Text(strings.layerStandard, fontSize = 11.sp) }
                        )
                    }

                    // Canvas Globe / Map View (Tap anywhere to pin coordinates)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                    ) {
                        GeospatialGlobeView(
                            currentLocation = uiState.selectedLocation,
                            projection = uiState.mapProjection,
                            activeLayer = uiState.mapLayer,
                            onCoordinateSelected = { lat, lon ->
                                viewModel.setCustomCoordinates(lat, lon)
                            }
                        )
                    }
                    Text(
                        text = "💡 Tap or drag anywhere on the globe/map to drop a field pin and sample NASA telemetry.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // --- 3. NASA Earth Observations & Live Telemetry Dashboard ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.liveNasaTelemetry,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "NASA POWER • MODIS Land Surface • SMAP Soil Hydrology",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weather Anomaly Banner
                    uiState.nasaTelemetry.weatherAnomalyAlert?.let { alert ->
                        Surface(
                            color = if (alert.contains("Warning") || alert.contains("Hazard")) Color(0x33FF5252) else Color(0x3300E676),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (alert.contains("Warning") || alert.contains("Hazard")) Color(0xFFFF5252) else Color(0xFF00E676),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = alert,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 4 Quadrants of Telemetry Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryMetricCard(
                                icon = Icons.Default.WbSunny,
                                title = strings.temperature,
                                value = "${uiState.nasaTelemetry.airTemperatureC}°C",
                                subtitle = "Topsoil: ${uiState.nasaTelemetry.topsoilTemperatureC}°C",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryMetricCard(
                                icon = Icons.Default.WaterDrop,
                                title = strings.moisture,
                                value = "${(uiState.nasaTelemetry.volumetricSoilMoistureM3 * 100).toInt()}% m³/m³",
                                subtitle = "Precip: ${uiState.nasaTelemetry.precipitationForecastMm} mm",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryMetricCard(
                                icon = Icons.Default.Air,
                                title = strings.wind,
                                value = "${uiState.nasaTelemetry.windSpeedKmh} km/h",
                                subtitle = "${uiState.nasaTelemetry.windDirectionCompass} (${uiState.nasaTelemetry.windDirectionDegrees}°) • Gust ${uiState.nasaTelemetry.windGustKmh}",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryMetricCard(
                                icon = Icons.Default.Eco,
                                title = "NDVI & Canopy",
                                value = "${uiState.nasaTelemetry.ndviIndex} NDVI",
                                subtitle = "Canopy: ${uiState.nasaTelemetry.canopyCoverPercent}% Cover",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TelemetryMetricCard(
                                icon = Icons.Default.Grass,
                                title = "Soil pH & NPK",
                                value = "pH ${uiState.nasaTelemetry.soilPh}",
                                subtitle = "N: ${uiState.nasaTelemetry.nitrogenKgHa.toInt()} P: ${uiState.nasaTelemetry.phosphorusKgHa.toInt()} K: ${uiState.nasaTelemetry.potassiumKgHa.toInt()} kg/ha",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryMetricCard(
                                icon = Icons.Default.Co2,
                                title = strings.carbonSeq,
                                value = "${uiState.nasaTelemetry.carbonSequestrationRateTonCo2} t/ha",
                                subtitle = "Annual CO₂e Net Sequestration",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun TelemetryMetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
