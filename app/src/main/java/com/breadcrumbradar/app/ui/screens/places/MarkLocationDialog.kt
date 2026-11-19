package com.breadcrumbradar.app.ui.screens.places

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.breadcrumbradar.app.data.model.LocationCategory
import com.breadcrumbradar.app.data.model.SavedLocation
import com.breadcrumbradar.app.sensor.GeoUtils
import com.breadcrumbradar.app.sensor.LocationState
import com.breadcrumbradar.app.ui.components.GlassCard
import com.breadcrumbradar.app.ui.theme.CyberCyan
import com.breadcrumbradar.app.ui.theme.ElectricEmerald

val categoryIconMap = mapOf(
    "directions_car" to Icons.Default.DirectionsCar,
    "local_parking" to Icons.Default.LocalParking,
    "hotel" to Icons.Default.Hotel,
    "backpack" to Icons.Default.Backpack,
    "terrain" to Icons.Default.Terrain,
    "groups" to Icons.Default.Groups,
    "devices" to Icons.Default.Devices,
    "place" to Icons.Default.Place,
    "star" to Icons.Default.Star,
    "flag" to Icons.Default.Flag,
    "home" to Icons.Default.Home,
    "store" to Icons.Default.Storefront
)

fun getCategoryIcon(iconName: String): ImageVector {
    return categoryIconMap[iconName] ?: Icons.Default.Place
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkLocationDialog(
    locationState: LocationState,
    initialLocation: SavedLocation? = null,
    onDismiss: () -> Unit,
    onSave: (SavedLocation) -> Unit
) {
    var name by remember { mutableStateOf(initialLocation?.name ?: "") }
    var selectedCategory by remember {
        mutableStateOf(
            LocationCategory.fromString(initialLocation?.category)
        )
    }
    var selectedIcon by remember { mutableStateOf(initialLocation?.iconName ?: selectedCategory.defaultIcon) }
    var notes by remember { mutableStateOf(initialLocation?.notes ?: "") }

    val quickNamePresets = listOf(
        "My Car", "Parked Spot", "Hotel", "Baggage", "Campsite", "Bike", "Shoes", "Meeting Spot"
    )

    // Current coordinates (or existing location's if editing)
    val latitude = initialLocation?.latitude ?: locationState.latitude
    val longitude = initialLocation?.longitude ?: locationState.longitude
    val altitude = initialLocation?.altitude ?: locationState.altitude
    val accuracy = initialLocation?.accuracy ?: locationState.accuracy

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialLocation == null) "Mark Location" else "Edit Location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // GPS Coordinate Summary Card
                GlassCard(contentPadding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GPS FIX ACCURACY",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricEmerald
                            )
                            Text(
                                text = GeoUtils.formatCoordinates(latitude, longitude),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "±%.1fm".format(accuracy),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                    }
                }

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location Name") },
                    placeholder = { Text("e.g. My Parked Car") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Quick Name Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickNamePresets) { preset ->
                        Surface(
                            modifier = Modifier.clickable {
                                name = preset
                                when (preset) {
                                    "My Car", "Parked Spot" -> {
                                        selectedCategory = LocationCategory.PARKING
                                        selectedIcon = "local_parking"
                                    }
                                    "Hotel" -> {
                                        selectedCategory = LocationCategory.TRAVEL
                                        selectedIcon = "hotel"
                                    }
                                    "Baggage", "Shoes" -> {
                                        selectedCategory = LocationCategory.PERSONAL
                                        selectedIcon = "backpack"
                                    }
                                    "Campsite" -> {
                                        selectedCategory = LocationCategory.OUTDOOR
                                        selectedIcon = "terrain"
                                    }
                                    "Meeting Spot" -> {
                                        selectedCategory = LocationCategory.MEETING
                                        selectedIcon = "groups"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = preset,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(LocationCategory.entries.toTypedArray()) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier.clickable {
                                selectedCategory = cat
                                selectedIcon = cat.defaultIcon
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = cat.displayName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Icon Picker
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categoryIconMap.keys.take(6).forEach { iconKey ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(iconKey),
                                contentDescription = iconKey,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Near pillar B-14, 2nd floor") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val finalName = if (name.isNotBlank()) name.trim() else "Saved Place"
                            val toSave = (initialLocation ?: SavedLocation(
                                name = finalName,
                                latitude = latitude,
                                longitude = longitude,
                                altitude = altitude,
                                accuracy = accuracy
                            )).copy(
                                name = finalName,
                                category = selectedCategory.name,
                                iconName = selectedIcon,
                                notes = notes.trim(),
                                latitude = latitude,
                                longitude = longitude,
                                altitude = altitude,
                                accuracy = accuracy
                            )
                            onSave(toSave)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (initialLocation == null) "Bookmark" else "Save")
                    }
                }
            }
        }
    }
}
