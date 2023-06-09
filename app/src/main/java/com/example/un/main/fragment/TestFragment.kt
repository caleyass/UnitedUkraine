package com.example.un.main.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.un.R
import com.example.un.databinding.FragmentTestBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class TestFragment : Fragment() {
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    /**
     * Called when the fragment should create its view hierarchy.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Returns the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root
    }
    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored into the view.
     *
     * @param view The View returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Handle the selected image URI
                binding.imageView.setImageURI(uri)
                val imageExtension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(this.requireActivity().contentResolver.getType(uri!!))
                val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                    "Image" + System.currentTimeMillis() + "."
                            + imageExtension
                )
                sRef.putFile(uri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        // The image upload is success
                        Log.e(
                            "Firebase Image URL",
                            taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                        )

                        // Get the downloadable url from the task snapshot
                        taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { url ->
                                Log.e("Downloadable Image URL", url.toString())

                                binding.textView2.text =
                                    "Your image uploaded successfully :: $url"
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this.requireContext(),
                                    exception.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e(javaClass.simpleName, exception.message, exception)
                            }
                    }
            } else {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.image_selection_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.imageView.setOnClickListener {
            pickImage()
        }
    }
    /**
     * Launches the image picker to select an image.
     */
    private fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }
}