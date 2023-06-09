package com.example.un.data

import com.example.un.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {

        user = null
        dataSource.logout()
    }

    suspend fun login(username: String, password: String, coroutineScope: CoroutineScope): Result<LoggedInUser> {
        return withContext(coroutineScope.coroutineContext) {
            val result = dataSource.login(username, password)

            if (result is Result.Success) {
                setLoggedInUser(result.data)
            }

            result
        }
    }

    suspend fun register(fName:String, lName:String, username: String, password: String, coroutineScope: CoroutineScope): Result<LoggedInUser> {
        return withContext(coroutineScope.coroutineContext) {
            val result = dataSource.register(fName, lName, username, password)

            if (result is Result.Success) {
                setLoggedInUser(result.data)
            }

            result
        }
    }


    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}