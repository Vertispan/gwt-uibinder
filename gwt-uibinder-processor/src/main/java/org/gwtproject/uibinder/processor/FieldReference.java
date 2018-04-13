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

import java.util.Arrays;
import java.util.LinkedHashSet;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Represents a <code>{field.reference}</code>. Collects all the types a particular reference has
 * been asked to return, and can validate that it actually does so.
 */
public class FieldReference {

  private static class LeftHand {

    /**
     * The type of values acceptible to this LHS, in order of preference.
     */
    private final TypeMirror[] types;
    /**
     * The element on the LHS, for error reporting.
     */
    private final XMLElement source;

    LeftHand(XMLElement source, TypeMirror... types) {
      this.types = Arrays.copyOf(types, types.length);
      this.source = source;
    }
  }

  public static String renderTypesList(TypeMirror[] types) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < types.length; i++) {
      if (i > 0 && i == types.length - 1) {
        b.append(" or ");
      } else if (i > 0) {
        b.append(", ");
      }
      b.append(AptUtil.getParameterizedQualifiedSourceName(types[i]));
    }

    return b.toString();
  }

  private final FieldManager fieldManager;
  private final XMLElement source;
  private final String debugString;
  private final String[] elements;

  private final LinkedHashSet<LeftHand> leftHandTypes = new LinkedHashSet<LeftHand>();

  FieldReference(String reference, XMLElement source, FieldManager fieldManager) {
    this.source = source;
    this.debugString = "{" + reference + "}";
    this.fieldManager = fieldManager;
    elements = reference.split("\\.");
  }

  public void addLeftHandType(XMLElement source, TypeMirror... types) {
    leftHandTypes.add(new LeftHand(source, types));
  }

  public String getFieldName() {
    return elements[0];
  }

  public TypeMirror getReturnType() {
    return getReturnType(null);
  }

  /**
   * Returns the type returned by this field ref.
   *
   * @param logger optional logger to report errors on, may be null
   * @return the field ref, or null
   */
  public TypeMirror getReturnType(MonitoredLogger logger) {
    FieldWriter field = fieldManager.lookup(elements[0]);
    if (field == null) {
      if (logger != null) {
        /*
         * It's null when called from HtmlTemplateMethodWriter, which fires
         * after validation has already succeeded.
         */
        logger.error(source, "in %s, no field named %s", this, elements[0]);
      }
      return null;
    }

    return field.getReturnType(elements, logger);
  }

  public XMLElement getSource() {
    return source;
  }

  @Override
  public String toString() {
    return debugString;
  }

  public void validate(MonitoredLogger logger) {
    TypeMirror myReturnType = getReturnType(logger);
    if (myReturnType == null) {
      return;
    }

    for (LeftHand left : leftHandTypes) {
      ensureAssignable(left, myReturnType, logger);
    }
  }

  /**
   * Returns a failure message if the types don't mesh, or null on success.
   */
  private void ensureAssignable(LeftHand left, TypeMirror rightHandType, MonitoredLogger logger) {
    assert left.types.length > 0;

    for (TypeMirror leftType : left.types) {

      if (leftType == rightHandType) {
        return;
      }

      if (matchingNumberTypes(leftType, rightHandType)) {
        return;
      }

      boolean[] explicitFailure = {false};
      if (handleMismatchedNonNumericPrimitives(leftType, rightHandType, explicitFailure)) {
        if (explicitFailure[0]) {
          continue;
        }
      }

      TypeElement leftClass = AptUtil.asTypeElement(leftType);
      if (leftClass != null) {
        TypeElement rightClass = AptUtil.asTypeElement(rightHandType);
        if ((rightClass == null) || !AptUtil.isAssignableFrom(leftClass, rightClass)) {
          continue;
        }
      }

      /*
       * If we have reached the bottom of the loop, we don't see a problem with
       * assigning to this left hand type. Return without logging any error.
       * This is pretty conservative -- we have a white list of bad conditions,
       * not an exhaustive check of valid assignments. We're not confident that
       * we know every error case, and are more worried about being artificially
       * restrictive.
       */
      return;
    }

    /*
     * Every possible left hand type had some kind of failure. Log this sad
     * fact, which will halt processing.
     */
    logger.error(left.source, "%s required, but %s returns %s", renderTypesList(left.types),
        FieldReference.this, AptUtil.asTypeElement(rightHandType).getQualifiedName().toString());
  }

  private boolean handleMismatchedNonNumericPrimitives(TypeMirror leftType,
      TypeMirror rightHandType, boolean[] explicitFailure) {

    boolean leftPrimitive = leftType.getKind().isPrimitive();
    boolean rightPrimitive = rightHandType.getKind().isPrimitive();

    if (!leftPrimitive && !rightPrimitive) {
      return false;
    }

    if (leftPrimitive) {

      TypeElement autobox = AptUtil.getTypeUtils()
          .boxedClass(AptUtil.getTypeUtils().getPrimitiveType(leftType.getKind()));

      if (rightHandType != autobox) {
        explicitFailure[0] = true;
      }
    } else { // rightPrimitive != null
      TypeElement autobox = AptUtil.getTypeUtils()
          .boxedClass(AptUtil.getTypeUtils().getPrimitiveType(rightHandType.getKind()));
      if (leftType != autobox) {
        explicitFailure[0] = true;
      }
    }

    return true;
  }

  private boolean isNumber(TypeMirror type) {
    TypeElement numberType = AptUtil.getElementUtils()
        .getTypeElement(Number.class.getCanonicalName());

    TypeElement asElement = AptUtil.asTypeElement(type);
    if (asElement != null) {
      return AptUtil.isAssignableFrom(numberType, asElement);
    }

    boolean isPrimitive = type.getKind().isPrimitive();
    if (isPrimitive) {
      TypeElement autoboxed = AptUtil.getTypeUtils()
          .boxedClass(AptUtil.getTypeUtils().getPrimitiveType(type.getKind()));

      return AptUtil.isAssignableFrom(numberType, autoboxed);
    }

    return false;
  }

  private boolean matchingNumberTypes(TypeMirror leftHandType, TypeMirror rightHandType) {
    /*
     * int i = (int) 1.0 is okay Integer i = (int) 1.0 is okay int i = (int)
     * Double.valueOf(1.0) is not
     */
    if (isNumber(leftHandType) && isNumber(rightHandType) //
        && !rightHandType.getKind().isPrimitive()) {
      return true; // They will be cast into submission
    }

    return false;
  }
}
