package com.geodrone.geodrone.api

import com.geodrone.geodrone.model.Flight
import com.geodrone.geodrone.model.Project
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("api/auth/me")
    suspend fun currentUser(): User

    @GET("api/projects")
    suspend fun projects(): List<Project>

    @POST("api/projects")
    suspend fun createProject(@Body request: ProjectRequest): Project

    @GET("api/flights")
    suspend fun flights(@Query("project_id") projectId: String? = null): List<Flight>

    @POST("api/flights")
    suspend fun createFlight(@Body request: FlightRequest): Flight

    @GET("api/images")
    suspend fun images(@Query("flight_id") flightId: String): List<ApiImage>

    @Multipart
    @POST("api/images/upload")
    suspend fun uploadImages(
        @Part("flight_id") flightId: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): ImageUploadResponse
}
