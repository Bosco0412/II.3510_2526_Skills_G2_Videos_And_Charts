package com.example.learningdashboard.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Import R and stringResource
import androidx.compose.ui.res.stringResource
import com.example.learningdashboard.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.learningdashboard.ViewModels.ProfileUiState
import com.example.learningdashboard.ViewModels.ProfileViewModel
import com.example.learningdashboard.auth.AuthUiState
import com.example.learningdashboard.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    authState: AuthUiState,
    authViewModel: AuthViewModel?, // Keep using AuthViewModel for auth state
    profileViewModel: ProfileViewModel, // <-- *** ADD ProfileViewModel for profile/password logic ***
    onLogout: () -> Unit // <-- Add logout callback
) {
    // 1. State for the USERNAME input
    var usernameInput by remember { mutableStateOf("") }

    // --- States for Password Update ---
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // --- State for Tabs ---
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf(
        stringResource(R.string.profile_tab_edit_username), // <-- Changed
        stringResource(R.string.profile_tab_change_password) // <-- Changed
    )

    // 5. Observe the NEW ProfileViewModel's state
    val profileUiState by profileViewModel.uiState.collectAsState() // <-- *** NEW ***

    // 2. Sync USERNAME input when authState changes
    LaunchedEffect(authState.username) {
        usernameInput = authState.username ?: ""
    }

    // 3. Clear profile error when user starts typing USERNAME
    LaunchedEffect(usernameInput) {
        if (profileUiState.profileErrorResId != null || profileUiState.profileErrorMessage != null) { // <-- Changed
            profileViewModel.clearProfileError() // <-- Changed
        }
    }

    // --- Clear password error OR success when user starts typing passwords ---
    LaunchedEffect(currentPassword, newPassword, confirmPassword) {
        if (profileUiState.passwordErrorResId != null || profileUiState.passwordErrorMessage != null || profileUiState.passwordUpdateSuccess) { // <-- Changed
            profileViewModel.clearPasswordError() // <-- Changed
        }
    }

    // --- NEW: Detect password change success and clear fields ---
    LaunchedEffect(profileUiState.passwordUpdateSuccess) { // <-- Changed
        if (profileUiState.passwordUpdateSuccess) { // <-- Changed
            // Clear fields only on success
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""

            // We also need to clear the success message in the VM
            // (or it will reappear on recomposition)
            // Let's modify clearPasswordError in ProfileViewModel to do this
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile_title), // <-- Changed
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 4. Display read-only user information (from AuthState)
        UserInfoCard(
            username = authState.username,
            email = authState.email,
            registeredDate = authState.registeredDate
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. TabRow container
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        // --- NEW: Clear states when switching tabs ---
                        profileViewModel.clearPasswordError() // <-- Changed
                        profileViewModel.clearProfileError() // <-- Changed
                    },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Conditional content based on selected tab
        when (selectedTabIndex) {
            // Tab 0: Edit Name
            0 -> {
                EditProfileCard(
                    usernameInput = usernameInput,
                    onUsernameChange = { usernameInput = it },
                    // 6. Pass the NEW ProfileUiState
                    profileUiState = profileUiState, // <-- *** MODIFIED ***
                    // 7. Call the ProfileViewModel function
                    onUpdateProfile = {
                        profileViewModel.updateFullName(authState.username, usernameInput) // <-- *** MODIFIED ***
                        // We also need to update AuthViewModel's state
                        authViewModel?.updateUsername(usernameInput) // <-- *** ADDED ***
                    }
                )
            }

            // Tab 1: Change Password
            1 -> {
                ChangePasswordCard(
                    currentPassword = currentPassword,
                    onCurrentPasswordChange = { currentPassword = it },
                    currentPasswordVisible = currentPasswordVisible,
                    onCurrentPasswordVisibilityChange = { currentPasswordVisible = !currentPasswordVisible },
                    newPassword = newPassword,
                    onNewPasswordChange = { newPassword = it },
                    newPasswordVisible = newPasswordVisible,
                    onNewPasswordVisibilityChange = { newPasswordVisible = !newPasswordVisible },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    confirmPasswordVisible = confirmPasswordVisible,
                    onConfirmPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                    // 8. Pass the NEW ProfileUiState
                    profileUiState = profileUiState, // <-- *** MODIFIED ***
                    // 9. Call the ProfileViewModel function
                    onUpdatePassword = {
                        profileViewModel.updatePassword( // <-- *** MODIFIED ***
                            authState.username,
                            currentPassword,
                            newPassword,
                            confirmPassword
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Logout Button ---
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout") // This could also be a string resource
        }
    }
}

// ===================================================================
// ...
// ===================================================================

@Composable
private fun EditProfileCard(
    usernameInput: String,
    onUsernameChange: (String) -> Unit,
    profileUiState: ProfileUiState, // <-- *** MODIFIED ***
    onUpdateProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.profile_tab_edit_username), // <-- Changed
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = usernameInput,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(R.string.profile_label_username)) }, // <-- Changed
                modifier = Modifier.fillMaxWidth(),
                // 10. Use the ProfileUiState
                isError = profileUiState.profileErrorResId != null || profileUiState.profileErrorMessage != null, // <-- *** MODIFIED ***
                singleLine = true
            )

            // 11. Use the ProfileUiState for error messages
            val errorMessage = profileUiState.profileErrorResId?.let {
                stringResource(id = it)
            } ?: profileUiState.profileErrorMessage // Fallback to generic string error

            if (errorMessage != null) { // <-- *** MODIFIED ***
                Text(
                    text = errorMessage, // <-- *** MODIFIED ***
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onUpdateProfile,
                // 12. Use the ProfileUiState
                enabled = !profileUiState.isProfileLoading, // <-- *** MODIFIED ***
                modifier = Modifier.fillMaxWidth()
            ) {
                // 13. Use the ProfileUiState
                if (profileUiState.isProfileLoading) { // <-- *** MODIFIED ***
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.profile_button_save_changes)) // <-- Changed
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordCard(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    currentPasswordVisible: Boolean,
    onCurrentPasswordVisibilityChange: () -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    newPasswordVisible: Boolean,
    onNewPasswordVisibilityChange: () -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    profileUiState: ProfileUiState, // <-- *** MODIFIED ***
    onUpdatePassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.profile_tab_change_password), // <-- Changed
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val hasError = profileUiState.passwordErrorResId != null || profileUiState.passwordErrorMessage != null // <-- Changed

            // Current Password
            PasswordTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = stringResource(R.string.profile_label_current_password), // <-- Changed
                isVisible = currentPasswordVisible,
                onVisibilityChange = onCurrentPasswordVisibilityChange,
                // 14. Error check is now based on the error message
                isError = hasError // <-- *** MODIFIED ***
            )
            Spacer(modifier = Modifier.height(8.dp))

            // New Password
            PasswordTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                label = stringResource(R.string.profile_label_new_password), // <-- Changed
                isVisible = newPasswordVisible,
                onVisibilityChange = onNewPasswordVisibilityChange,
                isError = hasError // <-- *** MODIFIED ***
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Confirm New Password
            PasswordTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(R.string.profile_label_confirm_new_password), // <-- Changed
                isVisible = confirmPasswordVisible,
                onVisibilityChange = onConfirmPasswordVisibilityChange,
                isError = hasError // <-- *** MODIFIED ***
            )

            // 15. Use the ProfileUiState
            val errorMessage = profileUiState.passwordErrorResId?.let {
                stringResource(id = it)
            } ?: profileUiState.passwordErrorMessage

            if (errorMessage != null) { // <-- *** MODIFIED ***
                Text(
                    text = errorMessage, // <-- *** MODIFIED ***
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // 16. Use the ProfileUiState
            if (profileUiState.passwordUpdateSuccess) { // <-- *** MODIFIED ***
                Text(
                    text = stringResource(R.string.profile_success_password_updated), // <-- Changed
                    color = MaterialTheme.colorScheme.primary, // Non-error color
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onUpdatePassword,
                // 17. Use the ProfileUiState
                enabled = !profileUiState.isPasswordLoading, // <-- *** MODIFIED ***
                modifier = Modifier.fillMaxWidth()
            ) {
                // 18. Use the ProfileUiState
                if (profileUiState.isPasswordLoading) { // <-- *** MODIFIED ***
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.profile_button_update_password)) // <-- Changed
                }
            }
        }
    }
}


