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

import static org.gwtproject.uibinder.processor.AptUtil.asTypeElement;

import org.gwtproject.uibinder.processor.elementparsers.BeanParser;
import org.gwtproject.uibinder.processor.elementparsers.SimpleInterpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.MessagesWriter;
import org.gwtproject.uibinder.processor.model.ImplicitClientBundle;
import org.gwtproject.uibinder.processor.model.ImplicitCssResource;
import org.gwtproject.uibinder.processor.model.ImplicitDataResource;
import org.gwtproject.uibinder.processor.model.ImplicitImageResource;
import org.gwtproject.uibinder.processor.model.OwnerField;

import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.resources.rg.GssResourceGenerator.GssOptions;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Parses the root UiBinder element, and kicks of the parsing of the rest of the document.
 */
public class UiBinderParser {

  enum Resource {
    DATA {
      @Override
      void create(UiBinderParser parser, XMLElement elem)
          throws UnableToCompleteException {
        parser.createData(elem);
      }
    },
    IMAGE {
      @Override
      void create(UiBinderParser parser, XMLElement elem)
          throws UnableToCompleteException {
        parser.createImage(elem);
      }
    },
    IMPORT {
      @Override
      void create(UiBinderParser parser, XMLElement elem)
          throws UnableToCompleteException {
        parser.createImport(elem);
      }
    },
    STYLE {
      @Override
      void create(UiBinderParser parser, XMLElement elem)
          throws UnableToCompleteException {
        parser.createStyle(elem);
      }
    },
    WITH {
      @Override
      void create(UiBinderParser parser, XMLElement elem)
          throws UnableToCompleteException {
        parser.createResource(elem);
      }
    };

    abstract void create(UiBinderParser parser, XMLElement elem)
        throws UnableToCompleteException;
  }

  private static final String FLIP_RTL_ATTRIBUTE = "flipRtl";
  private static final String FIELD_ATTRIBUTE = "field";
  private static final String REPEAT_STYLE_ATTRIBUTE = "repeatStyle";
  private static final String SOURCE_ATTRIBUTE = "src";
  private static final String TYPE_ATTRIBUTE = "type";
  private static final String GSS_ATTRIBUTE = "gss";
  private static final String DO_NOT_EMBED_ATTRIBUTE = "doNotEmbed";
  private static final String MIME_TYPE_ATTRIBUTE = "mimeType";

  // TODO(rjrjr) Make all the ElementParsers receive their dependencies via
  // constructor like this one does, and make this an ElementParser. I want
  // guice!!!

  private static final String IMPORT_ATTRIBUTE = "import";
  private static final String TAG = "UiBinder";
  private final UiBinderWriter writer;

  private final MessagesWriter messagesWriter;
  private final FieldManager fieldManager;
  private final ImplicitClientBundle bundleClass;
  private final TypeMirror cssResourceType;
  private final TypeMirror imageResourceType;

  private final TypeMirror dataResourceType;
  private final String binderUri;
  private final UiBinderContext uiBinderContext;
  private GssOptions gssOptions;

  public UiBinderParser(UiBinderWriter writer, MessagesWriter messagesWriter,
      FieldManager fieldManager, ImplicitClientBundle bundleClass,
      String binderUri, UiBinderContext uiBinderContext, GssOptions gssOptions) {
    this.writer = writer;

    this.messagesWriter = messagesWriter;
    this.fieldManager = fieldManager;
    this.bundleClass = bundleClass;
    this.uiBinderContext = uiBinderContext;
    Elements elementUtils = AptUtil.getElementUtils();
    this.cssResourceType = elementUtils
        .getTypeElement(UiBinderApiPackage.current().getCssResourceFqn()).asType();
    this.imageResourceType = elementUtils
        .getTypeElement(UiBinderApiPackage.current().getImageResourceFqn()).asType();
    this.dataResourceType = elementUtils
        .getTypeElement(UiBinderApiPackage.current().getDataResourceFqn()).asType();
    this.binderUri = binderUri;
    this.gssOptions = gssOptions;
  }

