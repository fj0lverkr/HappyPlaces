package com.nilsnahooy.happyplaces.database

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.nilsnahooy.happyplaces.HappyPlaceApp
import com.nilsnahooy.happyplaces.activities.AddPlaceActivity
import com.nilsnahooy.happyplaces.activities.MainActivity
import com.nilsnahooy.happyplaces.databinding.ItemHappyPlaceBinding
import com.nilsnahooy.happyplaces.models.HappyPlaceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class HappyPlaceAdapter(private val context: Context,
                             private val placesList: ArrayList<HappyPlaceModel>,
                             private val clickListener: (id: Int) -> Unit)
    : RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder>(){
        inner class ViewHolder(b: ItemHappyPlaceBinding): RecyclerView.ViewHolder(b.root){
            val clItem = b.clItemHappyPlace
            val civImage = b.civPlaceImage
            val tvTitle = b.tvHappyPlaceTitle
            val tvDescription = b.tvHappyPlaceDescription
            val tvDate = b.tvHappyPlaceDate
            val tvLocation = b.tvHappyPlaceLocation
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context),
        parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = placesList[position]

        holder.clItem.setBackgroundColor(
            if(position % 2 == 0){
                MaterialColors.getColor(holder.itemView,
                    com.google.android.material.R.attr.colorOnPrimary)
            } else {
                MaterialColors.getColor(holder.itemView,
                    com.google.android.material.R.attr.colorPrimaryContainer)
            }
        )

        val title = if (item.title?.length!! >= 20) {
            "${item.title?.subSequence(0, 17)}..."
        } else {
            item.title
        }

        val desc = if (item.description?.length!! >= 30) {
            "${item.description?.subSequence(0, 27)}..."
        } else {
            item.description
        }

        val loc = if (item.location?.length!! >= 20) {
            "${item.location?.subSequence(0, 17)}..."
        }else{
            item.location
        }

        holder.civImage.setImageURI(Uri.parse(item.imageUri))
        holder.tvTitle.text = title
        holder.tvDescription.text = desc
        holder.tvDate.text = item.date
        holder.tvLocation.text = loc

        holder.clItem.setOnClickListener {
           clickListener.invoke(item.id)
        }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    fun notifyOnEdit(activity: Activity, position: Int, requestCode: Int){
        val intent = Intent(context, AddPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, placesList[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun notifyOnDelete(position: Int){
        val application = HappyPlaceApp()
        val dao = application.db.happyPlaceDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.deleteHappyPlace(placesList[position])
            placesList.removeAt(position)
        }
        notifyItemRemoved(position)
    }
}