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

import org.gwtproject.uibinder.example.NavigationRepository.Token;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Simple navigation handling.
 */
@Singleton
public class NavigationRepository implements HasValueChangeHandlers<Token> {
  public static final String DEFAULT_VIEW_TOKEN_NAME = "DEFAULT";
  private boolean initialized = false;

  private final Map<Token, Provider<? extends IsWidget>> navigationMap = new HashMap<>();
  private final Provider<? extends IsWidget> defaultView;

  private Token currentLocation;
  private HasWidgets container;
  private EventBus eventBus;

  @Inject
  public NavigationRepository(
      @Named("navigationMap") Map<Token, Provider<? extends IsWidget>> navMap) {
    this.defaultView = navMap.get(Token.DEFAULT);

    navigationMap.putAll(navMap);
    this.eventBus = new SimpleEventBus();
  }

  public Collection<Token> getTokens() {
    return Collections.unmodifiableSet(navigationMap.keySet());
  }

  public void initHistoryHandling(final HasWidgets container) {
    if (!this.initialized) {
      this.container = container;
      History.addValueChangeHandler(event -> {
        Token historyToken = Token.from(event.getValue());
        doNavigate(historyToken);
      });
      this.initialized = true;
    }
  }

  public void handleCurrentHistory() {
    if (!initialized) {
      return;
    }
    doNavigate(Token.from(History.getToken()));
  }

  public Token getCurrentLocation() {
    return currentLocation;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Token> handler) {
    return eventBus.addHandler(ValueChangeEvent.getType(), handler);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    eventBus.fireEvent(event);
  }

  private void doNavigate(Token historyToken) {
    if (!initialized || historyToken.equals(currentLocation)) {
      return;
    }

    Provider<? extends IsWidget> contentProvider = navigationMap
        .getOrDefault(historyToken, defaultView);
    container.clear();
    container.add(contentProvider.get().asWidget());
    currentLocation = historyToken;
    ValueChangeEvent.fire(this, currentLocation);
  }

  /**
   * Token entity used for navigation purposes.
   */
  public static final class Token {
    public static final Token DEFAULT = create(DEFAULT_VIEW_TOKEN_NAME, "Home");

    public static Token create(String tokenString, String label) {
      return new Token(tokenString, label);
    }

    public static Token from(String tokenString) {
      return create(tokenString, tokenString);
    }

    private final String token;
    private final String label;

    private Token(String token, String label) {
      this.token = token;
      this.label = label;
    }

    public String getToken() {
      return token;
    }

    public String getLabel() {
      return label;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Token token1 = (Token) o;
      return Objects.equals(token, token1.token);
    }

    @Override
    public int hashCode() {
      return Objects.hash(token);
    }
  }
}
