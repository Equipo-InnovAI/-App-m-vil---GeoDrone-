package com.geodrone.geodrone.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Project(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val status: String = "active"
) : Serializable

data class Flight(
    val id: String = "",
    @SerializedName("project_id")
    val projectId: String = "",
    val name: String = "",
    val date: String = "",
    @SerializedName("altitude_m")
    val altitude: Double? = null,
    @SerializedName("overlap_percent")
    val overlap: Double? = null,
    val notes: String? = null
) : Serializable
