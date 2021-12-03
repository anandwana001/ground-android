/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gnd.ui.home.mapcontainer;

import static java8.util.stream.StreamSupport.stream;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gnd.R;
import com.google.android.gnd.model.AuditInfo;
import com.google.android.gnd.model.Project;
import com.google.android.gnd.model.feature.Point;
import com.google.android.gnd.model.feature.PolygonFeature;
import com.google.android.gnd.model.layer.Layer;
import com.google.android.gnd.persistence.uuid.OfflineUuidGenerator;
import com.google.android.gnd.rx.BooleanOrError;
import com.google.android.gnd.rx.annotations.Hot;
import com.google.android.gnd.system.LocationManager;
import com.google.android.gnd.system.auth.AuthenticationManager;
import com.google.android.gnd.ui.common.AbstractViewModel;
import com.google.android.gnd.ui.common.SharedViewModel;
import com.google.android.gnd.ui.map.MapFeature;
import com.google.android.gnd.ui.map.MapPin;
import com.google.android.gnd.ui.map.MapPolygon;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import java.util.ArrayList;
import java.util.List;
import java8.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Inject;
import timber.log.Timber;

@SharedViewModel
public class PolygonDrawingViewModel extends AbstractViewModel {

  /** Min. distance in dp between two points for them be considered as overlapping. */
  public static final int DISTANCE_THRESHOLD_DP = 24;

  @Hot private final Subject<PolygonDrawingState> polygonDrawingState = PublishSubject.create();

  @Hot private final Subject<Optional<MapPolygon>> mapPolygonFlowable = PublishSubject.create();

  /** Denotes whether the drawn polygon is complete or not. This is different from drawing state. */
  @Hot private final LiveData<Boolean> polygonCompleted;

  /** Features drawn by the user but not yet saved. */
  @Hot private final LiveData<ImmutableSet<MapFeature>> unsavedMapFeatures;

  @Hot(replays = true)
  private final MutableLiveData<Boolean> locationLockEnabled = new MutableLiveData<>();

  private final LiveData<Integer> iconTint;
  @Hot private final Subject<Boolean> locationLockChangeRequests = PublishSubject.create();
  private final LocationManager locationManager;
  private final LiveData<BooleanOrError> locationLockState;
  private final List<Point> vertices = new ArrayList<>();

  /** The currently selected layer and project for the polygon drawing. */
  private final BehaviorProcessor<Layer> selectedLayer = BehaviorProcessor.create();
  private final BehaviorProcessor<Project> selectedProject = BehaviorProcessor.create();

  private final OfflineUuidGenerator uuidGenerator;
  private final AuthenticationManager authManager;
  @Nullable private Point cameraTarget;

  /**
   * If true, then it means that the last vertex is added automatically and should be removed before
   * adding any permanent vertex. Used for rendering a line between last added point and current
   * camera target.
   */
  boolean isLastVertexNotSelectedByUser;

  private Optional<MapPolygon> mapPolygon = Optional.empty();

  @Inject
  PolygonDrawingViewModel(
      LocationManager locationManager,
      AuthenticationManager authManager,
      OfflineUuidGenerator uuidGenerator) {
    this.locationManager = locationManager;
    this.authManager = authManager;
    this.uuidGenerator = uuidGenerator;
    // TODO: Create custom ui component for location lock button and share across app.
    Flowable<BooleanOrError> locationLockStateFlowable = createLocationLockStateFlowable().share();
    this.locationLockState =
        LiveDataReactiveStreams.fromPublisher(
            locationLockStateFlowable.startWith(BooleanOrError.falseValue()));
    this.iconTint =
        LiveDataReactiveStreams.fromPublisher(
            locationLockStateFlowable
                .map(locked -> locked.isTrue() ? R.color.colorMapBlue : R.color.colorGrey800)
                .startWith(R.color.colorGrey800));
    Flowable<Optional<MapPolygon>> polygonFlowable =
        mapPolygonFlowable
            .startWith(Optional.empty())
            .toFlowable(BackpressureStrategy.LATEST)
            .share();
    this.polygonCompleted =
        LiveDataReactiveStreams.fromPublisher(
            polygonFlowable
                .map(polygon -> polygon.map(MapPolygon::isPolygonComplete).orElse(false))
                .startWith(false));
    this.unsavedMapFeatures =
        LiveDataReactiveStreams.fromPublisher(
            polygonFlowable.map(
                polygon ->
                    polygon
                        .map(PolygonDrawingViewModel::unsavedFeaturesFromPolygon)
                        .orElse(ImmutableSet.of())));
  }

