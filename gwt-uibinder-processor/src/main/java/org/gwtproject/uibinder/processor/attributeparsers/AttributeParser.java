package org.gwtproject.uibinder.processor.attributeparsers;


import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

/**
 * Attribute parsers are classes that parse xml attribute values, turning them into valid Java
 * expressions.
 */
public interface AttributeParser {

  /**
   * Parse the given attribute value.
   *
   * @param source the source code the value came from, for error reporting purposes
   * @param value the attribute value to be parsed
   * @return a valid Java expression
   * @throws UnableToCompleteException on parse error
   */
  String parse(XMLElement source, String value)
      throws UnableToCompleteException;
}
