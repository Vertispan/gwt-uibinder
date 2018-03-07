package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLAttribute;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

/**
 * Interprets generic message tags like: <b>&lt;ui:text from="{myMsg.message}" /&gt;</b>. It's
 * called in both text and HTML contexts.
 */
public class UiTextInterpreter implements XMLElement.Interpreter<String> {

  /**
   * Used in {@link #interpretElement} to invoke the {@link ComputedAttributeInterpreter}.
   */
  protected class Delegate implements ComputedAttributeInterpreter.Delegate {

    public String getAttributeToken(XMLAttribute attribute) throws UnableToCompleteException {
      return writer
          .tokenForStringExpression(attribute.getElement(), attribute.consumeStringValue());
    }
  }

  protected final UiBinderWriter writer;
  protected final ComputedAttributeInterpreter computedAttributeInterpreter;
  private final MortalLogger logger;

  public UiTextInterpreter(UiBinderWriter writer) {
    this.writer = writer;
    this.logger = writer.getLogger();
    this.computedAttributeInterpreter = createComputedAttributeInterpreter();
  }

  public String interpretElement(XMLElement elem)
      throws UnableToCompleteException {
    // Must be in the format: <ui:string from="{myMsg.message}" />
    if (writer.isBinderElement(elem) && getLocalName().equals(elem.getLocalName())) {
      if (!elem.hasAttribute("from")) {
        logger.die(elem, "Attribute 'from' not found.");
      }
      if (!elem.getAttribute("from").hasComputedValue()) {
        logger.die(elem, "Attribute 'from' does not have a computed value");
      }
      // Make sure all computed attributes are interpreted first
      computedAttributeInterpreter.interpretElement(elem);

      String fieldRef = elem.consumeStringAttribute("from");
      // Make sure that "from" was the only attribute
      elem.assertNoAttributes();
      return "\" + " + fieldRef + " + \"";
    }
    return null;
  }

  protected ComputedAttributeInterpreter createComputedAttributeInterpreter() {
    return new ComputedAttributeInterpreter(writer);
  }

  protected String getLocalName() {
    return "text";
  }
}
