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

import com.google.gwt.core.ext.typeinfo.JAnnotationType;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JConstructor;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JGenericType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JRawType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JTypeParameter;
import com.google.gwt.core.ext.typeinfo.JWildcardType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.lang.model.element.TypeElement;

/**
 * Simple wrapper for JClassType for APT.
 *
 * Note that much of the class needs implementations.  Only the parts needed at the time were done.
 */
public class JClassTypeWrapper implements JClassType {

  private final TypeElement typeElement;

  public JClassTypeWrapper(TypeElement typeElement) {
    this.typeElement = typeElement;
  }

  @Override
  public JParameterizedType asParameterizationOf(JGenericType type) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public <T extends Annotation> T findAnnotationInTypeHierarchy(Class<T> annotationType) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JConstructor findConstructor(JType[] paramTypes) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JField findField(String name) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JMethod findMethod(String name, JType[] paramTypes) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType findNestedType(String typeName) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JConstructor getConstructor(JType[] paramTypes) throws NotFoundException {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JConstructor[] getConstructors() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType getEnclosingType() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType getErasedType() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JField getField(String name) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JField[] getFields() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public Set<? extends JClassType> getFlattenedSupertypeHierarchy() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType[] getImplementedInterfaces() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JMethod[] getInheritableMethods() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JMethod getMethod(String name, JType[] paramTypes) throws NotFoundException {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JMethod[] getMethods() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType getNestedType(String typeName) throws NotFoundException {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType[] getNestedTypes() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public TypeOracle getOracle() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JMethod[] getOverloads(String name) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JMethod[] getOverridableMethods() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JPackage getPackage() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType[] getSubtypes() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType getSuperclass() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isAbstract() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isAssignableFrom(JClassType possibleSubtype) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isAssignableTo(JClassType possibleSupertype) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isDefaultInstantiable() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isEnhanced() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isFinal() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isMemberType() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isPrivate() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isProtected() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isPublic() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isPackageProtected() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isStatic() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public void setEnhanced() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public Annotation[] getAnnotations() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getJNISignature() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JType getLeafType() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getParameterizedQualifiedSourceName() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getQualifiedBinaryName() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getQualifiedSourceName() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public String getSimpleSourceName() {
    return typeElement.getSimpleName().toString();
  }

  @Override
  public JAnnotationType isAnnotation() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JArrayType isArray() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType isClass() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType isClassOrInterface() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JEnumType isEnum() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JGenericType isGenericType() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JClassType isInterface() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JParameterizedType isParameterized() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JPrimitiveType isPrimitive() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JRawType isRawType() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JTypeParameter isTypeParameter() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public JWildcardType isWildcard() {
    throw new UnsupportedOperationException("not implemented");
  }
}
