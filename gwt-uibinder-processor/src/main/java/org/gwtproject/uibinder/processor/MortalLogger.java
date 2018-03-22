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
package org.gwtproject.uibinder.processor;

import org.gwtproject.uibinder.processor.XMLElement.Location;
import org.gwtproject.uibinder.processor.ext.MyTreeLogger;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.Diagnostic.Kind;

/**
 * Wraps a {@link MyTreeLogger} with handy {@link String#format} style methods and can be told to
 * die. Perhaps we should instead add die(), warn(), etc. to Treelogger.
 */
public class MortalLogger {

  protected static String locationOf(XMLElement context) {
    if (context == null) {
      return "";
    }

    Location location = context.getLocation();
    if (location != null) {
      String displayFileName = location.getSystemId();
      if (displayFileName == null) {
        // We see this in the test cases that don't use actual source files
        displayFileName = "Unknown";
      } else {
        // Parse the system id as a URI, which it almost always is
        try {
          URI uri = new URI(location.getSystemId());
          String path = uri.getPath();
          if (path != null) {
            displayFileName = path.substring(path.lastIndexOf('/') + 1);
          }
        } catch (URISyntaxException e) {
          // Fall back to the raw system id
        }
      }
      // Log in a way that usually triggers IDE hyperlinks
      return ": " + context.toString() + " (" + displayFileName + ":"
          + location.getLineNumber() + ")";
    } else {
      /*
       * This shouldn't occur unless the XMLElement came from a DOM Node created
       * by something other than W3cDocumentBuilder.
       */
      return " " + context.toString();
    }
  }

  private final MyTreeLogger logger;

  MortalLogger(MyTreeLogger treeLoggerAdapter) {
    this.logger = treeLoggerAdapter;
  }

  public void die(String message, Object... params) throws UnableToCompleteException {
    die(null, message, params);
  }

  public void die(XMLElement context, String message, Object... params)
      throws UnableToCompleteException {

    logLocation(Kind.ERROR, context, String.format(message, params));
    throw new UnableToCompleteException();
  }

  /**
   * Returns instance of TreeLoggerAdapter.
   */
  public final MyTreeLogger getTreeLogger() {
    return logger;
  }

  public void logLocation(Kind kind, XMLElement context, String message) {
    message += locationOf(context);
    logger.log(kind, message);
  }

  /**
   * Post a warning message.
   */
  public void warn(String message, Object... params) {
    warn(null, message, params);
  }

  /**
   * Post a warning message related to a specific XMLElement.
   */
  public void warn(XMLElement context, String message, Object... params) {
    logLocation(Kind.WARNING, context, String.format(message, params));
  }
}
