package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.MessageWriter;
import org.gwtproject.uibinder.processor.messages.MessagesWriter;
import org.gwtproject.uibinder.processor.messages.PlaceholderInterpreter;

/**
 * Processes <ui:msg> elements inside HTML values, which themselves are allowed to contain HTML.
 * That HTML may hold elements with ui:field attributes and computed attributes, which must be
 * replaced by placeholders in the generated message.
 */
public class HtmlMessageInterpreter implements XMLElement.Interpreter<String> {

  /**
   * Provides instances of {@link PlaceholderInterpreter}, to allow customized handling of the
   * innards of a msg element.
   */
  public interface PlaceholderInterpreterProvider {

    PlaceholderInterpreter get(MessageWriter message);
  }

  private final UiBinderWriter uiWriter;
  private final PlaceholderInterpreterProvider phiProvider;

  /**
   * Build a HtmlMessageInterpreter that uses customized placeholder interpreter instances.
   */
  public HtmlMessageInterpreter(UiBinderWriter uiWriter,
      PlaceholderInterpreterProvider phiProvider) {
    this.uiWriter = uiWriter;
    this.phiProvider = phiProvider;
  }

  /**
   * Build a normally configured HtmlMessageInterpreter, able to handle put placeholders around dom
   * elements with ui:ph attributes and computed attributes.
   */
  public HtmlMessageInterpreter(final UiBinderWriter uiWriter,
      final String ancestorExpression) {
    this(uiWriter, new PlaceholderInterpreterProvider() {
      public PlaceholderInterpreter get(MessageWriter message) {
        return new HtmlPlaceholderInterpreter(uiWriter, message, ancestorExpression);
      }
    });
  }

  public String interpretElement(XMLElement elem)
      throws UnableToCompleteException {
    MessagesWriter messages = uiWriter.getMessages();
    if (messages.isMessage(elem)) {
      if (!elem.hasChildNodes()) {
        uiWriter.die(elem, "Empty message");
      }

      MessageWriter message = messages.newMessage(elem);
      message.setDefaultMessage(elem.consumeInnerHtml(phiProvider.get(message)));
      return uiWriter.tokenForSafeConstant(elem, messages.declareMessage(message));
    }

    return null;
  }
}
