package com.example.learningdashboard.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningdashboard.R // Import R file to access resource IDs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = AuthRepository(userDao)
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Login
     */
    fun login(user: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, profileErrorResId = null) }

            if (user.isBlank()) {
                // Change: Use R.string... ID, not a hardcoded string
                _uiState.update { it.copy(isLoading = false, profileErrorResId = R.string.error_username_empty) }
                return@launch
            }
            if (pass.isBlank()) {
                // Change: Use R.string... ID
                _uiState.update { it.copy(isLoading = false, profileErrorResId = R.string.error_password_empty) }
                return@launch
            }

            try {
                val userData = repository.getUserByUsername(user)

                if (userData == null) {
                    // Change: Use R.string... ID
                    _uiState.update { it.copy(isLoading = false, profileErrorResId = R.string.error_user_not_exist) }
                } else if (userData.passwordHash != pass) {
                    // Change: Use R.string... ID
                    _uiState.update { it.copy(isLoading = false, profileErrorResId = R.string.error_incorrect_password) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            username = userData.username,
                            email = userData.email,
                            fullName = userData.fullName,
                            registeredDate = userData.registeredDate
                        )
                    }
                }
            } catch (e: Exception) {
                // Unchanged, but the UI layer can now decide how to format this generic error
                _uiState.update { it.copy(isLoading = false, profileErrorMessage = "Error: ${e.message}") }
            }
        }
    }

    /**
     * Register
     */
    fun register(user: String, email: String, pass: String, confirmPass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, profileErrorResId = null) }

            // A. Validate all inputs (logic is the same)
            // Change: validateRegistration now returns an Int? (Resource ID)
            val validationErrorResId = validateRegistration(user, email, pass, confirmPass)
            if (validationErrorResId != null) {
                _uiState.update { it.copy(isLoading = false, profileErrorResId = validationErrorResId) }
                return@launch
            }

            try {
                if (repository.getUserByUsername(user) != null) {
                    // Change: Use R.string... ID
                    _uiState.update { it.copy(isLoading = false, profileErrorResId = R.string.error_username_taken) }
                    return@launch
                }
                if (repository.getUserByEmail(email) != null) {
                    // Change: Use R.string... ID
                    _uiState.update { it.copy(isLoading = false, profileErrorResId = R.string.error_email_taken) }
                    return@launch
                }

                // C. Registration success
                val currentTime = System.currentTimeMillis()
                val newUser = User(
                    username = user,
                    email = email,
                    passwordHash = pass,
                    fullName = user,
                    registeredDate = currentTime
                )

                repository.registerUser(newUser)

                // D. Auto-login
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        username = newUser.username,
                        email = newUser.email,
                        fullName = newUser.fullName,
                        registeredDate = newUser.registeredDate
                    )
                }

            } catch (e: Exception) {
                // Unchanged
                _uiState.update { it.copy(isLoading = false, profileErrorMessage = "Registration failed: ${e.message}") }
            }
        }
    }

    // Helper function: returns an Int? resource ID
    private fun validateRegistration(user: String, email: String, pass: String, confirmPass: String): Int? {
        if (user.isBlank()) return R.string.error_username_empty
        if (user.length < 3) return R.string.error_username_min_length
        if (!user.matches(Regex("^[a-zA-Z0-9_]+$"))) return R.string.error_username_invalid_chars
        if (email.isBlank()) return R.string.error_email_empty
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) return R.string.error_email_invalid
        if (pass.isBlank()) return R.string.error_password_empty
        if (pass.length < 6) return R.string.error_password_min_length
        if (pass != confirmPass) return R.string.error_passwords_no_match
        return null // All checks passed
    }



    /**
     * Update user password
     */
    fun updatePassword(currentPass: String, newPass: String, confirmPass: String) {
        val currentUser = _uiState.value.username ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPasswordLoading = true,
                    passwordErrorResId = null, // Change
                    passwordUpdateSuccess = false
                )
            }

            // Change: Use R.string... ID
            if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
                _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_all_fields_required) }
                return@launch
            }
            if (newPass != confirmPass) {
                _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_new_passwords_no_match) }
                return@launch
            }
            if (newPass.length < 6) {
                _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_password_min_length) }
                return@launch
            }
            if (newPass == currentPass) {
                _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_new_password_same_as_old) }
                return@launch
            }

            try {
                val userData = repository.getUserByUsername(currentUser)
                if (userData == null) {
                    _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_user_not_found) }
                    return@launch
                }

                if (userData.passwordHash != currentPass) {
                    _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_incorrect_current_password) }
                    return@launch
                }

                repository.updatePassword(currentUser, newPass)
                _uiState.update {
                    it.copy(
                        isPasswordLoading = false,
                        passwordErrorResId = null,
                        passwordUpdateSuccess = true
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isPasswordLoading = false, passwordErrorMessage = "Update failed: ${e.message}") }
            }
        }
    }


    /**
     * Logout
     */
    fun logout() {
        _uiState.value = AuthUiState()
    }

    /**
     * Clear profile error
     */
    fun clearProfileError() {
        // Change: Clear resource ID and old string message
        _uiState.update { it.copy(profileErrorResId = null, profileErrorMessage = null) }
    }

    /**
     * Clear password error
     */
    fun clearPasswordError() {
        // Change: Clear resource ID and old string message
        _uiState.update { it.copy(passwordErrorResId = null, passwordErrorMessage = null, passwordUpdateSuccess = false) }
    }

    // ... (updateUsername function remains unchanged, but should also be changed to use resource IDs) ...
    fun updateUsername(newUsername: String) {
        // (Ensure newUsername is not blank, and more validation might be needed)
        if (newUsername.isBlank()) {
            _uiState.value = _uiState.value.copy(
                profileErrorResId = R.string.error_username_empty // Change
            )
            return
        }

        // (You might also need to check if the new username is already taken)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(isProfileLoading = true)

                val oldUsername = _uiState.value.username
                if (oldUsername == null) {
                    // If oldUsername is null, cannot update
                    _uiState.value = _uiState.value.copy(
                        profileErrorResId = R.string.error_user_not_found, // Change
                        isProfileLoading = false
                    )
                    return@launch
                }

                // 1. Call Repository to update the database
                repository.updateUsername(oldUsername, newUsername)

                // 2. Update UI state
                _uiState.value = _uiState.value.copy(
                    username = newUsername,
                    isProfileLoading = false,
                    profileErrorResId = null // Clear error
                )

            } catch (e: Exception) {
                // (Handle possible errors, e.g., new username already exists)
                _uiState.value = _uiState.value.copy(
                    profileErrorMessage = "Error updating username: ${e.message}", // Keep generic error
                    isProfileLoading = false
                )
            }
        }
    }
}


/**
 * UI State - Updated Version
 *
 * Change: Replaced `profileErrorMessage` and `passwordErrorMessage` with
 * `profileErrorResId` and `passwordErrorResId` (Int?), for storing R.string... IDs.
 *
 * Kept `profileErrorMessage` and `passwordErrorMessage` as fallbacks,
 * for displaying dynamic/database exception messages that cannot be converted to resource IDs.
 */
data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val registeredDate: Long? = null,

    // General loading for Login/Register
    val isLoading: Boolean = false,

    // States for "Update Full Name"
    val isProfileLoading: Boolean = false,
    val profileErrorResId: Int? = null, // <-- Change
    val profileErrorMessage: String? = null, // <-- Kept, as fallback

    // States for "Update Password"
    val isPasswordLoading: Boolean = false,
    val passwordErrorResId: Int? = null, // <-- Change
    val passwordErrorMessage: String? = null, // <-- Kept, as fallback
    val passwordUpdateSuccess: Boolean = false
)