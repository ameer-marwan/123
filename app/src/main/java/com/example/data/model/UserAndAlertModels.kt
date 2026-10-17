package com.example.data.model

import java.util.UUID

// --- User Profile System ---
data class FarmerProfile(
    val id: String = "farmer_01",
    val fullName: String = "Sarah Jenkins",
    val email: String = "sarah.jenkins@fieldshift-agri.com",
    val organizationName: String = "Jenkins Regenerative Horizons",
    val farmingSpecialty: String = "Conservation Grain & Cover-Crop Rotations",
    val country: String = "United States",
    val preferredLanguage: String = "en",
    val memberSince: String = "March 2024",
    val activeFarmId: String = "farm_iowa_01"
)

// --- Saved Farm Entity ---
data class SavedFarm(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val location: FieldLocation,
    val acreage: Double = 320.0,
    val soilProfile: SoilProfile = SoilProfile(),
    val rotationPlan: List<YearRotation> = listOf(
        YearRotation(1, CropCatalog.ALL_CROPS.first { it.id == "corn" }, CropCatalog.ALL_CROPS.first { it.id == "cereal_rye" }),
        YearRotation(2, CropCatalog.ALL_CROPS.first { it.id == "soybean" }, CropCatalog.ALL_CROPS.first { it.id == "radish" }),
        YearRotation(3, CropCatalog.ALL_CROPS.first { it.id == "wheat" }, CropCatalog.ALL_CROPS.first { it.id == "clover" })
    ),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

// --- Alert System Models ---
enum class AlertSeverity(val label: String) {
    CRITICAL("Critical Threat"),
    WARNING("Advisory Warning"),
    INFO("Agronomic Notice")
}

enum class AlertType(val displayName: String) {
    FROST_HAZARD("Ground Frost & Freeze Threat"),
    HEATWAVE_STRESS("Extreme Heatwave & Evaporative Deficit"),
    SOIL_MOISTURE_CRITICAL("Critical Soil Moisture Anomaly"),
    SOIL_TEMP_SPIKE("Rhizosphere Temperature Spike"),
    NDVI_ANOMALY_DROP("NDVI Vegetation Biomass Deficit"),
    FLASH_INUNDATION("Flash Inundation & Leaching Threat")
}

data class FarmAlert(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val farmName: String,
    val type: AlertType,
    val severity: AlertSeverity,
    val title: String,
    val message: String,
    val parameterObserved: String,
    val recommendedAction: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAcknowledged: Boolean = false,
    val isSnoozed: Boolean = false
)

enum class AlertFrequency(val label: String) {
    INSTANT("Instant Dispatch"),
    DAILY_DIGEST("Daily 06:00 AM Digest")
}

data class AlertNotificationSettings(
    val pushNotificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val emailAddress: String = "sarah.jenkins@fieldshift-agri.com",
    val alertFrequency: AlertFrequency = AlertFrequency.INSTANT,
    val frostThresholdC: Double = 2.0,
    val heatwaveThresholdC: Double = 33.0,
    val moistureMinPercent: Int = 14,
    val moistureMaxPercent: Int = 42,
    val ndviDropThreshold: Double = 0.15
)
