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
package org.gwtproject.uibinder.processor;

import org.gwtproject.uibinder.processor.model.OwnerFieldClass;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.type.TypeMirror;

/**
 * A shared context cache for UiBinder.
 */
public class UiBinderContext {

  private final Map<TypeMirror, OwnerFieldClass> fieldClassesCache = new HashMap<>();

  public OwnerFieldClass getOwnerFieldClass(TypeMirror type) {
    return fieldClassesCache.get(type);
  }

  public void putOwnerFieldClass(TypeMirror forType, OwnerFieldClass clazz) {
    fieldClassesCache.put(forType, clazz);
  }

}
