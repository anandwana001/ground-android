/*
 * Copyright 2023 Google LLC
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

package com.google.android.ground.ui.datacollection.tasks.point

import com.google.android.ground.model.geometry.Coordinate
import com.google.android.ground.ui.datacollection.tasks.point.LatLngConverter.processCoordinate
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LatLngConverterTest {

  @Test
  fun testProcessCoordinates_ne() {
    assertThat(processCoordinate(Coordinate(10.555, 10.555))).isEqualTo("10°33'18\" N 10°33'18\" E")
  }

  @Test
  fun testProcessCoordinates_se() {
    assertThat(processCoordinate(Coordinate(-10.555, 10.555)))
      .isEqualTo("10°33'18\" S 10°33'18\" E")
  }
  @Test
  fun testProcessCoordinates_nw() {
    assertThat(processCoordinate(Coordinate(10.555, -10.555)))
      .isEqualTo("10°33'18\" N 10°33'18\" W")
  }
  @Test
  fun testProcessCoordinates_sw() {
    assertThat(processCoordinate(Coordinate(-10.555, -10.555)))
      .isEqualTo("10°33'18\" S 10°33'18\" W")
  }
}