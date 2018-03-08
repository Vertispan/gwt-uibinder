package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.FieldWriter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.XMLElement.Interpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * A parser for menu items.
 */
public class MenuItemParser implements ElementParser {

  public void parse(final XMLElement elem, String fieldName, TypeMirror type,
      final UiBinderWriter writer) throws UnableToCompleteException {

    // Use special initializer for standard MenuItem,
    // custom subclass should have default constructor.
    if (MenuItem.class.getName()
        .equals(AptUtil.asQualifiedNameable(type).getQualifiedName().toString())) {
      writer.setFieldInitializerAsConstructor(fieldName, "\"\"",
          "(com.google.gwt.user.client.Command) null");
    }

    final TypeElement menuBarType = AptUtil.getElementUtils().getTypeElement(
        MenuBar.class.getCanonicalName());

    class MenuBarInterpreter implements Interpreter<Boolean> {

      FieldWriter menuBarField = null;

      public Boolean interpretElement(XMLElement child)
          throws UnableToCompleteException {

        if (isMenuBar(child)) {
          if (menuBarField != null) {
            writer.die(child, "Only one MenuBar may be contained in a MenuItem");
          }
          menuBarField = writer.parseElementToField(child);
          return true;
        }

        return false;
      }

      boolean isMenuBar(XMLElement child) throws UnableToCompleteException {
        return AptUtil.isAssignableFrom(menuBarType.asType(), writer.findFieldType(child));
      }
    }

    MenuBarInterpreter interpreter = new MenuBarInterpreter();
    elem.consumeChildElements(interpreter);

    if (interpreter.menuBarField != null) {
      writer.genPropertySet(fieldName, "subMenu",
          interpreter.menuBarField.getNextReference());
    }
  }
}
