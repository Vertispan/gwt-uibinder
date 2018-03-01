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
