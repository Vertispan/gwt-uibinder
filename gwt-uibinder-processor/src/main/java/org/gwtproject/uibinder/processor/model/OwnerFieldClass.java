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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.beans.Introspector;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

/**
 * Descriptor for a class which can be used as a @UiField. This is usually a widget, but can also be
 * a resource (such as Messages or an ImageBundle). Also notice that the existence of an
 * OwnerFieldClass doesn't mean the class is actually present as a field in the owner.
 */
public class OwnerFieldClass {

  private static final int DEFAULT_COST = 4;
  private static final Map<String, Integer> TYPE_RANK;

  static {
    HashMap<String, Integer> tmpTypeRank = new HashMap<String, Integer>();
    tmpTypeRank.put("java.lang.String", 1);
    tmpTypeRank.put("boolean", 2);
    tmpTypeRank.put("byte", 2);
    tmpTypeRank.put("char", 2);
    tmpTypeRank.put("double", 2);
    tmpTypeRank.put("float", 2);
    tmpTypeRank.put("int", 2);
    tmpTypeRank.put("long", 2);
    tmpTypeRank.put("short", 2);
    tmpTypeRank.put("java.lang.Boolean", 3);
    tmpTypeRank.put("java.lang.Byte", 3);
    tmpTypeRank.put("java.lang.Character", 3);
    tmpTypeRank.put("java.lang.Double", 3);
    tmpTypeRank.put("java.lang.Float", 3);
    tmpTypeRank.put("java.lang.Integer", 3);
    tmpTypeRank.put("java.lang.Long", 3);
    tmpTypeRank.put("java.lang.Short", 3);
    TYPE_RANK = Collections.unmodifiableMap(tmpTypeRank);
  }

  /**
   * Gets or creates the descriptor for the given field class.
   *
   * @param forType the field type to get a descriptor for
   * @param logger TODO
   * @return the descriptor
   */
  public static OwnerFieldClass getFieldClass(TypeMirror forType, MortalLogger logger,
      UiBinderContext context) throws UnableToCompleteException {
    OwnerFieldClass clazz = context.getOwnerFieldClass(forType);
    if (clazz == null) {
      clazz = new OwnerFieldClass(forType, logger, context);
      context.putOwnerFieldClass(forType, clazz);
    }
    return clazz;
  }

  private Set<String> ambiguousSetters;
  private final MortalLogger logger;
  private final TypeMirror rawType;
  private final UiBinderContext context;
  private final Map<String, ExecutableElement> setters = new HashMap<>();
  /**
   * Mapping from all of the @UiChild tags to their corresponding methods and limits on being
   * called.
   */
  private final Map<String, SimpleEntry<ExecutableElement, Integer>> uiChildren = new HashMap<>();

  private ExecutableElement uiConstructor;

  /**
   * Default constructor. This is package-visible for testing only.
   *
   * @param forType the type of the field class
   * @param logger the logger
   * @throws UnableToCompleteException if the class is not valid
   */
  OwnerFieldClass(TypeMirror forType, MortalLogger logger, UiBinderContext context)
      throws UnableToCompleteException {
    this.rawType = forType;
    this.logger = logger;
    this.context = context;

    findUiConstructor(forType);
    findSetters(forType);
    findUiChildren(forType);
  }

  /**
   * Returns the field's raw type.
   */
  public TypeMirror getRawType() {
    return rawType;
  }

  /**
   * Finds the setter method for a given property.
   *
   * @param propertyName the name of the property
   * @return the setter method, or null if none exists
   */
  public ExecutableElement getSetter(String propertyName) throws UnableToCompleteException {
    if (ambiguousSetters != null && ambiguousSetters.contains(propertyName)) {
      logger.die(
          "Ambiguous setter requested: " + AptUtil.asQualifiedNameable(rawType).getQualifiedName()
              .toString() + "." + propertyName);
    }

    return setters.get(propertyName);
  }

  /**
   * Returns a list of methods annotated with @UiChild.
   *
   * @return a list of all add child methods
   */
  public Map<String, SimpleEntry<ExecutableElement, Integer>> getUiChildMethods() {
    return uiChildren;
  }

  /**
   * Returns the constructor annotated with @UiConstructor, or null if none exists.
   */
  public ExecutableElement getUiConstructor() {
    return uiConstructor;
  }

