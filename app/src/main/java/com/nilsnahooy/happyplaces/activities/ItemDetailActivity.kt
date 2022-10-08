package com.nilsnahooy.happyplaces.activities

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
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
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                        as HappyPlaceModel?
            }
            actionBar?.title = "${place?.title}"
            b?.ivPlaceImage?.setImageURI(Uri.parse(place?.imageUri))
        } else {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
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