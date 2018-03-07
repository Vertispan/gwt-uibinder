package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses widgets that implement {@link com.google.gwt.user.client.ui.HasText}.
 */
public class HasTextParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Widgets that implement HasText will use their elements' inner text.
    String text = elem.consumeInnerTextEscapedAsHtmlStringLiteral(new TextInterpreter(writer));
    if (text.trim().length() > 0) {
      writer.genStringPropertySet(fieldName, "text", text);
    }
  }
}
