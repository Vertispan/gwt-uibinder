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
