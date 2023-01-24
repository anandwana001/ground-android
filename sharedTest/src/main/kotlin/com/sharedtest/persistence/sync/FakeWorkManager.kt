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
package com.sharedtest.persistence.sync

import android.annotation.SuppressLint
import android.app.PendingIntent
import androidx.lifecycle.LiveData
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import java.util.*

@SuppressLint("RestrictedApi")
class FakeWorkManager : WorkManager() {

  override fun enqueue(requests: List<WorkRequest?>): Operation {
    throw NotImplementedError()
  }

  override fun beginWith(work: List<OneTimeWorkRequest>): WorkContinuation {
    throw NotImplementedError()
  }

  override fun beginUniqueWork(
    uniqueWorkName: String,
    existingWorkPolicy: ExistingWorkPolicy,
    work: List<OneTimeWorkRequest>
  ): WorkContinuation {
    throw NotImplementedError()
  }

  override fun enqueueUniqueWork(
    uniqueWorkName: String,
    existingWorkPolicy: ExistingWorkPolicy,
    work: List<OneTimeWorkRequest>
  ): Operation {
    throw NotImplementedError()
  }

  override fun enqueueUniquePeriodicWork(
    uniqueWorkName: String,
    existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy,
    periodicWork: PeriodicWorkRequest
  ): Operation {
    throw NotImplementedError()
  }

  override fun cancelWorkById(id: UUID): Operation {
    throw NotImplementedError()
  }

  override fun cancelAllWorkByTag(tag: String): Operation {
    throw NotImplementedError()
  }

  override fun cancelUniqueWork(uniqueWorkName: String): Operation {
    throw NotImplementedError()
  }

  override fun cancelAllWork(): Operation {
    throw NotImplementedError()
  }

  override fun createCancelPendingIntent(id: UUID): PendingIntent {
    throw NotImplementedError()
  }

  override fun pruneWork(): Operation {
    throw NotImplementedError()
  }

  override fun getLastCancelAllTimeMillisLiveData(): LiveData<Long> {
    throw NotImplementedError()
  }

  override fun getLastCancelAllTimeMillis(): ListenableFuture<Long> {
    throw NotImplementedError()
  }

  override fun getWorkInfoByIdLiveData(id: UUID): LiveData<WorkInfo> {
    throw NotImplementedError()
  }

  override fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> {
    throw NotImplementedError()
  }

  override fun getWorkInfosByTagLiveData(tag: String): LiveData<List<WorkInfo>> {
    throw NotImplementedError()
  }

  override fun getWorkInfosByTag(tag: String): ListenableFuture<List<WorkInfo>> {
    throw NotImplementedError()
  }

  override fun getWorkInfosForUniqueWorkLiveData(uniqueWorkName: String): LiveData<List<WorkInfo>> {
    throw NotImplementedError()
  }

  override fun getWorkInfosForUniqueWork(uniqueWorkName: String): ListenableFuture<List<WorkInfo>> {
    throw NotImplementedError()
  }

  override fun getWorkInfosLiveData(workQuery: WorkQuery): LiveData<List<WorkInfo>> {
    throw NotImplementedError()
  }

  override fun getWorkInfos(workQuery: WorkQuery): ListenableFuture<List<WorkInfo>> {
    throw NotImplementedError()
  }
}
