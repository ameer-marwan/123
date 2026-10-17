package com.example.data.model

// --- Geospatial & Field Location ---
data class FieldLocation(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Int,
    val biome: String,
    val baselineSoilType: SoilType
)

object AgriculturalHubs {
    val PRESETS = listOf(
        FieldLocation("iowa", "Iowa Corn Belt", "United States", 41.8780, -93.0977, 280, "Temperate Grassland / Corn-Soybean Plain", SoilType.LOAM),
        FieldLocation("pampas", "Pampas Granary", "Argentina", -34.6037, -58.3816, 25, "Humid Pampas Chernozem", SoilType.LOAM),
        FieldLocation("punjab", "Punjab Wheat Belt", "India", 30.7333, 76.7794, 305, "Indo-Gangetic Alluvial Plain", SoilType.SILTY_CLAY),
        FieldLocation("cerrado", "Cerrado Agro-Planalto", "Brazil", -12.9714, -49.6200, 450, "Tropical Savannah Oxisol", SoilType.CLAY),
        FieldLocation("california", "Central Valley", "United States", 36.7378, -119.7871, 92, "Mediterranean Irrigated Basin", SoilType.SANDY_LOAM),
        FieldLocation("nile", "Nile Delta Basin", "Egypt", 30.8753, 31.0004, 18, "Subtropical Silt Sediment", SoilType.SILTY_CLAY),
        FieldLocation("ukraine", "Steppe Chernozem", "Ukraine", 48.3794, 31.1656, 175, "Black Soil Steppe", SoilType.LOAM),
        FieldLocation("rift_valley", "Great Rift Valley", "Kenya", -0.3031, 36.0800, 1850, "Volcanic Ash Highland", SoilType.PEAT),
        FieldLocation("bordeaux", "Aquitaine Basin", "France", 44.8378, -0.5792, 45, "Atlantic Oceanic Alluvial", SoilType.LOAM),
        FieldLocation("darling", "Darling Downs", "Australia", -27.5606, 151.9507, 430, "Subtropical Cracking Clays", SoilType.CLAY)
    )
}

// --- NASA Earth Observations & Telemetry ---
data class NasaTelemetry(
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val airTemperatureC: Double,
    val relativeHumidityPercent: Int,
    val uvIndex: Double,
    val precipitationForecastMm: Double,
    val solarIrradianceMj: Double,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Int,
    val windDirectionCompass: String,
    val windGustKmh: Double,
    val volumetricSoilMoistureM3: Double, // 0.05 to 0.50 m3/m3
    val topsoilTemperatureC: Double,
    val soilPh: Double,
    val nitrogenKgHa: Double,
    val phosphorusKgHa: Double,
    val potassiumKgHa: Double,
    val canopyCoverPercent: Int,
    val ndviIndex: Double, // 0.0 to 1.0
    val carbonSequestrationRateTonCo2: Double, // t CO2e/ha/yr
    val weatherAnomalyAlert: String? = null
)

// --- Soil Studio Customization ---
enum class SoilType(val label: String, val baseHealth: Int, val description: String) {
    LOAM("Loam", 88, "Balanced texture; optimal water retention and nutrient exchange."),
    CLAY("Clay", 72, "High nutrient holding; dense structure susceptible to compaction."),
    SANDY_LOAM("Sandy Loam", 68, "High aeration and drainage; rapid leaching risk."),
    SILTY_CLAY("Silty Clay", 75, "Fine sediment; fertile but prone to surface crusting."),
    PEAT("Peat", 82, "High organic content; acidic with extraordinary water capacity.")
}

enum class TillagePractice(val label: String, val soilConservationBonus: Int, val fuelReductionPercent: Int) {
    NO_TILL("Continuous No-Till", 25, 60),
    REDUCED_TILL("Strip / Reduced Till", 15, 35),
    CONVENTIONAL("Conventional Moldboard Tillage", 0, 0)
}

enum class IrrigationSystem(val label: String, val waterEfficiencyPercent: Int) {
    DRIP("Precision Drip Irrigation", 92),
    SPRINKLER("Center Pivot / Sprinkler", 75),
    RAINFED("Dryland / 100% Rainfed", 55)
}

data class SoilProfile(
    val soilType: SoilType = SoilType.LOAM,
    val drainageRateMmHr: Double = 18.0, // 2 to 60 mm/hr
    val bulkDensityGcm3: Double = 1.25, // 1.0 to 1.8 g/cm3
    val organicMatterPercent: Double = 3.8, // 0.5% to 8.0%
    val tillage: TillagePractice = TillagePractice.NO_TILL,
    val irrigation: IrrigationSystem = IrrigationSystem.DRIP
) {
    val soilHealthIndex: Int
        get() {
            var score = soilType.baseHealth
            score += tillage.soilConservationBonus
            score += ((organicMatterPercent - 2.0) * 5).toInt()
            if (bulkDensityGcm3 > 1.5) score -= 12
            if (irrigation == IrrigationSystem.DRIP) score += 6
            return score.coerceIn(20, 99)
        }

    val waterHoldingCapacityMm: Double
        get() = (organicMatterPercent * 18.5 + (if (soilType == SoilType.CLAY) 45.0 else 30.0)).coerceIn(30.0, 180.0)
}

// --- Smart Crop Rotation & Simulator ---
enum class CropCategory(val label: String) {
    CASH_CROP("Cash Crop"),
    LEGUME_FIXER("Nitrogen-Fixing Legume"),
    COVER_CROP("Cover & Bio-Armor Crop")
}

