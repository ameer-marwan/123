package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgriculturalHubs
import com.example.data.model.AlertNotificationSettings
import com.example.data.model.Crop
import com.example.data.model.CropCatalog
import com.example.data.model.CropCategory
import com.example.data.model.FarmAlert
import com.example.data.model.FarmerProfile
import com.example.data.model.FieldLocation
import com.example.data.model.FinancialSettings
import com.example.data.model.IrrigationSystem
import com.example.data.model.NasaTelemetry
import com.example.data.model.PriorityWeights
import com.example.data.model.RotationSimulationResult
import com.example.data.model.SavedFarm
import com.example.data.model.SoilProfile
import com.example.data.model.SoilType
import com.example.data.model.TillagePractice
import com.example.data.model.YearRotation
import com.example.data.remote.ChatMessage
import com.example.data.remote.GeminiClient
import com.example.data.remote.NasaPowerService
import com.example.data.repository.FarmRepository
import com.example.ui.components.MapLayer
import com.example.ui.components.MapProjection
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class FieldShiftUiState(
    val showIntroAnimation: Boolean = false,
    val selectedLocation: FieldLocation = AgriculturalHubs.PRESETS.first(),
    val currentFarmName: String = "North Creek Prairie (Home Base)",
    val mapProjection: MapProjection = MapProjection.GLOBE_3D,
    val mapLayer: MapLayer = MapLayer.NDVI_THERMAL,
    val nasaTelemetry: NasaTelemetry = NasaPowerService.generatePlanetaryTelemetry(41.8780, -93.0977),
    val isLoadingTelemetry: Boolean = false,
    val soilProfile: SoilProfile = SoilProfile(),
    val priorityWeights: PriorityWeights = PriorityWeights(),
    val rotationPlan: List<YearRotation> = listOf(
        YearRotation(1, CropCatalog.ALL_CROPS.first { it.id == "corn" }, CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" }),
        YearRotation(2, CropCatalog.ALL_CROPS.first { it.id == "soybean" }, CropCatalog.ALL_CROPS.first { it.id == "radish" }),
        YearRotation(3, CropCatalog.ALL_CROPS.first { it.id == "wheat" }, CropCatalog.ALL_CROPS.first { it.id == "clover" }),
        YearRotation(4, CropCatalog.ALL_CROPS.first { it.id == "corn" }, CropCatalog.ALL_CROPS.first { it.id == "hairy_vetch" }),
        YearRotation(5, CropCatalog.ALL_CROPS.first { it.id == "soybean" }, CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" })
    ),
    val financialSettings: FinancialSettings = FinancialSettings(),
    val simulationResult: RotationSimulationResult = RotationSimulationResult(
        totalNitrogenSavingsKgHa = 145.0,
        totalNitrogenSavingsUsd = 210.25,
        weedSuppressionAvgPercent = 88,
        waterFootprintReductionPercent = 28,
        fiveYearSoilCarbonGainTons = 14.8,
        soilHealthTrajectoryScore = listOf(72, 77, 83, 89, 94),
        projectedNetProfitPerYearUsd = listOf(86500.0, 94200.0, 102400.0, 115000.0, 126800.0),
        farmResilienceRadar = mapOf(
            "Soil" to 0.94f,
            "Water" to 0.82f,
            "Profit" to 0.88f,
            "Biodiversity" to 0.92f,
            "Climate" to 0.90f
        )
    ),
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            role = "model",
            content = "Greetings! I am the **FieldShift AI Agronomist**, powered by NASA Earth observations and agro-ecological modeling. Ask me about your soil metrics, crop rotation sequencing, nitrogen dynamics, or carbon credit monetization."
        )
    ),
    val isChatLoading: Boolean = false,
    val activeLanguage: AppLanguage = AppLanguage.ENGLISH,
    val activeTheme: AppThemeMode = AppThemeMode.AGRITECH_EMERALD,
    val searchQuery: String = "",
    val showAlertsHub: Boolean = false
)

class FieldShiftViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FieldShiftUiState())
    val uiState: StateFlow<FieldShiftUiState> = _uiState.asStateFlow()

    val farmerProfile: StateFlow<FarmerProfile> = FarmRepository.farmerProfile
    val savedFarms: StateFlow<List<SavedFarm>> = FarmRepository.savedFarms
    val alerts: StateFlow<List<FarmAlert>> = FarmRepository.alerts
    val alertSettings: StateFlow<AlertNotificationSettings> = FarmRepository.alertSettings

    init {
        // Load initial active farm from repository if present
        savedFarms.value.firstOrNull()?.let { initialFarm ->
            loadFarmIntoState(initialFarm)
        }
        recalculateSimulation()
        refreshTelemetry()
    }

    fun dismissIntro() {
        _uiState.update { it.copy(showIntroAnimation = false) }
    }

    fun replayIntro() {
        _uiState.update { it.copy(showIntroAnimation = true) }
    }

    fun setShowAlertsHub(show: Boolean) {
        _uiState.update { it.copy(showAlertsHub = show) }
    }

    fun setMapProjection(proj: MapProjection) {
        _uiState.update { it.copy(mapProjection = proj) }
    }

    fun setMapLayer(layer: MapLayer) {
        _uiState.update { it.copy(mapLayer = layer) }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(activeLanguage = lang) }
    }

    fun setTheme(theme: AppThemeMode) {
        _uiState.update { it.copy(activeTheme = theme) }
    }

    fun selectLocation(loc: FieldLocation) {
        _uiState.update {
            it.copy(
                selectedLocation = loc,
                soilProfile = it.soilProfile.copy(soilType = loc.baselineSoilType),
                currentFarmName = loc.name
            )
        }
        refreshTelemetry()
        recalculateSimulation()
    }

    fun setCustomCoordinates(lat: Double, lon: Double) {
        val customLoc = FieldLocation(
            id = "custom_${System.currentTimeMillis()}",
            name = "Pinned Field Site",
            country = "Custom Target",
            latitude = lat,
            longitude = lon,
            elevationMeters = 210,
            biome = "Custom Agro-Climatic Zone",
            baselineSoilType = _uiState.value.soilProfile.soilType
        )
        _uiState.update { it.copy(selectedLocation = customLoc, currentFarmName = "Pinned Field Site") }
        refreshTelemetry()
        recalculateSimulation()
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTelemetry = true) }
            val loc = _uiState.value.selectedLocation
            val telemetry = NasaPowerService.fetchLiveTelemetry(loc.latitude, loc.longitude)
            _uiState.update {
                it.copy(
                    nasaTelemetry = telemetry,
                    isLoadingTelemetry = false
                )
            }
            recalculateSimulation()

            // Run real-time alert evaluation for current active farm
            val activeFarm = savedFarms.value.find { it.id == farmerProfile.value.activeFarmId }
                ?: SavedFarm(
                    name = _uiState.value.currentFarmName,
                    location = loc,
                    acreage = _uiState.value.financialSettings.farmAcreage,
                    soilProfile = _uiState.value.soilProfile,
                    rotationPlan = _uiState.value.rotationPlan
                )
            FarmRepository.evaluateLiveTelemetryAlerts(activeFarm, telemetry)
        }
    }

    // --- User Profile & Multi-Farm Management ---
    fun switchActiveFarm(farmId: String) {
        val target = savedFarms.value.find { it.id == farmId } ?: return
        FarmRepository.setActiveFarm(farmId)
        loadFarmIntoState(target)
        refreshTelemetry()
        recalculateSimulation()
    }

    private fun loadFarmIntoState(farm: SavedFarm) {
        _uiState.update {
            it.copy(
                selectedLocation = farm.location,
                currentFarmName = farm.name,
                soilProfile = farm.soilProfile,
                rotationPlan = farm.rotationPlan,
                financialSettings = it.financialSettings.copy(farmAcreage = farm.acreage)
            )
        }
    }

    fun saveCurrentAsNewFarm(name: String, description: String) {
        val current = _uiState.value
        val newFarm = SavedFarm(
            id = "farm_${UUID.randomUUID().toString().take(8)}",
            name = name,
            description = description,
            location = current.selectedLocation,
            acreage = current.financialSettings.farmAcreage,
            soilProfile = current.soilProfile,
            rotationPlan = current.rotationPlan
        )
        FarmRepository.saveFarm(newFarm)
        FarmRepository.setActiveFarm(newFarm.id)
        _uiState.update { it.copy(currentFarmName = name) }
    }

    fun updateExistingFarm(farm: SavedFarm) {
        FarmRepository.saveFarm(farm)
        if (farmerProfile.value.activeFarmId == farm.id) {
            loadFarmIntoState(farm)
            recalculateSimulation()
        }
    }

    fun deleteSavedFarm(farmId: String) {
        FarmRepository.deleteFarm(farmId)
    }

    fun updateProfile(name: String, email: String, org: String, specialty: String) {
        FarmRepository.updateProfile(name, email, org, specialty)
    }

    fun acknowledgeAlert(alertId: String) {
        FarmRepository.acknowledgeAlert(alertId)
    }

    fun snoozeAlert(alertId: String) {
        FarmRepository.snoozeAlert(alertId)
    }

    fun updateAlertSettings(settings: AlertNotificationSettings) {
        FarmRepository.updateAlertSettings(settings)
    }

    // --- Soil Studio Updates ---
    fun updateSoilType(type: SoilType) {
        _uiState.update { it.copy(soilProfile = it.soilProfile.copy(soilType = type)) }
        recalculateSimulation()
    }

    fun updateDrainage(rateMmHr: Double) {
        _uiState.update { it.copy(soilProfile = it.soilProfile.copy(drainageRateMmHr = rateMmHr)) }
        recalculateSimulation()
    }

    fun updateBulkDensity(density: Double) {
        _uiState.update { it.copy(soilProfile = it.soilProfile.copy(bulkDensityGcm3 = density)) }
        recalculateSimulation()
    }

    fun updateOrganicMatter(percent: Double) {
        _uiState.update { it.copy(soilProfile = it.soilProfile.copy(organicMatterPercent = percent)) }
        recalculateSimulation()
    }

    fun updateTillage(tillage: TillagePractice) {
        _uiState.update { it.copy(soilProfile = it.soilProfile.copy(tillage = tillage)) }
        recalculateSimulation()
    }

    fun updateIrrigation(irrigation: IrrigationSystem) {
        _uiState.update { it.copy(soilProfile = it.soilProfile.copy(irrigation = irrigation)) }
        recalculateSimulation()
    }

    // --- Priority Weightings & Rotation ---
    fun updatePriorityWeights(weights: PriorityWeights) {
        _uiState.update { it.copy(priorityWeights = weights) }
        recalculateSimulation()
    }

    fun updateYearRotation(yearIndex: Int, mainCrop: Crop, coverCrop: Crop?) {
        val updated = _uiState.value.rotationPlan.map {
            if (it.yearIndex == yearIndex) it.copy(mainCrop = mainCrop, coverCrop = coverCrop) else it
        }
        _uiState.update { it.copy(rotationPlan = updated) }
        recalculateSimulation()
    }

    fun addRotationYear() {
        val current = _uiState.value.rotationPlan
        if (current.size >= 5) return
        val nextYear = current.size + 1
        val newEntry = YearRotation(
            yearIndex = nextYear,
            mainCrop = CropCatalog.ALL_CROPS.first { it.id == "wheat" },
            coverCrop = CropCatalog.ALL_CROPS.first { it.id == "clover" }
        )
        _uiState.update { it.copy(rotationPlan = current + newEntry) }
        recalculateSimulation()
    }

    fun removeRotationYear(yearIndex: Int) {
        val current = _uiState.value.rotationPlan
        if (current.size <= 2) return
        val filtered = current.filterNot { it.yearIndex == yearIndex }
            .mapIndexed { idx, yr -> yr.copy(yearIndex = idx + 1) }
        _uiState.update { it.copy(rotationPlan = filtered) }
        recalculateSimulation()
    }

    fun runSmartOptimization() {
        val weights = _uiState.value.priorityWeights
        val optPlan = listOf(
            YearRotation(
                1,
                CropCatalog.ALL_CROPS.first { it.id == "corn" },
                CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" }
            ),
            YearRotation(
                2,
                CropCatalog.ALL_CROPS.first { it.id == "soybean" },
                CropCatalog.ALL_CROPS.first { it.id == "radish" }
            ),
            YearRotation(
                3,
                CropCatalog.ALL_CROPS.first { it.id == "wheat" },
                CropCatalog.ALL_CROPS.first { it.id == "clover" }
            ),
            YearRotation(
                4,
                CropCatalog.ALL_CROPS.first { it.id == (if (weights.waterConservationVsBiomass > 0.6f) "sorghum" else "corn") },
                CropCatalog.ALL_CROPS.first { it.id == "hairy_vetch" }
            ),
            YearRotation(
                5,
                CropCatalog.ALL_CROPS.first { it.id == (if (weights.carbonCaptureVsPestResistance > 0.5f) "alfalfa" else "sunflower") },
                CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" }
            )
        )

        _uiState.update { it.copy(rotationPlan = optPlan) }
        recalculateSimulation()
        sendChatMessage("Explain why this 5-year rotation sequence was optimized for my field's soil and climate conditions.")
    }

    // --- Financial Settings ---
    fun updateAcreage(acres: Double) {
        _uiState.update { it.copy(financialSettings = it.financialSettings.copy(farmAcreage = acres)) }
        recalculateSimulation()
    }

    fun updateDieselPrice(price: Double) {
        _uiState.update { it.copy(financialSettings = it.financialSettings.copy(dieselFuelPricePerGallon = price)) }
        recalculateSimulation()
    }

    fun updateNitrogenPrice(price: Double) {
        _uiState.update { it.copy(financialSettings = it.financialSettings.copy(syntheticNitrogenPricePerKg = price)) }
        recalculateSimulation()
    }

    fun updateCarbonCreditPrice(price: Double) {
        _uiState.update { it.copy(financialSettings = it.financialSettings.copy(carbonCreditPricePerTonCo2 = price)) }
        recalculateSimulation()
    }

    // --- Gemini Chatbot ---
    fun sendChatMessage(text: String) {
        val userMsg = ChatMessage(role = "user", content = text)
        val currentMsgs = _uiState.value.chatMessages + userMsg
        _uiState.update { it.copy(chatMessages = currentMsgs, isChatLoading = true) }

        viewModelScope.launch {
            val state = _uiState.value
            val contextInfo = """
Field: ${state.currentFarmName} (${state.selectedLocation.latitude}°N, ${state.selectedLocation.longitude}°E)
Soil: ${state.soilProfile.soilType.label}, OM: ${state.soilProfile.organicMatterPercent}%, Tillage: ${state.soilProfile.tillage.label}, Irrigation: ${state.soilProfile.irrigation.label}
NASA Telemetry: Temp: ${state.nasaTelemetry.airTemperatureC}°C, Precip: ${state.nasaTelemetry.precipitationForecastMm} mm, Soil Moisture: ${state.nasaTelemetry.volumetricSoilMoistureM3} m³/m³, NDVI: ${state.nasaTelemetry.ndviIndex}
Acreage: ${state.financialSettings.farmAcreage} acres
Current Rotation: ${state.rotationPlan.joinToString(" -> ") { "Yr ${it.yearIndex}: ${it.mainCrop.name} (${it.coverCrop?.name ?: "None"})" }}
"""
            val reply = GeminiClient.sendChatMessage(
                history = currentMsgs,
                userMessage = text,
                fieldContext = contextInfo
            )
            val modelMsg = ChatMessage(role = "model", content = reply)
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + modelMsg,
                    isChatLoading = false
                )
            }
        }
    }

    private fun recalculateSimulation() {
        val state = _uiState.value
        val plan = state.rotationPlan
        val soil = state.soilProfile
        val fin = state.financialSettings

        var totalNFixKg = 0.0
        var totalWeedSupSum = 0
        var totalCarbonTon = 0.0
        val netProfits = mutableListOf<Double>()
        val grossProfits = mutableListOf<Double>()
        val expensesList = mutableListOf<Double>()

        val baseHealth = soil.soilHealthIndex
        val healthTrajectory = mutableListOf<Int>()

        plan.forEachIndexed { idx, yr ->
            val main = yr.mainCrop
            val cover = yr.coverCrop

            val nFix = (main.nitrogenFixationKgHa + (cover?.nitrogenFixationKgHa ?: 0.0)).coerceAtLeast(0.0)
            totalNFixKg += nFix

            val weedSup = ((main.weedSuppressionPercent + (cover?.weedSuppressionPercent ?: 60)) / 2)
            totalWeedSupSum += weedSup

            val cSeq = main.carbonSequestrationTonHa + (cover?.carbonSequestrationTonHa ?: 1.0)
            totalCarbonTon += cSeq

            // Annual financials
            val grossPerAcre = main.baseYieldPerAcre * main.avgMarketPricePerUnit
            val totalGross = grossPerAcre * fin.farmAcreage

            var operationalCostPerAcre = main.operationalCostPerAcre + (cover?.operationalCostPerAcre ?: 0.0)
            val fuelReductionRatio = soil.tillage.fuelReductionPercent / 100.0
            operationalCostPerAcre -= (45.0 * fuelReductionRatio)
            val nSavingsDollarPerAcre = (nFix * fin.syntheticNitrogenPricePerKg * 0.4046)
            operationalCostPerAcre = (operationalCostPerAcre - nSavingsDollarPerAcre).coerceAtLeast(120.0)

            val totalCost = operationalCostPerAcre * fin.farmAcreage
            val carbonRev = (cSeq * fin.carbonCreditPricePerTonCo2 * (fin.farmAcreage * 0.4046))

            val netProfit = (totalGross - totalCost + carbonRev).coerceAtLeast(15000.0)
            netProfits.add(Math.round(netProfit * 100.0) / 100.0)
            grossProfits.add(Math.round(totalGross * 100.0) / 100.0)
            expensesList.add(Math.round(totalCost * 100.0) / 100.0)

            val yrHealth = (baseHealth + (idx + 1) * 4.5 + (if (cover != null) 3 else 0)).toInt().coerceIn(30, 98)
            healthTrajectory.add(yrHealth)
        }

        val avgWeed = if (plan.isNotEmpty()) totalWeedSupSum / plan.size else 75
        val nSavingsUsd = totalNFixKg * fin.syntheticNitrogenPricePerKg
        val waterRedPct = (if (soil.irrigation == IrrigationSystem.DRIP) 38 else 22) + (if (soil.tillage == TillagePractice.NO_TILL) 10 else 0)

        val soilRadar = (healthTrajectory.lastOrNull() ?: 85) / 100f
        val waterRadar = (waterRedPct / 50f).coerceIn(0.5f, 0.98f)
        val profitRadar = (netProfits.average() / 140000.0).toFloat().coerceIn(0.6f, 0.99f)
        val bioRadar = (plan.map { it.mainCrop.id }.distinct().size * 0.22f + (if (plan.any { it.coverCrop != null }) 0.25f else 0f)).coerceIn(0.65f, 0.96f)
        val climateRadar = ((soilRadar + waterRadar + bioRadar) / 3f).coerceIn(0.7f, 0.98f)

        val result = RotationSimulationResult(
            totalNitrogenSavingsKgHa = Math.round(totalNFixKg * 10.0) / 10.0,
            totalNitrogenSavingsUsd = Math.round(nSavingsUsd * 100.0) / 10.0,
            weedSuppressionAvgPercent = avgWeed,
            waterFootprintReductionPercent = waterRedPct,
            fiveYearSoilCarbonGainTons = Math.round(totalCarbonTon * 10.0) / 10.0,
            soilHealthTrajectoryScore = healthTrajectory,
            projectedNetProfitPerYearUsd = netProfits,
            farmResilienceRadar = mapOf(
                "Soil" to soilRadar,
                "Water" to waterRadar,
                "Profit" to profitRadar,
                "Biodiversity" to bioRadar,
                "Climate" to climateRadar
            )
        )

        _uiState.update { it.copy(simulationResult = result) }
    }
}
