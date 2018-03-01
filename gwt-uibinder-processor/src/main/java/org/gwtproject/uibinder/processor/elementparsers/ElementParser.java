package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Element parsers are classes that parse xml elements, using the context to generate widget
 * initialization code.
 */
public interface ElementParser {

  /**
   * Parse the given element, generating the code to initialize it from the element's attributes and
   * children.
   *
   * @param elem the element to be parsed
   * @param fieldName the name of the widget field to be initialized
   * @param type TODO
   * @param writer the writer
   * @throws UnableToCompleteException on error
   */
  void parse(XMLElement elem, String fieldName, TypeMirror type, UiBinderWriter writer)
      throws UnableToCompleteException;
}
