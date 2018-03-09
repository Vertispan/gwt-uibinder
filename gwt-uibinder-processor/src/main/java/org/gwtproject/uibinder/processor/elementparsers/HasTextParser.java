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

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses widgets that implement {@link com.google.gwt.user.client.ui.HasText}.
 */
public class HasTextParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Widgets that implement HasText will use their elements' inner text.
    String text = elem.consumeInnerTextEscapedAsHtmlStringLiteral(new TextInterpreter(writer));
    if (text.trim().length() > 0) {
      writer.genStringPropertySet(fieldName, "text", text);
    }
  }
}
