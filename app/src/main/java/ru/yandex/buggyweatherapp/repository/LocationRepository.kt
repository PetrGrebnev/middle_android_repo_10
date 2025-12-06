package ru.yandex.buggyweatherapp.repository

import ru.yandex.buggyweatherapp.model.Location

interface LocationRepository {

    fun getCurrentLocation(callback: (Location?) -> Unit)

    fun getCityNameFromLocation(location: Location): String?
}