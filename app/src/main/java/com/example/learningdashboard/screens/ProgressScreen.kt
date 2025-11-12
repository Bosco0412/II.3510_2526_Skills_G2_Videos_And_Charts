package com.example.learningdashboard.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
// Import R and stringResource
import androidx.compose.ui.res.stringResource
import com.example.learningdashboard.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.learningdashboard.DailyUsageBarChart
// 1. Import the new PieChart Composable
import com.example.learningdashboard.DailyUsagePieChart
import com.example.learningdashboard.ViewModels.UsageTimeViewModel

// --- Screen 2: Progress ---
@Composable
fun ProgressScreen(
    viewModel: UsageTimeViewModel = viewModel()
) {
    // --- 2. Observe data for both charts ---
    val barChartData by viewModel.chartData.collectAsState()
    val labels by viewModel.dayLabels.collectAsState()
    // 3. Observe new pie chart data
    val pieChartData by viewModel.pieChartData.collectAsState()

    // Use DisposableEffect to manage the timer's lifecycle
    DisposableEffect(key1 = viewModel) {
        viewModel.startTracking()
        onDispose {
            viewModel.stopTracking()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            // 4. Add scrolling in case screen is small
            .verticalScroll(rememberScrollState())
    ) {
        // --- BAR CHART SECTION ---
        Text(
            text = stringResource(R.string.progress_title_daily_usage), // <-- Changed
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        DailyUsageBarChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            data = barChartData,
            labels = labels
        )

        // --- 5. PIE CHART SECTION ---
        Spacer(modifier = Modifier.height(32.dp)) // Space between charts

        Text(
            text = stringResource(R.string.progress_title_usage_distribution), // <-- Changed
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        DailyUsagePieChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp), // Give it a fixed height
            data = pieChartData
        )
    }
}