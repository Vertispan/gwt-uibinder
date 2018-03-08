package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.TabPanel} widgets.
 */
public class TabPanelParser implements ElementParser {

  private static final String TAG_TAB = "Tab";
  private static final String TAG_TABHTML = "TabHTML";

  public void parse(XMLElement panelElem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Parse children.
    for (XMLElement tabElem : panelElem.consumeChildElements()) {
      // TabPanel can only contain Tab elements.
      if (!isElementType(panelElem, tabElem, TAG_TAB)) {
        writer.die(tabElem, "Only <%s:%s> children are allowed.",
            panelElem.getPrefix(), TAG_TAB);
      }

      // Get the caption, or null if there is none
      String tabCaption = tabElem.consumeStringAttribute("text");

      // Get the single required child widget.
      String tabHTML = null;
      FieldWriter childField = null;
      for (XMLElement tabChild : tabElem.consumeChildElements()) {
        if (tabChild.getLocalName().equals(TAG_TABHTML)) {
          if (tabCaption != null || tabHTML != null) {
            writer.die(tabElem,
                "May have only one \"text\" attribute or <%1$s:%2$s>",
                tabElem.getPrefix(), TAG_TABHTML);
          }
          HtmlInterpreter interpreter = HtmlInterpreter.newInterpreterForUiObject(
              writer, fieldName);
          tabHTML = tabChild.consumeInnerHtml(interpreter);
        } else {
          if (childField != null) {
            writer.die(tabChild, "May only have a single child widget");
          }
          if (!writer.isWidgetElement(tabChild)) {
            writer.die(tabChild, "Must be a widget");
          }
          childField = writer.parseElementToField(tabChild);
        }
      }

      if (childField == null) {
        writer.die(tabElem, "Must have a child widget");
      }
      if (tabHTML != null) {
        writer.addStatement("%1$s.add(%2$s, %3$s, true);", fieldName,
            childField.getNextReference(),
            writer.declareTemplateCall(tabHTML, fieldName));
      } else if (tabCaption != null) {
        writer.addStatement("%1$s.add(%2$s, %3$s);", fieldName,
            childField.getNextReference(), tabCaption);
      } else {
        writer.die(tabElem,
            "Requires either a \"text\" attribute or <%1$s:%2$s>",
            tabElem.getPrefix(), TAG_TABHTML);
      }
    }
  }

  private boolean isElementType(XMLElement parent, XMLElement child, String type) {
    return parent.getNamespaceUri().equals(child.getNamespaceUri())
        && type.equals(child.getLocalName());
  }
}
