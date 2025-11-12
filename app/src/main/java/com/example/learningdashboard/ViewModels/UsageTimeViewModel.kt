package com.example.learningdashboard.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import R
import com.example.learningdashboard.R
import com.github.mikephil.charting.data.BarEntry
// 1. Import PieEntry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
// 2. Import LocalTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// 3. Define the time buckets (No string label needed)
enum class TimeOfDay {
    NIGHT,
    MORNING,
    AFTERNOON,
    EVENING
}

// 4. Change to AndroidViewModel to get context
class UsageTimeViewModel(application: Application) : AndroidViewModel(application) {

    // --- Data for Bar Chart ---
    private val _dayLabels = MutableStateFlow(generateDateLabels())
    val dayLabels: StateFlow<List<String>> = _dayLabels.asStateFlow()

    private val previousDaysUsage = mutableListOf(
        65f, 45f, 90f, 30f, 70f, 120f
    )
    private var todayUsageInSeconds = 0L
    private val _chartData = MutableStateFlow<List<BarEntry>>(emptyList())
    val chartData: StateFlow<List<BarEntry>> = _chartData.asStateFlow()

    // --- Data for Pie Chart ---
    // 5. Pre-defined average usage
    private val historicalUsageByTimeOfDay = mutableMapOf(
        TimeOfDay.NIGHT to 30f,     // 30 min avg
        TimeOfDay.MORNING to 120f,  // 120 min avg
        TimeOfDay.AFTERNOON to 90f, // 90 min avg
        TimeOfDay.EVENING to 180f   // 180 min avg
    )

    // 6. Real-time usage for today
    private var todayUsageInSecondsByTimeOfDay = mutableMapOf(
        TimeOfDay.NIGHT to 0L,
        TimeOfDay.MORNING to 0L,
        TimeOfDay.AFTERNOON to 0L,
        TimeOfDay.EVENING to 0L
    )

    // 7. StateFlow for the pie chart data
    private val _pieChartData = MutableStateFlow<List<PieEntry>>(emptyList())
    val pieChartData: StateFlow<List<PieEntry>> = _pieChartData.asStateFlow()


    private var usageTimerJob: Job? = null

    init {
        // Load initial data for both charts
        updateChartData()
    }

    /**
     * Merges historical data with today's real-time data for both charts
     */
    private fun updateChartData() {
        // --- 1. Update Bar Chart Data ---
        val barEntries = mutableListOf<BarEntry>()
        previousDaysUsage.forEachIndexed { index, usageInMinutes ->
            barEntries.add(BarEntry(index.toFloat(), usageInMinutes))
        }
        val todayUsageInMinutes = todayUsageInSeconds / 60f
        barEntries.add(BarEntry(previousDaysUsage.size.toFloat(), todayUsageInMinutes))
        _chartData.value = barEntries

        // --- 2. Update Pie Chart Data ---
        val pieEntries = mutableListOf<PieEntry>()
        // 8. Get application context to resolve strings
        val context = getApplication<Application>().applicationContext

        for (timeOfDay in TimeOfDay.values()) {
            val historicalMinutes = historicalUsageByTimeOfDay.getOrDefault(timeOfDay, 0f)
            val todayMinutes = todayUsageInSecondsByTimeOfDay.getOrDefault(timeOfDay, 0L) / 60f
            val totalMinutes = historicalMinutes + todayMinutes

            // Add to chart only if there is data
            if (totalMinutes > 0) {
                // 9. Resolve the string label from R.string
                val label = when (timeOfDay) {
                    TimeOfDay.NIGHT -> context.getString(R.string.progress_label_night)
                    TimeOfDay.MORNING -> context.getString(R.string.progress_label_morning)
                    TimeOfDay.AFTERNOON -> context.getString(R.string.progress_label_afternoon)
                    TimeOfDay.EVENING -> context.getString(R.string.progress_label_evening)
                }
                // The PieEntry takes the value (minutes) and a label
                pieEntries.add(PieEntry(totalMinutes, label))
            }
        }
        _pieChartData.value = pieEntries
    }

    /**
     * Starts tracking when the screen is visible
     */
    fun startTracking() {
        if (usageTimerJob?.isActive == true) return

        usageTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Every second

                // --- 10. Increment Total Usage (for Bar Chart) ---
                todayUsageInSeconds++

                // --- 11. Increment Bucket Usage (for Pie Chart) ---
                val currentBucket = getCurrentTimeOfDay()
                val currentSeconds = todayUsageInSecondsByTimeOfDay.getOrDefault(currentBucket, 0L)
                todayUsageInSecondsByTimeOfDay[currentBucket] = currentSeconds + 1L

                // --- 12. Update Both Charts ---
                updateChartData()
            }
        }
    }

    /**
     * Stops tracking when the screen is not visible
     */
    fun stopTracking() {
        usageTimerJob?.cancel()
        usageTimerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    // --- Helper Functions ---

    private fun generateDateLabels(): List<String> {
        val labels = mutableListOf<String>()
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("M/d")
        for (i in 6 downTo 1) {
            val date = today.minusDays(i.toLong())
            labels.add(date.format(formatter))
        }
        labels.add("Today")
        return labels
    }

    // 13. Helper function to determine the current time bucket
    private fun getCurrentTimeOfDay(): TimeOfDay {
        val currentHour = LocalTime.now().hour
        return when (currentHour) {
            in 0..5 -> TimeOfDay.NIGHT
            in 6..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            else -> TimeOfDay.EVENING // 18-23
        }
    }
}