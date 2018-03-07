package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLAttribute;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.HashMap;
import java.util.Map;

/**
 * Assigns computed values to element attributes, e.g. styleName={resources.style.pretty}, which
 * will become something like myWidget.setStyleName(resources.style().pretty()) in the generated
 * code.
 */
class ComputedAttributeInterpreter implements XMLElement.Interpreter<String> {

  interface Delegate {

    String getAttributeToken(XMLAttribute attribute)
        throws UnableToCompleteException;
  }

  class DefaultDelegate implements Delegate {

    public String getAttributeToken(XMLAttribute attribute)
        throws UnableToCompleteException {
      return writer
          .tokenForStringExpression(attribute.getElement(), attribute.consumeStringValue());
    }
  }

  private final UiBinderWriter writer;
  private final Delegate delegate;

  public ComputedAttributeInterpreter(UiBinderWriter writer) {
    this.writer = writer;
    this.delegate = new DefaultDelegate();
  }

  public ComputedAttributeInterpreter(UiBinderWriter writer, Delegate delegate) {
    this.delegate = delegate;
    this.writer = writer;
  }

  public String interpretElement(XMLElement elem)
      throws UnableToCompleteException {
    Map<String, String> attNameToToken = new HashMap<String, String>();

    for (int i = elem.getAttributeCount() - 1; i >= 0; i--) {
      XMLAttribute att = elem.getAttribute(i);

      if (att.hasComputedValue()) {
        String attToken = delegate.getAttributeToken(att);
        attNameToToken.put(att.getName(), attToken);
      } else {
        /*
         * No computed value, but make sure that any {{ madness gets escaped.
         * TODO(rjrjr) Move this to XMLElement RSN
         */
        String n = att.getName();
        String v = att.consumeRawValue().replace("\\{", "{");
        elem.setAttribute(n, v);
      }
    }

    for (Map.Entry<String, String> attr : attNameToToken.entrySet()) {
      elem.setAttribute(attr.getKey(), attr.getValue());
    }

    // Return null because we don't want to replace the dom element
    return null;
  }
}
