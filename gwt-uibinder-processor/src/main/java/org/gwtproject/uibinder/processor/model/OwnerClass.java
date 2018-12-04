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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

/**
 * Model class with all attributes of the owner class. This includes factories, fields and
 * handlers.
 */
public class OwnerClass {

  /**
   * Map from field name to model.
   */
  private final Map<String, OwnerField> uiFields = new TreeMap<>();

  /**
   * Map from field type to model.
   *
   * This is used for binding resources - for widgets, there may be multiple widgets with the same
   * type, in which case this becomes meaningless.
   */
  private final Map<TypeMirror, OwnerField> uiFieldTypes = new HashMap<>();

  /**
   * Map from type to the method that produces it.
   */
  private final Map<TypeMirror, ExecutableElement> uiFactories = new HashMap<>();

  /**
   * List of all @UiHandler methods in the owner class.
   */
  private final List<ExecutableElement> uiHandlers = new ArrayList<>();

  private final MortalLogger logger;

  private final TypeMirror ownerType;

  private final UiBinderContext context;

  /**
   * Constructor.
   *
   * @param ownerType the type of the owner class
   */
  public OwnerClass(TypeMirror ownerType, MortalLogger logger,
      UiBinderContext context)
      throws UnableToCompleteException {
    this.logger = logger;
    this.ownerType = ownerType;
    this.context = context;
    findUiFields(ownerType);
    findUiFactories(ownerType);
    findUiHandlers(ownerType);
  }

  public TypeMirror getOwnerType() {
    return ownerType;
  }

  /**
   * Returns the method annotated with @UiFactory which returns the given type.
   *
   * @param forType the type to look for a factory of
   * @return the factory method, or null if none exists
   */
  public ExecutableElement getUiFactoryMethod(TypeMirror forType) {
    return uiFactories.get(AptUtil.getTypeUtils().erasure(forType));
  }

  /**
   * Gets a field with the given name. It's important to notice that a field may not exist on the
   * owner class even if it has a name in the XML and even has handlers attached to it -  such a
   * field will only exist in the generated binder class.
   *
   * @param name the name of the field to get
   * @return the field descriptor, or null if the owner doesn't have that field
   */
  public OwnerField getUiField(String name) {
    return uiFields.get(name);
  }

  /**
   * Gets the field with the given type. Note that multiple fields can have the same type, so it
   * only makes sense to call this to retrieve resource fields, such as messages and image bundles,
   * for which only one instance is expected.
   *
   * @param type the type of the field
   * @return the field descriptor
   * @deprecated This will die with BundleAttributeParser TODO
   */
  @Deprecated
  public OwnerField getUiFieldForType(TypeMirror type) {
    return uiFieldTypes.get(type);
  }

  /**
   * Returns a collection of all fields in the owner class.
   */
  public Collection<OwnerField> getUiFields() {
    return uiFields.values();
  }

  /**
   * Returns all the UiHandler methods defined in the owner class.
   */
  public List<ExecutableElement> getUiHandlers() {
    return uiHandlers;
  }

  /**
   * Scans the owner class to find all methods annotated with @UiFactory, and puts them in {@link
   * #uiFactories}.
   *
   * @param ownerType the type of the owner class
   */
  private void findUiFactories(TypeMirror ownerType)
      throws UnableToCompleteException {

    List<ExecutableElement> methods =
        ElementFilter.methodsIn(AptUtil.getTypeUtils().asElement(ownerType).getEnclosedElements());
    for (ExecutableElement method : methods) {
      if (AptUtil.isAnnotationPresent(method, UiBinderApiPackage.current().getUiFactoryFqn())) {
        TypeElement factoryType = AptUtil.asTypeElement(method.getReturnType());
        if (factoryType == null) {
          logger.die("Factory return type is not a class in method "
              + method.getSimpleName());
        }

        TypeMirror erasure = AptUtil.getTypeUtils().erasure(factoryType.asType());

        if (uiFactories.containsKey(erasure)) {
          logger.die("Duplicate factory in class "
              + method.getEnclosingElement().getSimpleName() + " for type "
              + factoryType.getSimpleName());
        }

        uiFactories.put(erasure, method);
      }
    }

    // Recurse to superclass

    TypeMirror superclass = AptUtil.asTypeElement(ownerType).getSuperclass();
    if (superclass != null && !TypeKind.NONE.equals(superclass.getKind())) {
      findUiFactories(superclass);
    }
  }

  /**
   * Scans the owner class to find all fields annotated with @UiField, and puts them in {@link
   * #uiFields} and {@link #uiFieldTypes}.
   *
   * @param ownerType the type of the owner class
   */
  private void findUiFields(TypeMirror ownerType) throws UnableToCompleteException {
    Types typeUtils = AptUtil.getTypeUtils();

    TypeElement ownerElement = AptUtil
        .asTypeElement(typeUtils.asElement(ownerType));

    List<VariableElement> fields = ElementFilter.fieldsIn(ownerElement.getEnclosedElements());
    for (VariableElement field : fields) {
      if (AptUtil.isAnnotationPresent(field, UiBinderApiPackage.current().getUiFieldFqn())) {
        TypeMirror ownerFieldType = field.asType();

        if (ownerFieldType == null) {
          logger.die("Field type is not a class in field " + field.getSimpleName());
        }

        OwnerField ownerField = new OwnerField(field, logger, context);
        String ownerFieldName = field.getSimpleName().toString();
        uiFields.put(ownerFieldName, ownerField);
        uiFieldTypes.put(ownerFieldType, ownerField);
      }
    }

    // Recurse to superclass
    TypeMirror superclass = ownerElement.getSuperclass();
    if (!TypeKind.NONE.equals(superclass.getKind())) {
      findUiFields(superclass);
    }
  }

  /**
   * Scans the owner class to find all methods annotated with @UiHandler, and adds them to their
   * respective fields.
   *
   * @param ownerType the type of the owner class
   */
  private void findUiHandlers(TypeMirror ownerType) {

    List<ExecutableElement> methods =
        ElementFilter.methodsIn(AptUtil.getTypeUtils().asElement(ownerType).getEnclosedElements());
    for (ExecutableElement method : methods) {
      if (AptUtil.isAnnotationPresent(method, UiBinderApiPackage.current().getUiHandlerFqn())) {
        uiHandlers.add(method);
      }
    }

    // Recurse to superclass
    TypeMirror superclass = AptUtil.asTypeElement(ownerType).getSuperclass();
    if (superclass != null && !TypeKind.NONE.equals(superclass.getKind())) {
      findUiHandlers(superclass);
    }
  }
}
