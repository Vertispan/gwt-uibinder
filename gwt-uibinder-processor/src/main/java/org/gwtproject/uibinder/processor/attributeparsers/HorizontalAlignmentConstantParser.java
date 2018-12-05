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
 * Parses a HasHorizontalAlignment.HorizontalAlignmentConstant.
 */
class HorizontalAlignmentConstantParser extends StrictAttributeParser {

  private static final String PREFIX = UiBinderApiPackage.current().getHasHorizontalAlignmentFqn()
      + ".ALIGN_";
  private static final HashMap<String, String> values = new HashMap<>();

  static {
    values.put("LEFT", PREFIX + "LEFT");
    values.put("CENTER", PREFIX + "CENTER");
    values.put("RIGHT", PREFIX + "RIGHT");
    values.put("JUSTIFY", PREFIX + "JUSTIFY");
    values.put("DEFAULT", PREFIX + "DEFAULT");
    values.put("LOCALE_START", PREFIX + "LOCALE_START");
    values.put("LOCALE_END", PREFIX + "LOCALE_END");
    values.put("ALIGN_LEFT", PREFIX + "LEFT");
    values.put("ALIGN_CENTER", PREFIX + "CENTER");
    values.put("ALIGN_RIGHT", PREFIX + "RIGHT");
    values.put("ALIGN_JUSTIFY", PREFIX + "JUSTIFY");
    values.put("ALIGN_DEFAULT", PREFIX + "DEFAULT");
    values.put("ALIGN_LOCALE_START", PREFIX + "LOCALE_START");
    values.put("ALIGN_LOCALE_END", PREFIX + "LOCALE_END");
  }

  HorizontalAlignmentConstantParser(FieldReferenceConverter converter, TypeMirror type,
      MortalLogger logger) {
    super(converter, logger, type);
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
