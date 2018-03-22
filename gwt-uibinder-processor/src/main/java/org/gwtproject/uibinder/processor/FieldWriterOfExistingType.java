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

import javax.lang.model.type.TypeMirror;

/**
 *
 */
public class FieldWriterOfExistingType extends AbstractFieldWriter {

  final TypeMirror type;
  final MortalLogger logger;

  FieldWriterOfExistingType(FieldManager manager, FieldWriterType fieldType,
      TypeMirror type, String name, MortalLogger logger) {
    super(manager, fieldType, name, logger);
    this.logger = logger;
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null");
    }
    this.type = type;
  }

  public TypeMirror getAssignableType() {
    return type;
  }

  public TypeMirror getInstantiableType() {
    return type;
  }

  public String getQualifiedSourceName() {
    return AptUtil.asQualifiedNameable(type).getQualifiedName().toString();
  }
}
