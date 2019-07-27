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

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

/**
 *
 */
public class AptUtil {

  private AptUtil() {
    throw new UnsupportedOperationException("utility class");
  }

  private static ThreadLocal<ProcessingEnvironment>
      processingEnvironmentThreadLocal = new ThreadLocal<>();

  public static void setProcessingEnvironment(ProcessingEnvironment processingEnvironment) {
    if (processingEnvironment == null) {
      processingEnvironmentThreadLocal.remove();
    }
    processingEnvironmentThreadLocal.set(processingEnvironment);
  }

  static ProcessingEnvironment getProcessingEnvironment() {
    return processingEnvironmentThreadLocal.get();
  }

  public static Elements getElementUtils() {
    return getProcessingEnvironment().getElementUtils();
  }

  public static Filer getFiler() {
    return getProcessingEnvironment().getFiler();
  }

  public static Types getTypeUtils() {
    return getProcessingEnvironment().getTypeUtils();
  }

  /**
   * Retrieves annotation from element.
   *
   * @param element the element containing the annotation.
   * @param annotationClassName the annotation class name.
   * @return annotation or null if not found.
   */
  public static AnnotationMirror getAnnotation(Element element, String annotationClassName) {
    List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
    if (annotationMirrors == null || annotationMirrors.isEmpty()) {
      return null;
    }

    for (AnnotationMirror mirror : annotationMirrors) {
      if (isSameType(mirror.getAnnotationType(), annotationClassName)) {
        return mirror;
      }
    }

    // unable to find annotation
    return null;
  }

