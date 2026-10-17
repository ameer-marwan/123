package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IrrigationSystem
import com.example.data.model.SoilType
import com.example.data.model.TillagePractice
import com.example.ui.components.SoilTrajectoryChart
import com.example.ui.localization.LocalizationProvider
import com.example.viewmodel.FieldShiftUiState
import com.example.viewmodel.FieldShiftViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoilStudioScreen(
    viewModel: FieldShiftViewModel,
    uiState: FieldShiftUiState,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(uiState.activeLanguage)
    val profile = uiState.soilProfile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("soil_studio_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header & Baseline Coordinates Banner ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.soilStudioTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Geospatial Baseline: ${uiState.selectedLocation.name} (${String.format("%.2f°N, %.2f°E", uiState.selectedLocation.latitude, uiState.selectedLocation.longitude)})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Soil Health Index Hero Gauge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(strings.soilHealthIndex, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${profile.soilHealthIndex} / 100",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (profile.soilHealthIndex > 80) Color(0xFF00E676) else Color(0xFFFFB300)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Available Water Capacity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${profile.waterHoldingCapacityMm.toInt()} mm/m",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF29B6F6)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Soil Type Selection ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Soil Texture Classification",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SoilType.values().forEach { st ->
                            FilterChip(
                                selected = profile.soilType == st,
                                onClick = { viewModel.updateSoilType(st) },
                                label = { Text(st.label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = profile.soilType.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 3. Interactive Parameters (Sliders) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Physical & Biological Metrics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Organic Matter %
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Soil Organic Matter (SOM)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${String.format("%.1f", profile.organicMatterPercent)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
                        Slider(
                            value = profile.organicMatterPercent.toFloat(),
                            onValueChange = { viewModel.updateOrganicMatter(it.toDouble()) },
                            valueRange = 0.5f..8.0f,
                            steps = 15,
                            modifier = Modifier.testTag("som_slider")
                        )
                        Text("Higher organic matter dramatically expands rhizosphere water storage.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Drainage Rate
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Internal Drainage Rate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${profile.drainageRateMmHr.toInt()} mm/hr", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = profile.drainageRateMmHr.toFloat(),
                            onValueChange = { viewModel.updateDrainage(it.toDouble()) },
                            valueRange = 2f..60f,
                            steps = 29,
                            modifier = Modifier.testTag("drainage_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bulk Density
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bulk Density (Compaction)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${String.format("%.2f", profile.bulkDensityGcm3)} g/cm³", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (profile.bulkDensityGcm3 > 1.5) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface)
                        }
                        Slider(
                            value = profile.bulkDensityGcm3.toFloat(),
                            onValueChange = { viewModel.updateBulkDensity(it.toDouble()) },
                            valueRange = 1.0f..1.8f,
                            steps = 16,
                            modifier = Modifier.testTag("bulk_density_slider")
                        )
                        Text(if (profile.bulkDensityGcm3 > 1.5) "⚠️ Compaction layer restricts deep root penetration." else "Optimal aeration and capillary pore space.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // --- 4. Tillage & Irrigation Practice ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Management & Infrastructure",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tillage System", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TillagePractice.values().forEach { tp ->
                            FilterChip(
                                selected = profile.tillage == tp,
                                onClick = { viewModel.updateTillage(tp) },
                                label = { Text(tp.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Irrigation Infrastructure", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        IrrigationSystem.values().forEach { ir ->
                            FilterChip(
                                selected = profile.irrigation == ir,
                                onClick = { viewModel.updateIrrigation(ir) },
                                label = { Text(ir.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // --- 5. 5-Year Soil Regeneration Trajectory Chart ---
        item {
            SoilTrajectoryChart(
                soilTrajectoryScores = uiState.simulationResult.soilHealthTrajectoryScore,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // --- 6. AI Consultation Action Button ---
        item {
            Button(
                onClick = {
                    viewModel.sendChatMessage("Analyze my current soil profile (${profile.soilType.label}, ${profile.organicMatterPercent}% OM, ${profile.tillage.label}) and recommend biological amendments.")
                    onNavigateToChat()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("ai_soil_consult_button")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Consult AI Agronomist on Soil Health",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
