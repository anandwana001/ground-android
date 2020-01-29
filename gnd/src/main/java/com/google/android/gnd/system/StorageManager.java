/*
 * Copyright 2020 Google LLC
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

package com.google.android.gnd.system;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import com.google.android.gnd.system.ActivityStreams.ActivityResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java8.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Manages permissions needed for accessing storage and related flows to/from Activity. */
@Singleton
public class StorageManager {

  public static final String TAG = StorageManager.class.getName();

  private static final int PICK_PHOTO_REQUEST_CODE = StorageManager.class.hashCode() & 0xffff;
  private final Context context;
  private final PermissionsManager permissionsManager;
  private final ActivityStreams activityStreams;

  @Inject
  public StorageManager(
      Context context, PermissionsManager permissionsManager, ActivityStreams activityStreams) {
    this.context = context;
    this.permissionsManager = permissionsManager;
    this.activityStreams = activityStreams;
  }

  public Completable launchPhotoPicker() {
    return permissionsManager
        .obtainPermission(permission.READ_EXTERNAL_STORAGE)
        .andThen(sendPhotoPickerIntent());
  }

  public Observable<Optional<Bitmap>> photoPickerResult() {
    return activityStreams
        .getNextActivityResult(PICK_PHOTO_REQUEST_CODE)
        .filter(ActivityResult::isOk)
        .map(activityResult -> Optional.ofNullable(activityResult.getData()).map(Intent::getData))
        .map(data -> Optional.of(Media.getBitmap(context.getContentResolver(), data.get())));
  }

  private Completable sendPhotoPickerIntent() {
    return Completable.fromAction(
        () ->
            activityStreams.withActivity(
                activity -> {
                  Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                  intent.setType("image/*");
                  activity.startActivityForResult(intent, PICK_PHOTO_REQUEST_CODE);
                  Log.d(TAG, "file picker intent sent");
                }));
  }
}
