package com.example.learningdashboard.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.learningdashboard.auth.AuthViewModel
import androidx.compose.ui.res.stringResource // <-- Import
import com.example.learningdashboard.R // <-- Import R

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onLoginClick: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }


    val uiState by authViewModel.uiState.collectAsState()

    val isLoading = uiState.isLoading
    // Check for both types of errors
    val hasError = uiState.profileErrorResId != null || uiState.profileErrorMessage != null

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.register_title), // <-- Change
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = user,
            onValueChange = {
                user = it
                authViewModel.clearProfileError()
            },
            label = { Text(stringResource(id = R.string.label_username)) }, // <-- Change
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                authViewModel.clearProfileError()
            },
            label = { Text(stringResource(id = R.string.label_email)) }, // <-- Change
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        OutlinedTextField(
            value = pass,
            onValueChange = {
                pass = it
                authViewModel.clearProfileError()
            },
            label = { Text(stringResource(id = R.string.label_password)) }, // <-- Change
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        OutlinedTextField(
            value = confirm,
            onValueChange = {
                confirm = it
                authViewModel.clearProfileError()
            },
            label = { Text(stringResource(id = R.string.label_confirm_password)) }, // <-- Change
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Button(
            onClick = {
                authViewModel.register(
                    user = user.trim(),
                    email = email.trim(),
                    pass = pass,
                    confirmPass = confirm
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) { Text(stringResource(id = R.string.button_register)) } // <-- Change

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) { Text(stringResource(id = R.string.prompt_login)) } // <-- Change

        if (hasError) {
            val errorMessage = uiState.profileErrorResId?.let {
                stringResource(id = it)
            } ?: uiState.profileErrorMessage ?: ""

            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}