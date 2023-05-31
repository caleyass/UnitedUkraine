package com.example.un.data


import com.example.un.data.model.LoggedInUser
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
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

    suspend fun register(username: String, password: String): Result<LoggedInUser> = suspendCancellableCoroutine { continuation ->
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener(
                OnCompleteListener<AuthResult> { task ->
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
            )
        }
}