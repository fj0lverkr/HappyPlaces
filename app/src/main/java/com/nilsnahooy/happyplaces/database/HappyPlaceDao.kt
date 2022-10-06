package com.nilsnahooy.happyplaces.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nilsnahooy.happyplaces.models.HappyPlaceModel

@Dao
interface HappyPlaceDao {
    @Query("SELECT * FROM happyPlaces")
    fun getHappyPlaces(): List<HappyPlaceModel>
    @Insert
    fun insertHappyPlace(happyPlace: HappyPlaceModel)
}