package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.model.OwnerField;

import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.FlowPanel} widgets.
 */
public class FlowPanelParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      final UiBinderWriter writer) throws UnableToCompleteException {
    String customTag = elem.consumeStringAttribute("tag", null);
    if (null != customTag) {
      OwnerField uiField = writer.getOwnerClass().getUiField(fieldName);
      if (uiField != null && uiField.isProvided()) {
        writer.die("UiField %s for FlowPanel cannot set tag when it is also provided.", fieldName);
      }
      writer.setFieldInitializerAsConstructor(fieldName, customTag);
    }
  }

}
