package com.example.un.main.fragment

import android.content.ClipData
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import com.example.un.R
import com.example.un.data.Constants
import com.example.un.data.model.Charity
import com.example.un.databinding.FragmentCharitydetailsBinding
import com.example.un.firestore.FirestoreClass
import com.squareup.picasso.Picasso

class CharityDetailsFragment : Fragment() {
    private var _binding: FragmentCharitydetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var mSelectedImageFileUri: Uri? = null
    private var mCharityImageURL: String = ""
    private var imageChanged = false
    private var charity: Charity? = null

    private lateinit var charityId: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentCharitydetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        charityId = arguments?.getString("charity_id") ?: ""
        val owner = arguments?.getBoolean("owner") ?: false
        // Fetch the charity details from the database and populate the fields
        fetchCharityDetails()
        if(owner == false){
            binding.btnSubmit.visibility = View.GONE

            val editText = view.findViewById<EditText>(R.id.et_product_quantity)
            editText.isFocusable = false
            editText.isClickable = true

            editText.setOnClickListener {
                val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", editText.text.toString())
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context, "Зкопіював номер картки", Toast.LENGTH_SHORT).show()
            }

            binding.etProductTitle.isEnabled = false
            binding.etProductPrice.isEnabled = false
            binding.etProductDescription.isEnabled = false

        }
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
    /**
     * Fetches the charity details from Firestore using the charityId
     * and populates the fields accordingly.
     */
    private fun fetchCharityDetails() {
        // Retrieve the charity details using the charityId
        // and populate the fields accordingly

        // Example code to retrieve charity details from Firestore

        FirestoreClass().getCharity(this,charityId);

    }
    /**
     * Populates the input fields with the given charity details.
     *
     * @param charity The charity object containing the details to populate.
     */
    public fun populateFields(charity: Charity) {
        binding.etProductTitle.setText(charity.title)
        binding.etProductPrice.setText(charity.goal.toString())
        binding.etProductDescription.setText(charity.description)
        binding.etProductQuantity.setText(charity.card.toString())
        mSelectedImageFileUri = charity.image.toUri()
        this.charity = charity
        // Load the charity image using the image URL
        Picasso.get()
            .load(charity.image)
            .into(binding.ivProductImage)
    }
    /**
     * Updates the charity details in the database.
     */
    private fun updateCharityDetails() {
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
            "Не верифіковані",
            false,
            charityId
        )

        FirestoreClass().updateCharityDetails(this, product)
    }
    /**
     * Launches the image picker to select an image.
     */
    private fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }
    /**
     * Validates the entered charity details.
     *
     * @return True if the details are valid, false otherwise.
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
                true
            }
        }
    }
    /**
     * Validates the entered charity details.
     *
     * @return True if the details are valid, false otherwise.
     */
    private fun uploadCharityImage() {
        FirestoreClass().uploadImageToCloudStorage(
            this,
            mSelectedImageFileUri,
            Constants.PRODUCT_IMAGE
        )
    }
    /**
     * Callback function called when the image upload is successful.
     *
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
        imageChanged = true
        updateCharityDetails()
    }
    /**
     * Called when the view is destroyed. Cleans up any resources associated with the view.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
    /**
     * Companion object for the CharityDetailsFragment class.
     */
    public companion object {
        public const val ARG_CHARITY_ID = "charity_id"
        /**
         * Creates a new instance of CharityDetailsFragment.
         *
         * @param charityId The ID of the charity.
         * @param owner True if the user is the owner of the charity, false otherwise.
         * @return The newly created CharityDetailsFragment instance.
         */
        public fun newInstance(charityId: String, owner: Boolean): CharityDetailsFragment {
            val args = Bundle()
            args.putString(ARG_CHARITY_ID, charityId)
            args.putBoolean("owner", owner)
            val fragment = CharityDetailsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
