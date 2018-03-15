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

import com.google.gwt.user.client.ui.HasVerticalAlignment;

import java.util.HashMap;
import java.util.Locale;

import javax.lang.model.type.TypeMirror;

/**
 * Parses a {@link HasVerticalAlignment.VerticalAlignmentConstant} .
 */
class VerticalAlignmentConstantParser extends StrictAttributeParser {

  private static final String PREFIX = HasVerticalAlignment.class.getCanonicalName()
      + ".ALIGN_";
  private static final HashMap<String, String> values = new HashMap<>();

  static {
    values.put("TOP", PREFIX + "TOP");
    values.put("MIDDLE", PREFIX + "MIDDLE");
    values.put("BOTTOM", PREFIX + "BOTTOM");
    values.put("ALIGN_TOP", PREFIX + "TOP");
    values.put("ALIGN_MIDDLE", PREFIX + "MIDDLE");
    values.put("ALIGN_BOTTOM", PREFIX + "BOTTOM");
  }

  VerticalAlignmentConstantParser(FieldReferenceConverter converter, TypeMirror type,
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