data class Crop(
    val id: String,
    val name: String,
    val category: CropCategory,
    val waterDemand: String, // Low, Medium, High
    val nitrogenFixationKgHa: Double, // positive = adds N, negative = depletes N
    val biomassYieldTonHa: Double,
    val carbonSequestrationTonHa: Double,
    val weedSuppressionPercent: Int,
    val baseYieldPerAcre: Double,
    val yieldUnit: String, // bu/acre, tons/acre, lbs/acre
    val avgMarketPricePerUnit: Double, // USD
    val operationalCostPerAcre: Double // USD
)

object CropCatalog {
    val ALL_CROPS = listOf(
        // Cash Crops
        Crop("corn", "Corn (Maize)", CropCategory.CASH_CROP, "High", -140.0, 14.5, 3.2, 55, 185.0, "bu/ac", 4.90, 680.0),
        Crop("soybean", "Soybeans", CropCategory.CASH_CROP, "Medium", 45.0, 6.2, 2.1, 70, 54.0, "bu/ac", 11.80, 470.0),
        Crop("wheat", "Winter Wheat", CropCategory.CASH_CROP, "Medium", -65.0, 7.8, 2.4, 75, 78.0, "bu/ac", 6.20, 390.0),
        Crop("cotton", "Upland Cotton", CropCategory.CASH_CROP, "Medium", -80.0, 4.5, 1.8, 50, 850.0, "lbs/ac", 0.78, 590.0),
        Crop("sorghum", "Grain Sorghum", CropCategory.CASH_CROP, "Low", -50.0, 8.5, 3.1, 65, 95.0, "bu/ac", 4.60, 360.0),
        Crop("sunflower", "Oilseed Sunflower", CropCategory.CASH_CROP, "Low", -45.0, 4.8, 2.0, 60, 1900.0, "lbs/ac", 0.28, 380.0),
        Crop("canola", "Spring Canola", CropCategory.CASH_CROP, "Medium", -70.0, 5.2, 2.3, 72, 42.0, "bu/ac", 13.50, 410.0),

        // Nitrogen-Fixing Legumes
        Crop("alfalfa", "Perennial Alfalfa", CropCategory.LEGUME_FIXER, "Medium", 180.0, 11.0, 4.2, 85, 4.8, "tons/ac", 210.0, 450.0),
        Crop("clover", "Crimson Clover", CropCategory.LEGUME_FIXER, "Low", 110.0, 5.5, 2.8, 88, 2.4, "tons/ac", 160.0, 240.0),
        Crop("chickpea", "Kabuli Chickpeas", CropCategory.LEGUME_FIXER, "Low", 70.0, 4.2, 1.9, 65, 1600.0, "lbs/ac", 0.35, 310.0),
        Crop("field_pea", "Austrian Winter Peas", CropCategory.LEGUME_FIXER, "Low", 95.0, 5.0, 2.5, 80, 2100.0, "lbs/ac", 0.19, 260.0),
        Crop("lentil", "Red Lentils", CropCategory.LEGUME_FIXER, "Low", 65.0, 3.8, 1.7, 60, 1400.0, "lbs/ac", 0.32, 280.0),

        // Cover & Bio-Armor Crops
        Crop("cereal_rye", "Cereal Rye Bio-Armor", CropCategory.COVER_CROP, "Low", -10.0, 7.2, 3.8, 94, 0.0, "cover", 0.0, 110.0),
        Crop("hairy_vetch", "Hairy Vetch", CropCategory.COVER_CROP, "Low", 135.0, 6.0, 3.0, 90, 0.0, "cover", 0.0, 130.0),
        Crop("radish", "Daikon Tillage Radish", CropCategory.COVER_CROP, "Low", 20.0, 8.0, 3.4, 91, 0.0, "cover", 0.0, 95.0),
        Crop("buckwheat", "Summer Buckwheat", CropCategory.COVER_CROP, "Low", 15.0, 4.5, 1.9, 92, 0.0, "cover", 0.0, 85.0)
    )
}

data class YearRotation(
    val yearIndex: Int,
    val mainCrop: Crop,
    val coverCrop: Crop? = null
)

data class PriorityWeights(
    val shortTermProfitVsSoilHealth: Float = 0.5f, // 0 = Max Soil Health, 1 = Max Profit
    val waterConservationVsBiomass: Float = 0.6f, // 0 = Max Biomass, 1 = Max Water Saving
    val carbonCaptureVsPestResistance: Float = 0.5f // 0 = Pest Break, 1 = Carbon Sequestration
)

data class RotationSimulationResult(
    val totalNitrogenSavingsKgHa: Double,
    val totalNitrogenSavingsUsd: Double,
    val weedSuppressionAvgPercent: Int,
    val waterFootprintReductionPercent: Int,
    val fiveYearSoilCarbonGainTons: Double,
    val soilHealthTrajectoryScore: List<Int>,
    val projectedNetProfitPerYearUsd: List<Double>,
    val farmResilienceRadar: Map<String, Float> // 0.0 to 1.0 (Soil, Water, Profit, Biodiversity, Climate)
)

// --- Financial Calculator ---
data class FinancialSettings(
    val farmAcreage: Double = 350.0,
    val dieselFuelPricePerGallon: Double = 3.90,
    val syntheticNitrogenPricePerKg: Double = 1.45,
    val carbonCreditPricePerTonCo2: Double = 32.0,
    val customInputCostAdjustmentPercent: Double = 0.0 // +/- %
)
