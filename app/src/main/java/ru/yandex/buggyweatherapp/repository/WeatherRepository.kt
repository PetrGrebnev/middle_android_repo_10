package ru.yandex.buggyweatherapp.repository

import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData

interface WeatherRepository {

    fun getWeatherData(location: Location, callback: (WeatherData?, Exception?) -> Unit)

    fun getWeatherByCity(cityName: String, callback: (WeatherData?, Exception?) -> Unit)
}