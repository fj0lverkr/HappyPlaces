package com.nilsnahooy.happyplaces

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nilsnahooy.happyplaces.databinding.ActivityAddPlaceBinding
import com.nilsnahooy.happyplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var b: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b?.root)

        b?.fabAddPlace?.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        b = null
    }
}