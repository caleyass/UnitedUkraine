package com.example.un.ui.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.un.data.LoginRepository
import com.example.un.data.Result

import com.example.un.R
import com.example.un.data.model.User
import com.example.un.ui.login.LoggedInUserView
import com.example.un.ui.login.LoginFormState
import com.example.un.ui.login.LoginResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    /**
     *Performs the login operation with the provided username and password.
     *@param username The username entered by the user.
     *@param password The password entered by the user.
     *@param coroutineScope The CoroutineScope used to launch the asynchronous job.
     */
    fun login(username: String, password: String, coroutineScope: CoroutineScope) {
        // can be launched in a separate asynchronous job
        coroutineScope.launch(Dispatchers.Default) {
            val result = loginRepository.login(username, password, coroutineScope)
            withContext(Dispatchers.Main) {
                if (result is Result.Success) {
                    _loginResult.value =
                        LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
                } else {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                }
            }
        }
    }

    /**
     *Performs the registration operation with the provided first name, last name, username, and password.
     *@param fName The first name entered by the user during registration.
     *@param lName The last name entered by the user during registration.
     *@param username The username entered by the user during registration.
     *@param password The password entered by the user during registration.
     *@param coroutineScope The CoroutineScope used to launch the asynchronous job.
     */
    fun register(fName:String, lName:String, username: String, password: String, coroutineScope: CoroutineScope) {
        // can be launched in a separate asynchronous job
        coroutineScope.launch(Dispatchers.Default) {
            val result = loginRepository.register(fName, lName, username, password, coroutineScope)
            withContext(Dispatchers.Main) {
                if (result is Result.Success) {
                    _loginResult.value =
                        LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
                } else {
                    //TODO CHECK WHETER THE USER EXISTS?
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                }
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}