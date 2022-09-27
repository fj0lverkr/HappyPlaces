package com.nilsnahooy.happyplaces

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nilsnahooy.happyplaces.databinding.ActivityAddPlaceBinding

class AddPlaceActivity : AppCompatActivity() {
    private var b: ActivityAddPlaceBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(b?.root)
    }

    override fun onDestroy() {
        super.onDestroy()

        b = null
    }
}