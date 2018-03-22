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
package org.gwtproject.uibinder.test.view.impl;

import org.gwtproject.uibinder.test.view.Shell;
import org.gwtproject.uibinder.test.view.newuibinder.NewSimpleFormView;
import org.gwtproject.uibinder.test.view.olduibinder.OldSimpleFormView;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class ShellImpl implements Shell {

  private DockLayoutPanel container;
  private ScrollPanel center;

  @Inject
  public ShellImpl() {
  }

  @Override
  public Widget asWidget() {
    if (container == null) {
      center = new ScrollPanel();

      container = new DockLayoutPanel(Unit.EM);
      Style style = container.getElement().getStyle();
      style.setPosition(Position.ABSOLUTE);
      style.setTop(0, Unit.PX);
      style.setLeft(0, Unit.PX);
      style.setWidth(100, Unit.PCT);
      style.setHeight(100, Unit.PCT);
      style.setOverflow(Overflow.HIDDEN);

      // FIXME - for now, until navigation is here
      center.add(simpleTest());

      container.add(center);
    }
    return container;
  }

  private Widget simpleTest() {
    HorizontalPanel panel = new HorizontalPanel();
    panel.setBorderWidth(1);
    panel.setWidth("100%");

    VerticalPanel oldPanel = new VerticalPanel();
    panel.add(oldPanel);

    oldPanel.add(sectionTitle("Old UiBinder"));
    oldPanel.add(new OldSimpleFormView());

    VerticalPanel newPanel = new VerticalPanel();
    panel.add(newPanel);

    newPanel.add(sectionTitle("New UiBinder"));
    newPanel.add(new NewSimpleFormView());

    return panel;
  }

  private Label sectionTitle(String text) {
    return new Label(text);
  }


}
