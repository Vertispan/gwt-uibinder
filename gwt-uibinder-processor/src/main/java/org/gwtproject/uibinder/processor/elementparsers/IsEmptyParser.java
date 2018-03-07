package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * The last parser, asserts that everything has been consumed and so the template has nothing
 * unexpected.
 */
public class IsEmptyParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type, UiBinderWriter writer)
      throws UnableToCompleteException {
    elem.assertNoAttributes();
    elem.assertNoBody();
  }
}
