package com.example.un.ui.login.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.un.R
import com.example.un.data.Constants
import com.example.un.data.LoginDataSource
import com.example.un.data.model.User
import com.example.un.databinding.FragmentRegisterBinding
import com.example.un.firestore.FirestoreClass
import com.example.un.ui.login.LoggedInUserView
import com.example.un.ui.login.LoginViewModelFactory
import com.example.un.ui.login.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth


class RegisterFragment : Fragment() {
    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = "Register"

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                binding.progressBar.visibility = View.GONE
                loginResult.error?.let {
                    showRegisterFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                }
            })
        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginFormState.usernameError?.let {
                    binding.etEmail.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    binding.etPassword.error = getString(it)
                }
            })
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }
    /**
     * Validates the registration details entered by the user.
     * Displays error messages if any of the fields are empty or if the passwords do not match.
     * Also checks if the terms and conditions checkbox is checked.
     * Returns true if all the validations pass, false otherwise.
     *
     * @return True if the registration details are valid, false otherwise.
     */
    private fun validateRegisterDetails(): Boolean {
        val appContext = context?.applicationContext
        return when {
            TextUtils.isEmpty(binding.etFirstName.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(appContext, resources.getString(R.string.err_msg_enter_first_name), Toast.LENGTH_LONG).show()
                false
            }
            TextUtils.isEmpty(binding.etLastName.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(appContext, resources.getString(R.string.err_msg_enter_last_name), Toast.LENGTH_LONG).show()
                false
            }

            TextUtils.isEmpty(binding.etEmail.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(appContext, resources.getString(R.string.err_msg_enter_email), Toast.LENGTH_LONG).show()
                false
            }

            TextUtils.isEmpty(binding.etPassword.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(appContext, resources.getString(R.string.err_msg_enter_password), Toast.LENGTH_LONG).show()
                false
            }

            TextUtils.isEmpty(binding.etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(appContext, resources.getString(R.string.err_msg_enter_confirm_password), Toast.LENGTH_LONG).show()
                false
            }

            binding.etPassword.text.toString().trim { it <= ' ' } != binding.etConfirmPassword.text.toString()
                .trim { it <= ' ' } -> {
                Toast.makeText(appContext, resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), Toast.LENGTH_LONG).show()
                false
            }
            !binding.cbTermsAndCondition.isChecked -> {
                Toast.makeText(appContext, resources.getString(R.string.err_msg_agree_terms_and_condition), Toast.LENGTH_LONG).show()
                false
            }
            else -> {
                // TODO Step 4: Remove this success message as we are now validating and registering the user.
                true
            }
        }
    }
    /**
     * Registers a new user with the provided details.
     * Validates the entries and initiates the registration process.
     * If the entries are valid, the user is registered with the provided email and password.
     * After successful registration, the user is signed out and the progress bar is shown.
     * Note: Make sure to call validateRegisterDetails() before calling this function.
     */
    private fun registerUser() {

        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails()) {
            binding.progressBar.visibility = View.VISIBLE

            val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
            val password: String = binding.etPassword.text.toString().trim { it <= ' ' }
            val fName  = binding.etFirstName.text.toString().trim { it <= ' ' }
            val lName = binding.etLastName.text.toString().trim { it <= ' ' }
            // Create an instance and create a register a user with email and password.

            loginViewModel.register(fName, lName, email, password, lifecycleScope)
            FirebaseAuth.getInstance().signOut()
            //requireFragmentManager().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /**
     * Updates the UI with the logged-in user information.
     *
     * @param model The LoggedInUserView object containing the user information.
     */
    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }
    /**
     * Shows a registration failure message.
     *
     * @param errorString The string resource ID of the error message.
     */
    private fun showRegisterFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}