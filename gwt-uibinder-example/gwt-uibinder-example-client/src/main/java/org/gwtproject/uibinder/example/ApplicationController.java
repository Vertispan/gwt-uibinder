/*
 * Copyright 2018 Vertispan LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.uibinder.example;

import org.gwtproject.uibinder.example.view.Shell;

import com.google.gwt.user.client.ui.HasWidgets;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Application controller.
 */
@Singleton
public class ApplicationController {

  private Shell shell;
  private NavigationRepository navigationRepository;

  @Inject
  public ApplicationController(Shell shell, NavigationRepository repository) {
    this.shell = shell;
    navigationRepository = repository;
  }

  public void start(HasWidgets hasWidgets) {
    navigationRepository.initHistoryHandling(shell.getMainDisplay());

    hasWidgets.clear();
    hasWidgets.add(shell.asWidget());

    navigationRepository.handleCurrentHistory();
  }
}
