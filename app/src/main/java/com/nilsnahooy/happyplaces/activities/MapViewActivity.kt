package com.nilsnahooy.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.addCallback
import com.nilsnahooy.happyplaces.databinding.ActivityMapViewBinding

class MapViewActivity : AppCompatActivity() {

    private var b:ActivityMapViewBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(b?.root)

        //setup actionbar
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //override back navigation as we do not want to have other actions
        // open this Activity unwanted (which they did for some unknown reason...)
        val callback = this.onBackPressedDispatcher.addCallback(this) {
            intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        callback.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        b = null
    }
}