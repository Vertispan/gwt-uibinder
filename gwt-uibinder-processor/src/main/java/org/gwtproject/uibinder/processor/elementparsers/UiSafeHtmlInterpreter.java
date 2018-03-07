package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLAttribute;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

/**
 * Interprets generic message tags like: <b>&lt;ui:safehtml from="{myMsg.message}" /&gt;</b>. It's
 * called in HTML contexts.
 */
public class UiSafeHtmlInterpreter extends UiTextInterpreter {

  /**
   * Used in {@link #interpretElement} to invoke the {@link ComputedAttributeInterpreter}.
   */
  private class Delegate extends UiTextInterpreter.Delegate {

    public String getAttributeToken(XMLAttribute attribute) throws UnableToCompleteException {
      return writer.tokenForSafeHtmlExpression(attribute.getElement(),
          attribute.consumeSafeHtmlValue());
    }
  }

  public UiSafeHtmlInterpreter(UiBinderWriter writer) {
    super(writer);
  }

  protected ComputedAttributeInterpreter createComputedAttributeInterpreter() {
    return new ComputedAttributeInterpreter(writer, new Delegate());
  }

  @Override
  protected String getLocalName() {
    return "safehtml";
  }
}
