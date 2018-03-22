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

import static org.gwtproject.uibinder.processor.AptUtil.asQualifiedNameable;

import org.gwtproject.uibinder.processor.attributeparsers.CssNameConverter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.model.ImplicitCssResource;

import com.google.gwt.resources.client.CssResource;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Writes the source to implement an {@link ImplicitCssResource} interface.
 */
public class CssResourceWriter {

  private static final TypeMirror[] NO_PARAMS = new TypeMirror[0];
  private final ImplicitCssResource css;
  private final IndentedWriter writer;
  private final TypeMirror cssResourceType;
  private final TypeMirror stringType;
  private final CssNameConverter nameConverter;
  private final MortalLogger logger;

  public CssResourceWriter(ImplicitCssResource css, PrintWriter writer, MortalLogger logger) {
    this.css = css;
    this.writer = new IndentedWriter(writer);
    Elements elementUtils = AptUtil.getElementUtils();
    this.cssResourceType = elementUtils.getTypeElement(CssResource.class.getName()).asType();
    this.stringType = elementUtils.getTypeElement(String.class.getName()).asType();
    this.nameConverter = new CssNameConverter();
    this.logger = logger;
  }

  public void write() throws UnableToCompleteException {
    // Package declaration
    String packageName = css.getPackageName();
    if (packageName.length() > 0) {
      writer.write("package %1$s;", packageName);
      writer.newline();
    }

    TypeMirror superType = css.getExtendedInterface();
    if (superType == null) {
      superType = cssResourceType;
    }

    writer
        .write("import %s;", asQualifiedNameable(superType).getQualifiedName().toString());
    writer.newline();

    // Open interface
    writer.write("public interface %s extends %s {", css.getClassName(),
        asQualifiedNameable(superType).getSimpleName().toString());
    writer.indent();

    writeCssMethods(superType);

    // Close interface.
    writer.outdent();
    writer.write("}");
  }

  private boolean isOverride(String methodName, TypeMirror superType) {
    ExecutableElement method = AptUtil.findMethod(superType, methodName, NO_PARAMS);
    if (method != null && AptUtil.getTypeUtils().isSameType(stringType, method.getReturnType())) {
      return true;
    }
    return false;
  }

  private void writeCssMethods(TypeMirror superType)
      throws UnableToCompleteException {
    Set<String> rawClassNames = css.getCssClassNames();
    Map<String, String> convertedClassNames = null;

    try {
      convertedClassNames = nameConverter.convertSet(rawClassNames);
    } catch (CssNameConverter.Failure e) {
      logger.die(e.getMessage());
    }

    for (Map.Entry<String, String> entry : convertedClassNames.entrySet()) {
      String className = entry.getValue();
      /*
       * Only write names that we are not overriding from super, or else we'll
       * re-obfuscate any @Shared ones
       */
      if (!isOverride(className, superType)) {
        if (!rawClassNames.contains(className)) {
          writer.write("@ClassName(\"%s\")", entry.getKey());
        }
        writer.write("String %s();", className);
      }
    }
  }
}
