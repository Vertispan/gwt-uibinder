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
