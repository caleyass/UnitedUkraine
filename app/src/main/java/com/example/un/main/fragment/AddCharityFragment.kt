package com.example.un.main.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.un.R
import com.example.un.data.Constants
import com.example.un.data.model.Charity
import com.example.un.databinding.FragmentAddcharityBinding
import com.example.un.firestore.FirestoreClass

class AddCharityFragment : Fragment() {
    private var _binding: FragmentAddcharityBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var mSelectedImageFileUri:Uri? = null
    private var mCharityImageURL: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentAddcharityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Handle the selected image URI
                binding.ivProductImage.setImageURI(uri)
                this.mSelectedImageFileUri = uri
            } else {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.image_selection_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.ivProductImage.setOnClickListener{
            pickImage()
        }
        binding.btnSubmit.setOnClickListener {
            if (validateCharityDetails()) {
                if (mSelectedImageFileUri != null) {
                    uploadCharityImage()
                }
            }
        }
    }



    private fun updateCharityDetails(){
        val userHashMap = HashMap<String, Any>()
        val title = binding.etProductTitle.text.toString().trim { it <= ' ' }
        val goal = binding.etProductPrice.text.toString().trim { it <= ' ' }
        val description = binding.etProductDescription.text.toString().trim { it <= ' ' }
        val card = binding.etProductQuantity.text.toString().trim { it <= ' ' }
        //TODO IMAGE UPDATE

        if (title.isNotEmpty()) {
            userHashMap[Constants.TITLE] = title
        }
        if (mCharityImageURL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mCharityImageURL.toString()
        }
        userHashMap[Constants.GOAL] = goal.toLong()
        userHashMap[Constants.DESCRIPTION] = description
        userHashMap[Constants.CARD] = card

        // call the registerUser function of FireStore class to make an entry in the database.
        FirestoreClass().updateUserProfileData(
            this,
            userHashMap
        )
    }

    /**
     * Launches the image picker to select an image.
     */
    private fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }

    /**
     * Validates the charity details before uploading.
     * @return true if all details are valid, false otherwise.
     */
    private fun validateCharityDetails(): Boolean {
        return when {

            mSelectedImageFileUri == null -> {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.err_msg_select_product_image),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            TextUtils.isEmpty(binding.etProductTitle.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.err_msg_enter_product_title),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            TextUtils.isEmpty(binding.etProductPrice.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.err_msg_enter_product_price),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            TextUtils.isEmpty(binding.etProductDescription.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.err_msg_enter_product_description),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            TextUtils.isEmpty(binding.etProductQuantity.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.err_msg_enter_product_quantity),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            else -> {
                if(binding.etProductQuantity.text.toString().length != 16 || binding.etProductQuantity.text.toString().any() { it.isLetter() }){
                    Toast.makeText(
                        this.requireContext(),
                        resources.getString(R.string.err_msg_enter_product_quantity),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
                else if(binding.etProductPrice.text.toString().any() { it.isLetter() }){
                    Toast.makeText(
                        this.requireContext(),
                        resources.getString(R.string.err_msg_enter_product_price),
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }
                true
            }
        }
    }
    /**
     * Uploads the charity image to the cloud storage.
     */
    private fun uploadCharityImage() {

        FirestoreClass().uploadImageToCloudStorage(
            this,
            mSelectedImageFileUri,
            Constants.PRODUCT_IMAGE
        )
    }

    /**
     * Callback function for successful image upload.
     * @param imageURL The URL of the uploaded image.
     */
    fun imageUploadSuccess(imageURL: String) {
        Toast.makeText(
            this.requireActivity(),
            "Your image is uploaded successfully. Image URL is $imageURL",
            Toast.LENGTH_SHORT
        ).show()
        // Initialize the global image url variable.
        mCharityImageURL = imageURL

        uploadCharityDetails()
    }

    /**
     * Uploads charity details to the database.
     *
     * @return True if the upload is successful, false otherwise.
     */
    private fun uploadCharityDetails() {

        // Get the logged in username from the SharedPreferences that we have stored at a time of login.
        val username =
            this.requireActivity().getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
                .getString(Constants.LOGGED_IN_USERNAME, "")!!
        Log.d("MyTag", username)
        // Here we get the text from editText and trim the space
        val title = binding.etProductTitle.text.toString().trim { it <= ' ' }
        val goal = binding.etProductPrice.text.toString().trim { it <= ' ' }.toLong()
        val description = binding.etProductDescription.text.toString().trim { it <= ' ' }
        val card = binding.etProductQuantity.text.toString().trim { it <= ' ' }.toLong()
        val category = "Не верифіковані"
        Log.d("MyTag", title)
        Log.d("MyTag", description)

        val product = Charity(
            FirestoreClass().getCurrentUserID(),
            username,
            title,
            goal,
            description,
            card,
            mCharityImageURL,
            category
        )
        FirestoreClass().uploadCharityDetails(this, product)
    }

    /**
     * A function to return the successful result of Product upload.
     */
    fun productUploadSuccess() {
        Toast.makeText(
            this.requireContext(),
            resources.getString(R.string.product_uploaded_success_message),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Clears the data in the input fields and resets the product image.
     */
    fun clearData() {
        binding.etProductTitle.text.clear()
        binding.etProductPrice.text.clear()
        binding.etProductDescription.text.clear()
        binding.etProductQuantity.text.clear()
        binding.ivProductImage.setImageDrawable(null)
    }

}