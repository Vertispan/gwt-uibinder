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

import java.util.Locale;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses CellPanel widgets.
 */
public class CellPanelParser implements ElementParser {

  private static final String HALIGN_ATTR = "horizontalAlignment";
  private static final String VALIGN_ATTR = "verticalAlignment";
  private static final String WIDTH_ATTR = "width";
  private static final String HEIGHT_ATTR = "height";
  private static final String CELL_TAG = "Cell";

  /**
   * Parses the alignment and size attributes common to all CellPanels.
   *
   * This is exposed publicly because there is a DockPanelParser that overrides the default
   * behavior, but still needs to parse these attributes.
   */
  protected static void parseCellAttributes(XMLElement cellElem, String fieldName,
      FieldWriter childField, UiBinderWriter writer)
      throws UnableToCompleteException {
    TypeElement hAlignConstantType = AptUtil.getElementUtils().getTypeElement(
        UiBinderApiPackage.current().getHorizontalAlignmentConstantFqn());
    TypeElement vAlignConstantType = AptUtil.getElementUtils().getTypeElement(
        UiBinderApiPackage.current().getVerticalAlignmentConstantFqn());

    // Parse horizontal and vertical alignment attributes.
    if (cellElem.hasAttribute(HALIGN_ATTR)) {
      String value = cellElem.consumeAttribute(HALIGN_ATTR, hAlignConstantType.asType());
      writer.addStatement("%1$s.setCellHorizontalAlignment(%2$s, %3$s);",
          fieldName, childField.getNextReference(), value);
    }

    if (cellElem.hasAttribute(VALIGN_ATTR)) {
      String value = cellElem.consumeAttribute(VALIGN_ATTR, vAlignConstantType.asType());
      writer.addStatement("%1$s.setCellVerticalAlignment(%2$s, %3$s);",
          fieldName, childField.getNextReference(), value);
    }

    // Parse width and height attributes.
    if (cellElem.hasAttribute(WIDTH_ATTR)) {
      String value = cellElem.consumeStringAttribute(WIDTH_ATTR);
      writer.addStatement("%1$s.setCellWidth(%2$s, %3$s);", fieldName,
          childField.getNextReference(), value);
    }

    if (cellElem.hasAttribute(HEIGHT_ATTR)) {
      String value = cellElem.consumeStringAttribute(HEIGHT_ATTR);
      writer.addStatement("%1$s.setCellHeight(%2$s, %3$s);", fieldName,
          childField.getNextReference(), value);
    }
  }

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    for (XMLElement child : elem.consumeChildElements()) {
      String ns = child.getNamespaceUri();
      String tagName = child.getLocalName();

      if (ns != null && ns.equals(elem.getNamespaceUri())
          && localTagNameIsCell(tagName)) {
        // It's a cell element, so parse its single child as a widget.
        XMLElement widget = child.consumeSingleChildElement();
        FieldWriter childField = writer.parseElementToField(widget);
        writer.addStatement("%1$s.add(%2$s);", fieldName, childField.getNextReference());

        // Parse the cell tag's alignment & size attributes.
        parseCellAttributes(child, fieldName, childField, writer);
      } else {
        if (!writer.isWidgetElement(child)) {
          writer.die(elem, "Expected a widget or <%s:%s>, found %s",
              elem.getPrefix(), CELL_TAG.toLowerCase(Locale.ROOT), child);
        }
        // It's just a normal child, so parse it as a widget.
        FieldWriter childField = writer.parseElementToField(child);
        writer.addStatement("%1$s.add(%2$s);", fieldName, childField.getNextReference());
      }
    }
  }

  private boolean localTagNameIsCell(String tagName) {
    // Older templates had this as "Cell", but now we prefer "cell"
    return tagName.equals(CELL_TAG) || tagName.equals(CELL_TAG.toLowerCase(Locale.ROOT));
  }
}
