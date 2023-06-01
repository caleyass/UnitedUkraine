package com.example.un.data


import com.example.un.data.model.LoggedInUser
import com.example.un.data.model.User
import com.example.un.firestore.FirestoreClass
import com.example.un.ui.login.fragment.RegisterFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    suspend fun login(username: String, password: String): Result<LoggedInUser> = suspendCancellableCoroutine { continuation ->
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                val result = if (task.isSuccessful) {
                    Result.Success(
                        LoggedInUser(
                            FirebaseAuth.getInstance().currentUser!!.uid,
                            username
                        )
                    )
                } else {
                    Result.Error(IOException(task.exception!!.message.toString()))
                }

                continuation.resume(result)

            }
    }


    fun logout() {
        // TODO: revoke authentication
    }

    suspend fun register(fName:String, lName:String, email: String, password: String): Result<LoggedInUser> = suspendCancellableCoroutine { continuation ->
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->
                    val result = if (task.isSuccessful) {
                        var firebaseUser: FirebaseUser = task.result!!.user!!
                        val user = User(
                            firebaseUser.uid,
                            fName,
                            lName,
                            email
                        )
                        FirestoreClass().registerUser(RegisterFragment(),user)
                        Result.Success(
                            LoggedInUser(
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                email
                            )
                        )
                    } else {
                        Result.Error(IOException(task.exception!!.message.toString()))
                    }

                    continuation.resume(result)
                }
            )
        }
}