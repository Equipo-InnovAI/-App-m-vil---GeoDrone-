package com.geodrone.geodrone.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String? = null,
    val user: User? = null,
    val message: String? = null,
    val data: AuthData? = null
) {
    val authToken: String?
        get() = token.takeIf { !it.isNullOrBlank() } ?: data?.token
    val authUser: User?
        get() = user ?: data?.user
}

data class AuthData(
    val token: String? = null,
    val user: User? = null
)

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "operator"
)

data class ProjectRequest(
    val name: String,
    val description: String?,
    val location: String?,
    val status: String
)

data class FlightRequest(
    val name: String,
    val date: String,
    @SerializedName("altitude_m")
    val altitude: Double?,
    @SerializedName("overlap_percent")
    val overlap: Double?,
    val notes: String?,
    @SerializedName("project_id")
    val projectId: String
)

data class ImageUploadResponse(
    val message: String = "",
    val data: List<UploadedImageResult> = emptyList()
)

data class UploadedImageResult(
    val image: ApiImage? = null,
    val coordinate: ApiCoordinate? = null
)

data class ApiImage(
    val id: String = "",
    val filename: String = "",
    @SerializedName("original_name")
    val originalName: String? = null,
    val path: String = "",
    @SerializedName("captured_at")
    val capturedAt: String? = null,
    @SerializedName("flight_id")
    val flightId: String = "",
    @SerializedName("Coordinate")
    val coordinate: ApiCoordinate? = null
)

data class ApiCoordinate(
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerializedName("altitude_m")
    val altitude: Double? = null
)
