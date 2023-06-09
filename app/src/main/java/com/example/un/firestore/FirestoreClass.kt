package com.example.un.firestore

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.un.data.Constants
import com.example.un.data.model.Charity
import com.example.un.data.model.User
import com.example.un.main.adapter.MyProductsListAdapter
import com.example.un.main.fragment.AddCharityFragment
import com.example.un.main.fragment.CharityDetailsFragment
import com.example.un.main.fragment.CharityFragment
import com.example.un.main.fragment.MainFragment
import com.example.un.main.fragment.UserProfileFragment
import com.example.un.ui.login.fragment.LoginFragment
import com.example.un.ui.login.fragment.RegisterFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()
    fun registerUser(fragment: RegisterFragment, userInfo: User) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
            }
            .addOnFailureListener { e ->
                //activity.hideProgressDialog()
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }
    }

    fun uploadCharityDetails(fragment: AddCharityFragment, charityInfo: Charity) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.CHARITY)
            // Document ID for users fields. Here the document it is the User ID.
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(charityInfo, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(
                    fragment.requireContext(),
                    "Your charity is successfully created",
                    Toast.LENGTH_SHORT
                ).show()
                fragment.clearData()
            }
            .addOnFailureListener { e ->
                //activity.hideProgressDialog()
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }
    }

    fun updateCharityDetails(fragment: CharityDetailsFragment, charityInfo: Charity) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.CHARITY)
            // Document ID for the charity fields. Use the provided charityId.
            .document(charityInfo.id)
            // Set the charityInfo fields and use SetOptions.merge() to merge instead of replacing the fields.
            .set(charityInfo, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(
                    fragment.requireContext(),
                    "Your charity is successfully updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while updating the charity.",
                    e
                )
            }
    }

    fun deleteCharity(fragment: Fragment, charityId: String) {
        // The "charities" is the collection name.
        mFireStore.collection(Constants.CHARITY)
            // Document ID for the charity to delete. Use the provided charityId.
            .document(charityId)
            // Delete the charity document.
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    fragment.requireContext(),
                    "Charity deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while deleting the charity.",
                    e
                )
            }
    }


    fun uploadImageToCloudStorage(fragment: Fragment, imageFileURI: Uri?, imageType: String) {

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "."
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

                        // Here call a function of base activity for transferring the result to it.
                        when (fragment) {
                            is UserProfileFragment -> {
                                fragment.imageUploadSuccess(uri.toString())
                            }

                            is AddCharityFragment -> {
                                fragment.imageUploadSuccess(uri.toString())
                            }

                            is CharityDetailsFragment -> {
                                fragment.imageUploadSuccess(uri.toString())
                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->


                Log.e(
                    fragment.requireActivity().javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
    }

    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    fun getUserDetails(fragment: Fragment) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->

                Log.i(fragment.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!
                val sharedPreferences =
                    fragment.requireActivity().getSharedPreferences(
                        Constants.MYSHOPPAL_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    "${user.firstName} ${user.lastName}"
                )
                editor.apply()
                // START
                when (fragment) {
                    is LoginFragment -> {
                        // Call a function of base activity for transferring the result to it.
                        fragment.userLoggedInSuccess(user)
                    }
                }
                // END
            }
            .addOnFailureListener { e ->
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while getting user details.",
                    e
                )
            }
    }
    fun updateUserProfileData(fragment: Fragment, userHashMap: HashMap<String, Any>) {
        // Collection Name
        mFireStore.collection(Constants.USERS)
            // Document ID against which the data to be updated. Here the document id is the current logged in user id.
            .document(getCurrentUserID())
            // A HashMap of fields which are to be updated.
            .update(userHashMap)
            .addOnSuccessListener {
                // START
                // Notify the success result.
                when (fragment) {
                    is UserProfileFragment -> {
                        // Call a function of base activity for transferring the result to it.
                        fragment.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(
                    fragment.javaClass.simpleName,
                    "Error while updating the user details.",
                    e
                )
            }
    }

    fun getCharityList(fragment: Fragment) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.CHARITY)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Charity> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Charity::class.java)
                    product!!.id = i.id

                    productsList.add(product)
                }

                when (fragment) {
                    is MainFragment -> {
                        fragment.successProductsListFromFireStore(productsList)
                    }

                    is CharityFragment -> {
                        fragment.successCharityListFromFireStore(productsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }


    fun getCharity(fragment: Fragment,charityId: String) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.CHARITY)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())
                val productsList: ArrayList<Charity> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Charity::class.java)
                    product!!.id = i.id

                    productsList.add(product)
                }

                // Here we have created a new instance for Products ArrayList.
                val product = productsList.find { it.id == charityId }



                when (fragment) {
                    is CharityDetailsFragment -> {
                        fragment.populateFields(product!!)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }

    fun getWholeCharityList(fragment: Fragment) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.CHARITY)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Charity> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Charity::class.java)
                    product!!.id = i.id

                    productsList.add(product)
                }

                when (fragment) {
                    is MainFragment -> {
                        fragment.successProductsListFromFireStore(productsList)
                    }


                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }

    fun getCharityByCategoryList(fragment: Fragment, category: String) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.CHARITY)
            .whereEqualTo(Constants.CATEGORY, category)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Charity> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Charity::class.java)
                    product!!.id = i.id

                    productsList.add(product)
                }

                when (fragment) {
                    is MainFragment -> {
                        fragment.successProductsListFromFireStore(productsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }

    fun getCategories(fragment: Fragment) {
        val categoryList: ArrayList<String> = ArrayList()
        mFireStore.collection(Constants.CHARITY)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.


                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Charity::class.java)
                    product!!.id = i.id

                    if(!categoryList.contains(product.category)) {
                        categoryList.add(product.category)
                    }
                }

                when (fragment) {
                    is MainFragment -> {
                        fragment.populateDropdown(categoryList)
                    }
                }

            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }

}