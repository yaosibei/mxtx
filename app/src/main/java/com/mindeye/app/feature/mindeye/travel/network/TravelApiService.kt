package com.mindeye.app.feature.mindeye.travel.network

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 出行模块独立接口定义。
 * 先提供稳定的 Retrofit 契约，后续再按后端联调结果细化字段模型。
 */
interface TravelApiService {

    @POST("api/v1/travel/destination/resolve")
    suspend fun resolveDestination(@Body request: JsonObject): JsonObject

    @GET("api/v1/travel/env/check")
    suspend fun checkEnvironment(): JsonObject

    @POST("api/v1/travel/plan/generate")
    suspend fun generatePlan(@Body request: JsonObject): JsonObject

    @POST("api/v1/travel/location/report")
    suspend fun reportLocation(@Body request: JsonObject): JsonObject

    @POST("api/v1/travel/obstacle/report")
    suspend fun reportObstacle(@Body request: JsonObject): JsonObject

    @POST("api/v1/travel/route/replan")
    suspend fun replanRoute(@Body request: JsonObject): JsonObject
}