@Composable
private fun UserInfoCard(
    username: String?,
    email: String?,
    registeredDate: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val notAvailable = stringResource(R.string.profile_label_not_available) // <-- Changed
            ProfileInfoRow(
                label = stringResource(R.string.profile_label_username), // <-- Changed
                value = username ?: notAvailable
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_label_email), // <-- Changed
                value = email ?: notAvailable
            )
            ProfileInfoRow(
                label = stringResource(R.string.profile_label_member_since), // <-- Changed
                value = registeredDate?.toFormattedDate() ?: notAvailable
            )
        }
    }
}

// ... (ProfileInfoRow, PasswordTextField, toFormattedDate remain mostly unchanged) ...
@Composable
private fun ProfileInfoRow(label: String, value: String) {
// ... (no changes needed here) ...
// ...
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(0.4f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String, // <-- Now passed as a resolved string
    isVisible: Boolean,
    onVisibilityChange: () -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
// ... (rest of the function is the same) ...
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) }, // <-- Uses the passed string
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            val image = if (isVisible)
                Icons.Filled.Visibility
            else
                Icons.Filled.VisibilityOff

            val description = if (isVisible) "Hide password" else "Show password"

            IconButton(onClick = onVisibilityChange) {
                Icon(imageVector = image, description)
            }
        }
    )
}


// Helper function (no changes)
private fun Long.toFormattedDate(): String {
// ... (no changes needed here) ...
// ...
    val date = Date(this)
    val format = SimpleDateFormat("dd MMM yY", Locale.getDefault())
    return format.format(date)
}