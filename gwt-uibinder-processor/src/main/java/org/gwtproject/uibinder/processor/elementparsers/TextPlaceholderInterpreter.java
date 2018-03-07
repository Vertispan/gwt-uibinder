package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.NullInterpreter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.MessageWriter;
import org.gwtproject.uibinder.processor.messages.PlaceholderInterpreter;


/**
 * Interprets the interior of text message. Any ph elements in the message will be consumed by a
 * call to their {@link XMLElement#consumeInnerText} method. A ph inside a text message may contain
 * no other elements.
 */
public final class TextPlaceholderInterpreter extends PlaceholderInterpreter {

  public TextPlaceholderInterpreter(UiBinderWriter writer,
      MessageWriter message) {
    super(writer, message);
  }

  @Override
  protected String consumePlaceholderInnards(XMLElement elem)
      throws UnableToCompleteException {
    return elem.consumeInnerTextEscapedAsHtmlStringLiteral(new NullInterpreter<>());
  }
}
