package org.gwtproject.uibinder.processor.attributeparsers;

import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses a single double attribute.
 */
class DoubleAttributeParser extends StrictAttributeParser {

  DoubleAttributeParser(FieldReferenceConverter converter,
      TypeMirror doubleType, MortalLogger logger) {
    super(converter, logger, doubleType);
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    try {
      Double.parseDouble(value);
      // Happy double
      return value;
    } catch (NumberFormatException e) {
      // Not a double, maybe super sees a field ref
    }
    String fieldMaybe = super.parse(source, value);
    if ("".equals(fieldMaybe)) {
      return "";
    }
    return "(double)" + fieldMaybe;
  }
}
