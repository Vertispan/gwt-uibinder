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
package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.XMLElement.Interpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * A parser for menu items.
 */
public class MenuItemParser implements ElementParser {

  public void parse(final XMLElement elem, String fieldName, TypeMirror type,
      final UiBinderWriter writer) throws UnableToCompleteException {

    // Use special initializer for standard MenuItem,
    // custom subclass should have default constructor.
    if (UiBinderApiPackage.current().getMenuItemFqn()
        .equals(AptUtil.asQualifiedNameable(type).getQualifiedName().toString())) {
      writer.setFieldInitializerAsConstructor(fieldName, "\"\"",
          "(" + UiBinderApiPackage.current().getCommandFqn() + ") null");
    }

    final TypeElement menuBarType = AptUtil.getElementUtils().getTypeElement(
        UiBinderApiPackage.current().getMenuBarFqn());

    class MenuBarInterpreter implements Interpreter<Boolean> {

      FieldWriter menuBarField = null;

      public Boolean interpretElement(XMLElement child)
          throws UnableToCompleteException {

        if (isMenuBar(child)) {
          if (menuBarField != null) {
            writer.die(child, "Only one MenuBar may be contained in a MenuItem");
          }
          menuBarField = writer.parseElementToField(child);
          return true;
        }

        return false;
      }

      boolean isMenuBar(XMLElement child) throws UnableToCompleteException {
        return AptUtil.isAssignableFrom(menuBarType.asType(), writer.findFieldType(child));
      }
    }

    MenuBarInterpreter interpreter = new MenuBarInterpreter();
    elem.consumeChildElements(interpreter);

    if (interpreter.menuBarField != null) {
      writer.genPropertySet(fieldName, "subMenu",
          interpreter.menuBarField.getNextReference());
    }
  }
}