  /**
   * Parses the root UiBinder element, and kicks off the parsing of the rest of the document.
   */
  public FieldWriter parse(XMLElement elem) throws UnableToCompleteException {
    if (!writer.isBinderElement(elem)) {
      writer.die(elem, "Bad prefix on <%s:%s>? The root element must be in "
              + "xml namespace \"%s\" (usually with prefix \"ui:\"), "
              + "but this has prefix \"%s\"", elem.getPrefix(),
          elem.getLocalName(), binderUri, elem.getPrefix());
    }

    if (!TAG.equals(elem.getLocalName())) {
      writer.die(elem, "Root element must be %s:%s", elem.getPrefix(), TAG);
    }

    findResources(elem);
    messagesWriter.findMessagesConfig(elem);
    XMLElement uiRoot = elem.consumeSingleChildElement();
    return writer.parseElementToField(uiRoot);
  }

  private TypeMirror consumeCssResourceType(XMLElement elem)
      throws UnableToCompleteException {
    String typeName = elem.consumeRawAttribute(TYPE_ATTRIBUTE, null);
    if (typeName == null) {
      return cssResourceType;
    }

    return findCssResourceType(elem, typeName);
  }

  private TypeElement consumeTypeAttribute(XMLElement elem)
      throws UnableToCompleteException {
    if (!elem.hasAttribute(TYPE_ATTRIBUTE)) {
      return null;
    }
    String resourceTypeName = elem.consumeRawAttribute(TYPE_ATTRIBUTE);

    TypeElement resourceType = AptUtil.getElementUtils().getTypeElement(resourceTypeName);
    if (resourceType == null) {
      writer.die(elem, "No such type %s", resourceTypeName);
    }

    return resourceType;
  }

  /**
   * Interprets <ui:data> elements.
   */
  private void createData(XMLElement elem) throws UnableToCompleteException {
    String name = elem.consumeRequiredRawAttribute(FIELD_ATTRIBUTE);
    String source = elem.consumeRequiredRawAttribute(SOURCE_ATTRIBUTE);
    // doNotEmbed is optional on DataResource
    Boolean doNotEmbed = elem.consumeBooleanConstantAttribute(DO_NOT_EMBED_ATTRIBUTE);
    // mimeType is optional on DataResource
    String mimeType = elem.consumeRawAttribute(MIME_TYPE_ATTRIBUTE);
    ImplicitDataResource dataMethod = bundleClass.createDataResource(
        name, source, mimeType, doNotEmbed);
    FieldWriter field = fieldManager.registerField(dataResourceType,
        dataMethod.getName());
    field.setInitializer(String.format("%s.%s()",
        fieldManager.convertFieldToGetter(bundleClass.getFieldName()),
        dataMethod.getName()));
  }

  /**
   * Interprets <ui:image> elements.
   */
  private void createImage(XMLElement elem) throws UnableToCompleteException {
    String name = elem.consumeRequiredRawAttribute(FIELD_ATTRIBUTE);
    // @source is optional on ImageResource
    String source = elem.consumeRawAttribute(SOURCE_ATTRIBUTE, null);

    Boolean flipRtl = elem.consumeBooleanConstantAttribute(FLIP_RTL_ATTRIBUTE);

    // FIXME - using old RepeatStyle, but really only for the valueOf..
    RepeatStyle repeatStyle = null;
    if (elem.hasAttribute(REPEAT_STYLE_ATTRIBUTE)) {
      String value = elem.consumeRawAttribute(REPEAT_STYLE_ATTRIBUTE);
      try {
        repeatStyle = RepeatStyle.valueOf(value);
      } catch (IllegalArgumentException e) {
        writer.die(elem, "Bad repeatStyle value %s", value);
      }
    }

    ImplicitImageResource imageMethod = bundleClass.createImageResource(name,
        source, flipRtl, repeatStyle);

    FieldWriter field = fieldManager.registerField(imageResourceType,
        imageMethod.getName());
    field.setInitializer(String.format("%s.%s()",
        fieldManager.convertFieldToGetter(bundleClass.getFieldName()),
        imageMethod.getName()));
  }

