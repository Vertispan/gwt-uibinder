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
import org.gwtproject.uibinder.client.UiChild;
import org.gwtproject.uibinder.client.UiTemplate;
import org.gwtproject.uibinder.example.view.UiChildTestView;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import javax.inject.Inject;

/**
 *
 */
public class UiChildTestViewImpl implements UiChildTestView {

  @UiTemplate(legacyWidgets = true)
  interface MyUiBinder extends UiBinder<Widget, UiChildTestViewImpl> {
  }

  private MyUiBinder uiBinder = new UiChildTestViewImpl_MyUiBinderImpl();

  private Widget widget;

  @Inject
  public UiChildTestViewImpl() {
  }

  @Override
  public Widget asWidget() {
    if (widget == null) {
      widget = uiBinder.createAndBindUi(this);
    }
    return widget;
  }

  /**
   * Custom Widget class to demonstrate @UiChild.
   */
  public static final class MyCustomWidget implements IsWidget {
    private HorizontalPanel container = new HorizontalPanel();
    // using label to know if we've initialized
    private Label label;

    @Override
    public Widget asWidget() {
      if (label == null) {
        Style style = container.getElement().getStyle();
        style.setBorderWidth(1, Unit.PX);
        style.setBorderStyle(BorderStyle.SOLID);
        style.setBorderColor("red");
        style.setMarginTop(25, Unit.PX);

        label = new Label("Custom Child Widget:");
        container.insert(label, 0);
      }
      return container;
    }

    @UiChild(tagname = "customchild")
    public void addCustomChild(Widget childWidget) {
      container.add(childWidget);
    }

    @UiChild(tagname = "anotherchild")
    public void addAnotherChildWithArgs(Label label, String text) {
      label.setText(text);
      container.add(label);
    }

    @UiChild(tagname = "third")
    public void addThirdWithSomePrimitive(Label label, double borderWidth, String borderColor) {
      Style style = label.getElement().getStyle();
      style.setBorderWidth(borderWidth, Unit.PX);
      style.setBorderStyle(BorderStyle.SOLID);
      style.setBorderColor(borderColor);
      container.add(label);
    }
  }
}
