package com.breadcrumbradar.app.data.model

enum class LocationCategory(val displayName: String, val defaultIcon: String) {
    CAR("My Car", "directions_car"),
    KEYS("My Keys", "vpn_key"),
    SHOES("My Shoes", "directions_walk"),
    BAG("My Bag", "backpack"),
    HOTEL("My Hotel", "hotel"),
    MEETING("Meeting Point", "groups"),
    BIKE("My Bike", "two_wheeler"),
    PARKING("Parking Spot", "local_parking"),
    OUTDOOR("Campsite & Nature", "terrain"),
    CUSTOM("Custom Marker", "place");

    companion object {
        fun fromString(name: String?): LocationCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CUSTOM
        }
    }
}
