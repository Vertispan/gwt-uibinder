package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * A parser for ListBox items.
 */
public class ListBoxParser implements ElementParser {

  private static final String ITEM_TAG = "item";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      String tagName = child.getLocalName();
      if (!tagName.equals(ITEM_TAG)) {
        writer.die(elem, "Invalid ListBox child element: " + tagName);
      }
      String value = child.consumeStringAttribute("value");
      String innerText = child.consumeInnerTextEscapedAsStringLiteral(
          new TextInterpreter(writer));
      if (value != null) {
        writer.addStatement("%s.addItem(\"%s\", %s);", fieldName, innerText, value);
      } else {
        writer.addStatement("%s.addItem(\"%s\");", fieldName, innerText);
      }
    }
  }

}
