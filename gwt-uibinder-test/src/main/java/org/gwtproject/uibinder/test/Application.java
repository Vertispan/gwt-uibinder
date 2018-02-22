package org.gwtproject.uibinder.test;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gwtproject.uibinder.test.view.Shell;

/**
 *
 */
public class Application implements EntryPoint, UncaughtExceptionHandler {

  private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

  @Override
  public void onModuleLoad() {
    GWT.setUncaughtExceptionHandler(this);

    GWT.runAsync(new RunAsyncCallback() {
      @Override
      public void onFailure(Throwable throwable) {
        Application.this.onUncaughtException(throwable);
      }

      @Override
      public void onSuccess() {
        Shell shell = new Shell();
        RootPanel panel = RootPanel.get();
        panel.add(shell);
      }
    });
  }

  @Override
  public void onUncaughtException(Throwable throwable) {
    Window.alert("Error in application.  View console");
    LOGGER.log(Level.SEVERE, "Application error", throwable);
  }
}
