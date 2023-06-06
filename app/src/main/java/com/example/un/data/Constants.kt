package com.example.un.data

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.un.main.fragment.AddCharityFragment
import com.example.un.main.fragment.UserProfileFragment
import com.example.un.ui.login.SharedViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object Constants {

    const val USERS:String = "users"

    const val MYSHOPPAL_PREFERENCES: String = "MyPrefs"
    const val LOGGED_IN_USERNAME: String = "logged_in_username"
    const val EXTRA_USER_DETAILS: String = "extra_user_details"
    const val READ_STORAGE_PERMISSION_CODE = 2
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val COMPLETE_PROFILE:String = "profileCompleted"

    const val MALE: String = "Male"
    const val FEMALE: String = "Female"

    // Firebase database field names
    const val MOBILE: String = "mobile"
    const val GENDER: String = "gender"
    const val IMAGE: String = "image"

    const val TITLE:String = "title"
    const val CHARITY:String = "charity"
    const val CARD: String = "card"
    const val CATEGORY: String = "category"
    const val DESCRIPTION: String = "description"
    const val GOAL: String = "card"
    const val PRODUCT_IMAGE: String = "Product_Image"
    const val USER_ID = "user_id"


    const val USER_PROFILE_IMAGE:String = "User_Profile_Image"

    /*fun showImageChooser(activity: Activity) {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }*/

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

    fun uploadImageToCloudStorage(fragment: Fragment, imageFileURI: Uri?) {

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            Constants.USER_PROFILE_IMAGE + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(
                fragment.requireActivity(),
                imageFileURI
            )
        )

        //adding the file to reference
        sRef.putFile(imageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // TODO Step 8: Pass the success result to base class.
                        // START
                        // Here call a function of base activity for transferring the result to it.
                        when (fragment) {
                            is UserProfileFragment -> {
                                fragment.imageUploadSuccess(uri.toString())
                            }
                            is AddCharityFragment->{
                                fragment.imageUploadSuccess(uri.toString())
                            }
                        }
                        // END
                    }
            }
            .addOnFailureListener { exception ->

                // Hide the progress dialog if there is any error. And print the error in log.
                /*when (fragment) {
                    is UserProfileFragment -> {
                        activity.hideProgressDialog()
                    }
                }*/

                Log.e(
                    fragment.javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
    }
}