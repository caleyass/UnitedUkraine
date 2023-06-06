package com.example.un.main.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.un.R
import com.example.un.data.model.Charity
import com.squareup.picasso.Picasso
import java.lang.IllegalArgumentException

open class ProductsListAdapter(
    private val context: Context,
    private var list: ArrayList<Charity>,
    private val fragment: Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
// END
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_list_layout2,
                parent,
                false
            )
        ) {}
    }


    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        val itemView = holder.itemView
        val imageView = itemView.findViewById<ImageView>(R.id.iv_item_image)
        val nameTextView = itemView.findViewById<TextView>(R.id.tv_item_name)
        val priceTextView = itemView.findViewById<TextView>(R.id.tv_item_price)
        //GlideLoader(context).loadProductPicture(model.image, imageView)
        try {
            Picasso.get()
                .load(model.image)
                .rotate(90f)
                .into(imageView)
        } catch (e:IllegalArgumentException){
        }
        nameTextView.text = model.title
        priceTextView.text = "$${model.goal}"
    }


    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}