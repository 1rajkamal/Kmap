package com.breadcrumbradar.app.data.model

enum class LocationCategory(val displayName: String, val defaultIcon: String) {
    VEHICLE("Vehicle", "directions_car"),
    PARKING("Parking Spot", "local_parking"),
    TRAVEL("Hotel / Stay", "hotel"),
    PERSONAL("Personal Bag", "backpack"),
    OUTDOOR("Campsite & Trail", "terrain"),
    MEETING("Meeting Point", "groups"),
    GADGET("Device / Gear", "devices"),
    CUSTOM("Custom Marker", "place");

    companion object {
        fun fromString(name: String?): LocationCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CUSTOM
        }
    }
}
