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
package org.gwtproject.uibinder.example.view;

import org.gwtproject.uibinder.example.view.newuibinder.NewSimpleFormView;
import org.gwtproject.uibinder.example.view.olduibinder.OldSimpleFormView;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import javax.inject.Inject;

/**
 *
 */
public class OldVsNewComparisonView implements IsWidget {
  private Widget widget;
  private OldSimpleFormView oldView;
  private NewSimpleFormView newView;

  @Inject
  public OldVsNewComparisonView(OldSimpleFormView oldView, NewSimpleFormView newView) {
    this.oldView = oldView;
    this.newView = newView;
  }

  @Override
  public Widget asWidget() {
    if (widget == null) {
      VerticalPanel container = new VerticalPanel();

      container
          .add(new Label("Demonstrates multiple aspects of the New UiBinder vs the Old UiBinder"));

      TabLayoutPanel tabPanel = new TabLayoutPanel(2.5, Unit.EM);
      container.add(tabPanel);

      tabPanel.setSize("640px", "480px");

      tabPanel.add(oldView, "Old UiBinder");
      tabPanel.add(newView, "New UiBinder");

      widget = container;
    }
    return widget;
  }
}
