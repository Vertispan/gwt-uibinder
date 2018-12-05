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
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.HashMap;
import java.util.Locale;

import javax.lang.model.type.TypeMirror;

/**
 * Parses a HasVerticalAlignment.VerticalAlignmentConstant.
 */
class VerticalAlignmentConstantParser extends StrictAttributeParser {

  private final HashMap<String, String> values = new HashMap<>();

  VerticalAlignmentConstantParser(FieldReferenceConverter converter, TypeMirror type,
      MortalLogger logger) {
    super(converter, logger, type);

    final String prefix = UiBinderApiPackage.current().getHasVerticalAlignmentFqn() + ".ALIGN_";
    values.put("TOP", prefix + "TOP");
    values.put("MIDDLE", prefix + "MIDDLE");
    values.put("BOTTOM", prefix + "BOTTOM");
    values.put("ALIGN_TOP", prefix + "TOP");
    values.put("ALIGN_MIDDLE", prefix + "MIDDLE");
    values.put("ALIGN_BOTTOM", prefix + "BOTTOM");
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    String translated = values.get(value.toUpperCase(Locale.ROOT));
    if (translated != null) {
      return translated;
    }
    return super.parse(source, value);
  }
}
