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
package org.gwtproject.uibinder.example.view.newuibinder;

import org.gwtproject.uibinder.client.UiBinder;
import org.gwtproject.uibinder.client.UiFactory;
import org.gwtproject.uibinder.client.UiField;
import org.gwtproject.uibinder.client.UiHandler;
import org.gwtproject.uibinder.client.UiTemplate;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import javax.inject.Inject;

/**
 *
 */
public class NewSimpleFormView implements IsWidget {

  @UiTemplate(value = "org.gwtproject.uibinder.example.view.SimpleFormView.ui.xml", legacyWidgets = true)
  interface MyUiBinder extends UiBinder<VerticalPanel, NewSimpleFormView> {
  }

  private MyUiBinder binder = new NewSimpleFormView_MyUiBinderImpl();

  private Widget widget;

  @UiField(provided = true)
  Label sourceLocation = new Label("Source: New UiBinder");

  @Inject
  public NewSimpleFormView() {
  }

  @Override
  public Widget asWidget() {
    if (widget == null) {
      widget = binder.createAndBindUi(this);
    }
    return widget;
  }

  @UiFactory
  protected Label createButtonLabel(String theText) {
    // silly, but demonstrates passing attributes to factory methods
    return new Label(theText);
  }

  @UiHandler("simpleButton")
  protected void onSimpleButtonClick(ClickEvent event) {
    Window.alert("Button Clicked: New UiBinder");
  }
}
