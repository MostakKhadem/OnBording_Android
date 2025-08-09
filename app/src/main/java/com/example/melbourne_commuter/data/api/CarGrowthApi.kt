//package com.example.melbourne_commuter.data.api
//
//
//import com.example.melbourne_commuter.data.model.CarGrowth
//import retrofit2.http.GET
//import retrofit2.http.Query
//
//interface CarGrowthApi {
//    @GET("api/car_growth")
//    suspend fun getCarGrowth(
//        @Query("from") from: Int,
//        @Query("to") to: Int
//    ): List<CarGrowth>
//}


package com.example.melbourne_commuter.data.api

import com.example.melbourne_commuter.data.model.CarGrowth
import retrofit2.http.GET
import retrofit2.http.Query

interface CarGrowthApi {
    @GET("cargrowth")
    suspend fun getCarGrowth(
        @Query("from") from: Int,
        @Query("to") to: Int
    ): List<CarGrowth>
}
