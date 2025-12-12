package com.charmings.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val id: Int,
    val name: String,
    val description: String,
    val story: String,
    val imageResName: String,
    val requirementsDescription: String,
    val probability: Double,
    val requirements: List<Requirement>
)

@Serializable
data class Requirement(
    val steps: Int? = null,
    val day: String? = null,  // "monday", "tuesday", etc. or list like "saturday,friday"
    val month: List<String>? = null,  // ["january", "february", etc.]
    val time: List<Int>? = null,  // [startHour, endHour]
    val dateday: Int? = null,  // day of month
    val holiday: String? = null,
    val weather: List<String>? = null,
    val temperature: List<String>? = null,  // [min, max] where "_" means no limit
    val windDirection: List<String>? = null,
    val windSpeed: List<String>? = null,  // [min, max] where "_" means no limit
    val distance: Int? = null
)
