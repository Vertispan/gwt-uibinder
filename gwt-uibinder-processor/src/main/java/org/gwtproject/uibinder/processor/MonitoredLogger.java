package org.gwtproject.uibinder.processor;


import javax.tools.Diagnostic.Kind;

/**
 * Handy for logging a bunch of errors and then dying later. Keeps a hasErrors flag that is set if
 * any errors are logged.
 */
public class MonitoredLogger {

  private boolean hasErrors = false;
  private final MortalLogger logger;

  public MonitoredLogger(MortalLogger mortalLogger) {
    this.logger = mortalLogger;
  }

  /**
   * Post an error. Sets the {@link #hasErrors} flag
   */
  public void error(String message, Object... params) {
    hasErrors = true;
    logger.log(Kind.ERROR, String.format(message, params));
  }

  public void error(XMLElement context, String message, Object... params) {
    hasErrors = true;
    logger.logLocation(Kind.ERROR, context, String.format(message, params));
  }

  /**
   * Returns true if {@link #error} has ever been called.
   */
  public boolean hasErrors() {
    return hasErrors;
  }
}
