package com.nilsnahooy.happyplaces.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nilsnahooy.happyplaces.databinding.ActivityMapViewBinding


class MapViewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var b:ActivityMapViewBinding? = null
    private var mLat = 0.0
    private var mLong = 0.0
    private var mTitle = "Location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMapViewBinding.inflate(layoutInflater)
        setContentView(b?.root)

        //setup actionbar
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra(ItemDetailActivity.EXTRA_LATITUDE)
            && intent.hasExtra(ItemDetailActivity.EXTRA_LONGITUDE)
            && intent.hasExtra(ItemDetailActivity.EXTRA_TITLE)) {
            mLat = intent.getDoubleExtra(ItemDetailActivity.EXTRA_LATITUDE, mLat)
            mLong = intent.getDoubleExtra(ItemDetailActivity.EXTRA_LONGITUDE, mLong)
            mTitle = intent.getStringExtra(ItemDetailActivity.EXTRA_TITLE)!!
            actionBar?.title = mTitle
        }

        //setup map
        val mapFragment = supportFragmentManager.findFragmentById(b?.fgmMap?.id!!)
                as SupportMapFragment?
        mapFragment?.getMapAsync(this)
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

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        val place = LatLng(mLat, mLong)
        mMap.addMarker(MarkerOptions().position(place).title(mTitle))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 15.0f))

    }

    override fun onDestroy() {
        super.onDestroy()
        b = null
    }
}