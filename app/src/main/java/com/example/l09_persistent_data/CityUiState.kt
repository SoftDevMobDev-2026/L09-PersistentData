package com.example.l09_persistent_data

sealed class CityUiState {
    data object Loading : CityUiState()
    data class Success(val cities: List<City>) : CityUiState()
    data class Error(val message: String) : CityUiState()
}
