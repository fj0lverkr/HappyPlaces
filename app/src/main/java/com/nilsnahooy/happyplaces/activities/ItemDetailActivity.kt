package com.nilsnahooy.happyplaces.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import com.nilsnahooy.happyplaces.databinding.ActivityItemDetailBinding
import com.nilsnahooy.happyplaces.models.HappyPlaceModel

class ItemDetailActivity : AppCompatActivity() {

    private var b: ActivityItemDetailBinding?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val place: HappyPlaceModel?
        super.onCreate(savedInstanceState)
        b = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(b?.root)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            place = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
               intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS,
               HappyPlaceModel::class.java)
            }else {
                //ignored since newer versions are handled above
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                        as HappyPlaceModel?
            }
            actionBar?.title = "${place?.title}"
            b?.ivPlaceImage?.setImageURI(Uri.parse(place?.imageUri))

            //override back navigation as we do not want to have other actions
            // open this Activity unwanted (which they did for some unknown reason...)
            val callback = this.onBackPressedDispatcher.addCallback(this) {
                intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            callback.isEnabled = true

        } else {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        b = null
    }
}