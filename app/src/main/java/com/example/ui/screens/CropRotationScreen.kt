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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Crop
import com.example.data.model.CropCatalog
import com.example.data.model.CropCategory
import com.example.data.model.PriorityWeights
import com.example.data.model.YearRotation
import com.example.ui.components.ResilienceRadarChart
import com.example.ui.localization.LocalizationProvider
import com.example.viewmodel.FieldShiftUiState
import com.example.viewmodel.FieldShiftViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CropRotationScreen(
    viewModel: FieldShiftViewModel,
    uiState: FieldShiftUiState,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(uiState.activeLanguage)
    val weights = uiState.priorityWeights
    val sim = uiState.simulationResult

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("crop_rotation_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Screen Title & Smart Optimization Button ---
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
                                text = strings.rotationSimulatorTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Multi-Year Decision Support & Bio-Armor Modeling",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.runSmartOptimization() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("smart_optimization_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.runSmartOptimization,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // --- 2. Priority Weighting Sliders ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Operational Priority Weighting",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Priority 1: Short-term profit vs Long-term soil health
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🌱 Soil Health", fontSize = 11.sp, color = Color(0xFF00E676))
                            Text("💵 Cash Profit", fontSize = 11.sp, color = Color(0xFF29B6F6))
                        }
                        Slider(
                            value = weights.shortTermProfitVsSoilHealth,
                            onValueChange = { viewModel.updatePriorityWeights(weights.copy(shortTermProfitVsSoilHealth = it)) },
                            modifier = Modifier.testTag("priority_profit_soil_slider")
                        )
                    }

                    // Priority 2: Water conservation vs Maximum biomass
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🌾 Max Biomass", fontSize = 11.sp, color = Color(0xFFFFB300))
                            Text("💧 Water Savings", fontSize = 11.sp, color = Color(0xFF29B6F6))
                        }
                        Slider(
                            value = weights.waterConservationVsBiomass,
                            onValueChange = { viewModel.updatePriorityWeights(weights.copy(waterConservationVsBiomass = it)) },
                            modifier = Modifier.testTag("priority_water_biomass_slider")
                        )
                    }

                    // Priority 3: Carbon capture vs Pest/disease resistance
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🛡️ Pest Break", fontSize = 11.sp, color = Color(0xFFAB47BC))
                            Text("🌍 Carbon Capture", fontSize = 11.sp, color = Color(0xFF00E676))
                        }
                        Slider(
                            value = weights.carbonCaptureVsPestResistance,
                            onValueChange = { viewModel.updatePriorityWeights(weights.copy(carbonCaptureVsPestResistance = it)) },
                            modifier = Modifier.testTag("priority_carbon_pest_slider")
                        )
                    }
                }
            }
        }

        // --- 3. Simulation Impact Metrics Banner ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Simulated Ecological & Input Abatement",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImpactMetricBadge(
                            icon = Icons.Default.Eco,
                            title = "Nitrogen Savings",
                            value = "$${sim.totalNitrogenSavingsUsd.toInt()}",
                            sub = "${sim.totalNitrogenSavingsKgHa} kg N/ha",
                            modifier = Modifier.weight(1f)
                        )
                        ImpactMetricBadge(
                            icon = Icons.Default.Shield,
                            title = "Weed Bio-Armor",
                            value = "${sim.weedSuppressionAvgPercent}%",
                            sub = "Canopy Suppression",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ImpactMetricBadge(
                            icon = Icons.Default.WaterDrop,
                            title = "Water Footprint",
                            value = "-${sim.waterFootprintReductionPercent}%",
                            sub = "Irrigation Abated",
                            modifier = Modifier.weight(1f)
                        )
                        ImpactMetricBadge(
                            icon = Icons.Default.Co2,
                            title = "5-Yr Carbon Gain",
                            value = "${sim.fiveYearSoilCarbonGainTons} t",
                            sub = "CO₂e / ha sequestered",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 4. Multi-Year Rotation Schedule Timeline ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rotation Timeline (${uiState.rotationPlan.size} Years)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (uiState.rotationPlan.size < 5) {
                    OutlinedButton(
                        onClick = { viewModel.addRotationYear() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Year", fontSize = 12.sp)
                    }
                }
            }
        }

        items(uiState.rotationPlan.size) { index ->
            val yr = uiState.rotationPlan[index]
            RotationYearCard(
                yearRotation = yr,
                onUpdate = { main, cover -> viewModel.updateYearRotation(yr.yearIndex, main, cover) },
                onRemove = { viewModel.removeRotationYear(yr.yearIndex) },
                canRemove = uiState.rotationPlan.size > 2
            )
        }

        // --- 5. 5-Axis Resilience Radar Chart ---
        item {
            ResilienceRadarChart(
                radarValues = sim.farmResilienceRadar,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun RotationYearCard(
    yearRotation: YearRotation,
    onUpdate: (Crop, Crop?) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    var showMainCropMenu by remember { mutableStateOf(false) }
    var showCoverCropMenu by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Year ${yearRotation.yearIndex}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                if (canRemove) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Year", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Cash Crop Selector
            Text("Main Crop:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box {
                Surface(
                    onClick = { showMainCropMenu = true },
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
                            text = "${yearRotation.mainCrop.name} (${yearRotation.mainCrop.category.label})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Edit ▼", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                DropdownMenu(
                    expanded = showMainCropMenu,
                    onDismissRequest = { showMainCropMenu = false }
                ) {
                    CropCatalog.ALL_CROPS.filter { it.category != CropCategory.COVER_CROP }.forEach { crop ->
                        DropdownMenuItem(
                            text = { Text("${crop.name} [${crop.category.label}]") },
                            onClick = {
                                onUpdate(crop, yearRotation.coverCrop)
                                showMainCropMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cover Crop Selector
            Text("Off-Season Cover / Bio-Armor:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box {
                Surface(
                    onClick = { showCoverCropMenu = true },
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
                            text = yearRotation.coverCrop?.name ?: "None (Fallow / Exposed)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (yearRotation.coverCrop != null) Color(0xFF00E676) else Color(0xFFFFB300)
                        )
                        Text("Edit ▼", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                DropdownMenu(
                    expanded = showCoverCropMenu,
                    onDismissRequest = { showCoverCropMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Fallow / Bare)") },
                        onClick = {
                            onUpdate(yearRotation.mainCrop, null)
                            showCoverCropMenu = false
                        }
                    )
                    CropCatalog.ALL_CROPS.filter { it.category == CropCategory.COVER_CROP || it.category == CropCategory.LEGUME_FIXER }.forEach { cover ->
                        DropdownMenuItem(
                            text = { Text(cover.name) },
                            onClick = {
                                onUpdate(yearRotation.mainCrop, cover)
                                showCoverCropMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactMetricBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = sub, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
