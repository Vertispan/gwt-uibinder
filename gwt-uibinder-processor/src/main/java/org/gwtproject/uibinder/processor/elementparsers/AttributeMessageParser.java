package org.gwtproject.uibinder.processor.elementparsers;


import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * This parser is not tied to a particular class of element, but rather is run as the first parser
 * in any parser stack.
 *
 * <p>It looks for attribute values that are set as calls to the template's generated Messages
 * interface, by calling {@link org.gwtproject.uibinder.processor.messages.MessagesWriter#consumeAndStoreMessageAttributesFor(XMLElement)
 * MessagesWriter.consumeAndStoreMessageAttributesFor}
 */
public class AttributeMessageParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type, UiBinderWriter writer)
      throws UnableToCompleteException {
    writer.getMessages().consumeAndStoreMessageAttributesFor(elem);
  }
}