  /**
   * Given a collection of setters for the same property, picks which one to use. Not having a
   * proper setter is not an error unless of course the user tries to use it.
   *
   * @param propertyName the name of the property/setter.
   * @param propertySetters the collection of setters.
   * @return the setter to use, or null if none is good enough.
   */
  private ExecutableElement disambiguateSetters(String propertyName,
      Collection<ExecutableElement> propertySetters) {

    // if only have one overload, there is no need to rank them.
    if (propertySetters.size() == 1) {
      return propertySetters.iterator().next();
    }

    // rank overloads and pick the one with minimum 'cost' of conversion.
    ExecutableElement preferredMethod = null;
    int minRank = Integer.MAX_VALUE;
    for (ExecutableElement method : propertySetters) {
      int rank = rankMethodOnParameters(method);
      if (rank < minRank) {
        minRank = rank;
        preferredMethod = method;
        ambiguousSetters.remove(propertyName);
      } else if (rank == minRank && !ambiguousSetters.contains(propertyName)) {
        ambiguousSetters.add(propertyName);
      }
    }

    // if the setter is ambiguous, return null.
    if (ambiguousSetters.contains(propertyName)) {
      return null;
    }

    // the setter is not ambiguous therefore return the preferred overload.
    return preferredMethod;
  }

  /**
   * Recursively finds all setters for the given class and its superclasses.
   *
   * @param fieldType the leaf type to look at
   * @return a multimap of property name to the setter methods
   */
  private Multimap<String, ExecutableElement> findAllSetters(TypeMirror fieldType) {
    Multimap<String, ExecutableElement> allSetters = LinkedHashMultimap.create();

    for (ExecutableElement method : AptUtil.getInheritableMethods(fieldType)) {
      if (!isSetterMethod(method)) {
        continue;
      }

      // Take out "set"
      String propertyName = method.getSimpleName().toString().substring(3);

      // turn "PropertyName" into "propertyName"
      String beanPropertyName = Introspector.decapitalize(propertyName);
      allSetters.put(beanPropertyName, method);

      // keep backwards compatibility (i.e. hTML instead of HTML for setHTML)
      String legacyPropertyName = propertyName.substring(0, 1).toLowerCase(Locale.ROOT)
          + propertyName.substring(1);
      if (!legacyPropertyName.equals(beanPropertyName)) {
        allSetters.put(legacyPropertyName, method);
      }
    }

    return allSetters;
  }

  /**
   * Finds all setters in the class, and puts them in the {@link #setters} field.
   *
   * @param fieldType the type of the field
   */
  private void findSetters(TypeMirror fieldType) {
    // Pass one - get all setter methods
    Multimap<String, ExecutableElement> allSetters = findAllSetters(fieldType);

    // Pass two - disambiguate
    ambiguousSetters = new HashSet<>();
    for (String propertyName : allSetters.keySet()) {
      Collection<ExecutableElement> propertySetters = allSetters.get(propertyName);
      ExecutableElement setter = disambiguateSetters(propertyName, propertySetters);
      setters.put(propertyName, setter);
    }

    if (ambiguousSetters.size() == 0) {
      ambiguousSetters = null;
    }
  }

  /**
   * Scans the class to find all methods annotated with @UiChild.
   *
   * @param ownerType the type of the owner class
   */
  private void findUiChildren(TypeMirror ownerType) throws UnableToCompleteException {
    while (!TypeKind.NONE.equals(ownerType.getKind())) {
      TypeElement ownerElement = AptUtil.asTypeElement(ownerType);
      List<ExecutableElement> methods = ElementFilter.methodsIn(ownerElement.getEnclosedElements());
      for (ExecutableElement method : methods) {
        AnnotationMirror annotation = AptUtil
            .getAnnotation(method, UiBinderApiPackage.current().getUiChildFqn());
        if (annotation == null) {
          // FIXME - this is only for backwards compatibility
          //  - any legacy widgets would have the old @UiChild annotation, not the new
          // if it's null, let's check for legacy annotation
          annotation = AptUtil
              .getAnnotation(method, UiBinderApiPackage.LEGACY.getUiChildFqn());
        }
        if (annotation != null) {
          Map<String, ? extends AnnotationValue> annotationValues = AptUtil
              .getAnnotationValues(annotation);
          String tag = (String) annotationValues.get("tagname").getValue();
          int limit = (int) annotationValues.get("limit").getValue();
          if ("".equals(tag)) {
            String name = method.getSimpleName().toString();
            if (name.startsWith("add")) {
              tag = name.substring(3).toLowerCase(Locale.ROOT);
            } else {
              logger.die(method.getSimpleName()
                  + " must either specify a UiChild tagname or begin "
                  + "with \"add\".");
            }
          }
          List<? extends VariableElement> parameters = method.getParameters();
          if (parameters.isEmpty()) {
            logger.die("%s must take at least one Object argument", method.getSimpleName());
          }
          TypeMirror type = parameters.get(0).asType();
          if (!TypeKind.DECLARED.equals(type.getKind())) {
            logger.die("%s first parameter must be an object type, found %s",
                method.getSimpleName(), type.getKind());
          }
          uiChildren.put(tag, new SimpleEntry<>(method, limit));
        }
      }

      ownerType = ownerElement.getSuperclass();
    }
  }

