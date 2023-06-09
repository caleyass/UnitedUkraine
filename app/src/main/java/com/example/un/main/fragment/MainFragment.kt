package com.example.un.main.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.un.R
import com.example.un.data.Constants
import com.example.un.data.model.Charity
import com.example.un.databinding.FragmentMainBinding
import com.example.un.firestore.FirestoreClass
import com.example.un.main.adapter.MyProductsListAdapter

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        FirestoreClass().getCategories(this)
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //FirestoreClass().getWholeCharityList(this)
    }

    fun successProductsListFromFireStore(productsList: ArrayList<Charity>) {

        // Hide Progress dialog.
        for(i in productsList){
            Log.d("MyTag", i.title)
        }

        if (productsList.size > 0) {
            binding.rvDashboardItems.visibility = View.VISIBLE
            binding.tvNoDashboardItemsFound.visibility = View.GONE

            binding.rvDashboardItems.layoutManager = LinearLayoutManager(activity)
            binding.rvDashboardItems.setHasFixedSize(true)

            // TODO Step 7: Pass the third parameter value.
            // START
            val adapterProducts =
                MyProductsListAdapter(requireActivity(), productsList, this)
            // END
            binding.rvDashboardItems.adapter = adapterProducts
        } else {
            binding.rvDashboardItems.visibility = View.GONE
            binding.tvNoDashboardItemsFound.visibility = View.VISIBLE
        }
    }

    fun populateDropdown(categories: ArrayList<String>){
        val spinner: Spinner = this.view?.findViewById(R.id.spinner) as Spinner
        val adapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle the selection change here
                val selectedItem = parent?.getItemAtPosition(position).toString()
                FirestoreClass().getCharityByCategoryList(this@MainFragment, selectedItem)
                // Do something with the selected item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected
            }
        }
    }

}