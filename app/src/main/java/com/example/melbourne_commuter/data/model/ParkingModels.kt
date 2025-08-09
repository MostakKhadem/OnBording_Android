package com.example.melbourne_commuter.data.model


data class ParkingApiResponse(
    val total_count: Int,
    val results: List<ParkingRecord>
)

data class ParkingRecord(
    val lastupdated: String?,
    val status_timestamp: String?,
    val zone_number: Int?,
    val status_description: String?,
    val kerbsideid: Long?,
    val location: GeoPoint?
)

data class GeoPoint(
    val lon: Double,
    val lat: Double
)
