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

import org.gwtproject.uibinder.processor.ext.MyTreeLogger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * Factory for printwriters creating source files in a particular package.
 */
public class PrintWriterManager {

  private final ProcessingEnvironment processingEnv;
  private final MyTreeLogger logger;
  private final String packageName;
  private final Set<PrintWriter> writers = new HashSet<>();

  PrintWriterManager(ProcessingEnvironment processingEnv, MyTreeLogger logger,
      String packageName) {

    this.processingEnv = processingEnv;
    this.logger = logger;
    this.packageName = packageName;
  }

  /**
   * Commit all writers we have vended.
   */
  void commit() {
    for (PrintWriter writer : writers) {
      // TODO
      writer.close();
    }
  }

  /**
   * @param name classname
   * @return the printwriter
   * @throws RuntimeException if this class has already been written
   */
  PrintWriter makePrintWriterFor(String name) {
    PrintWriter writer = tryToMakePrintWriterFor(name);
    if (writer == null) {
      throw new RuntimeException(String.format("Tried to write %s.%s twice.",
          packageName, name));
    }

    return writer;
  }

  /**
   * @param name classname
   * @param originatingElements type or package elements causally associated with the creation of
   * this file, may be elided or null
   * @return the printwriter, or null if this class has already been written
   */
  PrintWriter tryToMakePrintWriterFor(String name, Element... originatingElements) {
    PrintWriter writer = tryCreate(name, originatingElements);
    if (writer != null) {
      writers.add(writer);
    }
    return writer;
  }

  private PrintWriter tryCreate(String simpleTypeName, Element... originatingElements) {

    String typeName;
    if (packageName.length() == 0) {
      typeName = simpleTypeName;
    } else {
      typeName = packageName + '.' + simpleTypeName;
    }

    TypeElement existingType = processingEnv.getElementUtils().getTypeElement(typeName);
    // FIXME - identify when we should overwrite
    /*
    if (existingType != null) {
      logger.log(Kind.NOTE, "Type '" + typeName + "' already exists and will not be re-created");
      return null;
    }
    */

    try {
      JavaFileObject sourceFile = processingEnv.getFiler()
          .createSourceFile(typeName, originatingElements);
      return new PrintWriter(sourceFile.openWriter()) {
        @Override
        public void println() {
          super.print('\n');
          super.flush();
        }
      };
    } catch (IOException e) {
      logger.log(Kind.ERROR, "Unable to create source file", e);
    }
    return null;
  }
}
