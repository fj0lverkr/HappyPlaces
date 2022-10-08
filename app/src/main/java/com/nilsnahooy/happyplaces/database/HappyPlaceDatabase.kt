package com.nilsnahooy.happyplaces.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nilsnahooy.happyplaces.models.HappyPlaceModel

@Database(entities = [HappyPlaceModel::class], version = 2)
abstract class HappyPlaceDatabase: RoomDatabase() {
    abstract fun happyPlaceDao(): HappyPlaceDao

    companion object{
        @Volatile
        private var INSTANCE: HappyPlaceDatabase? = null

        fun getInstance(c: Context): HappyPlaceDatabase{
            synchronized(this){
                var instance = INSTANCE
                if (instance == null){
                    instance =  Room.databaseBuilder(
                        c.applicationContext,
                        HappyPlaceDatabase::class.java,
                        "happyPlace_database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}