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

import static org.gwtproject.uibinder.processor.AptUtil.asQualifiedNameable;

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.UiBinderContext;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.model.OwnerFieldClass;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

/**
 * Parses any children of widgets that use the {@code org.gwtproject.uibinder.client.UiChild}
 * annotation.
 */
public class UiChildParser implements ElementParser {

  private String fieldName;

  /**
   * Mapping of child tag to the number of times it has been called.
   */
  private Map<String, Integer> numCallsToChildMethod = new HashMap<String, Integer>();
  private Map<String, SimpleEntry<ExecutableElement, Integer>> uiChildMethods;
  private UiBinderWriter writer;
  private final UiBinderContext uiBinderCtx;

  /**
   *
   */
  public UiChildParser(UiBinderContext uiBinderCtx) {
    this.uiBinderCtx = uiBinderCtx;
  }

  public void parse(final XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    this.fieldName = fieldName;
    this.writer = writer;

    OwnerFieldClass ownerFieldClass = OwnerFieldClass.getFieldClass(type,
        writer.getLogger(), uiBinderCtx);

    uiChildMethods = ownerFieldClass.getUiChildMethods();

    // Parse children.
    elem.consumeChildElements(child -> {
      if (isValidChildElement(elem, child)) {
        handleChild(child);
        return true;
      }
      return false;
    });
  }

  /**
   * Checks if this call will go over the limit for the number of valid calls. If it won't, it will
   * increment the number of calls made.
   */
  private void checkLimit(int limit, String tag, XMLElement toAdd)
      throws UnableToCompleteException {
    Integer priorCalls = numCallsToChildMethod.get(tag);
    if (priorCalls == null) {
      priorCalls = 0;
    }
    if (limit > 0 && priorCalls > 0 && priorCalls + 1 > limit) {
      writer.die(toAdd, "Can only use the @UiChild tag " + tag + " " + limit
          + " time(s).");
    }
    numCallsToChildMethod.put(tag, priorCalls + 1);
  }

  private TypeMirror getFirstParamType(ExecutableElement method) {
    VariableElement variableElement = method.getParameters().get(0);
    TypeElement typeElement = AptUtil.asTypeElement(variableElement.asType());
    return typeElement != null ? typeElement.asType() : null;
  }

  /**
   * Process a child element that should be added using a UiChild method.
   */
  private void handleChild(XMLElement child) throws UnableToCompleteException {
    String tag = child.getLocalName();
    SimpleEntry<ExecutableElement, Integer> methodPair = uiChildMethods.get(tag);
    ExecutableElement method = methodPair.getKey();
    int limit = methodPair.getValue();
    Iterator<XMLElement> children = child.consumeChildElements().iterator();

    // If the UiChild tag has no children just return.
    if (!children.hasNext()) {
      return;
    }
    XMLElement toAdd = children.next();

    if (!writer.isImportedElement(toAdd)) {
      writer.die(child, "Expected child from a urn:import namespace, found %s",
          toAdd);
    }

    TypeMirror paramClass = getFirstParamType(method);
    if (!writer.isElementAssignableTo(toAdd, paramClass)) {
      writer.die(child, "Expected child of type %s in %s, found %s",
          asQualifiedNameable(paramClass).getQualifiedName(), child, toAdd);
    }

    // Make sure that there is only one element per tag.
    if (children.hasNext()) {
      writer.die(toAdd, "Can only have one element per @UiChild parser tag.");
    }

    // Check that this element won't put us over the limit.
    checkLimit(limit, tag, toAdd);

    // Add the child using the @UiChild function
    String[] parameters = makeArgsList(child, method, toAdd);

    writer.addStatement("%1$s.%2$s(%3$s);", fieldName, method.getSimpleName(),
        UiBinderWriter.asCommaSeparatedList(parameters));
  }

  private boolean isValidChildElement(XMLElement parent, XMLElement child) {
    if (child != null && child.getNamespaceUri() != null
        && child.getNamespaceUri().equals(parent.getNamespaceUri())
        && uiChildMethods.containsKey(child.getLocalName())) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Go through all of the given method's required parameters and consume them from the given
   * element's attributes. If a parameter is not present in the element, it will be passed null.
   * Unexpected attributes are an error.
   *
   * @param element The element to find the necessary attributes for the parameters to the method.
   * @param method The method to gather parameters for.
   * @return The list of parameters to send to the function.
   */
  private String[] makeArgsList(XMLElement element, ExecutableElement method, XMLElement toAdd)
      throws UnableToCompleteException {
    List<? extends VariableElement> params = method.getParameters();
    String[] args = new String[params.size()];
    args[0] = writer.parseElementToField(toAdd).getNextReference();

    // First parameter is the child widget
    for (int index = 1; index < params.size(); index++) {
      VariableElement param = params.get(index);
      String defaultValue = null;

      if (param.asType().getKind().isPrimitive()) {
        PrimitiveType primitiveType = (PrimitiveType) param.asType();
        defaultValue = AptUtil.getUninitializedFieldExpression(primitiveType);
      }
      String value = element.consumeAttributeWithDefault(param.getSimpleName().toString(),
          defaultValue, param.asType());
      args[index] = value;
    }

    if (element.getAttributeCount() > 0) {
      writer.die(element, "Unexpected attributes");
    }
    return args;
  }
}
