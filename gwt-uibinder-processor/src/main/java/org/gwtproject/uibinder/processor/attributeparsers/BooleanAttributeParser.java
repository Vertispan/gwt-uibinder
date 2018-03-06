package org.gwtproject.uibinder.processor.attributeparsers;

import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses a single boolean attribute.
 */
class BooleanAttributeParser extends StrictAttributeParser {

  BooleanAttributeParser(FieldReferenceConverter converter,
      TypeMirror booleanType, MortalLogger logger) {
    super(converter, logger, booleanType);
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    if (value.equals("true") || value.equals("false")) {
      return value;
    }

    return super.parse(source, value);
  }
}
