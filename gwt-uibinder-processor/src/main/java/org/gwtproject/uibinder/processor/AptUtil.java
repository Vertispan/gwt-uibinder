package org.gwtproject.uibinder.processor;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
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

  public static PackageElement getPackageElement(Element element) {
    while (!(element instanceof PackageElement)) {
      element = element.getEnclosingElement();
    }
    return (PackageElement) element;
  }

  public static PackageElement getPackageElement(TypeMirror mirror) {
    return getPackageElement(asTypeElement(mirror));
  }

  public static List<? extends TypeMirror> getTypeArguments(TypeMirror typeMirror) {
    if (typeMirror instanceof DeclaredType) {
      DeclaredType declaredType = (DeclaredType) typeMirror;
      return declaredType.getTypeArguments();
    }
    return null;
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
}
