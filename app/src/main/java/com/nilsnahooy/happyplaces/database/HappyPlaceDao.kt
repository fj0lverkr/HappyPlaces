package com.nilsnahooy.happyplaces.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nilsnahooy.happyplaces.models.HappyPlaceModel
import kotlinx.coroutines.flow.Flow

@Dao
interface HappyPlaceDao {
    @Query("SELECT * FROM 'happyPlaces'")
    fun getHappyPlaces(): Flow<List<HappyPlaceModel>>
    @Insert
    suspend fun insertHappyPlace(happyPlace: HappyPlaceModel)
    @Update
    suspend fun updateHappyPlace(happyPlace: HappyPlaceModel)
    @Delete
    suspend fun deleteHappyPlace(happyPlace: HappyPlaceModel)
}