package at.rtr.rmbt.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import at.rmbt.client.control.data.MapPresentationType
import at.rtr.rmbt.android.ui.viewstate.MapViewState
import at.specure.data.entity.MarkerMeasurementRecord
import at.specure.data.repository.MapRepository
import at.specure.location.LocationInfoLiveData
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import javax.inject.Inject

class MapViewModel @Inject constructor(private val repository: MapRepository, val locationInfoLiveData: LocationInfoLiveData) : BaseViewModel() {

    val state = MapViewState()

    val provider = RetrofitTileProvider(repository, state)

    var markersLiveData: LiveData<List<MarkerMeasurementRecord>> =
        Transformations.switchMap(state.coordinatesLiveData) { repository.getMarkers(it?.latitude, it?.longitude) }

    init {
        addStateSaveHandler(state)
    }

    fun loadMarkers(zoom: Int) {
        state.coordinatesLiveData.value?.latitude?.let { repository.loadMarkers(it, state.coordinatesLiveData.value!!.longitude, zoom) }
    }
}

class RetrofitTileProvider(private val repository: MapRepository, private val state: MapViewState) : TileProvider {

    override fun getTile(x: Int, y: Int, zoom: Int): Tile {
        val type = state.type.get() ?: MapPresentationType.POINTS
        return if (type == MapPresentationType.AUTOMATIC && zoom > 10) {
            Tile(256, 256, repository.loadAutomaticTiles(x, y, zoom))
        } else {
            Tile(256, 256, repository.loadTiles(x, y, zoom, type))
        }
    }
}
