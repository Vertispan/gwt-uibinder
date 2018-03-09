/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.uibinder.processor.elementparsers;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.DockLayoutPanel} widgets.
 *
 * TODO(jgw): The code that explicitly excludes SplitLayoutPanel in a fairly awkward way could be
 * greatly simplified if we hoisted the "dock-ness" into an interface implemented by both
 * DockLayoutPanel and SplitLayoutPanel, and moved most of this code into a parser for that specific
 * interface. This parser would then be reduced to a simple special case for the ctor param.
 */
public class DockLayoutPanelParser implements ElementParser {

  private static class CenterChild {

    final String widgetName;
    final XMLElement child;

    public CenterChild(XMLElement child, String widgetName) {
      this.widgetName = widgetName;
      this.child = child;
    }
  }

  private static final Map<String, String> DOCK_NAMES = new HashMap<String, String>();

  static {
    DOCK_NAMES.put("north", "addNorth");
    DOCK_NAMES.put("south", "addSouth");
    DOCK_NAMES.put("east", "addEast");
    DOCK_NAMES.put("west", "addWest");
    DOCK_NAMES.put("lineStart", "addLineStart");
    DOCK_NAMES.put("lineEnd", "addLineEnd");
    DOCK_NAMES.put("center", "add");
  }

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Generate instantiation (requires a 'unit' ctor param).
    // (Don't generate a ctor for the SplitLayoutPanel, it has its own parser).
    if (type != getSplitLayoutPanelType(writer)) {
      TypeElement unitEnumType = AptUtil.getElementUtils().getTypeElement(
          Unit.class.getCanonicalName());
      String unit = elem.consumeAttributeWithDefault("unit",
          String
              .format("%s.%s", AptUtil.asQualifiedNameable(unitEnumType).getQualifiedName(), "PX"),
          unitEnumType.asType());
      writer.setFieldInitializerAsConstructor(fieldName, unit);
    }

    CenterChild center = null;

    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      // Make sure the element is one of the fixed set of valid directions.
      if (!isValidChildElement(elem, child)) {
        writer.die(elem,
            "Child must be one of "
                + "<%1$s:north>, <%1$s:south>, <%1$s:east>, <%1$s:west> or <%1$s:center>, "
                + "but found %2$s", elem.getPrefix(), child);
      }

      // Consume the single widget element.
      XMLElement widget = child.consumeSingleChildElement();
      if (!writer.isWidgetElement(widget)) {
        writer.die(elem, "%s must contain a widget, but found %s", child,
            widget);
      }
      FieldWriter widgetField = writer.parseElementToField(widget);

      if (child.getLocalName().equals("center")) {
        if (center != null) {
          writer.die(elem, "Only one <%s:center> is allowed", elem.getPrefix());
        }
        center = new CenterChild(child, widgetField.getNextReference());
      } else {
        String size = child.consumeRequiredDoubleAttribute("size");
        writer.addStatement("%s.%s(%s, %s);", fieldName, addMethodName(child),
            widgetField.getNextReference(), size);
      }
    }

    if (center != null) {
      writer.addStatement("%s.%s(%s);", fieldName, addMethodName(center.child),
          center.widgetName);
    }
  }

  private String addMethodName(XMLElement elem) {
    return DOCK_NAMES.get(elem.getLocalName());
  }

  private TypeElement getSplitLayoutPanelType(UiBinderWriter writer) {
    return AptUtil.getElementUtils().getTypeElement(SplitLayoutPanel.class.getName());
  }

  private boolean isValidChildElement(XMLElement parent, XMLElement child) {
    if (!parent.getNamespaceUri().equals(child.getNamespaceUri())) {
      return false;
    }
    if (!DOCK_NAMES.containsKey(child.getLocalName())) {
      return false;
    }
    // Made it through the gauntlet.
    return true;
  }
}
