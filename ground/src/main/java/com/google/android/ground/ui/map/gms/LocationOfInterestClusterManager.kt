package com.google.android.ground.ui.map.gms

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.ground.model.geometry.Point
import com.google.android.ground.model.locationofinterest.LocationOfInterest
import com.google.android.ground.ui.map.MapLocationOfInterest
import com.google.android.ground.util.toImmutableSet
import com.google.maps.android.clustering.ClusterManager
import timber.log.Timber

class LocationOfInterestClusterManager(context: Context?, map: GoogleMap) :
  ClusterManager<LocationOfInterestClusterItem>(context, map) {

  private val renderer: LocationOfInterestClusterRenderer =
    LocationOfInterestClusterRenderer(context, map, this)
  var activeLocationOfInterest: String? = null

  override fun getRenderer(): LocationOfInterestClusterRenderer = renderer

  fun addOrUpdateLocationOfInterest(locationOfInterest: LocationOfInterest) {
    if (locationOfInterest.geometry !is Point) {
      Timber.d("can't manage a non-point")
      return
    }

    val clusterItem = algorithm.items.find { it.locationOfInterest.id == locationOfInterest.id }

    if (clusterItem != null) {
      updateItem(clusterItem)
    } else {
      Timber.d("adding loi to cluster manager: ${locationOfInterest}")
      addItem(
        LocationOfInterestClusterItem(
          locationOfInterest.geometry,
          locationOfInterest.caption ?: "",
          locationOfInterest.lastModified.toString(),
          locationOfInterest,
        )
      )
    }
  }

  fun removeLocationsOfInterest(locationsOfInterest: Set<LocationOfInterest>) {
    val existingIds = algorithm.items.map { it.locationOfInterest.id }.toSet()
    val deletedIds = existingIds - locationsOfInterest.map { it.id }.toSet()
    val deletedPoints: Set<LocationOfInterestClusterItem> =
      algorithm.items.filter { deletedIds.contains(it.locationOfInterest.id) }.toSet()

    Timber.d("removing points: ${deletedPoints}")
    removeItems(deletedPoints)
  }

  fun getMapLocationsOfInterest() =
    algorithm.items.map { MapLocationOfInterest(it.locationOfInterest) }.toImmutableSet()
}
