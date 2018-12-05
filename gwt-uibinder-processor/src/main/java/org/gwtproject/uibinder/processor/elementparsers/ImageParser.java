/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Custom parsing of Image widgets. Sets ImageResource via constructor, because Image.setResource
 * clobbers most setter values.
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
        .getTypeElement(UiBinderApiPackage.current().getImageResourceFqn());
    ExecutableElement constructor = AptUtil
        .findConstructor(type, new TypeMirror[]{imageResourceType.asType()});
    return constructor != null;
  }
}
