package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.LocalizationProvider
import com.example.viewmodel.FieldShiftUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExecutiveReportScreen(
    uiState: FieldShiftUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalizationProvider.getStrings(uiState.activeLanguage)
    val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

    val reportText = buildReportText(uiState, dateStr)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("executive_report_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Report Actions Header ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.tabReport,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Formal Climate Resilience & Agronomic Strategic Dossier",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { shareReport(context, reportText) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("share_report_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.shareReport, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { shareReport(context, reportText) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print / PDF", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 2. Print-Ready Executive Report Card (Dossier Format) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Document Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FIELDSHIFT EXECUTIVE REPORT",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(text = "Document Ref: FS-AGRI-${uiState.selectedLocation.id.uppercase()}-2026", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = dateStr, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Field Identity & Coordinates
                    ReportSectionHeader("1. FIELD GEOSPATIAL IDENTITY")
                    ReportKeyValue("Target Field Site", "${uiState.selectedLocation.name}, ${uiState.selectedLocation.country}")
                    ReportKeyValue("Coordinates", String.format("%.4f° N, %.4f° E (Elevation: %d m)", uiState.selectedLocation.latitude, uiState.selectedLocation.longitude, uiState.selectedLocation.elevationMeters))
                    ReportKeyValue("Agro-Climatic Biome", uiState.selectedLocation.biome)
                    ReportKeyValue("Farm Acreage", "${uiState.financialSettings.farmAcreage.toInt()} acres (${(uiState.financialSettings.farmAcreage * 0.404686).toInt()} ha)")

                    Spacer(modifier = Modifier.height(14.dp))

                    // NASA Earth Observations
                    ReportSectionHeader("2. NASA SATELLITE TELEMETRY SNAPSHOT")
                    val tel = uiState.nasaTelemetry
                    ReportKeyValue("Air Temp / Topsoil Temp", "${tel.airTemperatureC}°C / ${tel.topsoilTemperatureC}°C")
                    ReportKeyValue("Volumetric Soil Moisture", "${(tel.volumetricSoilMoistureM3 * 100).toInt()}% m³/m³ (NASA SMAP L4)")
                    ReportKeyValue("NDVI Vegetation Index", "${tel.ndviIndex} (Canopy Cover: ${tel.canopyCoverPercent}%)")
                    ReportKeyValue("Atmospheric Anomaly", tel.weatherAnomalyAlert ?: "Nominal Phenological Conditions")
                    ReportKeyValue("Wind & Solar Irradiance", "${tel.windSpeedKmh} km/h ${tel.windDirectionCompass} • ${tel.solarIrradianceMj} MJ/m²/day")

                    Spacer(modifier = Modifier.height(14.dp))

                    // Soil Studio Profile
                    ReportSectionHeader("3. SOIL STUDIO CLASSIFICATION & HEALTH")
                    val soil = uiState.soilProfile
                    ReportKeyValue("Soil Classification", "${soil.soilType.label} (Texture Index: ${soil.soilType.baseHealth})")
                    ReportKeyValue("Organic Matter (SOM)", "${soil.organicMatterPercent}%")
                    ReportKeyValue("Tillage & Infiltration", "${soil.tillage.label} (${soil.drainageRateMmHr.toInt()} mm/hr)")
                    ReportKeyValue("Irrigation Infrastructure", soil.irrigation.label)
                    ReportKeyValue("Comprehensive Soil Health Index", "${soil.soilHealthIndex} / 100 (Resilient)")

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multi-Year Rotation Schedule Table
                    ReportSectionHeader("4. MULTI-YEAR REGENERATIVE ROTATION")
                    uiState.rotationPlan.forEach { yr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Year ${yr.yearIndex}: ${yr.mainCrop.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Bio-Armor: ${yr.coverCrop?.name ?: "None"}", fontSize = 11.sp, color = Color(0xFF00E676))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Financial Projections & Carbon
                    ReportSectionHeader("5. 5-YEAR FINANCIAL RETURN & CARBON CREDITS")
                    val sim = uiState.simulationResult
                    ReportKeyValue("Annual Net Projected Profit", "$${String.format("%,d", sim.projectedNetProfitPerYearUsd.average().toInt())}/yr")
                    ReportKeyValue("Biological Nitrogen Savings", "$${sim.totalNitrogenSavingsUsd.toInt()} (${sim.totalNitrogenSavingsKgHa} kg N/ha)")
                    ReportKeyValue("5-Year Carbon Sequestration", "${sim.fiveYearSoilCarbonGainTons} metric tons CO₂e / ha")
                    ReportKeyValue("Weed Suppression & Water Reduction", "${sim.weedSuppressionAvgPercent}% Weed Break • -${sim.waterFootprintReductionPercent}% Water")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Official Seal Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verified by FieldShift Agro-Ecological Engine", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("PAGE 1 OF 1", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ReportSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun ReportKeyValue(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun buildReportText(state: FieldShiftUiState, date: String): String {
    return """
=============================================================
FIELDSHIFT STRATEGIC AGRO-RESILIENCE EXECUTIVE REPORT
Date: $date
Reference: FS-AGRI-${state.selectedLocation.id.uppercase()}-2026
=============================================================

1. FIELD GEOSPATIAL IDENTITY:
- Site: ${state.selectedLocation.name}, ${state.selectedLocation.country}
- Coordinates: ${state.selectedLocation.latitude}° N, ${state.selectedLocation.longitude}° E (Elevation: ${state.selectedLocation.elevationMeters} m)
- Biome: ${state.selectedLocation.biome}
- Farm Scale: ${state.financialSettings.farmAcreage.toInt()} acres

2. NASA SATELLITE TELEMETRY SNAPSHOT:
- Air Temp: ${state.nasaTelemetry.airTemperatureC}°C (Topsoil: ${state.nasaTelemetry.topsoilTemperatureC}°C)
- Volumetric Soil Moisture: ${(state.nasaTelemetry.volumetricSoilMoistureM3 * 100).toInt()}% m³/m³
- NDVI Index: ${state.nasaTelemetry.ndviIndex} (Canopy Cover: ${state.nasaTelemetry.canopyCoverPercent}%)
- Anomaly: ${state.nasaTelemetry.weatherAnomalyAlert}

3. SOIL STUDIO DIAGNOSTICS:
- Soil Classification: ${state.soilProfile.soilType.label}
- Soil Organic Matter (SOM): ${state.soilProfile.organicMatterPercent}%
- Tillage: ${state.soilProfile.tillage.label}
- Irrigation: ${state.soilProfile.irrigation.label}
- Soil Health Index: ${state.soilProfile.soilHealthIndex} / 100

4. 5-YEAR CROP ROTATION SCHEDULE:
${state.rotationPlan.joinToString("\n") { "Year ${it.yearIndex}: ${it.mainCrop.name} | Bio-Armor: ${it.coverCrop?.name ?: "None"}" }}

5. FINANCIAL & CLIMATE IMPACT:
- Projected Avg Net Profit: $${String.format("%,d", state.simulationResult.projectedNetProfitPerYearUsd.average().toInt())}/year
- Biological Nitrogen Savings: $${state.simulationResult.totalNitrogenSavingsUsd.toInt()} (${state.simulationResult.totalNitrogenSavingsKgHa} kg N/ha)
- 5-Year Carbon Sequestration: ${state.simulationResult.fiveYearSoilCarbonGainTons} t CO₂e/ha
- Water Footprint Reduction: -${state.simulationResult.waterFootprintReductionPercent}%
=============================================================
Verified by FieldShift Geospatial AI
"""
}

private fun shareReport(context: Context, content: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, content)
        putExtra(Intent.EXTRA_SUBJECT, "FieldShift Strategic Agronomic Report")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share FieldShift Executive Report")
    context.startActivity(shareIntent)
}
