package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.AttributeMessage;
import org.gwtproject.uibinder.processor.messages.MessagesWriter;

/**
 * Examines each element for child &lt;ui:attr/&gt; elements, and replaces the corresponding
 * attributes of the examinee with references to the translatable messages created. <p> That is,
 * when examining element foo in
 * <pre>
 *   &lt;foo bar="baz"&gt;
 *     &lt;ui:attr name="baz"&gt;
 *   &lt;/foo&gt;</pre>
 * cosume the ui:attr element, and declare a method on the Messages interface with {@literal
 * @}Default("baz")
 */
class AttributeMessageInterpreter implements XMLElement.Interpreter<String> {

  private final UiBinderWriter writer;

  public AttributeMessageInterpreter(UiBinderWriter writer) {
    this.writer = writer;
  }

  public String interpretElement(XMLElement elem)
      throws UnableToCompleteException {
    MessagesWriter messages = writer.getMessages();
    for (AttributeMessage am : messages.consumeAttributeMessages(elem)) {
      String message = am.getMessageUnescaped();
      if (!writer.useSafeHtmlTemplates()) {
        /*
         * We have to do our own simple escaping to if the SafeHtml integration
         * is off
         */
        message += ".replaceAll(\"&\", \"&amp;\").replaceAll(\"'\", \"&#39;\")";
      }
      elem.setAttribute(am.getAttribute(),
          writer.tokenForStringExpression(elem, message));
    }

    /*
     * Return null because we don't want to replace the dom element with any
     * particular string (though we may have consumed its id or ui:field)
     */
    return null;
  }
}
