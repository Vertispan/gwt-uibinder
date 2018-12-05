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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses widgets that inherit from HasAlignment. This class
 * is needed to resolve the parse order of alignment attributes for these classes. <p>
 *
 * See {@link "http://code.google.com/p/google-web-toolkit/issues/detail?id=5518"} for issue
 * details.
 */

public class HasAlignmentParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {

    // Get fully qualified class name for horizontal alignment
    TypeElement hAlignConstantType = AptUtil.getElementUtils().getTypeElement(
        UiBinderApiPackage.current().getHorizontalAlignmentConstantFqn());
    // Get horizontal alignment value
    String horizontalAlignment = elem.consumeAttributeWithDefault(
        "horizontalAlignment", null, hAlignConstantType.asType());
    // Set horizontal alignment if not null
    if (horizontalAlignment != null) {
      writer.addStatement("%s.setHorizontalAlignment(%s);", fieldName,
          horizontalAlignment);
    }

    // Get fully qualified class name for vertical alignment
    TypeElement vAlignConstantType = AptUtil.getElementUtils().getTypeElement(
        UiBinderApiPackage.current().getVerticalAlignmentConstantFqn());
    // Get vertical alignment value
    String verticalAlignment = elem.consumeAttributeWithDefault(
        "verticalAlignment", null, vAlignConstantType.asType());
    // Set vertical alignment if not null
    if (verticalAlignment != null) {
      writer.addStatement("%s.setVerticalAlignment(%s);", fieldName,
          verticalAlignment);
    }
  }
}