  /**
   * Process <code>&lt;ui:import field="com.example.Blah.CONSTANT"></code>.
   */
  private void createImport(XMLElement elem) throws UnableToCompleteException {
    String rawFieldName = elem.consumeRequiredRawAttribute(FIELD_ATTRIBUTE);
    if (elem.getAttributeCount() > 0) {
      writer.die(elem, "Should only find attribute \"%s\"", FIELD_ATTRIBUTE);
    }

    int idx = rawFieldName.lastIndexOf('.');
    if (idx < 1) {
      writer.die(elem, "Attribute %s does not look like a static import "
          + "reference", FIELD_ATTRIBUTE);
    }
    String enclosingName = rawFieldName.substring(0, idx);
    String constantName = rawFieldName.substring(idx + 1);

    TypeElement enclosingType = AptUtil.getElementUtils().getTypeElement(enclosingName);
    if (enclosingType == null) {
      writer.die(elem, "Unable to locate type %s", enclosingName);
    }

    if ("*".equals(constantName)) {
      for (VariableElement field : ElementFilter.fieldsIn(enclosingType.getEnclosedElements())) {
        Set<Modifier> fieldModifiers = field.getModifiers();
        if (!fieldModifiers.contains(Modifier.STATIC)) {
          continue;
        } else if (fieldModifiers.contains(Modifier.PUBLIC)) {
          // OK
        } else if (fieldModifiers.contains(Modifier.PROTECTED)
            || fieldModifiers.contains(Modifier.PRIVATE)) {
          continue;
          //

        } else if (!AptUtil.getTypeUtils()
            .isSameType(AptUtil.getPackageElement(enclosingType).asType(),
                AptUtil.getPackageElement(writer.getOwnerClass().getOwnerType()).asType())) {
          // package-protected in another package
          continue;
        }
        createSingleImport(elem, enclosingType.asType(), enclosingName + "."
            + field.getSimpleName().toString(), field.getSimpleName().toString());
      }
    } else {
      createSingleImport(elem, enclosingType.asType(), rawFieldName, constantName);
    }
  }

  /**
   * Interprets <ui:with> elements.
   */
  private void createResource(XMLElement elem) throws UnableToCompleteException {
    String resourceName = elem.consumeRequiredRawAttribute(FIELD_ATTRIBUTE);

    TypeElement resourceType = consumeTypeAttribute(elem);

    if (elem.getAttributeCount() > 0) {
      writer.die(elem, "Should only find attributes \"%s\" and \"%s\".", FIELD_ATTRIBUTE,
          TYPE_ATTRIBUTE);
    }

    /* Is it a parameter passed to a render method? */

    if (writer.isRenderer()) {
      TypeMirror matchingResourceType = findRenderParameterType(resourceName);
      if (matchingResourceType != null) {
        createResourceUiRenderer(elem, resourceName, resourceType.asType(), matchingResourceType);
        return;
      }
    }

    /* Perhaps it is provided via @UiField */

    if (writer.getOwnerClass() == null) {
      writer.die("No owner provided for %s",
          AptUtil.asQualifiedNameable(writer.getBaseClass()).getQualifiedName());
    }

    if (writer.getOwnerClass().getUiField(resourceName) != null) {
      // If the resourceType is present, is it the same as the one in the base class?
      OwnerField ownerField = writer.getOwnerClass().getUiField(resourceName);

      // If the resourceType was given, it must match the one declared with @UiField
      Types types = AptUtil.getTypeUtils();
      if (resourceType != null && !types.isSameType(types.erasure(resourceType.asType()),
          types.erasure(ownerField.getType().getRawType()))) {
        writer.die(elem, "Type must match %s.", ownerField);
      }

      if (ownerField.isProvided()) {
        createResourceUiField(resourceName, ownerField);
        return;
      } else {
        // Let's keep trying, but we know the type at least.
        resourceType = asTypeElement(types.erasure(ownerField.getType().getRawType()));
      }
    }

    /* Nope. If we know the type, maybe a @UiFactory will make it */

    if (resourceType != null
        && writer.getOwnerClass().getUiFactoryMethod(resourceType.asType()) != null) {
      createResourceUiFactory(elem, resourceName, resourceType.asType());
      return;
    }

    /*
     * If neither of the above, the FieldWriter's default GWT.create call will
     * do just fine.
     */
    if (resourceType != null) {
      fieldManager.registerField(FieldWriterType.IMPORTED, resourceType.asType(), resourceName);
    } else {
      writer.die(elem, "Could not infer type for field %s.", resourceName);
    }

    // process ui:attributes child for property setting
    boolean attributesChildFound = false;
    // Use consumeChildElements(Interpreter) so no assertEmpty check is performed
    for (XMLElement child : elem.consumeChildElements(new SimpleInterpreter<>(true))) {
      if (attributesChildFound) {
        writer.die(child, "<ui:with> can only contain a single <ui:attributes> child Element.");
      }
      attributesChildFound = true;

      if (!elem.getNamespaceUri().equals(child.getNamespaceUri()) || !"attributes"
          .equals(child.getLocalName())) {
        writer.die(child, "Found unknown child element.");
      }

      new BeanParser(uiBinderContext).parse(child, resourceName, resourceType.asType(), writer);
    }
  }

