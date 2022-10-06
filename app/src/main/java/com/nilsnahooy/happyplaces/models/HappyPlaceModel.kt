package com.nilsnahooy.happyplaces.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "happyPlaces")
data class HappyPlaceModel (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String = "",
    var imageUri: String = "",
    var description: String = "",
    var date: String = "",
    var location: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
    )