  private Flowable<BooleanOrError> createLocationLockStateFlowable() {
    return locationLockChangeRequests
        .switchMapSingle(
            enabled ->
                enabled
                    ? this.locationManager.enableLocationUpdates()
                    : this.locationManager.disableLocationUpdates())
        .toFlowable(BackpressureStrategy.LATEST);
  }

  /** Returns a set of {@link MapFeature} to be drawn on map for the given {@link MapPolygon}. */
  private static ImmutableSet<MapFeature> unsavedFeaturesFromPolygon(MapPolygon mapPolygon) {
    ImmutableList<Point> vertices = mapPolygon.getVertices();

    if (vertices.isEmpty()) {
      // Return if polygon has 0 vertices.
      return ImmutableSet.of();
    }

    // Include the given polygon and add 1 MapPin for each of its vertex.
    return ImmutableSet.<MapFeature>builder()
        .add(mapPolygon)
        .addAll(
            stream(vertices)
                .map(
                    point ->
                        MapPin.newBuilder()
                            .setId(mapPolygon.getId())
                            .setPosition(point)
                            // TODO: Use different marker style for unsaved markers.
                            .setStyle(mapPolygon.getStyle())
                            .build())
                .toList())
        .build();
  }

  @Hot
  public Observable<PolygonDrawingState> getDrawingState() {
    return polygonDrawingState;
  }

  public void onCameraMoved(Point newTarget) {
    cameraTarget = newTarget;
    if (locationLockState.getValue() != null && isLocationLockEnabled()) {
      Timber.d("User dragged map. Disabling location lock");
      locationLockChangeRequests.onNext(false);
    }
  }

  /**
   * Adds another vertex at the given point if {@param distanceInPixels} is more than the configured
   * threshold. Otherwise, snaps to the first vertex.
   *
   * @param newTarget Position of the map camera.
   * @param distanceInPixels Distance between the last vertex and {@param newTarget}.
   */
  public void updateLastVertex(Point newTarget, double distanceInPixels) {
    boolean isPolygonComplete = vertices.size() > 2 && distanceInPixels <= DISTANCE_THRESHOLD_DP;
    addVertex(isPolygonComplete ? vertices.get(0) : newTarget, true);
  }

  /** Attempts to remove the last vertex of drawn polygon, if any. */
  public void removeLastVertex() {
    if (vertices.isEmpty()) {
      polygonDrawingState.onNext(PolygonDrawingState.canceled());
      reset();
    } else {
      vertices.remove(vertices.size() - 1);
      updateVertices(ImmutableList.copyOf(vertices));
    }
  }

  public void selectCurrentVertex() {
    if (cameraTarget != null) {
      addVertex(cameraTarget, false);
    }
  }

  public void setLocationLockEnabled(boolean enabled) {
    locationLockEnabled.postValue(enabled);
  }

  /**
   * Adds a new vertex.
   *
   * @param vertex new position
   * @param isNotSelectedByUser whether the vertex is not selected by the user
   */
  private void addVertex(Point vertex, boolean isNotSelectedByUser) {
    // Clear last vertex if it is unselected
    if (isLastVertexNotSelectedByUser && !vertices.isEmpty()) {
      vertices.remove(vertices.size() - 1);
    }

    // Update selected state
    isLastVertexNotSelectedByUser = isNotSelectedByUser;

    // Add the new vertex
    vertices.add(vertex);

    // Render changes to UI
    updateVertices(ImmutableList.copyOf(vertices));
  }

