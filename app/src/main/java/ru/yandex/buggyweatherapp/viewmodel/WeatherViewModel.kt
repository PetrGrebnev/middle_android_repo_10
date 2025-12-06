package ru.yandex.buggyweatherapp.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepositoryImpl
import ru.yandex.buggyweatherapp.repository.WeatherRepositoryImpl
import ru.yandex.buggyweatherapp.ui.screens.WeatherScreenUIState
import ru.yandex.buggyweatherapp.utils.ImageLoader
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepositoryImpl,
    private val locationRepository: LocationRepositoryImpl,
    private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _viewState = MutableLiveData<WeatherScreenUIState>()
    val viewState: LiveData<WeatherScreenUIState> = _viewState

    init {
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }

    fun fetchCurrentLocationWeather() {
        isLoading()

        locationRepository.getCurrentLocation { location ->
            if (location != null) {
                val cityNameFromLocation = locationRepository.getCityNameFromLocation(location)

                _viewState.value = viewState.value?.copy(
                    isLoading = false,
                    currentLocation = location,
                    cityName = cityNameFromLocation
                )
                getWeatherForLocation(location)
            } else {
                _viewState.value = viewState.value?.copy(
                    isLoading = false,
                    error = "Unable to get current location"
                )
            }
        }
    }

    fun getWeatherForLocation(location: Location) {
        isLoading()

        viewModelScope.launch(coroutineDispatcher) {
            weatherRepository.getWeatherData(location) { data, exception ->
                if (data != null) {
                    _viewState.postValue(
                        viewState.value?.copy(
                            isLoading = false,
                            error = null,
                            weatherData = data
                        )
                    )
                } else {
                    _viewState.postValue(
                        viewState.value?.copy(
                            isLoading = false,
                            error = exception?.message ?: UNKNOW_ERROR,
                        )
                    )
                }
            }
        }
    }

    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            _viewState.value = viewState.value?.copy(
                error = "City name cannot be empty"
            )
            return
        }
        isLoading()

        viewModelScope.launch(coroutineDispatcher) {
            weatherRepository.getWeatherByCity(city) { data, exception ->
                if (data != null) {
                    _viewState.value = viewState.value?.copy(
                        isLoading = false,
                        error = null,
                        currentLocation = Location(0.0, 0.0, data.cityName),
                        weatherData = data,
                        cityName = data.cityName
                    )
                } else {
                    _viewState.postValue(
                        viewState.value?.copy(
                            isLoading = false,
                            error = exception?.message ?: UNKNOW_ERROR,
                        )
                    )
                }
            }
        }
    }


    fun formatTemperature(temp: Double): String {
        return "${temp.toInt()}°C"
    }


    fun loadWeatherIcon(iconCode: String) {
        viewModelScope.launch(coroutineDispatcher) {
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            ImageLoader.loadImage(iconUrl)
        }
    }


    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(REFRESH_DELAY)
                viewState.value?.let {
                    if (it.currentLocation != null) {
                        getWeatherForLocation(it.currentLocation)
                    }
                }
            }
        }
    }

    fun toggleFavorite(isFavorite: Boolean) {
        _viewState.value = viewState.value?.copy(
            weatherData = viewState.value?.weatherData?.copy(
                isFavorite = isFavorite
            )
        )
    }

    private fun isLoading() {
        _viewState.value = viewState.value?.copy(
            isLoading = true,
            error = null
        )
    }

    companion object {
        private const val UNKNOW_ERROR = "Unknown error"
        private const val REFRESH_DELAY: Long = 60000
    }
}