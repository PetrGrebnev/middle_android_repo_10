package ru.yandex.buggyweatherapp.ui.screens

import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData

data class WeatherScreenUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val weatherData: WeatherData? = null,
    val currentLocation: Location? = null,
    val cityName: String? = null,
)
