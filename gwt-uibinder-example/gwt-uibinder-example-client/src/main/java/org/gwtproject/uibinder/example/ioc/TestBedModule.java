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
package org.gwtproject.uibinder.example.ioc;

import org.gwtproject.uibinder.example.NavigationRepository.Token;
import org.gwtproject.uibinder.example.view.HomeView;
import org.gwtproject.uibinder.example.view.MiscTestView;
import org.gwtproject.uibinder.example.view.OldVsNewComparisonView;
import org.gwtproject.uibinder.example.view.Shell;
import org.gwtproject.uibinder.example.view.SupplementalView;
import org.gwtproject.uibinder.example.view.UiChildTestView;
import org.gwtproject.uibinder.example.view.impl.HomeViewImpl;
import org.gwtproject.uibinder.example.view.impl.MiscTestViewImpl;
import org.gwtproject.uibinder.example.view.impl.ShellImpl;
import org.gwtproject.uibinder.example.view.impl.SupplementalViewImpl;
import org.gwtproject.uibinder.example.view.impl.UiChildTestViewImpl;

import com.google.gwt.user.client.ui.IsWidget;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Main module for the test bed application.
 */
@Module
public abstract class TestBedModule {

  @Provides
  @Named("navigationMap")
  @Singleton
  static Map<Token, Provider<? extends IsWidget>> provideNavigationMapping(
      Provider<HomeView> homeViewProvider,
      Provider<OldVsNewComparisonView> oldVsNewComparisonViewProvider,
      Provider<UiChildTestView> uiChildTestViewProvider,
      Provider<SupplementalView> supplementalViewProvider,
      Provider<MiscTestView> miscTestViewProvider) {
    Map<Token, Provider<? extends IsWidget>> navMap = new LinkedHashMap<>();

    navMap.put(Token.DEFAULT, homeViewProvider);
    navMap.put(Token.create("simpleCompare", "Old Vs New Comparison"),
        oldVsNewComparisonViewProvider);
    navMap.put(Token.create("uichild", "UiChild Test"), uiChildTestViewProvider);
    navMap.put(Token.create("supplemental", "Supplemental View"), supplementalViewProvider);
    navMap.put(Token.create("misc", "Misc Tests"), miscTestViewProvider);

    return navMap;
  }

  @Binds
  abstract Shell shell(ShellImpl shell);

  @Binds
  abstract HomeView homeView(HomeViewImpl homeView);

  @Binds
  abstract UiChildTestView uiChildTestView(UiChildTestViewImpl impl);

  @Binds
  abstract SupplementalView supplementalView(SupplementalViewImpl impl);

  @Binds
  abstract MiscTestView miscTestView(MiscTestViewImpl impl);
}
