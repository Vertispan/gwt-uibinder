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
package org.gwtproject.uibinder.example.view.impl;

import org.gwtproject.uibinder.client.UiBinder;
import org.gwtproject.uibinder.client.UiFactory;
import org.gwtproject.uibinder.client.UiField;
import org.gwtproject.uibinder.client.UiHandler;
import org.gwtproject.uibinder.client.UiTemplate;
import org.gwtproject.uibinder.example.NavigationRepository;
import org.gwtproject.uibinder.example.NavigationRepository.Token;
import org.gwtproject.uibinder.example.view.Shell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellList.Style;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SimpleKeyProvider;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class ShellImpl implements Shell {

  @UiTemplate(legacyWidgets = true)
  interface MyUiBinder extends UiBinder<DockLayoutPanel, ShellImpl> {
  }

  interface ShellResources extends CellList.Resources {
    @Override
    //@Source("org/gwtproject/uibinder/example/view/impl/shell.gss")
    @Source("shell.gss")
    Style cellListStyle();
  }

  private DockLayoutPanel container;

  @UiField(provided = true)
  ScrollPanel mainArea = new ScrollPanel();

  private ShellResources resources = GWT.create(ShellResources.class);
  private MyUiBinder uiBinder = new ShellImpl_MyUiBinderImpl();
  private NavigationRepository navigationRepository;

  @Inject
  public ShellImpl(NavigationRepository navigationRepository) {
    this.navigationRepository = navigationRepository;

    container = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return container;
  }

  @UiHandler("logo")
  public void onLogoClick(ClickEvent clickEvent) {
    History.newItem("");
  }

  @UiFactory
  CellList<Token> createNavigation() {
    CellList<Token> cellList = new CellList<>(new AbstractCell<Token>() {
      @Override
      public void render(Context context, Token value, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<i class='icon_getstarted'></i>");
        sb.appendHtmlConstant("<span>")
            .appendEscaped(value.getLabel())
            .appendHtmlConstant("</span>");
      }
    }, resources);
    cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

    final SingleSelectionModel<Token> selectionModel = new SingleSelectionModel<Token>(
        new SimpleKeyProvider<Token>() {
          @Override
          public Object getKey(Token item) {
            return item.getToken();
          }
        });

    cellList.setSelectionModel(selectionModel);
    selectionModel.addSelectionChangeHandler(event -> {
      if (selectionModel.getSelectedObject() != null) {
        History.newItem(selectionModel.getSelectedObject().getToken());
      }
    });

    List<Token> navList = new ArrayList<>(navigationRepository.getTokens());
    navList.remove(Token.DEFAULT);

    ListDataProvider<Token> dataProvider = new ListDataProvider<>(navList);
    dataProvider.addDataDisplay(cellList);

    navigationRepository.addValueChangeHandler(event -> {
      selectionModel.setSelected(event.getValue(), true);
    });

    return cellList;
  }

  @Override
  public HasWidgets getMainDisplay() {
    return mainArea;
  }
}
