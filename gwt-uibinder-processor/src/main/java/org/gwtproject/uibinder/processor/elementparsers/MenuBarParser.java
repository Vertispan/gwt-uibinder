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
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses MenuBar widgets.
 */
public class MenuBarParser implements ElementParser {

  static final String BAD_CHILD
      = "Only MenuItem or MenuItemSeparator subclasses are valid children";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Generate instantiation (Vertical MenuBars require a ctor param).
    if (UiBinderApiPackage.current().getMenuBarFqn()
        .equals(AptUtil.asQualifiedNameable(type).getQualifiedName().toString())) {
      if (elem.hasAttribute("vertical")) {
        String vertical = elem.consumeBooleanAttribute("vertical");
        writer.setFieldInitializerAsConstructor(fieldName, vertical);
      }
    }

    // Prepare base types.
    TypeElement itemType = AptUtil.getElementUtils()
        .getTypeElement(UiBinderApiPackage.current().getMenuItemFqn());
    TypeElement separatorType = AptUtil.getElementUtils().getTypeElement(
        UiBinderApiPackage.current().getMenuItemSeparatorFqn());

    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      TypeMirror childType = writer.findFieldType(child);

      // MenuItem+
      if (AptUtil.isAssignableFrom(itemType.asType(), childType)) {
        FieldWriter childField = writer.parseElementToField(child);
        writer.addStatement("%1$s.addItem(%2$s);", fieldName, childField.getNextReference());
        continue;
      }

      // MenuItemSeparator+
      if (AptUtil.isAssignableFrom(separatorType.asType(), childType)) {
        FieldWriter childField = writer.parseElementToField(child);
        writer.addStatement("%1$s.addSeparator(%2$s);", fieldName, childField.getNextReference());
        continue;
      }

      // Fail
      writer.die(child, BAD_CHILD);
    }
  }
}
