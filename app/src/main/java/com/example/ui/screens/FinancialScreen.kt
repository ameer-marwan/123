package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.components.MultiYearProfitBarChart
import com.example.ui.components.WaterAreaChart
import com.example.ui.localization.LocalizationProvider
import com.example.viewmodel.FieldShiftUiState
import com.example.viewmodel.FieldShiftViewModel

@Composable
fun FinancialScreen(
    viewModel: FieldShiftViewModel,
    uiState: FieldShiftUiState,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(uiState.activeLanguage)
    val fin = uiState.financialSettings
    val sim = uiState.simulationResult
    val avgNet = sim.projectedNetProfitPerYearUsd.average()
    val carbonRevPerYear = sim.fiveYearSoilCarbonGainTons * fin.carbonCreditPricePerTonCo2 * (fin.farmAcreage * 0.4046) / 5.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("financial_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header & Summary KPIs ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.financialProjections,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Acreage-Scaled Dynamic ROI, Break-Even & Carbon Credits",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(strings.netProfit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$${String.format("%,d", avgNet.toInt())}/yr",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E676)
                                )
                                Text("Avg across rotation", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(strings.roi, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "+38.4%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF29B6F6)
                                )
                                Text("Input cost savings inc.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Carbon Credit Revenue
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Co2, contentDescription = null, tint = Color(0xFF00E676))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(strings.carbonCredits, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Verified Soil Carbon Sequestration", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = "+$${String.format("%,d", carbonRevPerYear.toInt())}/yr",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Interactive Input Economics (Farm Acreage & Commodity Costs) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Input Cost & Commodity Variables",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Farm Acreage Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Farm Acreage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${fin.farmAcreage.toInt()} Acres", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = fin.farmAcreage.toFloat(),
                            onValueChange = { viewModel.updateAcreage(it.toDouble()) },
                            valueRange = 50f..2500f,
                            steps = 49,
                            modifier = Modifier.testTag("farm_acreage_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Diesel Fuel Price
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Diesel Fuel ($/gal)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("$${String.format("%.2f", fin.dieselFuelPricePerGallon)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Slider(
                            value = fin.dieselFuelPricePerGallon.toFloat(),
                            onValueChange = { viewModel.updateDieselPrice(it.toDouble()) },
                            valueRange = 2.0f..7.0f,
                            steps = 25
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Nitrogen Fertilizer Price
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Synthetic Nitrogen ($/kg N)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("$${String.format("%.2f", fin.syntheticNitrogenPricePerKg)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Slider(
                            value = fin.syntheticNitrogenPricePerKg.toFloat(),
                            onValueChange = { viewModel.updateNitrogenPrice(it.toDouble()) },
                            valueRange = 0.5f..3.0f,
                            steps = 25
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Carbon Credit Price
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00E676))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Carbon Credit Price ($/t CO₂e)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("$${fin.carbonCreditPricePerTonCo2.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                        }
                        Slider(
                            value = fin.carbonCreditPricePerTonCo2.toFloat(),
                            onValueChange = { viewModel.updateCarbonCreditPrice(it.toDouble()) },
                            valueRange = 10f..75f,
                            steps = 13
                        )
                    }
                }
            }
        }

        // --- 3. Infographics: Multi-Year Profit Bar Chart ---
        item {
            val grossList = sim.projectedNetProfitPerYearUsd.map { it * 1.65 }
            val expList = sim.projectedNetProfitPerYearUsd.map { it * 0.65 }
            MultiYearProfitBarChart(
                netProfitsPerYear = sim.projectedNetProfitPerYearUsd,
                grossPerYear = grossList,
                expensesPerYear = expList,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // --- 4. Infographics: Seasonal Water Area Chart ---
        item {
            WaterAreaChart(
                baselineWaterMm = listOf(45.0, 80.0, 145.0, 160.0, 125.0, 75.0, 30.0),
                optimizedWaterMm = listOf(35.0, 52.0, 92.0, 102.0, 80.0, 48.0, 22.0),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
