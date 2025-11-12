package com.example.learningdashboard.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import R
import com.example.learningdashboard.R
import com.example.learningdashboard.auth.AppDatabase
import com.example.learningdashboard.auth.AuthRepository
import com.example.learningdashboard.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Define a UI state class for this screen
data class ProfileUiState(
    val isProfileLoading: Boolean = false,
    val profileErrorResId: Int? = null, // <-- Changed
    val profileErrorMessage: String? = null, // <-- Kept for generic errors
    val isPasswordLoading: Boolean = false,
    val passwordErrorResId: Int? = null, // <-- Changed
    val passwordErrorMessage: String? = null, // <-- Kept for generic errors
    val passwordUpdateSuccess: Boolean = false
)

// 2. Change ViewModel to AndroidViewModel to get the Application context
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // 3. Initialize the database and repository
    private val repository: AuthRepository

    // 4. Create a separate state flow for the profile UI
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // 5. Instantiate the repository so we can access the database
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = AuthRepository(userDao)
    }

    /**
     * Updates the user's full name.
     * @param username We need to know which user to update
     * @param newFullName The new name to set
     */
    fun updateFullName(username: String?, newFullName: String) {
        if (username == null) {
            _uiState.update { it.copy(profileErrorResId = R.string.error_user_not_found) } // <-- Changed
            return
        }

        if (newFullName.isBlank()) {
            _uiState.update { it.copy(profileErrorResId = R.string.profile_error_name_empty) } // <-- Changed
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, profileErrorResId = null) }
            try {
                // 6. Call the repository to update the database
                repository.updateUsername(username, newFullName)
                // Note: The AuthUiState in AuthViewModel also needs to be updated
                // For now, we just set loading to false.
                // A better solution would be for AuthViewModel to listen for user changes.
                _uiState.update { it.copy(isProfileLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProfileLoading = false, profileErrorMessage = e.message) } // <-- Kept for generic
            }
        }
    }

    /**
     * Updates the user's password.
     */
    fun updatePassword(
        username: String?,
        currentPass: String,
        newPass: String,
        confirmPass: String
    ) {
        if (username == null) {
            _uiState.update { it.copy(passwordErrorResId = R.string.error_user_not_found) } // <-- Changed
            return
        }

        // 7. Add password validation logic
        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            _uiState.update { it.copy(passwordErrorResId = R.string.error_all_fields_required) } // <-- Changed
            return
        }
        if (newPass != confirmPass) {
            _uiState.update { it.copy(passwordErrorResId = R.string.error_new_passwords_no_match) } // <-- Changed
            return
        }
        if (newPass.length < 6) { // Assuming a 6-character minimum
            _uiState.update { it.copy(passwordErrorResId = R.string.error_password_min_length) } // <-- Changed
            return
        }
        if (newPass == currentPass) {
            _uiState.update { it.copy(passwordErrorResId = R.string.error_new_password_same_as_old) } // <-- Changed
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPasswordLoading = true,
                    passwordErrorResId = null, // <-- Changed
                    passwordUpdateSuccess = false
                )
            }
            try {
                // 8. Verify the current password is correct
                val user = repository.getUserByUsername(username)

                if (user == null) {
                    _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_user_not_found) } // <-- Changed
                    return@launch
                }

                // !! Note: In a real app, passwords should be hashed!
                // We are comparing plaintext for simplicity.
                if (user.passwordHash != currentPass) {
                    _uiState.update { it.copy(isPasswordLoading = false, passwordErrorResId = R.string.error_incorrect_current_password) } // <-- Changed
                    return@launch
                }

                // 9. Call repository to update the password
                repository.updatePassword(username, newPass) // This should also be a hash
                _uiState.update {
                    it.copy(
                        isPasswordLoading = false,
                        passwordUpdateSuccess = true
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPasswordLoading = false,
                        passwordErrorMessage = e.message ?: "An error occurred" // <-- Kept for generic
                    )
                }
            }
        }
    }

    // 10. Functions to clear errors
    fun clearProfileError() {
        _uiState.update { it.copy(profileErrorResId = null, profileErrorMessage = null) }
    }

    fun clearPasswordError() {
        _uiState.update { it.copy(passwordErrorResId = null, passwordErrorMessage = null, passwordUpdateSuccess = false) }
    }
}