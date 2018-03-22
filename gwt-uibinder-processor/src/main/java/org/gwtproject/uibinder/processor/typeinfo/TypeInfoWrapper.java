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
package org.gwtproject.uibinder.processor.typeinfo;

import org.gwtproject.uibinder.processor.AptUtil;

import com.google.gwt.core.ext.typeinfo.JClassType;

import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Wraps APT classes into the associated GWT typeinfo classes.
 *
 * <p>This isn't ideal, but works for bridging older modules.
 */
public class TypeInfoWrapper {

  public static Set<JClassType> wrapJClassType(Set<TypeMirror> typeMirrors) {
    return typeMirrors.stream()
        .map(m -> wrapJClassType(m))
        .collect(Collectors.toSet());
  }

  public static JClassType wrapJClassType(TypeMirror typeMirror) {
    TypeElement element = AptUtil.asTypeElement(typeMirror);
    if (element == null) {
      return null;
    }
    return new JClassTypeWrapper(element);
  }
}
