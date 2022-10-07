package com.nilsnahooy.happyplaces.database

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.nilsnahooy.happyplaces.databinding.ItemHappyPlaceBinding
import com.nilsnahooy.happyplaces.models.HappyPlaceModel

class HappyPlaceAdapter(private val placesList: ArrayList<HappyPlaceModel>,
                        private val deleteListener: (id:Int) -> Unit)
    : RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder>(){
        inner class ViewHolder(b: ItemHappyPlaceBinding): RecyclerView.ViewHolder(b.root){
            val clItem = b.clItemHappyPlace
            val civImage = b.civPlaceImage
            val tvTitle = b.tvHappyPlaceTitle
            val tvDescription = b.tvHappyPlaceDescription
            val btnDelete = b.btnDelete
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

        holder.civImage.setImageURI(Uri.parse(item.imageUri))
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description

        holder.btnDelete.setOnClickListener{
            deleteListener.invoke(item.id)
        }
    }

    override fun getItemCount(): Int {
        return placesList.size
    }
}