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
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

/**
 *
 */
public abstract class BaseProcessor extends AbstractProcessor {

  @Override
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    MyTreeLogger logger = new MyTreeLogger(processingEnv.getMessager());
    try {
      if (!env.processingOver()) {
        AptUtil.setProcessingEnvironment(processingEnv);
        for (TypeElement annotation : annotations) {
          for (Element element : env.getElementsAnnotatedWith(annotation)) {
            logger.setCurrentElement(element);
            String generatedClassName = processElement((TypeElement) element, logger);
            logger.log(Kind.NOTE, "generated type " + generatedClassName);
          }
        }
        return true;
      }
    } catch (Exception e) {
      logger.log(Kind.ERROR, "Error Processing Annotation", e);
      return false;
    } finally {
      AptUtil.setProcessingEnvironment(null);
    }
    return false;
  }

  /**
   * Process single TypeElement.
   *
   * @param interfaceType the TypeElement to process
   * @param treeLogger the logger
   * @return canonical classname
   */
  protected abstract String processElement(TypeElement interfaceType, MyTreeLogger treeLogger)
      throws UnableToCompleteException;
}
