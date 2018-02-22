package org.gwtproject.uibinder.test.view.olduibinder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OldSimpleFormView implements IsWidget {


  @UiTemplate("org.gwtproject.uibinder.test.view.SimpleFormView.ui.xml")
  interface MyUiBinder extends UiBinder<VerticalPanel, OldSimpleFormView> {

  }

  private final MyUiBinder binder = GWT.create(MyUiBinder.class);


  private Widget widget;

  @Override
  public Widget asWidget() {
    if (widget == null) {
      widget = binder.createAndBindUi(this);
    }
    return widget;
  }

  @UiFactory
  protected Label createButtonLabel(String theText) {
    return new Label(theText);
  }

  @UiHandler("simpleButton")
  protected void onSimpleButtonClick(ClickEvent event) {
    Window.alert("Button Clicked");
  }

}