  private void createResourceUiFactory(XMLElement elem, String resourceName,
      TypeMirror resourceType) throws UnableToCompleteException {
    FieldWriter fieldWriter;
    ExecutableElement factoryMethod = writer.getOwnerClass().getUiFactoryMethod(resourceType);
    TypeElement methodReturnType = asTypeElement(
        AptUtil.getTypeUtils().erasure(factoryMethod.getReturnType()));
    if (!AptUtil.getTypeUtils().isSameType(resourceType, methodReturnType.asType())) {
      writer.die(elem, "Type must match %s.", methodReturnType);
    }

    String initializer;
//    if (writer.getDesignTime().isDesignTime()) {
//      String typeName = factoryMethod.getReturnType().getQualifiedSourceName();
//      initializer = writer.getDesignTime().getProvidedFactory(typeName,
//          factoryMethod.getName(), "");
//    } else {
    initializer = String.format("owner.%s()", factoryMethod.getSimpleName());
//    }
    fieldWriter = fieldManager.registerField(
        FieldWriterType.IMPORTED, resourceType, resourceName);
    fieldWriter.setInitializer(initializer);
  }

  private void createResourceUiField(String resourceName, OwnerField ownerField)
      throws UnableToCompleteException {
    FieldWriter fieldWriter;
    String initializer;

    initializer = "owner." + ownerField.getName();

    fieldWriter = fieldManager.registerField(
        FieldWriterType.IMPORTED,
        AptUtil.getTypeUtils().erasure(ownerField.getType().getRawType()),
        resourceName);
    fieldWriter.setInitializer(initializer);
  }

  private void createResourceUiRenderer(XMLElement elem, String resourceName,
      TypeMirror resourceType, TypeMirror matchingResourceType) throws UnableToCompleteException {
    // TODO implement
//    FieldWriter fieldWriter;
//    if (resourceType != null
//        && !resourceType.getErasedType().isAssignableFrom(matchingResourceType.getErasedType())) {
//      writer.die(elem, "Type must match the type of parameter %s in %s#render method.",
//          resourceName,
//          writer.getBaseClass().getQualifiedSourceName());
//    }
//
//    fieldWriter = fieldManager.registerField(
//        FieldWriterType.IMPORTED, matchingResourceType.getErasedType(), resourceName);
//    // Sets initialization as a NOOP. These fields are set from
//    // parameters passed to UiRenderer#render(), instead.
//    fieldWriter.setInitializer(resourceName);
  }

  private void createSingleImport(XMLElement elem, TypeMirror enclosingType,
      String rawFieldName, String constantName)
      throws UnableToCompleteException {
    // TODO implement
//    JField field = enclosingType.findField(constantName);
//    if (field == null) {
//      writer.die(elem, "Unable to locate a field named %s in %s", constantName,
//          enclosingType.getQualifiedSourceName());
//    } else if (!field.isStatic()) {
//      writer.die(elem, "Field %s in type %s is not static", constantName,
//          enclosingType.getQualifiedSourceName());
//    }
//
//    JType importType = field.getType();
//    JClassType fieldType;
//    if (importType instanceof JPrimitiveType) {
//      fieldType = oracle.findType(((JPrimitiveType) importType).getQualifiedBoxedSourceName());
//    } else {
//      fieldType = (JClassType) importType;
//    }
//
//    FieldWriter fieldWriter = fieldManager.registerField(
//        FieldWriterType.IMPORTED, fieldType, constantName);
//    fieldWriter.setInitializer(rawFieldName);
  }

