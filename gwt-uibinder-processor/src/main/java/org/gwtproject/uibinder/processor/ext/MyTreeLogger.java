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
package org.gwtproject.uibinder.processor.ext;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * Local APT implementation of a tree logger.
 *
 * <p>This class also can provide an upstream TreeLogger instance for use of passing to other
 * modules such as Gss, etc.
 *
 * FIXME - needs implementation around branches, etc.
 */
public class MyTreeLogger {

  private Element currentElement;
  private final Messager messager;
  private final TreeLogger adaptedTreeLogger;

  public MyTreeLogger(Messager messager) {
    this.messager = messager;
    LocalOutputStream localOutputStream = new LocalOutputStream(Kind.ERROR);
    this.adaptedTreeLogger = new PrintWriterTreeLogger(new PrintWriter(localOutputStream));
  }

  public final void log(Kind kind, String msg) {
    this.log(kind, msg, (Throwable) null);
  }

  public final void log(Kind kind, String msg, Throwable caught) {
    if (msg == null) {
      msg = "(Null log message)";
    }

    if (caught != null) {
      StringWriter stringWriter = new StringWriter();
      caught.printStackTrace(new PrintWriter(stringWriter));
      msg += ": " + stringWriter.getBuffer().toString();
    }

    if (currentElement == null) {
      messager.printMessage(kind, msg);
    } else {
      messager.printMessage(kind, msg, currentElement);
    }
  }

  /**
   * Sets the currentElement being processed.
   */
  public void setCurrentElement(Element currentElement) {
    this.currentElement = currentElement;
  }

  public TreeLogger getAdapted() {
    return adaptedTreeLogger;
  }

  private class LocalOutputStream extends OutputStream {
    private Kind level;

    /**
     * The internal memory for the written bytes.
     */
    private String mem;

    private LocalOutputStream(Kind level) {
      this.level = level;
      mem = "";
    }

    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     *
     * @param b DOCUMENT ME!
     */
    public void write(int b) {
      byte[] bytes = new byte[1];
      bytes[0] = (byte) (b & 0xff);
      mem = mem + new String(bytes);

      if (mem.endsWith("\n")) {
        mem = mem.substring(0, mem.length() - 1);
        flush();
      }
    }

    /**
     * Flushes the output stream.
     */
    public void flush() {
      log(level, mem);
      mem = "";
    }
  }
}
