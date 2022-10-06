package com.nilsnahooy.happyplaces

import android.app.Application
import android.content.res.Resources
import com.nilsnahooy.happyplaces.database.HappyPlaceDatabase

class HappyPlaceApp: Application() {
    val db by lazy {
        HappyPlaceDatabase.getInstance(this)
    }

    companion object{
        lateinit var res: Resources private set
    }

    override fun onCreate() {
        super.onCreate()
        res = resources
    }
}