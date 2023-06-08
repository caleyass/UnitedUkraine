package com.example.un.main.adapter

import android.content.Context
import android.os.Bundle
import android.provider.Settings.Global.putString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.un.R
import com.example.un.data.model.Charity
import com.example.un.firestore.FirestoreClass
import com.example.un.main.fragment.CharityDetailsFragment
import com.squareup.picasso.Picasso
import java.lang.IllegalArgumentException

open class MyProductsListAdapter(
    private val context: Context,
    private var list: ArrayList<Charity>,
    private val fragment: Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
// END
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    //get spinner from parent


    return object : RecyclerView.ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_list_layout,
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
        val deleteButton = itemView.findViewById<ImageView>(R.id.ib_delete_product)
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


        if(model.user_id == FirestoreClass().getCurrentUserID()) {
            deleteButton.visibility = View.VISIBLE
            itemView.setOnClickListener(View.OnClickListener {
                //add notification with name
                Toast.makeText(context, "Clicked on ${model.id}", Toast.LENGTH_SHORT).show()
                // Replace with the actual charity ID
                //val fragment = CharityDetailsFragment.newInstance(model.id)
                // Navigate to the CharityDetailsFragment
                //get recyclerView and replace it with CharityDetailsFragment
                //clear recyclerView
                holder.itemView.visibility = View.GONE
                replaceFragment(
                    fragment.parentFragmentManager,
                    CharityDetailsFragment.newInstance(model.id)
                )            //fragment.findNavController().navigate(R.id.action_homeFragment_to_charityDetailsFragment)
            })
            deleteButton.setOnClickListener(View.OnClickListener {
                //add notification with name
                //Toast.makeText(context, "Clicked on ${model.id} delete", Toast.LENGTH_SHORT).show()
                FirestoreClass().deleteCharity(fragment, model.id)
            })
        }

    }

    fun replaceFragment(fragmentManager: FragmentManager, newFragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.container, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
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