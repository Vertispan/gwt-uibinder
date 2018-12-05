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

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.model.OwnerField;

import javax.lang.model.type.TypeMirror;

/**
 * Parses FlowPanel widgets.
 */
public class FlowPanelParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      final UiBinderWriter writer) throws UnableToCompleteException {
    String customTag = elem.consumeStringAttribute("tag", null);
    if (null != customTag) {
      OwnerField uiField = writer.getOwnerClass().getUiField(fieldName);
      if (uiField != null && uiField.isProvided()) {
        writer.die("UiField %s for FlowPanel cannot set tag when it is also provided.", fieldName);
      }
      writer.setFieldInitializerAsConstructor(fieldName, customTag);
    }
  }

}
