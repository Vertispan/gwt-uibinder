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
package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.UiBinderContext;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLAttribute;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.AttributeMessage;
import org.gwtproject.uibinder.processor.model.OwnerField;
import org.gwtproject.uibinder.processor.model.OwnerFieldClass;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Utility methods for discovering bean-like properties and generating code to initialize them.
 */
public class BeanParser implements ElementParser {

  /**
   * Mapping between parameters and special UIObject methods. The {@link UIObjectParser} has a few
   * methods that extend the normal bean naming pattern. So, that implementations of IsWidget
   * behave like UIObjects, they have to be translated.
   */
  private static final Map<String, String> ADD_PROPERTY_TO_SETTER_MAP =
      new HashMap<String, String>() {
        {
          put("addStyleNames", "addStyleName");
          put("addStyleDependentNames", "addStyleDependentName");
        }
      };

  private final UiBinderContext context;

  public BeanParser(UiBinderContext context) {
    this.context = context;
  }

  /**
   * Generates code to initialize all bean attributes on the given element. Includes support for
   * &lt;ui:attribute /&gt; children that will apply to setters
   */
  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
//    writer.getDesignTime().handleUIObject(writer, elem, fieldName);

    final Map<String, String> setterValues = new HashMap<String, String>();
    final Map<String, String> localizedValues = fetchLocalizedAttributeValues(
        elem, writer);
    final Map<String, String[]> adderValues = new HashMap<>();

    final Map<String, String> requiredValues = new HashMap<>();
    final Map<String, TypeMirror> unfilledRequiredParams = new HashMap<>();

    final OwnerFieldClass ownerFieldClass = OwnerFieldClass.getFieldClass(type,
        writer.getLogger(), context);

    /*
     * Handle @UiFactory and @UiConstructor, but only if the user
     * hasn't provided an instance via @UiField(provided = true)
     */
    ExecutableElement creator = null;
    boolean uiConstructor = false;
    OwnerField uiField = writer.getOwnerClass().getUiField(fieldName);
    if ((uiField == null) || (!uiField.isProvided())) {
      // See if there's a factory method
      creator = writer.getOwnerClass().getUiFactoryMethod(type);
      if (creator == null) {
        // If not, see if there's a @UiConstructor
        creator = ownerFieldClass.getUiConstructor();
        // old source was able to use type hierarchy to determine this.  we can't here
        uiConstructor = creator != null;
      }

      if (creator != null) {
        for (VariableElement param : creator.getParameters()) {
          unfilledRequiredParams.put(param.getSimpleName().toString(), param.asType());
        }
      }
    }

    // Work through the localized attribute values and assign them
    // to appropriate constructor params or setters (which had better be
    // ready to accept strings)

    for (Entry<String, String> property : localizedValues.entrySet()) {
      String key = property.getKey();
      String value = property.getValue();

      TypeMirror paramType = unfilledRequiredParams.get(key);
      if (paramType != null) {
        if (!isString(writer, paramType)) {
          writer.die(elem,
              "In %s, cannot apply message attribute to non-string "
                  + "constructor argument %s.",
              AptUtil.asQualifiedNameable(paramType).getSimpleName(), key);
        }

        requiredValues.put(key, value);
        unfilledRequiredParams.remove(key);
      } else {
        ExecutableElement setter = ownerFieldClass.getSetter(key);
        List<? extends VariableElement> params = setter == null ? null : setter.getParameters();

        if (setter == null || !(params.size() == 1)
            || !isString(writer, params.get(0).asType())) {
          writer.die(elem, "No method found to apply message attribute %s", key);
        } else {
          setterValues.put(key, value);
        }
      }
    }

