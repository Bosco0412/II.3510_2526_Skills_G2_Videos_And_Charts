package com.example.learningdashboard.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.learningdashboard.auth.AuthViewModel
import androidx.compose.ui.res.stringResource // <-- Import
import com.example.learningdashboard.R // <-- Import R

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    val authState by authViewModel.uiState.collectAsState()

    // Check for both types of errors
    val hasError = authState.profileErrorResId != null || authState.profileErrorMessage != null


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(id = R.string.login_title), // <-- Change
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                authViewModel.clearProfileError()
            },
            label = { Text(stringResource(id = R.string.label_username)) }, // <-- Change
            modifier = Modifier.fillMaxWidth(),
            isError = hasError // <-- Change
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                authViewModel.clearProfileError()
            },
            label = { Text(stringResource(id = R.string.label_password)) }, // <-- Change
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = hasError // <-- Change
        )
        Spacer(modifier = Modifier.height(16.dp))


        // Error message display logic
        if (hasError) {
            val errorMessage = authState.profileErrorResId?.let {
                // Prioritize error from resource ID
                stringResource(id = it)
            } ?: authState.profileErrorMessage ?: "" // Otherwise, show error from string

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }


        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    authViewModel.login(username, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authState.isLoading
            ) {
                Text(stringResource(id = R.string.button_login)) // <-- Change
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onRegisterClick,
            enabled = !authState.isLoading
        ) {
            Text(stringResource(id = R.string.prompt_register)) // <-- Change
        }
    }
}