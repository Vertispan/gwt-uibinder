package org.gwtproject.uibinder.test.view.newuibinder;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.gwtproject.uibinder.client.UiBinder;
import org.gwtproject.uibinder.client.UiTemplate;

/**
 *
 */
public class NewSimpleFormView implements IsWidget {

  @UiTemplate("org.gwtproject.uibinder.test.view.SimpleFormView.ui.xml")
  interface MyUiBinder extends UiBinder<VerticalPanel, NewSimpleFormView> {

  }

  private MyUiBinder binder = new NewSimpleFormView_MyUiBinderImpl();

  private Widget widget;

  @Override
  public Widget asWidget() {
    if (widget == null) {
      widget = binder.createAndBindUi(this);
      // for the time being, use a label
      widget = new Label("unimplemented");
    }
    return widget;
  }


  // TODO
  protected Label createButtonLabel(String theText) {
    return new Label(theText);
  }

  // TODO
  protected void onSimpleButtonClick(ClickEvent event) {
    Window.alert("Button Clicked");
  }


}