  private void createStyle(XMLElement elem) throws UnableToCompleteException {
    String body = elem.consumeUnescapedInnerText();
    String[] source = elem.consumeRawArrayAttribute(SOURCE_ATTRIBUTE);

    if (0 == body.length() && 0 == source.length) {
      writer.die(elem, "Must have either a src attribute or body text");
    }

    String name = elem.consumeRawAttribute(FIELD_ATTRIBUTE, "style");
    TypeMirror publicType = consumeCssResourceType(elem);

    String[] importTypeNames = elem.consumeRawArrayAttribute(IMPORT_ATTRIBUTE);
    LinkedHashSet<TypeMirror> importTypes = new LinkedHashSet<>();
    for (String type : importTypeNames) {
      importTypes.add(findCssResourceType(elem, type));
    }

    boolean gss = determineGssForFile(elem.consumeBooleanConstantAttribute(GSS_ATTRIBUTE));
    ImplicitCssResource cssMethod = bundleClass.createCssResource(name, source,
        publicType, body, importTypes, gss);

    FieldWriter field = fieldManager.registerFieldForGeneratedCssResource(cssMethod);
    field.setInitializer(String.format("%s.%s()",
        fieldManager.convertFieldToGetter(bundleClass.getFieldName()),
        cssMethod.getName()));
  }

  private boolean determineGssForFile(Boolean attributeInUiBinderFile)
      throws UnableToCompleteException {
    if (attributeInUiBinderFile == null) {
      if (!gssOptions.isEnabled() && gssOptions.isGssDefaultInUiBinder()) {
        writer.die("Invalid combination of configuration properties. "
            + "CssResource.enableGss is false, but CssResource.uiBinderGssDefault is true");
      }
      return gssOptions.isGssDefaultInUiBinder();
    }

    if (Boolean.TRUE.equals(attributeInUiBinderFile)) {
      if (!gssOptions.isEnabled()) {
        writer.die("UiBinder file has attribute gss=\"true\", but GSS is disabled globally");
      }
      return true;
    }

    if (gssOptions.isEnabled() && gssOptions.isAutoConversionOff()) {
      writer.die("UiBinder file has attribute gss=\"false\", "
          + "but CssResource.conversionMode is \"off\"");
    }
    return false;
  }

  private TypeMirror findCssResourceType(XMLElement elem, String typeName)
      throws UnableToCompleteException {
    return null; // TODO implement
//    JClassType publicType = oracle.findType(typeName);
//    if (publicType == null) {
//      writer.die(elem, "No such type %s", typeName);
//    }
//
//    if (!publicType.isAssignableTo(cssResourceType)) {
//      writer.die(elem, "Type %s does not extend %s",
//          publicType.getQualifiedSourceName(),
//          cssResourceType.getQualifiedSourceName());
//    }
//    return publicType;
  }

  private TypeMirror findRenderParameterType(String resourceName) throws UnableToCompleteException {
    return null;// TODO implement
//    ExecutableElement renderMethod = null;
//    TypeMirror baseClass = writer.getBaseClass();
//    for (JMethod method : baseClass.getInheritableMethods()) {
//      if (method.getName().equals("render")) {
//        if (renderMethod == null) {
//          renderMethod = method;
//        } else {
//          writer.die("%s declares more than one method named render",
//              baseClass.getQualifiedSourceName());
//        }
//      }
//    }
//    if (renderMethod == null) {
//      return null;
//    }
//    TypeMirror matchingResourceType = null;
//    for (VariableElement jParameter : renderMethod.getParameters()) {
//      if (jParameter.getSimpleName().toString().equals(resourceName)) {
//        matchingResourceType = AptUtil.asTypeElement(jParameter.asType()).asType();
//        break;
//      }
//    }
//    return matchingResourceType;
  }

  private void findResources(XMLElement binderElement)
      throws UnableToCompleteException {
    binderElement.consumeChildElements(new XMLElement.Interpreter<Boolean>() {
      @Override
      public Boolean interpretElement(XMLElement elem)
          throws UnableToCompleteException {

        if (writer.isBinderElement(elem)) {
          try {
            Resource.valueOf(elem.getLocalName().toUpperCase(Locale.ROOT)).create(
                UiBinderParser.this, elem);
          } catch (IllegalArgumentException e) {
            writer.die(elem,
                "Unknown tag %s, or is not appropriate as a top level element",
                elem.getLocalName());
          }
          return true;
        }
        return false; // leave it be
      }
    });
  }
}
