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
package org.gwtproject.uibinder.processor.attributeparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Parses an enum attribute.
 */
class EnumAttributeParser extends StrictAttributeParser {

  private final Map<String, Element> values = new HashMap<>();

  EnumAttributeParser(FieldReferenceConverter converter, TypeMirror enumType, MortalLogger logger) {
    super(converter, logger, enumType);

    List<Element> enumValues = AptUtil.getEnumValues(AptUtil.asTypeElement(enumType));
    for (Element c : enumValues) {
      values.put(c.getSimpleName().toString(), c);
    }
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    Element c = values.get(value);
    if (c != null) {
      return String.format("%s.%s",
          AptUtil.asTypeElement(c.getEnclosingElement()).getQualifiedName().toString(), value);
    }
    return super.parse(source, value);
  }
}