  private void updateVertices(ImmutableList<Point> newVertices) {
    mapPolygon = mapPolygon.map(polygon -> polygon.toBuilder().setVertices(newVertices).build());
    mapPolygonFlowable.onNext(mapPolygon);
  }

  public void onCompletePolygonButtonClick() {
    if (selectedLayer.getValue() == null || selectedProject.getValue() == null) {
      throw new IllegalStateException("Project or layer is null");
    }
    MapPolygon polygon = mapPolygon.get();
    if (!polygon.isPolygonComplete()) {
      throw new IllegalStateException("Polygon is not complete");
    }
    AuditInfo auditInfo = AuditInfo.now(authManager.getCurrentUser());
    PolygonFeature polygonFeature =
        PolygonFeature.builder()
            .setId(polygon.getId())
            .setVertices(polygon.getVertices())
            .setProject(selectedProject.getValue())
            .setLayer(selectedLayer.getValue())
            .setCreated(auditInfo)
            .setLastModified(auditInfo)
            .build();
    polygonDrawingState.onNext(PolygonDrawingState.completed(polygonFeature));
    reset();
  }

  private void reset() {
    isLastVertexNotSelectedByUser = false;
    vertices.clear();
    mapPolygon = Optional.empty();
    mapPolygonFlowable.onNext(Optional.empty());
  }

  Optional<Point> getFirstVertex() {
    return mapPolygon.map(MapPolygon::getFirstVertex);
  }

  public void onLocationLockClick() {
    locationLockChangeRequests.onNext(!isLocationLockEnabled());
  }

  public LiveData<Boolean> isPolygonCompleted() {
    return polygonCompleted;
  }

  private boolean isLocationLockEnabled() {
    return locationLockState.getValue().isTrue();
  }

  public LiveData<Boolean> getLocationLockEnabled() {
    // TODO : current location is not working value is always false.
    return locationLockEnabled;
  }

  public void startDrawingFlow(Project selectedProject, Layer selectedLayer) {
    this.selectedLayer.onNext(selectedLayer);
    this.selectedProject.onNext(selectedProject);
    polygonDrawingState.onNext(PolygonDrawingState.inProgress());

    mapPolygon =
        Optional.of(
            MapPolygon.newBuilder()
                .setId(uuidGenerator.generateUuid())
                .setVertices(ImmutableList.of())
                .setStyle(selectedLayer.getDefaultStyle())
                .build());
  }

  public LiveData<Integer> getIconTint() {
    return iconTint;
  }

  public LiveData<ImmutableSet<MapFeature>> getUnsavedMapFeatures() {
    return unsavedMapFeatures;
  }

  @AutoValue
  public abstract static class PolygonDrawingState {

    public static PolygonDrawingState canceled() {
      return createDrawingState(State.CANCELED, null);
    }

    public static PolygonDrawingState inProgress() {
      return createDrawingState(State.IN_PROGRESS, null);
    }

    public static PolygonDrawingState completed(PolygonFeature unsavedFeature) {
      return createDrawingState(State.COMPLETED, unsavedFeature);
    }

    private static PolygonDrawingState createDrawingState(
        State state, @Nullable PolygonFeature unsavedFeature) {
      return new AutoValue_PolygonDrawingViewModel_PolygonDrawingState(state, unsavedFeature);
    }

    public boolean isCanceled() {
      return getState() == State.CANCELED;
    }

    public boolean isInProgress() {
      return getState() == State.IN_PROGRESS;
    }

    public boolean isCompleted() {
      return getState() == State.COMPLETED;
    }

    /** Represents state of PolygonDrawing action. */
    public enum State {
      IN_PROGRESS,
      COMPLETED,
      CANCELED
    }

    /** Current state of polygon drawing. */
    public abstract State getState();

    /** Final polygon feature. */
    @Nullable
    public abstract PolygonFeature getUnsavedPolygonFeature();
  }
}