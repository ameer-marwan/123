package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AgriculturalHubs
import com.example.data.model.AlertSeverity
import com.example.data.model.CropCatalog
import com.example.data.model.NasaTelemetry
import com.example.data.model.SavedFarm
import com.example.data.model.SoilProfile
import com.example.data.model.SoilType
import com.example.data.remote.NasaPowerService
import com.example.data.repository.FarmRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FieldShift", appName)
  }

  @Test
  fun `soil health calculation is robust`() {
    val profile = SoilProfile(soilType = SoilType.LOAM, organicMatterPercent = 4.2)
    assertTrue("Soil health score should be above 70", profile.soilHealthIndex > 70)
    assertTrue("Water holding capacity should be positive", profile.waterHoldingCapacityMm > 50.0)
  }

  @Test
  fun `nasa planetary telemetry models valid agro ranges`() {
    val telemetry = NasaPowerService.generatePlanetaryTelemetry(41.8780, -93.0977)
    assertNotNull(telemetry)
    assertTrue("Soil moisture within biological range", telemetry.volumetricSoilMoistureM3 in 0.05..0.50)
    assertTrue("NDVI within satellite index range", telemetry.ndviIndex in 0.1..1.0)
  }

  @Test
  fun `farmer profile and saved farms are accessible`() {
    val profile = FarmRepository.farmerProfile.value
    assertNotNull(profile)
    assertEquals("Sarah Jenkins", profile.fullName)

    val farms = FarmRepository.savedFarms.value
    assertTrue("Multiple farms exist in profile", farms.size >= 2)

    val initialFarm = farms.first()
    FarmRepository.setActiveFarm(initialFarm.id)
    assertEquals(initialFarm.id, FarmRepository.farmerProfile.value.activeFarmId)
  }

  @Test
  fun `real-time alert evaluation triggers on extreme weather and moisture deficit`() {
    val farm = FarmRepository.savedFarms.value.first()
    val frostTelemetry = NasaTelemetry(
      latitude = 41.8780,
      longitude = -93.0977,
      airTemperatureC = 0.5,
      relativeHumidityPercent = 80,
      uvIndex = 1.0,
      precipitationForecastMm = 0.0,
      solarIrradianceMj = 6.0,
      windSpeedKmh = 12.0,
      windDirectionDegrees = 180,
      windDirectionCompass = "S",
      windGustKmh = 16.0,
      volumetricSoilMoistureM3 = 0.10, // Deficit < 14%
      topsoilTemperatureC = 1.2, // Frost < 2.0C
      soilPh = 6.8,
      nitrogenKgHa = 70.0,
      phosphorusKgHa = 30.0,
      potassiumKgHa = 180.0,
      canopyCoverPercent = 40,
      ndviIndex = 0.45,
      carbonSequestrationRateTonCo2 = 2.1
    )

    FarmRepository.evaluateLiveTelemetryAlerts(farm, frostTelemetry)
    val alerts = FarmRepository.alerts.value
    assertTrue("Should trigger alerts for frost or soil deficit", alerts.isNotEmpty())
  }
}
