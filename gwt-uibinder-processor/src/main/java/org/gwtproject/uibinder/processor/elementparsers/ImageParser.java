package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.resources.client.ImageResource;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Custom parsing of Image widgets. Sets ImageResource via constructor, because {@link
 * com.google.gwt.user.client.ui.Image#setResource Image.setResource} clobbers most setter values.
 */
public class ImageParser implements ElementParser {

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    if (hasImageResourceConstructor(type)) {
      String resource = elem.consumeImageResourceAttribute("resource");
      if (null != resource) {
        writer.setFieldInitializerAsConstructor(fieldName, resource);
      }
    }
  }

  private boolean hasImageResourceConstructor(TypeMirror type) {
    TypeElement imageResourceType = AptUtil.getElementUtils()
        .getTypeElement(ImageResource.class.getName());
    ExecutableElement constructor = AptUtil
        .findConstructor(type, new TypeMirror[]{imageResourceType.asType()});
    return constructor != null;
  }
}
