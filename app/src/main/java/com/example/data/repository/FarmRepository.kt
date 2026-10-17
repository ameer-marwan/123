package com.example.data.repository

import com.example.data.model.AgriculturalHubs
import com.example.data.model.AlertNotificationSettings
import com.example.data.model.AlertSeverity
import com.example.data.model.AlertType
import com.example.data.model.CropCatalog
import com.example.data.model.FarmAlert
import com.example.data.model.FarmerProfile
import com.example.data.model.FieldLocation
import com.example.data.model.IrrigationSystem
import com.example.data.model.NasaTelemetry
import com.example.data.model.SavedFarm
import com.example.data.model.SoilProfile
import com.example.data.model.SoilType
import com.example.data.model.TillagePractice
import com.example.data.model.YearRotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object FarmRepository {
    private val defaultProfile = FarmerProfile(
        id = "farmer_01",
        fullName = "Sarah Jenkins",
        email = "sarah.jenkins@fieldshift-agri.com",
        organizationName = "Jenkins Regenerative Horizons",
        farmingSpecialty = "Continuous No-Till & Biodiversity Rotations",
        country = "United States",
        preferredLanguage = "en",
        memberSince = "March 2024",
        activeFarmId = "farm_iowa_01"
    )

    private val initialFarms = listOf(
        SavedFarm(
            id = "farm_iowa_01",
            name = "North Creek Prairie (Home Base)",
            description = "Primary fertile alluvial basin under continuous cereal rye bio-armor and no-till.",
            location = AgriculturalHubs.PRESETS.first { it.id == "iowa" },
            acreage = 420.0,
            soilProfile = SoilProfile(
                soilType = SoilType.LOAM,
                organicMatterPercent = 4.2,
                drainageRateMmHr = 22.0,
                bulkDensityGcm3 = 1.22,
                tillage = TillagePractice.NO_TILL,
                irrigation = IrrigationSystem.DRIP
            ),
            rotationPlan = listOf(
                YearRotation(1, CropCatalog.ALL_CROPS.first { it.id == "corn" }, CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" }),
                YearRotation(2, CropCatalog.ALL_CROPS.first { it.id == "soybean" }, CropCatalog.ALL_CROPS.first { it.id == "radish" }),
                YearRotation(3, CropCatalog.ALL_CROPS.first { it.id == "wheat" }, CropCatalog.ALL_CROPS.first { it.id == "clover" })
            )
        ),
        SavedFarm(
            id = "farm_pampas_02",
            name = "Southern Granary Parcel 4",
            description = "Chernozem flatlands testing sunflower and sorghum drought resilient sequencing.",
            location = AgriculturalHubs.PRESETS.first { it.id == "pampas" },
            acreage = 680.0,
            soilProfile = SoilProfile(
                soilType = SoilType.LOAM,
                organicMatterPercent = 3.6,
                drainageRateMmHr = 18.0,
                bulkDensityGcm3 = 1.28,
                tillage = TillagePractice.REDUCED_TILL,
                irrigation = IrrigationSystem.RAINFED
            ),
            rotationPlan = listOf(
                YearRotation(1, CropCatalog.ALL_CROPS.first { it.id == "sunflower" }, CropCatalog.ALL_CROPS.first { it.id == "hairy_vetch" }),
                YearRotation(2, CropCatalog.ALL_CROPS.first { it.id == "wheat" }, CropCatalog.ALL_CROPS.first { it.id == "buckwheat" }),
                YearRotation(3, CropCatalog.ALL_CROPS.first { it.id == "soybean" }, CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" })
            )
        ),
        SavedFarm(
            id = "farm_california_03",
            name = "Valley Orchard & Alfalfa Plot",
            description = "High efficiency subsurface drip trial with alfalfa nitrogen regeneration.",
            location = AgriculturalHubs.PRESETS.first { it.id == "california" },
            acreage = 240.0,
            soilProfile = SoilProfile(
                soilType = SoilType.SANDY_LOAM,
                organicMatterPercent = 2.4,
                drainageRateMmHr = 38.0,
                bulkDensityGcm3 = 1.34,
                tillage = TillagePractice.NO_TILL,
                irrigation = IrrigationSystem.DRIP
            ),
            rotationPlan = listOf(
                YearRotation(1, CropCatalog.ALL_CROPS.first { it.id == "alfalfa" }, null),
                YearRotation(2, CropCatalog.ALL_CROPS.first { it.id == "cotton" }, CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" })
            )
        )
    )

    private val initialAlerts = listOf(
        FarmAlert(
            id = "alert_01",
            farmId = "farm_iowa_01",
            farmName = "North Creek Prairie",
            type = AlertType.FROST_HAZARD,
            severity = AlertSeverity.WARNING,
            title = "Overnight Ground Freeze Advisory",
            message = "NASA MODIS land surface projection indicates topsoil temperature dropping to 1.8°C at 04:00 AM.",
            parameterObserved = "Topsoil: 1.8°C (Threshold < 2.0°C)",
            recommendedAction = "Verify emergence stage; avoid early spring herbicide pass until daytime soil warms above 8°C."
        ),
        FarmAlert(
            id = "alert_02",
            farmId = "farm_california_03",
            farmName = "Valley Orchard & Alfalfa Plot",
            type = AlertType.SOIL_MOISTURE_CRITICAL,
            severity = AlertSeverity.CRITICAL,
            title = "Subsoil Moisture Deficit Anomaly",
            message = "Volumetric soil moisture sampled at 11.2% m³/m³, entering severe evapotranspiration stress zone.",
            parameterObserved = "Moisture: 11.2% m³/m³ (Critical Minimum < 14%)",
            recommendedAction = "Initiate 6-hour precision drip pulse cycle; maintain living bio-armor residue to shield surface evaporation."
        ),
        FarmAlert(
            id = "alert_03",
            farmId = "farm_pampas_02",
            farmName = "Southern Granary Parcel 4",
            type = AlertType.NDVI_ANOMALY_DROP,
            severity = AlertSeverity.INFO,
            title = "Canopy Vegetation Index Acceleration",
            message = "NDVI index climbed to 0.78, indicating 24% faster biomass canopy closure than regional historical mean.",
            parameterObserved = "NDVI: 0.78 (+0.12 vs 10-Yr Baseline)",
            recommendedAction = "Optimal window for mid-season nitrogen tissue testing and carbon credit accreditation check."
        )
    )

    private val _farmerProfile = MutableStateFlow(defaultProfile)
    val farmerProfile: StateFlow<FarmerProfile> = _farmerProfile.asStateFlow()

    private val _savedFarms = MutableStateFlow<List<SavedFarm>>(initialFarms)
    val savedFarms: StateFlow<List<SavedFarm>> = _savedFarms.asStateFlow()

    private val _alerts = MutableStateFlow<List<FarmAlert>>(initialAlerts)
    val alerts: StateFlow<List<FarmAlert>> = _alerts.asStateFlow()

    private val _alertSettings = MutableStateFlow(AlertNotificationSettings())
    val alertSettings: StateFlow<AlertNotificationSettings> = _alertSettings.asStateFlow()

    fun updateProfile(name: String, email: String, org: String, specialty: String) {
        _farmerProfile.update {
            it.copy(fullName = name, email = email, organizationName = org, farmingSpecialty = specialty)
        }
    }

    fun setActiveFarm(farmId: String) {
        _farmerProfile.update { it.copy(activeFarmId = farmId) }
    }

    fun saveFarm(farm: SavedFarm) {
        _savedFarms.update { list ->
            val index = list.indexOfFirst { it.id == farm.id }
            if (index >= 0) {
                list.toMutableList().apply { set(index, farm) }
            } else {
                list + farm
            }
        }
    }

    fun deleteFarm(farmId: String) {
        _savedFarms.update { list -> list.filterNot { it.id == farmId } }
        if (_farmerProfile.value.activeFarmId == farmId) {
            _savedFarms.value.firstOrNull()?.let { setActiveFarm(it.id) }
        }
    }

    fun acknowledgeAlert(alertId: String) {
        _alerts.update { list ->
            list.map { if (it.id == alertId) it.copy(isAcknowledged = true) else it }
        }
    }

    fun snoozeAlert(alertId: String) {
        _alerts.update { list ->
            list.map { if (it.id == alertId) it.copy(isSnoozed = true) else it }
        }
    }

    fun updateAlertSettings(settings: AlertNotificationSettings) {
        _alertSettings.update { settings }
    }

    fun evaluateLiveTelemetryAlerts(farm: SavedFarm, telemetry: NasaTelemetry) {
        val settings = _alertSettings.value
        val newAlerts = mutableListOf<FarmAlert>()

        // 1. Frost Hazard
        if (telemetry.topsoilTemperatureC <= settings.frostThresholdC) {
            newAlerts.add(
                FarmAlert(
                    farmId = farm.id,
                    farmName = farm.name,
                    type = AlertType.FROST_HAZARD,
                    severity = AlertSeverity.CRITICAL,
                    title = "Active Frost Threat Detected",
                    message = "NASA land surface temperature has reached ${telemetry.topsoilTemperatureC}°C.",
                    parameterObserved = "Topsoil: ${telemetry.topsoilTemperatureC}°C (Threshold <= ${settings.frostThresholdC}°C)",
                    recommendedAction = "Protect vulnerable seedlings; delay field passes until freeze hazard dissipates."
                )
            )
        }

        // 2. Heatwave Stress
        if (telemetry.airTemperatureC >= settings.heatwaveThresholdC) {
            newAlerts.add(
                FarmAlert(
                    farmId = farm.id,
                    farmName = farm.name,
                    type = AlertType.HEATWAVE_STRESS,
                    severity = AlertSeverity.WARNING,
                    title = "Severe Heatwave & High Vapor Deficit",
                    message = "Air temperature is currently ${telemetry.airTemperatureC}°C with solar irradiance at ${telemetry.solarIrradianceMj} MJ/m².",
                    parameterObserved = "Temp: ${telemetry.airTemperatureC}°C (Threshold >= ${settings.heatwaveThresholdC}°C)",
                    recommendedAction = "Schedule irrigation during night/early morning hours to prevent excessive evaporative loss."
                )
            )
        }

        // 3. Soil Moisture
        val moisturePct = (telemetry.volumetricSoilMoistureM3 * 100).toInt()
        if (moisturePct <= settings.moistureMinPercent) {
            newAlerts.add(
                FarmAlert(
                    farmId = farm.id,
                    farmName = farm.name,
                    type = AlertType.SOIL_MOISTURE_CRITICAL,
                    severity = AlertSeverity.CRITICAL,
                    title = "Severe Soil Moisture Depletion",
                    message = "NASA SMAP shows root-zone moisture at $moisturePct% m³/m³.",
                    parameterObserved = "Moisture: $moisturePct% (Deficit Threshold <= ${settings.moistureMinPercent}%)",
                    recommendedAction = "Activate irrigation systems immediately; inspect soil crusting."
                )
            )
        } else if (moisturePct >= settings.moistureMaxPercent) {
            newAlerts.add(
                FarmAlert(
                    farmId = farm.id,
                    farmName = farm.name,
                    type = AlertType.FLASH_INUNDATION,
                    severity = AlertSeverity.WARNING,
                    title = "Soil Saturation & Anaerobic Hazard",
                    message = "Soil moisture at $moisturePct% m³/m³ creates denitrification risks.",
                    parameterObserved = "Moisture: $moisturePct% (Saturation >= ${settings.moistureMaxPercent}%)",
                    recommendedAction = "Verify drainage tile outlets; delay heavy machinery passes to prevent deep compaction."
                )
            )
        }

        if (newAlerts.isNotEmpty()) {
            _alerts.update { current ->
                val existingTitles = current.map { it.title }.toSet()
                val uniqueNew = newAlerts.filterNot { it.title in existingTitles }
                uniqueNew + current
            }
        }
    }
}
