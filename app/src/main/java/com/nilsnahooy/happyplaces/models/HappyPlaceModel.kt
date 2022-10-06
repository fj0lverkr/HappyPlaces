package com.nilsnahooy.happyplaces.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "happyPlaces")
data class HappyPlaceModel (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val imageUri: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
    )