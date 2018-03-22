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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.Tree} widgets.
 */
public class HasTreeItemsParser implements ElementParser {

  static final String BAD_CHILD = "Only TreeItem or Widget subclasses are valid children";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Prepare base types.
    TypeElement itemType = AptUtil.getElementUtils().getTypeElement(TreeItem.class.getName());
    TypeElement widgetType = AptUtil.getElementUtils().getTypeElement(Widget.class.getName());
    TypeElement isWidgetType = AptUtil.getElementUtils().getTypeElement(IsWidget.class.getName());

    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      TypeMirror childType = writer.findFieldType(child);

      // TreeItem+
      if (AptUtil.isAssignableFrom(itemType.asType(), childType)) {
        FieldWriter childField = writer.parseElementToField(child);
        writer.addStatement("%1$s.addItem(%2$s);", fieldName,
            childField.getNextReference());
        continue;
      }

      // Widget+ or IsWidget+
      if (AptUtil.isAssignableFrom(widgetType.asType(), childType)
          || AptUtil.isAssignableFrom(isWidgetType.asType(), childType)) {
        FieldWriter childField = writer.parseElementToField(child);
        writer.addStatement("%1$s.addItem(%2$s);", fieldName,
            childField.getNextReference());
        continue;
      }

      // Fail
      writer.die(child, BAD_CHILD);
    }
  }
}
