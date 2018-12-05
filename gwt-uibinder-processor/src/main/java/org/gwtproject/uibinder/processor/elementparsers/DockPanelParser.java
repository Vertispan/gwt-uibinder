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

import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.HashMap;

import javax.lang.model.type.TypeMirror;

/**
 * Parses DockPanel widgets.
 */
public class DockPanelParser implements ElementParser {

  private static final String TAG_DOCK = "Dock";
  private final HashMap<String, String> values = new HashMap<>();

  public DockPanelParser() {
    String dockPanelFqn = UiBinderApiPackage.current().getDockPanelFqn();
    values.put("NORTH", dockPanelFqn + ".NORTH");
    values.put("SOUTH", dockPanelFqn + ".SOUTH");
    values.put("EAST", dockPanelFqn + ".EAST");
    values.put("WEST", dockPanelFqn + ".WEST");
    values.put("CENTER", dockPanelFqn + ".CENTER");
    values.put("LINE_START", dockPanelFqn + ".LINE_START");
    values.put("LINE_END", dockPanelFqn + ".LINE_END");
  }

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      // DockPanel can only contain Dock elements.
      String ns = child.getNamespaceUri();
      String tagName = child.getLocalName();

      if (!ns.equals(elem.getNamespaceUri())) {
        writer.die(elem, "Invalid DockPanel child namespace: " + ns);
      }
      if (!tagName.equals(TAG_DOCK)) {
        writer.die(elem, "Invalid DockPanel child element: " + tagName);
      }

      // And they must specify a direction.
      if (!child.hasAttribute("direction")) {
        writer.die(elem, "Dock must specify the 'direction' attribute");
      }
      String value = child.consumeRawAttribute("direction");
      String translated = values.get(value);
      if (translated == null) {
        writer.die(elem, "Invalid value: dockDirection='" + value + "'");
      }

      // And they can only have a single child widget.
      XMLElement widget = child.consumeSingleChildElement();
      FieldWriter childField = writer.parseElementToField(widget);
      writer.addStatement("%1$s.add(%2$s, %3$s);", fieldName,
          childField.getNextReference(), translated);

      // Parse the CellPanel-specific attributes on the Dock element.
      CellPanelParser.parseCellAttributes(child, fieldName, childField, writer);
    }
  }
}
