package com.example.un.main.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FirestoreClass().getCharityList(this)
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

}