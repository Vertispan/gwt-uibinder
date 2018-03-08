package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses {@link com.google.gwt.user.client.ui.MenuBar} widgets.
 */
public class MenuBarParser implements ElementParser {

  static final String BAD_CHILD = "Only MenuItem or MenuItemSeparator subclasses are valid children";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    // Generate instantiation (Vertical MenuBars require a ctor param).
    if (MenuBar.class.getName()
        .equals(AptUtil.asQualifiedNameable(type).getQualifiedName().toString())) {
      if (elem.hasAttribute("vertical")) {
        String vertical = elem.consumeBooleanAttribute("vertical");
        writer.setFieldInitializerAsConstructor(fieldName, vertical);
      }
    }

    // Prepare base types.
    TypeElement itemType = AptUtil.getElementUtils().getTypeElement(MenuItem.class.getName());
    TypeElement separatorType = AptUtil.getElementUtils().getTypeElement(
        MenuItemSeparator.class.getName());

    // Parse children.
    for (XMLElement child : elem.consumeChildElements()) {
      TypeMirror childType = writer.findFieldType(child);

      // MenuItem+
      if (AptUtil.isAssignableFrom(itemType.asType(), childType)) {
        FieldWriter childField = writer.parseElementToField(child);
        writer.addStatement("%1$s.addItem(%2$s);", fieldName, childField.getNextReference());
        continue;
      }

      // MenuItemSeparator+
      if (AptUtil.isAssignableFrom(separatorType.asType(), childType)) {
        FieldWriter childField = writer.parseElementToField(child);
        writer.addStatement("%1$s.addSeparator(%2$s);", fieldName, childField.getNextReference());
        continue;
      }

      // Fail
      writer.die(child, BAD_CHILD);
    }
  }
}
