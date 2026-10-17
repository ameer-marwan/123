package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Yard
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.AgriculturalHubs
import com.example.data.model.FarmerProfile
import com.example.data.model.FieldLocation
import com.example.data.model.SavedFarm
import com.example.data.model.SoilProfile
import com.example.data.model.SoilType
import com.example.ui.localization.LocalizationProvider
import com.example.viewmodel.FieldShiftUiState
import com.example.viewmodel.FieldShiftViewModel

@Composable
fun UserDashboardScreen(
    viewModel: FieldShiftViewModel,
    uiState: FieldShiftUiState,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.farmerProfile.collectAsState()
    val savedFarms by viewModel.savedFarms.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddFarmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("user_dashboard_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Farmer Profile Card ---
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile.fullName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = profile.organizationName,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = profile.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Agriculture, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = profile.farmingSpecialty, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aggregate Holding KPIs
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val totalAcreage = savedFarms.sumOf { it.acreage }
                        val activeAlertCount = alerts.count { !it.isAcknowledged }

                        DashboardSummaryKpi(
                            title = "Total Scale",
                            value = "${totalAcreage.toInt()} ac",
                            sub = "${savedFarms.size} saved parcels",
                            modifier = Modifier.weight(1f)
                        )
                        DashboardSummaryKpi(
                            title = "Active Alerts",
                            value = "$activeAlertCount Alerts",
                            sub = if (activeAlertCount > 0) "Requires Review" else "All Clear",
                            highlightColor = if (activeAlertCount > 0) Color(0xFFFF5252) else Color(0xFF00E676),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 2. Saved Farms Section Header & Add Farm Button ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Farm Locations (${savedFarms.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select a parcel to load customized soil & rotation models",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddFarmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_new_farm_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Farm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 3. Saved Farms List ---
        items(savedFarms, key = { it.id }) { farm ->
            val isActive = profile.activeFarmId == farm.id
            SavedFarmCard(
                farm = farm,
                isActive = isActive,
                onActivate = { viewModel.switchActiveFarm(farm.id) },
                onDelete = { viewModel.deleteSavedFarm(farm.id) },
                canDelete = savedFarms.size > 1
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // --- DIALOG: Edit Farmer Profile ---
    if (showEditProfileDialog) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, org, specialty ->
                viewModel.updateProfile(name, email, org, specialty)
                showEditProfileDialog = false
            }
        )
    }

    // --- DIALOG: Add New Farm ---
    if (showAddFarmDialog) {
        AddNewFarmDialog(
            onDismiss = { showAddFarmDialog = false },
            onSave = { name, desc, hubPreset, customAcreage, soilType ->
                val newFarm = SavedFarm(
                    name = name,
                    description = desc,
                    location = hubPreset,
                    acreage = customAcreage,
                    soilProfile = SoilProfile(soilType = soilType)
                )
                viewModel.updateExistingFarm(newFarm)
                viewModel.switchActiveFarm(newFarm.id)
                showAddFarmDialog = false
            }
        )
    }
}

@Composable
private fun SavedFarmCard(
    farm: SavedFarm,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = farm.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF00E676),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (canDelete && !isActive) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Farm", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (farm.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = farm.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge("Acreage", "${farm.acreage.toInt()} ac", modifier = Modifier.weight(1f))
                SpecBadge("Soil Health", "${farm.soilProfile.soilHealthIndex}/100", Color(0xFF00E676), modifier = Modifier.weight(1f))
                SpecBadge("Texture", farm.soilProfile.soilType.label, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 ${farm.location.name} (${String.format("%.2f°N, %.2f°E", farm.location.latitude, farm.location.longitude)})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isActive) {
                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Activate Farm & Load Telemetry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DashboardSummaryKpi(title: String, value: String, sub: String, highlightColor: Color = MaterialTheme.colorScheme.onSurface, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = highlightColor)
            Text(sub, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: FarmerProfile,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(profile.fullName) }
    var email by remember { mutableStateOf(profile.email) }
    var org by remember { mutableStateOf(profile.organizationName) }
    var specialty by remember { mutableStateOf(profile.farmingSpecialty) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit Farmer Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = org,
                    onValueChange = { org = it },
                    label = { Text("Farm / Enterprise Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Farming Focus / Specialty") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, email, org, specialty) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddNewFarmDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, FieldLocation, Double, SoilType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var acreageText by remember { mutableStateOf("320") }
    var selectedHub by remember { mutableStateOf(AgriculturalHubs.PRESETS.first()) }
    var selectedSoilType by remember { mutableStateOf(SoilType.LOAM) }
    var showHubMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add New Farm Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Save coordinates, soil profile, and rotation plans", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Farm / Parcel Name") },
                    placeholder = { Text("e.g. Hilltop North 160") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Notes / Description") },
                    placeholder = { Text("e.g. Strip-till corn and rye cover") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = acreageText,
                    onValueChange = { acreageText = it },
                    label = { Text("Parcel Acreage") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Region selector
                Text("Agro-Climatic Hub:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box {
                    Surface(
                        onClick = { showHubMenu = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${selectedHub.name} (${selectedHub.country})", fontSize = 12.sp)
                            Text("▼", fontSize = 10.sp)
                        }
                    }
                    DropdownMenu(expanded = showHubMenu, onDismissRequest = { showHubMenu = false }) {
                        AgriculturalHubs.PRESETS.forEach { hub ->
                            DropdownMenuItem(
                                text = { Text("${hub.name} (${hub.country})") },
                                onClick = {
                                    selectedHub = hub
                                    selectedSoilType = hub.baselineSoilType
                                    showHubMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val acreage = acreageText.toDoubleOrNull() ?: 320.0
                            val validName = if (name.isBlank()) "Custom Farm Plot" else name
                            onSave(validName, desc, selectedHub, acreage, selectedSoilType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Create Farm")
                    }
                }
            }
        }
    }
}
