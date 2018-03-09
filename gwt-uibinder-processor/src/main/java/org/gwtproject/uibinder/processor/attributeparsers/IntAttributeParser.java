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

import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses an integer value.
 */
class IntAttributeParser extends StrictAttributeParser {

  IntAttributeParser(FieldReferenceConverter converter, TypeMirror intType, MortalLogger logger) {
    super(converter, logger, intType);
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    try {
      Integer.parseInt(value);
      // Yup, it's an int, use it as such.
      return value;
    } catch (NumberFormatException e) {
      // Not an int, let super see if it's a field ref
    }
    String fieldMaybe = super.parse(source, value);
    if ("".equals(fieldMaybe)) {
      return "";
    }
    return "(int)" + fieldMaybe;
  }
}
