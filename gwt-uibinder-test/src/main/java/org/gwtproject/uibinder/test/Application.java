/*
 * Copyright 2018 Google Inc.
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
package org.gwtproject.uibinder.test;

import org.gwtproject.uibinder.test.view.Shell;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Application implements EntryPoint, UncaughtExceptionHandler {

  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  @Override
  public void onModuleLoad() {
    GWT.setUncaughtExceptionHandler(this);

    GWT.runAsync(new RunAsyncCallback() {
      @Override
      public void onFailure(Throwable throwable) {
        Application.this.onUncaughtException(throwable);
      }

      @Override
      public void onSuccess() {
        Shell shell = new Shell();
        RootPanel panel = RootPanel.get();
        panel.add(shell);
      }
    });
  }

  @Override
  public void onUncaughtException(Throwable throwable) {
    Window.alert("Error in application.  View console");
    LOGGER.log(Level.SEVERE, "Application error", throwable);
  }
}