    // Now go through the element and dispatch its attributes, remembering
    // that constructor arguments get first dibs
    for (int i = elem.getAttributeCount() - 1; i >= 0; i--) {
      // Backward traversal b/c we're deleting attributes from the xml element

      XMLAttribute attribute = elem.getAttribute(i);

      // Ignore xmlns attributes
      if (attribute.getName().startsWith("xmlns:")) {
        continue;
      }

      String propertyName = attribute.getLocalName();
      if (setterValues.keySet().contains(propertyName)
          || requiredValues.containsKey(propertyName)) {
        writer.die(elem, "Duplicate attribute name: %s", propertyName);
      }

      if (unfilledRequiredParams.keySet().contains(propertyName)) {
        TypeMirror paramType = unfilledRequiredParams.get(propertyName);
        String value = elem.consumeAttributeWithDefault(attribute.getName(),
            null, paramType);
        if (value == null) {
          writer.die(elem, "Unable to parse %s as constructor argument "
              + "of type %s", attribute, AptUtil.asQualifiedNameable(paramType).getSimpleName());
        }
        requiredValues.put(propertyName, value);
        unfilledRequiredParams.remove(propertyName);
      } else {
        ExecutableElement setter = ownerFieldClass.getSetter(propertyName);
        if (setter != null) {
          String n = attribute.getName();
          String value = elem.consumeAttributeWithDefault(n, null, getParamTypes(type, setter));

          if (value == null) {
            writer.die(elem, "Unable to parse %s.", attribute);
          }
          setterValues.put(propertyName, value);
        } else if (ADD_PROPERTY_TO_SETTER_MAP.containsKey(propertyName)) {
          String addMethod = ADD_PROPERTY_TO_SETTER_MAP.get(propertyName);
          TypeElement stringType = AptUtil.getElementUtils().getTypeElement(String.class.getName());

          if (AptUtil.findMethod(ownerFieldClass.getRawType(), addMethod,
              new TypeMirror[]{stringType.asType()}) != null) {
            String n = attribute.getName();
            String[] value = elem.consumeStringArrayAttribute(n);

            if (value == null) {
              writer.die(elem, "Unable to parse %s.", attribute);
            }
            adderValues.put(addMethod, value);
          } else {
            writer.die(elem, "Class %s has no appropriate %s() method",
                elem.getLocalName(), addMethod);
          }
        } else {
          writer.die(elem, "Class %s has no appropriate set%s() method",
              elem.getLocalName(), initialCap(propertyName));
        }
      }
    }

    if (!unfilledRequiredParams.isEmpty()) {
      StringBuilder b = new StringBuilder(String.format(
          "%s missing required attribute(s):", elem));
      for (String name : unfilledRequiredParams.keySet()) {
        b.append(" ").append(name);
      }
      writer.die(elem, b.toString());
    }

    if (creator != null) {
      String[] args = makeArgsList(requiredValues, creator);
      if (!uiConstructor) { // Factory method
        ExecutableElement factoryMethod = (ExecutableElement) creator;
        String initializer;
//        if (writer.getDesignTime().isDesignTime()) {
//          String typeName = factoryMethod.getReturnType().getQualifiedSourceName();
//          initializer = writer.getDesignTime().getProvidedFactory(typeName,
//              factoryMethod.getName(),
//              UiBinderWriter.asCommaSeparatedList(args));
//        } else {
        initializer = String.format("owner.%s(%s)", factoryMethod.getSimpleName(),
            UiBinderWriter.asCommaSeparatedList(args));
//        }
        writer.setFieldInitializer(fieldName, initializer);
      } else { // Annotated Constructor
        writer.setFieldInitializerAsConstructor(fieldName, args);
      }
    }

    for (Map.Entry<String, String> entry : setterValues.entrySet()) {
      String propertyName = entry.getKey();
      String value = entry.getValue();
      writer.addStatement("%s.set%s(%s);", fieldName, initialCap(propertyName),
          value);
    }

    for (Map.Entry<String, String[]> entry : adderValues.entrySet()) {
      String addMethodName = entry.getKey();
      for (String s : entry.getValue()) {
        writer.addStatement("%s.%s(%s);", fieldName, addMethodName, s);
      }
    }
  }

  /**
   * Fetch the localized attributes that were stored by the AttributeMessageParser.
   */
  private Map<String, String> fetchLocalizedAttributeValues(XMLElement elem,
      UiBinderWriter writer) {
    final Map<String, String> localizedValues = new HashMap<String, String>();

    Collection<AttributeMessage> attributeMessages = writer.getMessages()
        .retrieveMessageAttributesFor(
            elem);

    if (attributeMessages != null) {
      for (AttributeMessage att : attributeMessages) {
        String propertyName = att.getAttribute();
        localizedValues.put(propertyName, att.getMessageUnescaped());
      }
    }
    return localizedValues;
  }

  private TypeMirror[] getParamTypes(TypeMirror ownerType, ExecutableElement setter) {
    DeclaredType declaredType = AptUtil.asDeclaredType(ownerType);
    TypeMirror declaredSetter = AptUtil.getTypeUtils().asMemberOf(declaredType, setter);
    ExecutableType method = (ExecutableType) declaredSetter;

    return method.getParameterTypes().toArray(new TypeMirror[0]);
  }

  private String initialCap(String propertyName) {
    return propertyName.substring(0, 1).toUpperCase(Locale.ROOT)
        + propertyName.substring(1);
  }

  private boolean isString(UiBinderWriter writer, TypeMirror paramType) {
    TypeElement stringType = AptUtil.getElementUtils().getTypeElement(String.class.getName());
    return AptUtil.getTypeUtils().isSameType(paramType, stringType.asType());
  }

  private String[] makeArgsList(final Map<String, String> valueMap, ExecutableElement method) {
    List<? extends VariableElement> params = method.getParameters();
    String[] args = new String[params.size()];
    int i = 0;
    for (VariableElement param : params) {
      args[i++] = valueMap.get(param.getSimpleName().toString());
    }
    return args;
  }

}
