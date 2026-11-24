package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.data.AppDatabase
import com.example.gaja_prueba1.model.RemoteSpacesRepository
import com.example.gaja_prueba1.model.RemoteWeatherRepository
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.model.SpaceWeather
import com.example.gaja_prueba1.model.SpacesRepository
import com.example.gaja_prueba1.model.WeatherRepository
import com.example.gaja_prueba1.model.distanceFrom
import com.example.gaja_prueba1.model.filterSpaces
import com.example.gaja_prueba1.model.sortByDistance
import com.example.gaja_prueba1.util.LocationResult
import com.example.gaja_prueba1.util.NearbyLocationProvider
import com.example.gaja_prueba1.validation.ReservationValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserLocation(val latitude: Double, val longitude: Double)

enum class WeatherStatus { Idle, Loading, Loaded, Error }

enum class NearbyState {
    Idle,
    Locating,
    Active,
    PermissionDenied,
    PermissionBlocked,
    LocationOff,
    Unavailable
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allSpaces: List<Space> = emptyList(),
    val query: String = "",
    val upcomingReservations: Int = 0,
    val pastReservations: Int = 0,
    val userLocation: UserLocation? = null,
    val nearby: NearbyState = NearbyState.Idle,
    val weather: Map<String, SpaceWeather> = emptyMap(),
    val weatherStatus: WeatherStatus = WeatherStatus.Idle
) {
    val visibleSpaces: List<Space>
        get() {
            val filtered = filterSpaces(allSpaces, query)
            val loc = userLocation ?: return filtered
            return sortByDistance(filtered, loc.latitude, loc.longitude)
        }
    val availableSpaces: Int get() = allSpaces.count { it.available }

    fun distanceTo(space: Space): Double? =
        userLocation?.let { space.distanceFrom(it.latitude, it.longitude) }
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // N3: los espacios vienen del microservicio. N2: el clima, de la API externa Open-Meteo.
    private val repository: SpacesRepository = RemoteSpacesRepository()
    private val weatherRepository: WeatherRepository = RemoteWeatherRepository()
    private val reservationDao = AppDatabase.get(application).reservationDao()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSpaces()
        observeReservationCounters()
    }

    private fun observeReservationCounters() {
        val today = ReservationValidator.toIsoDate(System.currentTimeMillis())
        viewModelScope.launch {
            combine(
                reservationDao.observeUpcomingCount(today),
                reservationDao.observePastCount(today)
            ) { upcoming, past -> upcoming to past }
                .collect { (upcoming, past) ->
                    _uiState.update {
                        it.copy(upcomingReservations = upcoming, pastReservations = past)
                    }
                }
        }
    }

    fun loadSpaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val spaces = repository.getSpaces()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allSpaces = spaces
                    )
                }
                loadWeather(spaces)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No pudimos cargar los espacios. Revisa tu conexión e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun refreshSpaces() {
        if (!_uiState.value.isLoading) loadSpaces()
    }

    /**
     * N2: el clima es un extra. Si falla (sin conexión), el Inicio sigue funcionando
     * y solo muestra un aviso.
     */
    fun loadWeather(spaces: List<Space> = _uiState.value.allSpaces) {
        viewModelScope.launch {
            _uiState.update { it.copy(weatherStatus = WeatherStatus.Loading) }
            try {
                val weather = weatherRepository.weatherFor(spaces)
                _uiState.update { it.copy(weather = weather, weatherStatus = WeatherStatus.Loaded) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(weatherStatus = WeatherStatus.Error) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onNearbyRequested() {
        _uiState.update { it.copy(nearby = NearbyState.Locating) }
        viewModelScope.launch {
            val result = NearbyLocationProvider.currentLocation(getApplication())
            _uiState.update { state ->
                when (result) {
                    is LocationResult.Found -> state.copy(
                        userLocation = UserLocation(result.latitude, result.longitude),
                        nearby = NearbyState.Active
                    )
                    LocationResult.NoPermission -> state.copy(nearby = NearbyState.PermissionDenied)
                    LocationResult.ProviderDisabled -> state.copy(nearby = NearbyState.LocationOff)
                    LocationResult.Unavailable -> state.copy(nearby = NearbyState.Unavailable)
                }
            }
        }
    }

    fun onPermissionDenied(permanently: Boolean) {
        _uiState.update {
            it.copy(nearby = if (permanently) NearbyState.PermissionBlocked else NearbyState.PermissionDenied)
        }
    }

    fun onNearbyCleared() {
        _uiState.update { it.copy(userLocation = null, nearby = NearbyState.Idle) }
    }
}