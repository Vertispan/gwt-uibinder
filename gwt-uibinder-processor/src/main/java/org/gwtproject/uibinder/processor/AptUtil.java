package org.gwtproject.uibinder.processor;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 *
 */
public class AptUtil {

  private AptUtil() {
    throw new UnsupportedOperationException("utility class");
  }


  private static ThreadLocal<ProcessingEnvironment>
      processingEnvironmentThreadLocal = new ThreadLocal<>();


  static void setProcessingEnvironment(ProcessingEnvironment processingEnvironment) {
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
      if (isSameType(mirror.getAnnotationType(), UiBinderClasses.UITEMPLATE)) {
        return mirror;
      }
    }

    // unable to find annotation
    return null;
  }

  public static Map<String, ? extends AnnotationValue> getAnnotationValues(
      AnnotationMirror annotationMirror) {

    Map<String, AnnotationValue> values = new HashMap<>();

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
  public static Set<? extends TypeMirror> getFlattenedSupertypeHierarchy(TypeMirror type) {
    Set<TypeMirror> superTypes = new HashSet<>();

    while (type != null && !TypeKind.NONE.equals(type.getKind())) {
      TypeElement typeElement = asTypeElement(type);
      type = typeElement.getSuperclass();

      superTypes.add(type);
      superTypes.addAll(typeElement.getInterfaces());
    }

    return superTypes;
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

  public static String getReadableDeclaration(ExecutableElement element, boolean noAccess,
      boolean noNative, boolean noStatic, boolean noFinal, boolean noAbstract) {
    if (element == null) {
      return "";
    }
    Set<Modifier> modifiers = element.getModifiers();
    StringBuilder sb = new StringBuilder();
    // FIXME
    throw new NullPointerException();
    //return null;
  }

  public static List<? extends TypeMirror> getTypeArguments(TypeMirror typeMirror) {
    if (typeMirror instanceof DeclaredType) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      return declaredType.getTypeArguments();
    }
    return null;
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

}
