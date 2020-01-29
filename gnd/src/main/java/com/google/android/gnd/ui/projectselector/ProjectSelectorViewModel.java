/*
 * Copyright 2018 Google LLC
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

package com.google.android.gnd.ui.projectselector;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import com.google.android.gnd.model.Project;
import com.google.android.gnd.repository.Loadable;
import com.google.android.gnd.repository.ProjectRepository;
import com.google.android.gnd.system.AuthenticationManager;
import com.google.android.gnd.ui.common.AbstractViewModel;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/** Represents view state and behaviors of the project selector dialog. */
public class ProjectSelectorViewModel extends AbstractViewModel {
  private final ProjectRepository projectRepository;
  private final LiveData<Loadable<List<Project>>> projectSummaries;

  @Inject
  ProjectSelectorViewModel(ProjectRepository projectRepository, AuthenticationManager authManager) {
    this.projectRepository = projectRepository;

    this.projectSummaries =
        LiveDataReactiveStreams.fromPublisher(
            projectRepository.getProjectSummaries(authManager.getCurrentUser()));
  }

  public LiveData<Loadable<List<Project>>> getProjectSummaries() {
    return projectSummaries;
  }

  private Project getProjectSummary(int idx) {
    return Loadable.getValue(this.projectSummaries).orElse(Collections.emptyList()).get(idx);
  }

  /**
   * Triggers the specified project to be loaded and activated.
   *
   * @param idx the index in the project summary list.
   */
  void activateProject(int idx) {
    projectRepository.activateProject(getProjectSummary(idx).getId());
  }
}
