package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

/**
 * Generates fields to hold named dom elements (e.g. &lt;div ui:field="importantDiv"&gt;)
 */
class FieldInterpreter implements XMLElement.Interpreter<String> {

  private final String element;
  private final UiBinderWriter writer;

  public FieldInterpreter(UiBinderWriter writer, String ancestorExpression) {
    this.writer = writer;
    this.element = ancestorExpression;
  }

  public String interpretElement(XMLElement elem)
      throws UnableToCompleteException {
    String fieldName = writer.declareFieldIfNeeded(elem);
    if (fieldName != null) {
      String token = writer.declareDomField(elem, fieldName, element);

      if (elem.hasAttribute("id")) {
        writer.die(elem, String.format(
            "Cannot declare id=\"%s\" and %s=\"%s\" on the same element",
            elem.consumeRawAttribute("id"), writer.getUiFieldAttributeName(),
            fieldName));
      }

      elem.setAttribute("id", token);
    }

    /*
     * Return null because we don't want to replace the dom element with any
     * particular string (though we may have consumed its id or gwt:field)
     */
    return null;
  }
}
