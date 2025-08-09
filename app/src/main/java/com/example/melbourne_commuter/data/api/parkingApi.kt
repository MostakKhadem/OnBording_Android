//package com.example.melbourne_commuter.data.api
//
//
//import com.example.melbourne_commuter.data.model.ParkingApiResponse
//import retrofit2.http.GET
//import retrofit2.http.Query
//
//interface ParkingApi {
//
//    @GET("api/explore/v2.1/catalog/datasets/on-street-parking-bay-sensors/records")
//    suspend fun getUnoccupied(
//        @Query("where") where: String = "status_description='Unoccupied'",
//        @Query("order_by") orderBy: String = "status_timestamp DESC",
//        @Query("limit") limit: Int = 50,
//        @Query("timezone") timezone: String = "Australia/Melbourne"
//    ): ParkingApiResponse
//}




package com.example.melbourne_commuter.data.api

import com.example.melbourne_commuter.data.model.ParkingApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ParkingApi {

    @GET("api/explore/v2.1/catalog/datasets/on-street-parking-bay-sensors/records")
    suspend fun getUnoccupied(
        @Query("where") where: String = "status_description='Unoccupied'",
        @Query("order_by") orderBy: String = "status_timestamp DESC",
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query(value = "timezone", encoded = true) timezone: String = "Australia%2FMelbourne"
    ): ParkingApiResponse
}
