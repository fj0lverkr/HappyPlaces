package com.nilsnahooy.happyplaces.activities

import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.nilsnahooy.happyplaces.HappyPlaceApp
import com.nilsnahooy.happyplaces.database.HappyPlaceAdapter
import com.nilsnahooy.happyplaces.database.HappyPlaceDao
import com.nilsnahooy.happyplaces.databinding.ActivityMainBinding
import com.nilsnahooy.happyplaces.models.HappyPlaceModel
import com.nilsnahooy.happyplaces.util.SwipeToDeleteCallback
import com.nilsnahooy.happyplaces.util.SwipeToEditCallback
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_PLACE_DETAILS = "extra_place_details"
        const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1685
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

        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = b?.rvPlacesItems?.adapter as HappyPlaceAdapter
                adapter.notifyOnEdit(this@MainActivity, viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = b?.rvPlacesItems?.adapter as HappyPlaceAdapter
                adapter.notifyOnDelete(viewHolder.adapterPosition)
            }
        }

        val editItemTouchHelper =  ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(b?.rvPlacesItems)

        val deleteItemTouchHelper =  ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(b?.rvPlacesItems)
    }

    private fun setupData(places: ArrayList<HappyPlaceModel>){
        if (places.isNotEmpty()){
            val adapter = HappyPlaceAdapter(this, places, ::goToItemDetail)
            b?.rvPlacesItems?.layoutManager = LinearLayoutManager(this)
            b?.rvPlacesItems?.adapter = adapter
            b?.rvPlacesItems?.visibility = View.VISIBLE
            b?.tvNoItems?.visibility = View.GONE
        } else {
            b?.rvPlacesItems?.visibility = View.GONE
            b?.tvNoItems?.visibility = View.VISIBLE
        }
    }

    private fun goToItemDetail(id: Int) {
        val intent = Intent(this, ItemDetailActivity::class.java)
        lifecycleScope.launch{
            dao.getHappyPlaceById(id).collect{
                intent.putExtra(EXTRA_PLACE_DETAILS, it)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        b = null
    }
}