package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses widgets that implement {@link com.google.gwt.user.client.ui.HasHTML}.
 */
public class HasHTMLParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {

    HtmlInterpreter interpreter =
        HtmlInterpreter.newInterpreterForUiObject(writer, fieldName);
    writer.beginAttachedSection(fieldName + ".getElement()");
    String html = elem.consumeInnerHtml(interpreter);
    writer.endAttachedSection();
    // TODO(jgw): throw an error if there's a conflicting 'html' attribute.
    if (html.trim().length() > 0) {
      writer.genPropertySet(fieldName, "HTML", writer.declareTemplateCall(html,
          fieldName));
    }
  }
}
