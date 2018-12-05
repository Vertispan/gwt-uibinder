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
package org.gwtproject.uibinder.processor.model;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.UiBinderContext;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Descriptor for a field of the owner class.
 *
 * <p>Please notice that some fields defined in the XML and in the generated binder class may not
 * be present in the owner class - for instance, they may not be relevant to the code of the owner
 * class. The fields in the binder class are instead represented by an instance of {@link
 * org.gwtproject.uibinder.processor.FieldWriter}.
 */
public class OwnerField {

  private final String name;
  private final OwnerFieldClass fieldType;
  private final boolean isProvided;

  /**
   * Constructor.
   *
   * @param field the field of the owner class
   */
  public OwnerField(VariableElement field, MortalLogger logger, UiBinderContext context)
      throws UnableToCompleteException {
    this.name = field.getSimpleName().toString();

    // Get the field type and ensure it's a class or interface
    TypeMirror fieldTypeMirror = field.asType();

    if (fieldTypeMirror == null) {
      logger.die("Type for field " + name + " is not a class: "
          + field.asType());
    }

    this.fieldType = OwnerFieldClass.getFieldClass(fieldTypeMirror, logger, context);

    // Get the UiField annotation and process it
    AnnotationMirror annotation = AptUtil
        .getAnnotation(field, UiBinderApiPackage.current().getUiFieldFqn());

    if (annotation == null) {
      logger.die("Field " + name + " is not annotated with @UiField");
    }

    AnnotationValue provided = AptUtil.getAnnotationValues(annotation)
        .get("provided");

    isProvided = provided == null ? false : (boolean) provided.getValue();
  }

  /**
   * Returns the name of the field in the owner class.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the type associated with this field.
   */
  public TypeMirror getRawType() {
    // This shorten getType().getRawType() and make tests easier.
    return getType().getRawType();
  }

  /**
   * Returns a descriptor for the type of the field.
   */
  public OwnerFieldClass getType() {
    return fieldType;
  }

  /**
   * Returns whether this field's value is provided by owner class. If it's not provided, then it's
   * the binder's responsibility to assign it.
   */
  public boolean isProvided() {
    return isProvided;
  }

  @Override
  public String toString() {
    return String.format("%s#%s",
        AptUtil.asQualifiedNameable(fieldType.getRawType()).getQualifiedName().toString(), name);
  }
}
