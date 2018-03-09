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

/**
 * Parses a pair of integer values.
 */
class IntPairAttributeParser implements AttributeParser {

  private final IntAttributeParser intParser;
  private final MortalLogger logger;
  
  IntPairAttributeParser(IntAttributeParser intParser, MortalLogger logger) {
    this.intParser = intParser;
    this.logger = logger;
  }
  
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    String[] values = value.split(",");
    if (values.length != 2) {
      logger.die(source, "Unable to parse \"%s\" as a pair of integers", value);
    }
    
    String left = intParser.parse(source, values[0].trim());
    String right = intParser.parse(source, values[1].trim());
    return String.format("%s, %s", left, right);
  }
}
