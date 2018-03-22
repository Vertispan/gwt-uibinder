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
package org.gwtproject.uibinder.test.ioc;

import org.gwtproject.uibinder.test.view.Shell;
import org.gwtproject.uibinder.test.view.impl.ShellImpl;

import dagger.Binds;
import dagger.Module;

/**
 * Main module for the test bed application.
 */
@Module
public abstract class TestBedModule {

  @Binds
  abstract Shell shell(ShellImpl shell);
}
