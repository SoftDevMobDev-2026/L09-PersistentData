package com.example.l09_persistent_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CityUiState>(CityUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadCities() {
        viewModelScope.launch {
            _uiState.value = CityUiState.Loading
            delay(1500) // Simulates work without blocking the main thread.
            val cities = listOf(
                City(1, "Melbourne", "VIC"),
                City(2, "Sydney", "NSW"),
                City(3, "Brisbane", "QLD")
            )
            _uiState.value = CityUiState.Success(cities)
        }
    }

    fun showError() { _uiState.value = CityUiState.Error("Unable to load cities") }
}
