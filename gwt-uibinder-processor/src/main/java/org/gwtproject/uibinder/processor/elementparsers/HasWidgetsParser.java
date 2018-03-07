package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses any widgets that implement {@link com.google.gwt.user.client.ui.HasWidgets}.
 *
 * This handles all panels that support a single-argument add() method.
 */
public class HasWidgetsParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      if (!writer.isWidgetElement(child)) {
        writer.die(child, "Expecting only widgets in %s", elem);
      }
      FieldWriter childField = writer.parseElementToField(child);
      writer.addStatement("%1$s.add(%2$s);", fieldName,
          childField.getNextReference());
    }
  }
}
