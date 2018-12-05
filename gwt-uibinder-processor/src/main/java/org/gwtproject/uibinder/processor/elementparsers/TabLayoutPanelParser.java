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
 * Parses TabLayoutPanel widgets.
 */
public class TabLayoutPanelParser implements ElementParser {

  private static class Children {

    XMLElement body;
    XMLElement header;
    XMLElement customHeader;
  }

  private static final String CUSTOM = "customHeader";
  private static final String HEADER = "header";
  private static final String TAB = "tab";

  public void parse(XMLElement panelElem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // TabLayoutPanel requires tabBar size and unit ctor args.

    String size = panelElem.consumeRequiredDoubleAttribute("barHeight");

    TypeElement unitEnumType = AptUtil.getElementUtils().getTypeElement(
        UiBinderApiPackage.current().getDomStyleUnitFqn());
    String unit = panelElem.consumeAttributeWithDefault("barUnit",
        String.format("%s.%s", AptUtil.asQualifiedNameable(unitEnumType).getQualifiedName(), "PX"),
        unitEnumType.asType());

    writer.setFieldInitializerAsConstructor(fieldName, size, unit);

    // Parse children.
    for (XMLElement tabElem : panelElem.consumeChildElements()) {
      // Get the tab element.
      if (!isElementType(panelElem, tabElem, TAB)) {
        writer.die(tabElem, "Only <%s:%s> children are allowed.",
            panelElem.getPrefix(), TAB);
      }

      // Find all the children of the <tab>.
      Children children = findChildren(tabElem, writer);

      // Parse the child widget.
      if (children.body == null) {
        writer.die(tabElem, "Must have a child widget");
      }
      if (!writer.isWidgetElement(children.body)) {
        writer.die(children.body, "Must be a widget");
      }
      FieldWriter childField = writer.parseElementToField(children.body);

      // Parse the header.
      if (children.header != null) {
        HtmlInterpreter htmlInt = HtmlInterpreter.newInterpreterForUiObject(
            writer, fieldName);
        String html = children.header.consumeInnerHtml(htmlInt);
        writer.addStatement("%s.add(%s, %s, true);", fieldName,
            childField.getNextReference(),
            writer.declareTemplateCall(html, fieldName));
      } else if (children.customHeader != null) {
        XMLElement headerElement = children.customHeader.consumeSingleChildElement();

        if (!writer.isWidgetElement(headerElement)) {
          writer.die(headerElement, "Is not a widget");
        }

        FieldWriter headerField = writer.parseElementToField(headerElement);
        writer.addStatement("%s.add(%s, %s);", fieldName,
            childField.getNextReference(), headerField.getNextReference());
      } else {
        // Neither a header or customHeader.
        writer.die(tabElem, "Requires either a <%1$s:%2$s> or <%1$s:%3$s>",
            tabElem.getPrefix(), HEADER, CUSTOM);
      }
    }
  }

  private Children findChildren(final XMLElement elem,
      final UiBinderWriter writer) throws UnableToCompleteException {
    final Children children = new Children();

    elem.consumeChildElements(new XMLElement.Interpreter<Boolean>() {
      public Boolean interpretElement(XMLElement child)
          throws UnableToCompleteException {

        if (isElementType(elem, child, HEADER)) {
          assertFirstHeader();
          children.header = child;
          return true;
        }

        if (isElementType(elem, child, CUSTOM)) {
          assertFirstHeader();
          children.customHeader = child;
          return true;
        }

        // Must be the body, then
        if (children.body != null) {
          writer.die(children.body, "May have only one body element");
        }

        children.body = child;
        return true;
      }

      void assertFirstHeader() throws UnableToCompleteException {
        if (children.header != null || children.customHeader != null) {
          writer.die(elem, "May have only one <%1$s:header> "
              + "or <%1$s:customHeader>", elem.getPrefix());
        }
      }
    });

    return children;
  }

  private boolean isElementType(XMLElement parent, XMLElement child, String type) {
    return parent.getNamespaceUri().equals(child.getNamespaceUri())
        && type.equals(child.getLocalName());
  }
}
