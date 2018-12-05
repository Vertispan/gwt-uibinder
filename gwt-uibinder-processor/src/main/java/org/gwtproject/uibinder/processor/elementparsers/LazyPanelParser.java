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

import javax.lang.model.type.TypeMirror;

/**
 * Parses LazyPanel widgets.
 */
public class LazyPanelParser implements ElementParser {

  private static final String INITIALIZER_FORMAT = "new %s() {\n"
      + "  protected %s createWidget() {\n"
      + "    return %s;\n"
      + "  }\n"
      + "}";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {

    if (writer.getOwnerClass().getUiField(fieldName).isProvided()) {
      return;
    }

    if (!writer.useLazyWidgetBuilders()) {
      writer.die("LazyPanel only works with UiBinder.useLazyWidgetBuilders enabled.");
    }

    XMLElement child = elem.consumeSingleChildElement();
    if (!writer.isWidgetElement(child)) {
      writer.die(child, "Expecting only widgets in %s", elem);
    }

    FieldWriter childField = writer.parseElementToField(child);

    String lazyPanelClassPath = UiBinderApiPackage.current().getLazyPanelFqn();
    String widgetClassPath = UiBinderApiPackage.current().getWidgetFqn();

    String code = String.format(INITIALIZER_FORMAT, lazyPanelClassPath,
        widgetClassPath, childField.getNextReference());
    writer.setFieldInitializer(fieldName, code);
  }
}
