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
package org.gwtproject.uibinder.test.view;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtproject.uibinder.test.view.newuibinder.NewSimpleFormView;
import org.gwtproject.uibinder.test.view.olduibinder.OldSimpleFormView;

/**
 *
 */
public class Shell implements IsWidget {

  private HorizontalPanel container;

  @Override
  public Widget asWidget() {
    if (container == null) {
      constructUi();
    }
    return container;

  }

  private void constructUi() {
    container = new HorizontalPanel();
    container.setBorderWidth(1);
    container.setWidth("100%");
    

    VerticalPanel oldPanel = new VerticalPanel();
    container.add(oldPanel);

    oldPanel.add(sectionTitle("Old UiBinder"));
    oldPanel.add(new OldSimpleFormView());

    VerticalPanel newPanel = new VerticalPanel();
    container.add(newPanel);

    newPanel.add(sectionTitle("New UiBinder"));
    newPanel.add(new NewSimpleFormView());
  }

  private Label sectionTitle(String text) {
    return new Label(text);
  }
}