  /**
   * Finds the constructor annotated with @UiConcontructor if there is one, and puts it in the
   * {@link #uiConstructor} field.
   *
   * @param fieldType the type of the field
   */
  private void findUiConstructor(TypeMirror fieldType) throws UnableToCompleteException {
    Element fieldElement = AptUtil.getTypeUtils().asElement(fieldType);

    ElementFilter.constructorsIn(fieldElement.getEnclosedElements());

    for (ExecutableElement ctor : ElementFilter
        .constructorsIn(fieldElement.getEnclosedElements())) {
      if (AptUtil.isAnnotationPresent(ctor, UiBinderApiPackage.current().getUiConstructorFqn())) {
        if (uiConstructor != null) {
          logger.die(fieldElement.getSimpleName().toString()
              + " has more than one constructor annotated with @UiConstructor");
        }
        uiConstructor = ctor;
      }
    }
  }

  /**
   * Checks whether the given method qualifies as a setter. This looks at the method qualifiers,
   * name and return type, but not at the parameter types.
   *
   * @param method the method to look at
   * @return whether it's a setter
   */
  private boolean isSetterMethod(ExecutableElement method) {
    // All setter methods should be public void setSomething(...)
    Set<Modifier> modifiers = method.getModifiers();
    return modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC)
        && method.getSimpleName().toString().startsWith("set")
        && method.getSimpleName().toString().length() > 3
        && method.getReturnType().getKind().equals(TypeKind.VOID);
  }

  /**
   * Ranks given method based on parameter conversion cost. A lower rank is preferred over a higher
   * rank since it has a lower cost of conversion.
   *
   * <pre>
   * The ranking criteria is as follows:
   * 1) methods with fewer arguments are preferred. for instance:
   *    'setValue(int)' is preferred 'setValue(int, int)'.
   * 2) within a set of overloads with the same number of arguments:
   * 2.1) String has the lowest cost = 1
   * 2.2) primitive types, cost = 2
   * 2.3) boxed primitive types, cost = 3
   * 2.4) any (reference types, etc), cost = 4.
   * 3) if a setter is overridden by a subclass and have the exact same argument
   * types, it will not be considered ambiguous.
   * </pre>
   *
   * The cost mapping is defined in {@link #TYPE_RANK typeRank }
   *
   * @return the rank of the method.
   */
  private int rankMethodOnParameters(ExecutableElement method) {
    List<? extends VariableElement> params = method.getParameters();
    int rank = 0;
    for (int i = 0; i < Math.min(params.size(), 10); i++) {
      int cost = DEFAULT_COST;

      TypeMirror paramTypeMirror = params.get(i).asType();
      if (paramTypeMirror.getKind().isPrimitive()) {
        if (TYPE_RANK.containsKey(paramTypeMirror.getKind().name().toLowerCase())) {
          cost = TYPE_RANK.get(paramTypeMirror.getKind().name().toLowerCase());
        }
      } else {
        QualifiedNameable nameable = AptUtil.asQualifiedNameable(paramTypeMirror);
        if (nameable != null) {
          Name qualifiedName = nameable.getQualifiedName();
          if (qualifiedName != null && TYPE_RANK.containsKey(qualifiedName.toString())) {
            cost = TYPE_RANK.get(qualifiedName.toString());
          }
        }
      }
      assert (cost >= 0 && cost <= 0x07);
      rank = rank | (cost << (3 * i));
    }
    assert (rank >= 0);
    return rank;
  }
}
