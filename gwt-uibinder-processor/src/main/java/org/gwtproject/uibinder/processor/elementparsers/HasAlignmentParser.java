package org.gwtproject.uibinder.processor.elementparsers;


import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses widgets that inherit from {@link com.google.gwt.user.client.ui.HasAlignment}. This class
 * is needed to resolve the parse order of alignment attributes for these classes. <p>
 *
 * See {@link "http://code.google.com/p/google-web-toolkit/issues/detail?id=5518"} for issue
 * details.
 */

public class HasAlignmentParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {

    // Get fully qualified class name for horizontal alignment
    TypeElement hAlignConstantType = AptUtil.getElementUtils().getTypeElement(
        HorizontalAlignmentConstant.class.getCanonicalName());
    // Get horizontal alignment value
    String horizontalAlignment = elem.consumeAttributeWithDefault(
        "horizontalAlignment", null, hAlignConstantType.asType());
    // Set horizontal alignment if not null
    if (horizontalAlignment != null) {
      writer.addStatement("%s.setHorizontalAlignment(%s);", fieldName,
          horizontalAlignment);
    }

    // Get fully qualified class name for vertical alignment
    TypeElement vAlignConstantType = AptUtil.getElementUtils().getTypeElement(
        VerticalAlignmentConstant.class.getCanonicalName());
    // Get vertical alignment value
    String verticalAlignment = elem.consumeAttributeWithDefault(
        "verticalAlignment", null, vAlignConstantType.asType());
    // Set vertical alignment if not null
    if (verticalAlignment != null) {
      writer.addStatement("%s.setVerticalAlignment(%s);", fieldName,
          verticalAlignment);
    }
  }
}
