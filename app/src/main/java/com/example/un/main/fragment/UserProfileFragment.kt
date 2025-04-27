package com.example.un.main.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.un.MainActivity
import com.example.un.NavigationDrawerActivity
import com.example.un.R
import com.example.un.data.Constants
import com.example.un.data.Constants.uploadImageToCloudStorage
import com.example.un.data.model.User
import com.example.un.databinding.FragmentUserProfileBinding
import com.example.un.firestore.FirestoreClass
import com.example.un.ui.login.LoginViewModelFactory
import com.example.un.ui.login.fragment.LoginFragmentDirections
import com.example.un.ui.login.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var user: User? = null
    private var mSelectedImageFileUri:Uri? = null
    private var mUserProfileImageURL: String = ""
    private var openedFromFragment = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: UserProfileFragmentArgs by navArgs()
        user = args.extraUserDetails ?: this.requireActivity().intent.getParcelableExtra("user", User::class.java)
        if (args.extraUserDetails != null) {
            openedFromFragment = true
        }
        Log.d("MyTag", user.toString())
        binding.etFirstName.isEnabled = false
        binding.etFirstName.setText(user?.firstName)

        binding.etLastName.isEnabled = false
        binding.etLastName.setText(user?.lastName)

        binding.etEmail.isEnabled = false
        binding.etEmail.setText(user?.email)

        binding.etMobileNumber.setText(user?.mobile.toString())
        try {
            Picasso.get()
                .load(user?.image)
                .rotate(90f)
                .into(binding.ivUserPhoto)
        }
        catch (e:IllegalArgumentException){

        }

        if(user?.gender == "Male"){
            binding.rbMale.isChecked = true
        }
        else{
            binding.rbFemale.isChecked = true
        }
        binding.exit.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            openActivity2()
        }
        binding.ivUserPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                //Toast.makeText(this.requireContext(), "You already have the storage permission.", Toast.LENGTH_LONG).show()
                pickImage()
            } else {

                /*Requests permissions to be granted to this application. These permissions
                 must be requested in your manifest, they should not be granted to your app,
                 and they should have protection level*/
                requestStoragePermission()
            }
        }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Handle the selected image URI
                binding.ivUserPhoto.setImageURI(uri)
                this.mSelectedImageFileUri = uri
            } else {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.image_selection_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnSubmit.setOnClickListener {
            if(validateUserProfileDetails()){
                if(mSelectedImageFileUri!=null) {
                    uploadImageToCloudStorage(this, mSelectedImageFileUri)
                    updateUserProfileDetails()
                }
                if(openedFromFragment){
                    openActivity(user!!)
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted
                pickImage()
            } else {
                // Permission is not granted
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    private fun requestStoragePermission() {
        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }
    private fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }

    private fun validateUserProfileDetails(): Boolean {
        return when {

            // We have kept the user profile picture is optional.
            // The FirstName, LastName, and Email Id are not editable when they come from the login screen.
            // The Radio button for Gender always has the default selected value.

            // Check if the mobile number is not empty as it is mandatory to enter.
            TextUtils.isEmpty(binding.etMobileNumber.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this.requireContext(),
                    resources.getString(R.string.err_msg_enter_mobile_number),
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            else -> {
                true
            }
        }
    }

    fun userProfileUpdateSuccess() {

        // Hide the progress dialog
        //hideProgressDialog()

        Toast.makeText(
            this.requireContext(),
            resources.getString(R.string.msg_profile_update_success),
            Toast.LENGTH_SHORT
        ).show()


        // Redirect to the Main Screen after profile completion.
        /*val action = UserProfileFragmentDirections.actionUserProfileFragmentToMainFragment()
        findNavController().navigate(action)*/
        //openActivity()
    }
    fun imageUploadSuccess(imageURL: String) {

        // Hide the progress dialog

        Toast.makeText(
            this.requireActivity(),
            "Your image is uploaded successfully. Image URL is $imageURL",
            Toast.LENGTH_SHORT
        ).show()
        mUserProfileImageURL = imageURL
        updateUserProfileDetails()

    }
    private fun updateUserProfileDetails(){
        val userHashMap = HashMap<String, Any>()
        val mobileNumber = binding.etMobileNumber.text.toString().trim { it <= ' ' }

        val gender = if (binding.rbMale.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }

        if (mobileNumber.isNotEmpty()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }
        if (mUserProfileImageURL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageURL.toString()
        }
        userHashMap[Constants.GENDER] = gender
        userHashMap[Constants.COMPLETE_PROFILE] = 1

        // call the registerUser function of FireStore class to make an entry in the database.
        FirestoreClass().updateUserProfileData(
            this,
            userHashMap
        )
    }
    fun openActivity(user:User) {
        val intent = Intent(activity, NavigationDrawerActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }
    fun openActivity2() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }
}