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
package com.google.android.ground.ui.tos

import com.google.android.ground.BaseHiltTest
import com.google.android.ground.repository.TermsOfServiceRepository
import com.google.android.ground.ui.common.Navigator
import com.google.android.ground.ui.home.HomeScreenFragmentDirections
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class TermsOfServiceViewModelTest : BaseHiltTest() {
  @Inject lateinit var navigator: Navigator

  @Inject lateinit var termsOfServiceRepository: TermsOfServiceRepository

  @Inject lateinit var viewModel: TermsOfServiceViewModel

  @Test
  fun testOnButtonClicked() {
    val testObserver = navigator.navigateRequests.test()

    viewModel.onButtonClicked()

    assertThat(termsOfServiceRepository.isTermsOfServiceAccepted).isTrue()
    testObserver
      .assertNoErrors()
      .assertNotComplete()
      .assertValue(HomeScreenFragmentDirections.showHomeScreen())
  }
}