  public static Map<String, ? extends AnnotationValue> getAnnotationValues(
      AnnotationMirror annotationMirror) {

    Map<String, AnnotationValue> values = new HashMap<>();

    // get details from real annotation (defaults)
    List<ExecutableElement> annotationAttributes = ElementFilter
        .methodsIn(annotationMirror.getAnnotationType().asElement().getEnclosedElements());
    for (ExecutableElement annotationAttribute : annotationAttributes) {
      values.put(annotationAttribute.getSimpleName().toString(),
          annotationAttribute.getDefaultValue());
    }

    // get the set values set on the annotationMirror
    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror
        .getElementValues().entrySet()) {
      values.put(entry.getKey().getSimpleName().toString(), entry.getValue());
    }
    return values;
  }

  public static DeclaredType asDeclaredType(TypeMirror typeMirror) {
    if (typeMirror instanceof DeclaredType) {
      return (DeclaredType) typeMirror;
    }

    return null;
  }

  public static QualifiedNameable asQualifiedNameable(Element element) {
    if (element instanceof QualifiedNameable) {
      return (QualifiedNameable) element;
    }
    return null;
  }

  public static QualifiedNameable asQualifiedNameable(TypeMirror typeMirror) {
    DeclaredType declaredType = asDeclaredType(typeMirror);
    if (declaredType != null) {
      return asQualifiedNameable(declaredType.asElement());
    }
    return null;
  }

  public static TypeElement asTypeElement(Element element) {
    if (element instanceof TypeElement) {
      return (TypeElement) element;
    }
    return null;
  }

  public static TypeElement asTypeElement(TypeMirror mirror) {
    return asTypeElement(getTypeUtils().asElement(mirror));
  }

  public static ExecutableElement findConstructor(TypeMirror typeMirror, TypeMirror[] paramTypes) {
    if (typeMirror == null || asTypeElement(typeMirror) == null) {
      return null;
    }

    List<ExecutableElement> matchingConstructors = findMatchingParameters(
        ElementFilter.constructorsIn(asTypeElement(typeMirror).getEnclosedElements()), paramTypes);

    if (matchingConstructors.size() != 1) {
      return null;
    }

    return matchingConstructors.get(0);
  }

  public static ExecutableElement findMethod(TypeMirror typeMirror, String methodName,
      TypeMirror[] paramTypes) {
    if (typeMirror == null || asTypeElement(typeMirror) == null) {
      return null;
    }

    List<ExecutableElement> matchingMethods = findMatchingParameters(
        ElementFilter.methodsIn(asTypeElement(typeMirror).getEnclosedElements())
            .stream().filter(method -> methodName.equals(method.getSimpleName().toString()))
            .collect(toList()), paramTypes);

    if (matchingMethods.size() != 1) {
      return null;
    }

    return matchingMethods.get(0);
  }

  /**
   * Locates a resource by searching multiple locations.
   *
   * <p>This method assumes that the path is a full package path such as
   * <code>org/gwtproject/uibinder/example/view/SimpleFormView.ui.xml</code>
   *
   * @return FileObject or null if file is not found.
   * @see #findResource(CharSequence, CharSequence)
   */
  public static FileObject findResource(CharSequence path) {
    String packageName = "";
    String relativeName = path.toString();

    int index = relativeName.lastIndexOf('/');
    if (index >= 0) {
      packageName = relativeName.substring(0, index)
          .replace('/', '.');
      relativeName = relativeName.substring(index + 1);
    }

    return findResource(packageName, relativeName);
  }

  /**
   * Locates a resource by searching multiple locations.
   *
   * <p>Searches in the order of</p>
   * <ul>
   * <li>{@link StandardLocation#SOURCE_PATH}</li>
   * <li>{@link StandardLocation#CLASS_PATH}</li>
   * <li>{@link StandardLocation#CLASS_OUTPUT}</li>
   * </ul>
   *
   * @return FileObject or null if file is not found.
   */
  public static FileObject findResource(CharSequence pkg, CharSequence relativeName) {
    return findResource(
        Arrays.asList(
            StandardLocation.SOURCE_PATH,
            StandardLocation.CLASS_PATH,
            StandardLocation.CLASS_OUTPUT
        ), pkg, relativeName);
  }

  /**
   * Locates a resource by searching multiple locations.
   *
   * @return FileObject or null if file is not found in given locations.
   */
  public static FileObject findResource(List<Location> searchLocations, CharSequence pkg,
      CharSequence relativeName) {
    if (searchLocations == null || searchLocations.isEmpty()) {
      return null;
    }

    for (Location location : searchLocations) {
      try {
        FileObject fileObject = getFiler().getResource(location, pkg, relativeName);
        if (fileObject != null) {
          return fileObject;
        }
      } catch (IOException ignored) {
        // ignored
      }
    }

    // unable to locate, return null.
    return null;
  }

  public static List<Element> getEnumValues(TypeElement enumTypeElement) {
    if (!ElementKind.ENUM.equals(enumTypeElement.getKind())) {
      return null;
    }
    return enumTypeElement.getEnclosedElements()
        .stream()
        .filter(input -> ElementKind.ENUM_CONSTANT.equals(input.getKind()))
        .collect(toList());
  }

  /**
   * Returns all of the superclasses and superinterfaces for a given type including the type itself.
   * The returned set maintains an internal breadth-first ordering of the type, followed by its
   * interfaces (and their super-interfaces), then the supertype and its interfaces, and so on.
   */
  public static List<? extends TypeMirror> getFlattenedSupertypeHierarchy(TypeMirror type) {
    List<TypeMirror> superTypes = new ArrayList<>();

    if (type == null) {
      return superTypes;
    }

    Deque<TypeMirror> stack = new ArrayDeque<>();
    stack.push(type);

    while (!stack.isEmpty()) {
      TypeElement current = asTypeElement(stack.pop());

      TypeMirror superTypeClass;

      try {
        superTypeClass = current.getSuperclass();
      } catch (RuntimeException e) {
        // not sure why this would happen
        superTypeClass = null;
      }

      if (superTypeClass != null && superTypeClass.getKind() != TypeKind.NONE) {
        if (!superTypes.contains(superTypeClass)) {
          stack.push(superTypeClass);
          superTypes.add(superTypeClass);
        }
      }

      for (TypeMirror superTypeInterface : current.getInterfaces()) {
        if (!superTypes.contains(superTypeInterface)) {
          stack.push(superTypeInterface);
          superTypes.add(superTypeInterface);
        }
      }
    }

    return superTypes;
  }

  /**
   * Iterates over the most-derived declaration of each unique inheritable method available in the
   * type hierarchy of the specified type, including those found in superclasses and
   * superinterfaces. A method is inheritable if its accessibility is <code>public</code>,
   * <code>protected</code>, or package protected.
   *
   * This method offers a convenient way for Generators to find candidate methods to call from a
   * subclass.
   *
   * @param type the type to find methods on.
   * @return an array of {@link ExecutableElement} objects representing inheritable methods
   */
  public static List<? extends ExecutableElement> getInheritableMethods(TypeMirror type) {
    Map<ExecutableElement, TypeElement> inheritableMethods = new LinkedHashMap<>();

    List<TypeMirror> typeHierarchy = new ArrayList<>(getFlattenedSupertypeHierarchy(type));
    typeHierarchy.add(0, type);

    // we're going to be looking at methods from child up through ancestry
    for (TypeMirror typeMirror : typeHierarchy) {
      TypeElement typeElement = asTypeElement(typeMirror);
      if (typeElement != null) {
        List<ExecutableElement> methods = ElementFilter
            .methodsIn(typeElement.getEnclosedElements());
        for (ExecutableElement method : methods) {
          // check to see if this method is overridden by anything we already have
          if (!inheritableMethods.entrySet().stream()
              .anyMatch(entry ->
                  getElementUtils().overrides(entry.getKey(), method, entry.getValue()))) {
            inheritableMethods.put(method, typeElement);
          }
        }
      }
    }

    return new ArrayList<>(inheritableMethods.keySet());
  }

  public static PackageElement getPackageElement(Element element) {
    while (!(element instanceof PackageElement)) {
      element = element.getEnclosingElement();
    }
    return (PackageElement) element;
  }

  public static PackageElement getPackageElement(TypeMirror mirror) {
    return getPackageElement(asTypeElement(mirror));
  }

  public static String getParameterizedQualifiedSourceName(TypeMirror returnType) {
    if (returnType.getKind().isPrimitive() || TypeKind.VOID.equals(returnType.getKind())) {
      return returnType.toString();
    }

    if (returnType instanceof TypeVariable) {
      return getParameterizedQualifiedSourceName(((TypeVariable) returnType).getUpperBound());
    }

    return returnType.toString();
  }

  /**
   * Returns a {@code String} representing the source code declaration of this method, containing
   * access modifiers, type parameters, return type, method name, parameter list, and throws.
   * Doesn't include the method body or trailing semicolon.
   *
   * @param element the element to create readable declaration
   * @param noAccess if true, print no access modifiers
   * @param noNative if true, don't print the native modifier
   * @param noStatic if true, don't print the static modifier
   * @param noFinal if true, don't print the final modifier
   * @param noAbstract if true, don't print the abstract modifier
   */
  public static String getReadableDeclaration(ExecutableElement element, boolean noAccess,
      boolean noNative, boolean noStatic, boolean noFinal, boolean noAbstract) {
    return getReadableDeclaration(element, false, noAccess, noNative, noStatic, noFinal,
        noAbstract);
  }

  public static String getReadableDeclaration(ExecutableElement element,
      boolean excludeParameterNames, boolean noAccess, boolean noNative, boolean noStatic,
      boolean noFinal, boolean noAbstract) {
    Set<Modifier> modifiers = new HashSet<>(element.getModifiers());

    if (noAccess) {
      modifiers.remove(Modifier.PUBLIC);
      modifiers.remove(Modifier.PRIVATE);
      modifiers.remove(Modifier.PROTECTED);
    }
    if (noNative) {
      modifiers.remove(Modifier.NATIVE);
    }
    if (noStatic) {
      modifiers.remove(Modifier.STATIC);
    }
    if (noFinal) {
      modifiers.remove(Modifier.FINAL);
    }
    if (noAbstract) {
      modifiers.remove(Modifier.ABSTRACT);
    }

    String[] names = modifiersToNamesForMethod(modifiers);
    StringBuilder sb = new StringBuilder();
    for (String name : names) {
      sb.append(name);
      sb.append(" ");
    }

    List<? extends TypeMirror> typeArguments = getTypeArguments(element.asType());
    if (typeArguments != null && typeArguments.size() > 0) {
      // FIXME toStringTypeParams(sb);
      sb.append("<");
      boolean needComma = false;
      for (TypeMirror typeArgument : typeArguments) {
        if (needComma) {
          sb.append(", ");
        } else {
          needComma = true;
        }
        sb.append(asQualifiedNameable(typeArgument).getQualifiedName().toString());
      }
      sb.append(">");
      sb.append(" ");
    }
    sb.append(getParameterizedQualifiedSourceName(element.getReturnType()));
    sb.append(" ");
    sb.append(element.getSimpleName());

    // string params and throws
    List<? extends VariableElement> params = element.getParameters();
    sb.append("(");
    boolean needComma = false;
    for (int i = 0, c = params.size(); i < c; ++i) {
      VariableElement param = params.get(i);
      if (needComma) {
        sb.append(", ");
      } else {
        needComma = true;
      }

      if (element.isVarArgs() && i == c - 1) {
        sb.append(getParameterizedQualifiedSourceName(
            getTypeUtils().getArrayType(param.asType()).getComponentType()));
        sb.append("...");
      } else {
        sb.append(getParameterizedQualifiedSourceName(param.asType()));
      }
      if (!excludeParameterNames) {
        sb.append(" ");
        sb.append(param.getSimpleName());
      }
    }
    sb.append(")");

    if (!element.getThrownTypes().isEmpty()) {
      sb.append(" throws ");
      needComma = false;
      for (TypeMirror thrownType : element.getThrownTypes()) {
        if (needComma) {
          sb.append(", ");
        } else {
          needComma = true;
        }
        sb.append(getParameterizedQualifiedSourceName(thrownType));
      }
    }

    return sb.toString();
  }

  public static List<? extends TypeMirror> getTypeArguments(TypeMirror typeMirror) {
    if (typeMirror instanceof DeclaredType) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      return declaredType.getTypeArguments();
    }
    return null;
  }

  public static String getUninitializedFieldExpression(PrimitiveType primitiveType) {
    TypeKind kind = primitiveType.getKind();
    switch (kind) {
      case BOOLEAN:
        return "false";
      default:
        return "0";
    }
  }

  /**
   * Check for a constructor which is compatible with the supplied argument types.
   *
   * @param type the type
   * @param argTypes the argument types
   * @return true if a constructor compatible with the supplied arguments exists
   */
  public static boolean hasCompatibleConstructor(TypeMirror type, TypeMirror... argTypes) {
    for (ExecutableElement ctor : ElementFilter
        .constructorsIn(asTypeElement(type).getEnclosedElements())) {

      if (typesAreCompatible(
          ctor.getParameters().stream().map(item -> item.asType()).collect(toList()),
          Arrays.asList(argTypes), ctor.isVarArgs())) {
        return true;
      }
    }

    return false;
  }

  public static boolean isAnnotationPresent(Element element, String annotationClassname) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (isSameType(annotationMirror.getAnnotationType().asElement(), annotationClassname)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isAssignableFrom(TypeElement e1, TypeElement e2) {
    if (e1 == null || e2 == null) {
      return false;
    }
    return isAssignableFrom(e1.asType(), e2.asType());
  }

  public static boolean isAssignableFrom(TypeMirror m1, TypeMirror m2) {
    m1 = getTypeUtils().erasure(m1);
    m2 = getTypeUtils().erasure(m2);

    return getTypeUtils().isAssignable(m2, m1);
  }

  public static boolean isAssignableTo(TypeMirror m1, TypeMirror m2) {
    return isAssignableFrom(m2, m1);
  }

  public static boolean isEnum(TypeMirror mirror) {
    return isEnum(getTypeUtils().asElement(mirror));
  }

  public static boolean isEnum(Element element) {
    if (element != null) {
      return ElementKind.ENUM.equals(element.getKind());
    }
    return false;
  }

  /**
   * Determines if TypeMirror is a raw type (outcome of erasure).
   */
  public static boolean isRaw(TypeMirror mirror) {
    if (TypeKind.DECLARED.equals(mirror.getKind())) {
      DeclaredType declaredType = (DeclaredType) mirror;
      if (declaredType.getTypeArguments().isEmpty()) {
        return true;
      }
    }

    return false;
  }

  public static boolean isSameType(DeclaredType declaredType, String canonicalName) {
    if (declaredType == null) {
      return false;
    }

    return isSameType(declaredType.asElement(), canonicalName);
  }

  public static boolean isSameType(Element element, String canonicalClassName) {
    QualifiedNameable qualifiedNameable = asQualifiedNameable(element);
    if (canonicalClassName == null) {
      return false;
    }

    return canonicalClassName.equals(qualifiedNameable.getQualifiedName().toString());
  }

  /**
   * Return true if the supplied argument type is assignment compatible with a declared parameter
   * type.
   *
   * @param paramType the param type
   * @param argType the arg type
   * @return true if the argument type is compatible with the parameter type
   */
  public static boolean typeIsCompatible(TypeMirror paramType, TypeMirror argType) {
    if (paramType == argType) {
      return true;
    }
    TypeElement paramClass = asTypeElement(paramType);
    if (paramClass != null) {
      TypeElement argClass = asTypeElement(argType);
      return argClass != null && isAssignableFrom(paramClass, argClass);
    }
    if (TypeKind.ARRAY.equals(paramType.getKind())) {
      ArrayType paramArray = getTypeUtils().getArrayType(paramType);
      if (TypeKind.ARRAY.equals(argType.getKind())) {
        ArrayType argArray = getTypeUtils().getArrayType(argType);
        return typeIsCompatible(paramArray.getComponentType(), argArray.getComponentType());
      }
    }
    if (paramType.getKind().isPrimitive() && argType.getKind().isPrimitive()) {
      return isWideningPrimitiveConversion((PrimitiveType) paramType, (PrimitiveType) argType);
    }
    // TODO: handle autoboxing?
    return false;
  }

  /**
   * Check if the types of supplied arguments are compatible with the parameter types of a method.
   *
   * @param paramTypes the param types
   * @param argTypes the arg types to match against
   * @param varArgs true if the method is a varargs method
   * @return true if all argument types are compatible with the parameter types
   */
  public static boolean typesAreCompatible(List<? extends TypeMirror> paramTypes,
      List<? extends TypeMirror> argTypes,
      boolean varArgs) {
    int expectedArgs = paramTypes.size();
    int actualArgs = argTypes.size();
    int comparedArgs = expectedArgs;
    if (varArgs) {
      comparedArgs--;
      if (actualArgs != expectedArgs
          || !typeIsCompatible(paramTypes.get(comparedArgs), argTypes.get(comparedArgs))) {
        if (actualArgs < comparedArgs) {
          return false;
        }
        ArrayType varargsArrayType = getTypeUtils().getArrayType(paramTypes.get(comparedArgs));
        assert varargsArrayType != null;
        TypeMirror varargsType = varargsArrayType.getComponentType();
        for (int i = comparedArgs; i < actualArgs; ++i) {
          if (!typeIsCompatible(varargsType, argTypes.get(i))) {
            return false;
          }
        }
      }
    } else if (actualArgs != expectedArgs) {
      return false;
    }
    for (int i = 0; i < comparedArgs; ++i) {
      if (!typeIsCompatible(paramTypes.get(i), argTypes.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static List<ExecutableElement> findMatchingParameters(
      List<ExecutableElement> executableElements, TypeMirror[] paramTypes) {
    return executableElements.stream()
        .filter(item -> {
          List<? extends VariableElement> elementParameters = item.getParameters();

          if (paramTypes.length != elementParameters.size()) {
            return false;
          }
          for (int i = 0; i < paramTypes.length; i++) {
            TypeMirror paramType = paramTypes[i];
            if (!getTypeUtils().isSameType(paramType, elementParameters.get(i).asType())) {
              return false;
            }
          }
          return true;
        }).collect(toList());
  }

  private static boolean isWideningPrimitiveConversion(PrimitiveType paramType,
      PrimitiveType argType) {
    switch (paramType.getKind()) {
      case DOUBLE:
        return argType.getKind() != TypeKind.BOOLEAN;
      case FLOAT:
        return argType.getKind() != TypeKind.BOOLEAN && argType.getKind() != TypeKind.DOUBLE;
      case LONG:
        return argType.getKind() != TypeKind.BOOLEAN && argType.getKind() != TypeKind.DOUBLE
            && argType.getKind() != TypeKind.FLOAT;
      case INT:
        return argType.getKind() == TypeKind.BYTE || argType.getKind() == TypeKind.SHORT
            || argType.getKind() == TypeKind.CHAR;
      case SHORT:
        return argType.getKind() == TypeKind.BYTE;
      default:
        return false;
    }
  }

  private static String[] modifiersToNamesForMethod(Set<Modifier> modifiers) {
    List<String> strings = modifiersToNamesForMethodsAndFields(modifiers);

    if (modifiers.contains(Modifier.ABSTRACT)) {
      strings.add("abstract");
    }

    if (modifiers.contains(Modifier.NATIVE)) {
      strings.add("native");
    }

    return strings.toArray(new String[strings.size()]);
  }

  private static List<String> modifiersToNamesForMethodsAndFields(Set<Modifier> modifiers) {
    List<String> strings = new ArrayList<>();

    if (modifiers.contains(Modifier.PUBLIC)) {
      strings.add("public");
    }

    if (modifiers.contains(Modifier.PRIVATE)) {
      strings.add("private");
    }

    if (modifiers.contains(Modifier.PROTECTED)) {
      strings.add("protected");
    }

    if (modifiers.contains(Modifier.STATIC)) {
      strings.add("static");
    }

    if (modifiers.contains(Modifier.FINAL)) {
      strings.add("final");
    }

    return strings;
  }
}
