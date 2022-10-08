package com.nilsnahooy.happyplaces.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nilsnahooy.happyplaces.HappyPlaceApp
import com.nilsnahooy.happyplaces.R
import com.nilsnahooy.happyplaces.database.HappyPlaceAdapter
import com.nilsnahooy.happyplaces.database.HappyPlaceDao
import com.nilsnahooy.happyplaces.databinding.ActivityMainBinding
import com.nilsnahooy.happyplaces.models.HappyPlaceModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }

    private var b: ActivityMainBinding? = null
    private lateinit var dao: HappyPlaceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = (application as HappyPlaceApp).db.happyPlaceDao()
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b?.root)

        b?.fabAddPlace?.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivity(intent)
        }
        lifecycleScope.launch{
            dao.getAllHappyPlaces().collect{
                setupData(ArrayList(it))
            }
        }
    }

    private fun setupData(places: ArrayList<HappyPlaceModel>){
        if (places.isNotEmpty()){
            val adapter = HappyPlaceAdapter(places, ::showDeletePlaceDialog, ::goToItemDetail)
            b?.rvPlacesItems?.layoutManager = LinearLayoutManager(this)
            b?.rvPlacesItems?.adapter = adapter
            b?.rvPlacesItems?.visibility = View.VISIBLE
            b?.tvNoItems?.visibility = View.GONE
        } else {
            b?.rvPlacesItems?.visibility = View.GONE
            b?.tvNoItems?.visibility = View.VISIBLE
        }
    }

    private fun showDeletePlaceDialog(place: HappyPlaceModel){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.title_delete_record))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.lbl_yes)){ dialogInterface, _ ->
            lifecycleScope.launch {
                dao.deleteHappyPlace(place)
                Toast.makeText(applicationContext, getString(R.string.toast_delete_confirmed),
                    Toast.LENGTH_LONG).show()
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(getString(R.string.lbl_no)){ dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun goToItemDetail(id: Int) {
        val intent = Intent(this, ItemDetailActivity::class.java)
        lifecycleScope.launch{
            dao.getHappyPlaceById(id).collect{
                intent.putExtra(EXTRA_PLACE_DETAILS, it)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        b = null
    }
}