package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parser of all UIObject types. Provides parsing of debugId, addStyleNames, addStyleDependentNames.
 * Also handles other setStyle* calls to ensure they happen before the addStyle* calls.
 */
public class UIObjectParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    String debugId = elem.consumeStringAttribute("debugId", null);
    if (null != debugId) {
      writer.addStatement("%s.ensureDebugId(%s);", fieldName, debugId);
    }

    String styleName = elem.consumeStringAttribute("styleName", null);
    String stylePrimaryName = elem.consumeStringAttribute("stylePrimaryName",
        null);

    if (null != styleName && null != stylePrimaryName) {
      writer.die(elem, "Cannot set both \"styleName\" "
          + "and \"stylePrimaryName\"");
    }

    if (null != styleName) {
      writer.addStatement("%s.setStyleName(%s);", fieldName, styleName);
    }
    if (null != stylePrimaryName) {
      writer.addStatement("%s.setStylePrimaryName(%s);", fieldName,
          stylePrimaryName);
    }

    String[] extraStyleNames = elem.consumeStringArrayAttribute("addStyleNames");
    for (String s : extraStyleNames) {
      writer.addStatement("%s.addStyleName(%s);", fieldName, s);
    }

    extraStyleNames = elem.consumeStringArrayAttribute("addStyleDependentNames");
    for (String s : extraStyleNames) {
      writer.addStatement("%s.addStyleDependentName(%s);", fieldName, s);
    }
  }
}
