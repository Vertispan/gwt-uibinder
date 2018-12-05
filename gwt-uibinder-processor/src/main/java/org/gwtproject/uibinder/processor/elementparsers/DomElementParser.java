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
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses a dom element and all of its children. Note that this parser does not make recursive calls
 * to parse child elements, unlike what goes on with widget parsers. Instead, we consume the inner
 * html of the given element into a single string literal, used to instantiate the dom tree at run
 * time.
 */
public class DomElementParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    HtmlInterpreter interpreter = new HtmlInterpreter(writer, fieldName,
        new HtmlMessageInterpreter(writer, fieldName));

    interpreter.interpretElement(elem);

    writer.beginAttachedSection(fieldName);
    String html = elem.consumeOpeningTag() + elem.consumeInnerHtml(interpreter)
        + elem.getClosingTag();
    writer.endAttachedSection();

    writer.setFieldInitializer(fieldName, String.format(
        "(%1$s) %3$s.fromHtml(%2$s)",
        AptUtil.asQualifiedNameable(type).getQualifiedName(),
        writer.declareTemplateCall(html, fieldName),
        UiBinderApiPackage.current().getUiBinderUtilFqn()));
  }
}
