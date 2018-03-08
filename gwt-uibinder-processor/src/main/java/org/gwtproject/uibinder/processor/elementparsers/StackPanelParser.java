package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.StackPanel} widgets.
 */
public class StackPanelParser implements ElementParser {

  private static final String ATTRIBUTE_TEXT = "StackPanel-text";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      if (!writer.isWidgetElement(child)) {
        writer.die(child, "Widget required");
      }

      // Stack panel label comes from the StackPanel-text attribute of the child
      String stackItemLabel = null;
      String variableAttributeName = elem.getPrefix() + ":" + ATTRIBUTE_TEXT;
      if (child.hasAttribute(variableAttributeName)) {
        stackItemLabel = child.consumeRawAttribute(variableAttributeName);
      }

      FieldWriter childField = writer.parseElementToField(child);
      if (stackItemLabel == null) {
        writer.addStatement("%1$s.add(%2$s);", fieldName,
            childField.getNextReference());
      } else {
        writer.addStatement("%1$s.add(%2$s, \"%3$s\");", fieldName,
            childField.getNextReference(), stackItemLabel);
      }
    }
  }
}
