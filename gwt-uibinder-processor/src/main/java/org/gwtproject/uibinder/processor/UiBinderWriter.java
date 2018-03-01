package org.gwtproject.uibinder.processor;

import static org.gwtproject.uibinder.processor.AptUtil.asQualifiedNameable;
import static org.gwtproject.uibinder.processor.AptUtil.getPackageElement;

import org.gwtproject.uibinder.processor.attributeparsers.AttributeParsers;
import org.gwtproject.uibinder.processor.elementparsers.ElementParser;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.MessagesWriter;
import org.gwtproject.uibinder.processor.model.HtmlTemplatesWriter;
import org.gwtproject.uibinder.processor.model.ImplicitClientBundle;
import org.gwtproject.uibinder.processor.model.ImplicitCssResource;
import org.gwtproject.uibinder.processor.model.OwnerClass;
import org.gwtproject.uibinder.processor.model.OwnerField;

import com.google.gwt.uibinder.client.impl.AbstractUiRenderer;
import com.google.gwt.user.client.ui.IsRenderable;
import com.google.gwt.user.client.ui.IsWidget;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class UiBinderWriter {

  private static final String SAFE_VAR_PREFIX =
      "somethingUnlikelyToCollideWithParamNamesWefio";

  private static final String UI_RENDERER_DISPATCHER_PREFIX = "UiRendererDispatcherFor";

  private static final String PACKAGE_URI_SCHEME = "urn:import:";

  // TODO(rjrjr) Another place that we need a general anonymous field
  // mechanism
  private static final String CLIENT_BUNDLE_FIELD =
      "clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay";

  public static String asCommaSeparatedList(String... args) {
    StringBuilder b = new StringBuilder();
    for (String arg : args) {
      if (b.length() > 0) {
        b.append(", ");
      }
      b.append(arg);
    }

    return b.toString();
  }


  /**
   * Escape text that will be part of a string literal to be interpreted at runtime as an HTML
   * attribute value.
   */
  public static String escapeAttributeText(String text) {
    text = escapeText(text, false);

    /*
     * Escape single-quotes to make them safe to be interpreted at runtime as an
     * HTML attribute value (for which we by convention use single quotes).
     */
    text = text.replaceAll("'", "&#39;");
    return text;
  }

  /**
   * Escape text that will be part of a string literal to be interpreted at runtime as HTML,
   * optionally preserving whitespace.
   */
  public static String escapeText(String text, boolean preserveWhitespace) {
    // Replace reserved XML characters with entities. Note that we *don't*
    // replace single- or double-quotes here, because they're safe in text
    // nodes.
    text = text.replaceAll("&", "&amp;");
    text = text.replaceAll("<", "&lt;");
    text = text.replaceAll(">", "&gt;");

    if (!preserveWhitespace) {
      text = text.replaceAll("\\s+", " ");
    }

    return escapeTextForJavaStringLiteral(text);
  }

  /**
   * Escape characters that would mess up interpretation of this string as a string literal in
   * generated code (that is, protect \, \n and " ).
   */
  public static String escapeTextForJavaStringLiteral(String text) {
    text = text.replace("\\", "\\\\");
    text = text.replace("\"", "\\\"");
    text = text.replace("\n", "\\n");

    return text;
  }

  /**
   * Returns a list of the given type and all its superclasses and implemented interfaces in a
   * breadth-first traversal.
   *
   * @param typeElement the base type
   * @return a breadth-first collection of its type hierarchy
   */
  static Iterable<TypeElement> getClassHierarchyBreadthFirst(TypeElement typeElement) {
    LinkedList<TypeElement> list = new LinkedList<>();
    LinkedList<TypeElement> q = new LinkedList<>();

    q.add(typeElement);
    while (!q.isEmpty()) {
      // Pop the front of the queue and add it to the result list.
      TypeElement curType = q.removeFirst();
      list.add(curType);

      // Add implemented interfaces to the back of the queue (breadth first,
      // remember?)

      for (TypeMirror intf : curType.getInterfaces()) {
        q.add(AptUtil.asTypeElement(intf));
      }
      // Add then add superclasses
      TypeMirror superClass = curType.getSuperclass();
      if (superClass != null) {
        q.add(AptUtil.asTypeElement(superClass));
      }
    }

    return list;
  }


  private final MortalLogger logger;

  /**
   * Class names of parsers for various ui types, keyed by the classname of the UI class they can
   * build.
   */
  private final Map<String, String> elementParsers = new HashMap<>();

  private final HandlerEvaluator handlerEvaluator;


  private final Tokenator tokenator = new Tokenator();

  private final TypeMirror baseClass;
  private final String implClassName;
  private final String templatePath;
  private final FieldManager fieldManager;

  private final HtmlTemplatesWriter htmlTemplates;

  private final ImplicitClientBundle bundleClass;

  private final MessagesWriter messages;
  private final UiBinderContext uiBinderCtx;
  private final String binderUri;

  private final TypeMirror uiRootType;
  private final TypeMirror uiOwnerType;
  private final TypeMirror isRenderableClassType;
  private final boolean isRenderer;
  private final AttributeParsers attributeParsers;
  private final OwnerClass ownerClass;

  private int domId = 0;

  private int fieldIndex;

  private String gwtPrefix;

  private String rendered;


  public UiBinderWriter(TypeMirror baseType, String implClassName, String templatePath,
      MortalLogger logger, FieldManager fieldManager, MessagesWriter messagesWriter,
      UiBinderContext uiBinderCtx, String binderUri) throws UnableToCompleteException {

    this.baseClass = baseType;
    this.implClassName = implClassName;
    this.logger = logger;
    this.templatePath = templatePath;
    this.fieldManager = fieldManager;
    this.messages = messagesWriter;
    this.uiBinderCtx = uiBinderCtx;
    this.binderUri = binderUri;

    this.htmlTemplates = new HtmlTemplatesWriter(fieldManager, logger);

    Types typeUtils = AptUtil.getTypeUtils();

    TypeElement uiBinderItself = AptUtil.getElementUtils()
        .getTypeElement(UiBinderClasses.UIBINDER);

    TypeElement baseTypeElement = AptUtil.asTypeElement(baseType);
    List<? extends TypeMirror> uiBinderTypeMirrors = baseTypeElement.getInterfaces();
    if (uiBinderTypeMirrors.isEmpty()) {
      throw new RuntimeException(
          "No implemented interfaces for " + baseTypeElement.getQualifiedName().toString());
    }

    TypeMirror uiBinderTypeMirror = uiBinderTypeMirrors.get(0);
    TypeMirror uiBinderTypeErasure = typeUtils.erasure(uiBinderTypeMirror);
    List<? extends TypeMirror> typeArgs = AptUtil.getTypeArguments(uiBinderTypeMirror);

    String binderType = AptUtil.asQualifiedNameable(uiBinderTypeMirror).getQualifiedName()
        .toString();

    TypeElement uiRendererElement = AptUtil.getElementUtils()
        .getTypeElement(UiBinderClasses.UIRENDERER);
    if (typeUtils.isAssignable(uiBinderTypeErasure, uiBinderItself.asType())) {
      if (typeArgs.size() < 2) {
        throw new RuntimeException("Root and owner type parameters are required for type %s"
            + binderType);
      }
      uiRootType = typeArgs.get(0);
      uiOwnerType = typeArgs.get(1);
      isRenderer = false;
    } else if (typeUtils.isAssignable(uiBinderTypeErasure, uiRendererElement.asType())) {
      if (typeArgs.size() >= 1) {
        throw new RuntimeException("UiRenderer is not a parameterizable type in " + binderType);
      }

      /*
      TODO implement useSafeHtmlTemplates
      if (!useSafeHtmlTemplates) {
        die("Configuration property UiBinder.useSafeHtmlTemplates\n"
            + "  must be set to true to generate a UiRenderer");
      }
      */
      uiOwnerType = uiBinderTypeMirror;
      uiRootType = null;
      isRenderer = true;
    } else {
      die(baseTypeElement.getQualifiedName() + " must implement UiBinder or UiRenderer");
      // This is unreachable in practice, but silences not initialized errors
      throw new RuntimeException();
    }

    isRenderableClassType = AptUtil.getElementUtils()
        .getTypeElement(IsRenderable.class.getCanonicalName()).asType();

    ownerClass = new OwnerClass(uiOwnerType, logger, uiBinderCtx);
    bundleClass =
        new ImplicitClientBundle(getPackageElement(baseType).getQualifiedName().toString(),
            this.implClassName, CLIENT_BUNDLE_FIELD, logger);
    handlerEvaluator = new HandlerEvaluator(uiOwnerType, logger);

    attributeParsers = new AttributeParsers(fieldManager, logger);
  }

  /**
   * Declare a variable that will be filled at runtime with a unique id, safe for use as a dom
   * element's id attribute. For {@code UiRenderer} based code, elements corresponding to a
   * ui:field, need and id initialized to a value that depends on the {@code fieldName}. For all
   * other cases let {@code fieldName} be {@code null}.
   *
   * @param fieldName name of the field corresponding to this variable.
   * @return that variable's name.
   */
  public String declareDomIdHolder(String fieldName) throws UnableToCompleteException {
    String domHolderName = "domId" + domId++;
    FieldWriter domField =
        fieldManager.registerField(FieldWriterType.DOM_ID_HOLDER,
            AptUtil.getElementUtils().getTypeElement(String.class.getName()).asType(),
            domHolderName);
    if (isRenderer && fieldName != null) {
      domField.setInitializer("buildInnerId(\"" + fieldName + "\", uiId)");
    } else {
      domField.setInitializer("com.google.gwt.dom.client.Document.get().createUniqueId()");
    }

    return domHolderName;
  }


  /**
   * Post an error message and halt processing. This method always throws an {@link
   * UnableToCompleteException}
   */
  public void die(String message) throws UnableToCompleteException {
    logger.die(message);
  }

  /**
   * Post an error message and halt processing. This method always throws an {@link
   * UnableToCompleteException}
   */
  public void die(String message, Object... params) throws UnableToCompleteException {
    logger.die(message, params);
  }

  /**
   * Post an error message about a specific XMLElement and halt processing. This method always
   * throws an {@link UnableToCompleteException}
   */
  public void die(XMLElement context, String message, Object... params)
      throws UnableToCompleteException {
    logger.die(context, message, params);
  }


  /**
   * Finds the JClassType that corresponds to this XMLElement, which must be a Widget or an
   * Element.
   *
   * @throws UnableToCompleteException If no such widget class exists
   * @throws RuntimeException if asked to handle a non-widget, non-DOM element
   */
  public TypeMirror findFieldType(XMLElement elem) throws UnableToCompleteException {
    String tagName = elem.getLocalName();

    if (!isImportedElement(elem)) {
      return null;// FIXME - findDomElementTypeForTag(tagName);
    }

    String ns = elem.getNamespaceUri();
    String packageName = ns.substring(PACKAGE_URI_SCHEME.length());
    String className = tagName;

    while (true) {
      TypeElement rtn = AptUtil.getElementUtils().getTypeElement(packageName + "." + className);
      if (rtn != null) {
        return rtn.asType();
      }

      // Try again: shift one element of the class name onto the package name.
      // If the class name has only one element left, fail.
      int index = className.indexOf(".");
      if (index == -1) {
        die(elem, "No class matching \"%s\" in %s", tagName, ns);
      }
      packageName = packageName + "." + className.substring(0, index);
      className = className.substring(index + 1);
    }
  }

  public TypeMirror getBaseClass() {
    return baseClass;
  }

  public OwnerClass getOwnerClass() {
    return ownerClass;
  }

  public String getUiFieldAttributeName() {
    return gwtPrefix + ":field";
  }

  public boolean isBinderElement(XMLElement elem) {
    String uri = elem.getNamespaceUri();
    return uri != null && binderUri.equals(uri);
  }

  public boolean isElementAssignableTo(XMLElement elem, Class<?> possibleSuperclass)
      throws UnableToCompleteException {
    TypeElement classType = AptUtil.getElementUtils()
        .getTypeElement(possibleSuperclass.getCanonicalName());
    return isElementAssignableTo(elem, classType);
  }

  public boolean isElementAssignableTo(XMLElement elem, TypeElement possibleSupertype)
      throws UnableToCompleteException {

    return false;

    // TODO update
//    /*
//     * Things like <W extends IsWidget & IsPlaid>
//     */
//    JTypeParameter typeParameter = possibleSupertype.isTypeParameter();
//    if (typeParameter != null) {
//      JClassType[] bounds = typeParameter.getBounds();
//      for (JClassType bound : bounds) {
//        if (!isElementAssignableTo(elem, bound)) {
//          return false;
//        }
//      }
//      return true;
//    }
//
//    /*
//     * Binder fields are always declared raw, so we're cheating if the user is
//     * playing with parameterized types. We're happy enough if the raw types
//     * match, and rely on them to make sure the specific types really do work.
//     */
//    JParameterizedType parameterized = possibleSupertype.isParameterized();
//    if (parameterized != null) {
//      return isElementAssignableTo(elem, parameterized.getRawType());
//    }
//
//    JClassType fieldtype = findFieldType(elem);
//    if (fieldtype == null) {
//      return false;
//    }
//    return fieldtype.isAssignableTo(possibleSupertype);
//  }
  }

  public boolean isImportedElement(XMLElement elem) {
    String uri = elem.getNamespaceUri();
    return uri != null && uri.startsWith(PACKAGE_URI_SCHEME);
  }

  public boolean isRenderableElement(XMLElement elem) throws UnableToCompleteException {
    return AptUtil.isAssignableTo(findFieldType(elem),
        isRenderableClassType);//  findFieldType(elem).isAssignableTo(isRenderableClassType);
  }

  public boolean isRenderer() {
    return isRenderer;
  }

  public boolean isWidgetElement(XMLElement elem) throws UnableToCompleteException {
    return isElementAssignableTo(elem, IsWidget.class);
  }

  /**
   * Parses the object associated with the specified element, and returns the field writer that will
   * hold it. The element is likely to make recursive calls back to this method to have its children
   * parsed.
   *
   * @param elem the xml element to be parsed
   * @return the field holder just created
   */
  public FieldWriter parseElementToField(XMLElement elem) throws UnableToCompleteException {
    if (elementParsers.isEmpty()) {
      registerParsers();
    }

    // Get the class associated with this element.
    TypeElement type = AptUtil.asTypeElement(findFieldType(elem));

    // Declare its field.
    FieldWriter field = declareField(elem, type.getQualifiedName().toString());

    /*
     * Push the field that will hold this widget on top of the parsedFieldStack
     * to ensure that fields registered by its parsers will be noted as
     * dependencies of the new widget. (See registerField.) Also push the
     * element being parsed, so that the fieldManager can hold that info for
     * later error reporting when field reference left hand sides are validated.
     */
    fieldManager.push(elem, field);

    // Give all the parsers a chance to generate their code.
    for (ElementParser parser : getParsersForClass(type.asType())) {
      parser.parse(elem, field.getName(), type.asType(), this);
    }
    fieldManager.pop();

    return field;
  }

  /**
   * Post a warning message.
   */
  public void warn(String message) {
    logger.warn(message);
  }

  /**
   * Post a warning message.
   */
  public void warn(String message, Object... params) {
    logger.warn(message, params);
  }

  /**
   * Post a warning message.
   */
  public void warn(XMLElement context, String message, Object... params) {
    logger.warn(context, message, params);
  }

  /**
   * Entry point for the code generation logic. It generates the implementation's superstructure,
   * and parses the root widget (leading to all of its children being parsed as well).
   *
   * @param doc TODO
   */
  void parseDocument(Document doc, PrintWriter printWriter) throws UnableToCompleteException {
    Element documentElement = doc.getDocumentElement();
    gwtPrefix = documentElement.lookupPrefix(binderUri);

    XMLElement elem = new XMLElementProviderImpl(attributeParsers, logger)
        .get(documentElement);
    this.rendered = tokenator.detokenate(parseDocumentElement(elem));
    printWriter.print(rendered);
  }

  private void addElementParser(String gwtClass, String parser) {
    elementParsers.put(gwtClass, parser);
  }

  private void addWidgetParser(String className) {
    String gwtClass = "com.google.gwt.user.client.ui." + className;
    String parser = "org.gwtproject.uibinder.processor.elementparsers." + className + "Parser";
    addElementParser(gwtClass, parser);
  }

  /**
   * Declares a field of the given type name, returning the name of the declared field. If the
   * element has a field or id attribute, use its value. Otherwise, create and return a new, private
   * field name for it.
   */
  private FieldWriter declareField(XMLElement source, String typeName)
      throws UnableToCompleteException {
    TypeElement type = AptUtil.getElementUtils().getTypeElement(typeName);
    if (type == null) {
      die(source, "Unknown type %s", typeName);
    }

    String fieldName = getFieldName(source);
    if (fieldName == null) {
      // TODO(rjrjr) could collide with user declared name, as is
      // also a worry in HandlerEvaluator. Need a general scheme for
      // anonymous fields. See the note in HandlerEvaluator and do
      // something like that, but in FieldManager.
      fieldName = "f_" + source.getLocalName() + ++fieldIndex;
    }
    fieldName = normalizeFieldName(fieldName);
    return fieldManager.registerField(type.asType(), fieldName);
  }

  /**
   * Add call to {@code com.google.gwt.resources.client.CssResource#ensureInjected()} on each CSS
   * resource field.
   */
  private void ensureInjectedCssFields() {
    for (ImplicitCssResource css : bundleClass.getCssMethods()) {
      String fieldName = css.getName();
      FieldWriter cssField = fieldManager.require(fieldName);
      cssField.addStatement("%s.ensureInjected();", fieldName);
    }
  }

  /**
   * Evaluate whether all @UiField attributes are also defined in the template. Dies if not.
   */
  private void evaluateUiFields() throws UnableToCompleteException {
    for (OwnerField ownerField : getOwnerClass().getUiFields()) {
      String fieldName = ownerField.getName();
      FieldWriter fieldWriter = fieldManager.lookup(fieldName);

      if (fieldWriter == null) {
        die("Template %s has no %s attribute for %s.%s#%s", templatePath,
            getUiFieldAttributeName(), getPackageElement(uiOwnerType).getSimpleName(),
            asQualifiedNameable(uiOwnerType),
            fieldName);
      }
    }
  }

  /**
   * Inspects this element for a gwt:field attribute. If one is found, the attribute is consumed and
   * its value returned.
   *
   * @return The field name declared by an element, or null if none is declared
   */
  private String getFieldName(XMLElement elem) throws UnableToCompleteException {
    String fieldName = null;
    boolean hasOldSchoolId = false;
    if (elem.hasAttribute("id") && isWidgetElement(elem)) {
      hasOldSchoolId = true;
      // If an id is specified on the element, use that.
      fieldName = elem.consumeRawAttribute("id");
      warn(elem, "Deprecated use of id=\"%1$s\" for field name. "
              + "Please switch to gwt:field=\"%1$s\" instead. "
              + "This will soon be a compile error!",
          fieldName);
    }
    if (elem.hasAttribute(getUiFieldAttributeName())) {
      if (hasOldSchoolId) {
        die(elem, "Cannot declare both id and field on the same element");
      }
      fieldName = elem.consumeRawAttribute(getUiFieldAttributeName());
    }
    return fieldName;
  }

  /**
   * Given a DOM tag name, return the corresponding JSO subclass.
   * TODO implement. need subclasses?
   * /
   private JClassType findDomElementTypeForTag(String tag) {
   TypeElement elementClass = AptUtil.getElementUtils()
   .getTypeElement("com.google.gwt.dom.client.Element");



   JClassType[] types = elementClass.getSubtypes();
   for (JClassType type : types) {
   TagName annotation = type.getAnnotation(TagName.class);
   if (annotation != null) {
   for (String annotationTag : annotation.value()) {
   if (annotationTag.equals(tag)) {
   return type;
   }
   }
   }
   }
   return elementClass;
   }
   */


  /**
   * Find a set of element parsers for the given ui type.
   *
   * The list of parsers will be returned in order from most- to least-specific.
   */
  private Iterable<ElementParser> getParsersForClass(TypeMirror type) {
    List<ElementParser> parsers = new ArrayList<ElementParser>();
    // TODO
//    /*
//     * Let this non-widget parser go first (it finds <m:attribute/> elements).
//     * Any other such should land here too.
//     *
//     * TODO(rjrjr) Need a scheme to associate these with a namespace uri or
//     * something?
//     */
//    parsers.add(new AttributeMessageParser());
//    parsers.add(new UiChildParser(uiBinderCtx));
//
//    for (JClassType curType : getClassHierarchyBreadthFirst(type)) {
//      try {
//        Class<? extends ElementParser> cls = getParserForClass(curType);
//        if (cls != null) {
//          ElementParser parser = cls.newInstance();
//          parsers.add(parser);
//        }
//      } catch (InstantiationException e) {
//        throw new RuntimeException("Unable to instantiate " + curType.getName(), e);
//      } catch (IllegalAccessException e) {
//        throw new RuntimeException("Unable to instantiate " + curType.getName(), e);
//      }
//    }
//
//    parsers.add(new BeanParser(uiBinderCtx));
//    parsers.add(new IsEmptyParser());

    return parsers;
  }

  private String normalizeFieldName(String fieldName) {
    // If a field name has a '.' in it, replace it with '$' to make it a legal
    // identifier. This can happen with the field names associated with nested
    // classes.
    return fieldName.replace('.', '$');
  }

  /**
   * Parse the document element and return the source of the Java class that will implement its
   * UiBinder.
   */
  private String parseDocumentElement(XMLElement elem) throws UnableToCompleteException {
    //TODO fieldManager.registerFieldOfGeneratedType();

    FieldWriter rootField = new UiBinderParser(this, messages, fieldManager, bundleClass,
        binderUri, uiBinderCtx).parse(elem);

    StringWriter stringWriter = new StringWriter();
    IndentedWriter niceWriter = new IndentedWriter(new PrintWriter(stringWriter));

    if (isRenderer) {
      // TODO ensureInjectedCssFields();
      //writeRenderer(niceWriter, rootField);
    } else {
      ensureInjectedCssFields();
      writeBinderForRenderableStrategy(niceWriter, rootField);
    }

    // ensureAttachmentCleanedUp();
    return stringWriter.toString();
  }

  private void registerParsers() {
    // TODO(rjrjr): Allow third-party parsers to register themselves
    // automagically

    addElementParser("com.google.gwt.dom.client.Element",
        "com.google.gwt.uibinder.elementparsers.DomElementParser");

    // Register widget parsers.
    addWidgetParser("UIObject");
    addWidgetParser("HasText");
    addWidgetParser("HasHTML");
    addWidgetParser("HasTreeItems");
    addWidgetParser("HasWidgets");
    addWidgetParser("HTMLPanel");
    addWidgetParser("FlowPanel");
    addWidgetParser("AbsolutePanel");
    addWidgetParser("DockPanel");
    addWidgetParser("StackPanel");
    addWidgetParser("DisclosurePanel");
    addWidgetParser("TabPanel");
    addWidgetParser("MenuItem");
    addWidgetParser("MenuBar");
    addWidgetParser("CellPanel");
    addWidgetParser("CustomButton");
    addWidgetParser("DialogBox");
    addWidgetParser("LayoutPanel");
    addWidgetParser("DockLayoutPanel");
    addWidgetParser("StackLayoutPanel");
    addWidgetParser("TabLayoutPanel");
    addWidgetParser("Image");
    addWidgetParser("ListBox");
    addWidgetParser("Grid");
    addWidgetParser("HasAlignment");
    addWidgetParser("DateLabel");
    addWidgetParser("NumberLabel");
    //if (useLazyWidgetBuilders) {
    addWidgetParser("LazyPanel");
    addWidgetParser("RenderablePanel");
    //}
  }


  /**
   * Writes a different optimized UiBinder's source for the renderable strategy.
   */
  private void writeBinderForRenderableStrategy(IndentedWriter w, FieldWriter rootField)
      throws UnableToCompleteException {
    writePackage(w);

    writeImports(w);
    w.newline();

    writeClassOpen(w);
    writeStatics(w);
    w.newline();

    writeTemplatesInterface(w);
    w.newline();

    // createAndBindUi method
    w.write("public %s createAndBindUi(final %s owner) {",
        asQualifiedNameable(uiRootType),
        asQualifiedNameable(uiOwnerType));
    w.indent();
    w.newline();

//    designTime.writeAttributes(this);
    w.newline();

    w.write("return new Widgets(owner).%s;", rootField.getNextReference());
    w.outdent();
    w.write("}");

    // Writes the inner class Widgets.
    w.newline();
    w.write("/**");
    w.write(" * Encapsulates the access to all inner widgets");
    w.write(" */");
    w.write("class Widgets {");
    w.indent();

    String ownerClassType = asQualifiedNameable(uiOwnerType).getQualifiedName().toString();
    w.write("private final %s owner;", ownerClassType);
    w.newline();

    writeHandlers(w);
    w.newline();

    w.write("public Widgets(final %s owner) {", ownerClassType);
    w.indent();
    w.write("this.owner = owner;");
    fieldManager.initializeWidgetsInnerClass(w, getOwnerClass());
    w.outdent();
    w.write("}");
    w.newline();

    htmlTemplates.writeTemplateCallers(w);

    evaluateUiFields();

    fieldManager.writeFieldDefinitions(w, getOwnerClass());

    w.outdent();
    w.write("}");

    // Close class
    w.outdent();
    w.write("}");
  }

  // FIXME --##*(#*(#*(#*(#*(#*(#*(#*(#(*#(*##################

  private void writeClassOpen(IndentedWriter w) {

    if (!isRenderer) {
      w.write("public class %s implements UiBinder<%s, %s>, %s {", implClassName,
          asQualifiedNameable(uiRootType),//.getParameterizedQualifiedSourceName(),
          asQualifiedNameable(uiOwnerType),//.getParameterizedQualifiedSourceName(),
          asQualifiedNameable(baseClass)); //.getParameterizedQualifiedSourceName());
    } else {
      w.write("public class %s extends %s implements %s {", implClassName,
          AbstractUiRenderer.class.getName(),
          asQualifiedNameable(baseClass));//.getParameterizedQualifiedSourceName());
    }
    w.indent();
  }
//
//  private void writeCssInjectors(IndentedWriter w) {
//    for (ImplicitCssResource css : bundleClass.getCssMethods()) {
//      w.write("%s.%s().ensureInjected();", bundleClass.getFieldName(), css.getName());
//    }
//    w.newline();
//  }
//
//  /**
//   * Write declarations for variables or fields to hold elements declared with gwt:field in the
//   * template. For those that have not had constructor generation suppressed, emit GWT.create()
//   * calls instantiating them (or die if they have no default constructor).
//   *
//   * @throws UnableToCompleteException on constructor problem
//   */
//  private void writeGwtFields(IndentedWriter niceWriter) throws UnableToCompleteException {
//    // For each provided field in the owner class, initialize from the owner
//    Collection<OwnerField> ownerFields = getOwnerClass().getUiFields();
//    for (OwnerField ownerField : ownerFields) {
//      if (ownerField.isProvided()) {
//        String fieldName = ownerField.getName();
//        FieldWriter fieldWriter = fieldManager.lookup(fieldName);
//
//        // TODO why can this be null?
//        if (fieldWriter != null) {
//          String initializer;
//          if (designTime.isDesignTime()) {
//            String typeName = ownerField.getType().getRawType().getQualifiedSourceName();
//            initializer = designTime.getProvidedField(typeName, ownerField.getName());
//          } else {
//            initializer = formatCode("owner.%1$s", fieldName);
//          }
//          fieldManager.lookup(fieldName).setInitializer(initializer);
//        }
//      }
//    }
//
//    fieldManager.writeGwtFieldsDeclaration(niceWriter);
//  }

  private void writeHandlers(IndentedWriter w) throws UnableToCompleteException {
    handlerEvaluator.run(w, fieldManager, "owner");
  }

  private void writeImports(IndentedWriter w) {
    w.write("import com.google.gwt.core.client.GWT;");
    w.write("import com.google.gwt.dom.client.Element;");
    if (!(htmlTemplates.isEmpty())) {
      w.write("import com.google.gwt.safehtml.client.SafeHtmlTemplates;");
      w.write("import com.google.gwt.safehtml.shared.SafeHtml;");
      w.write("import com.google.gwt.safehtml.shared.SafeHtmlUtils;");
      w.write("import com.google.gwt.safehtml.shared.SafeHtmlBuilder;");
      w.write("import com.google.gwt.safehtml.shared.SafeUri;");
      w.write("import com.google.gwt.safehtml.shared.UriUtils;");
      w.write("import com.google.gwt.uibinder.client.UiBinderUtil;");
    }

    if (!isRenderer) {
      w.write("import com.google.gwt.uibinder.client.UiBinder;");
      w.write("import com.google.gwt.uibinder.client.UiBinderUtil;");
      w.write("import %s.%s;", getPackageElement(uiRootType).getQualifiedName().toString(),
          asQualifiedNameable(uiRootType).getSimpleName().toString());
    } else {
      w.write("import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;");
    }
  }

//  /**
//   * Write statements created by {@link #addInitStatement}. This code must be placed after all
//   * instantiation code.
//   */
//  private void writeInitStatements(IndentedWriter niceWriter) {
//    for (String s : initStatements) {
//      niceWriter.write(s);
//    }
//  }
//
//  /**
//   * Write the statements to fill in the fields of the UI owner.
//   */
//  private void writeOwnerFieldSetters(IndentedWriter niceWriter) throws UnableToCompleteException {
//    if (designTime.isDesignTime()) {
//      return;
//    }
//    for (OwnerField ownerField : getOwnerClass().getUiFields()) {
//      String fieldName = ownerField.getName();
//      FieldWriter fieldWriter = fieldManager.lookup(fieldName);
//
//      if (fieldWriter != null) {
//        // ownerField is a widget.
//        JClassType type = fieldWriter.getInstantiableType();
//        if (type != null) {
//          maybeWriteFieldSetter(niceWriter, ownerField, fieldWriter.getInstantiableType(),
//              fieldName);
//        } else {
//          // Must be a generated type
//          if (!ownerField.isProvided()) {
//            niceWriter.write("owner.%1$s = %1$s;", fieldName);
//          }
//        }
//
//      } else {
//        // ownerField was not found as bundle resource or widget, must die.
//        die("Template %s has no %s attribute for %s.%s#%s", templatePath,
//            getUiFieldAttributeName(), uiOwnerType.getPackage().getName(), uiOwnerType.getName(),
//            fieldName);
//      }
//    }
//  }

  private void writePackage(IndentedWriter w) {
    String packageName = getPackageElement(baseClass).getQualifiedName().toString();
    if (packageName.length() > 0) {
      w.write("package %1$s;", packageName);
      w.newline();
    }
  }

//  /**
//   * Writes the UiRenderer's source for the renderable strategy.
//   */
//  private void writeRenderer(IndentedWriter w, FieldWriter rootField)
//      throws UnableToCompleteException {
//    validateRendererGetters(baseClass);
//    validateRenderParameters(baseClass);
//    JMethod[] eventMethods = findEventMethods(baseClass);
//    for (JMethod jMethod : eventMethods) {
//      validateEventMethod(jMethod);
//    }
//
//    writePackage(w);
//
//    writeImports(w);
//    w.newline();
//
//    writeClassOpen(w);
//    writeStatics(w);
//    w.newline();
//
//    // Create SafeHtml Template
//    writeTemplatesInterface(w);
//    w.newline();
//    htmlTemplates.writeTemplateCallers(w);
//
//    w.newline();
//
//    JParameter[] renderParameters = findRenderParameters(baseClass);
//    for (JParameter param : renderParameters) {
//      // Prevent fields from render() parameters from being optimized.
//      fieldManager.disableOptimization(param.getName());
//    }
//
//    // public UiRendererImplClass() {
//    w.write("public %s() {", implClassName);
//    w.indent();
//    w.write("build_fields();");
//    w.outdent();
//    // }
//    w.write("}");
//    w.newline();
//
//    // private build_fields() {
//    w.write("private void build_fields() {");
//    w.indent();
//    fieldManager.initializeWidgetsInnerClass(w, getOwnerClass());
//    w.outdent();
//    // }
//    w.write("}");
//    w.newline();
//
//    String renderParameterDeclarations = renderMethodParameters(renderParameters);
//    w.write("public void render(final %s sb%s%s) {", SafeHtmlBuilder.class.getName(),
//        renderParameterDeclarations.length() != 0 ? ", " : "", renderParameterDeclarations);
//    w.indent();
//    w.newline();
//
//    writeRenderParameterInitializers(w, renderParameters);
//
//    w.write("uiId = com.google.gwt.dom.client.Document.get().createUniqueId();");
//    w.newline();
//
//    w.write("build_fields();");
//    w.newline();
//
//    String safeHtml = rootField.getSafeHtml();
//
//    // TODO(rchandia) it should be possible to add the attribute when parsing
//    // the UiBinder file
//    w.write(
//        "sb.append(stampUiRendererAttribute(%s, RENDERED_ATTRIBUTE, uiId));",
//        safeHtml);
//    w.outdent();
//
//    w.write("}");
//    w.newline();
//
//    fieldManager.writeFieldDefinitions(w, getOracle(), getOwnerClass(), getDesignTime());
//
//    writeRendererGetters(w, baseClass, rootField.getName());
//
//    writeRendererEventMethods(w, eventMethods, rootField.getName());
//
//    // Close class
//    w.outdent();
//    w.write("}");
//  }
//
//  private void writeRendererDispatcher(IndentedWriter w, String dispatcherName,
//      JClassType targetType, String rootFieldName, JMethod[] uiHandlerMethods, JMethod sourceMethod)
//      throws UnableToCompleteException {
//
//    // static class UiRendererDispatcherForFoo extends UiRendererDispatcher<Foo> {
//    w.write("static class %s extends UiRendererDispatcher<%s> {", dispatcherName,
//        targetType.getQualifiedSourceName());
//    w.indent();
//
//    writeRendererDispatcherTableInit(w, rootFieldName, uiHandlerMethods,
//        dispatcherName);
//
//    writeRendererDispatcherExtraParameters(w, sourceMethod);
//
//    writeRendererDispatcherFire(w, sourceMethod);
//
//    w.write("@SuppressWarnings(\"rawtypes\")");
//    w.write("@Override");
//    // public void fireEvent(GwtEvent<?> somethingUnlikelyToCollideWithParamNames) {
//    w.write("public void fireEvent(com.google.gwt.event.shared.GwtEvent<?> %sEvent) {",
//        SAFE_VAR_PREFIX);
//    w.indent();
//    //   switch (getMethodIndex()) {
//    w.write("switch (getMethodIndex()) {");
//    w.indent();
//    for (int j = 0; j < uiHandlerMethods.length; j++) {
//      JMethod uiMethod = uiHandlerMethods[j];
//
//      // case 0:
//      w.write("case %s:", j);
//      w.indent();
//
//      //   getEventTarget().onClickRoot((ClickEvent) somethingUnlikelyToCollideWithParamNames,
//      //       getRoot(), a, b);
//      StringBuffer sb = new StringBuffer();
//      JParameter[] sourceParameters = sourceMethod.getParameters();
//      // Cat the extra parameters i.e. ", a, b"
//      JType[] uiHandlerParameterTypes = uiMethod.getParameterTypes();
//      if (uiHandlerParameterTypes.length >= 2) {
//        sb.append(", getRoot()");
//      }
//      for (int k = 2; k < uiHandlerParameterTypes.length; k++) {
//        JParameter sourceParam = sourceParameters[k + 1];
//        sb.append(", ");
//        sb.append(sourceParam.getName());
//      }
//      w.write("getEventTarget().%s((%s) %sEvent%s);", uiMethod.getName(),
//          uiHandlerParameterTypes[0].getQualifiedSourceName(), SAFE_VAR_PREFIX,
//          sb.toString());
//      //   break;
//      w.write("break;");
//      w.newline();
//      w.outdent();
//    }
//    //    default:
//    w.write("default:");
//    w.indent();
//    //      break;
//    w.write("break;");
//    w.outdent();
//    w.outdent();
//    w.write("}");
//
//    w.outdent();
//    w.write("}");
//
//    w.outdent();
//    w.write("}");
//  }
//
//  private void writeRendererDispatcherExtraParameters(IndentedWriter w, JMethod sourceMethod) {
//    for (int i = 3; i < sourceMethod.getParameters().length; i++) {
//      JParameter param = sourceMethod.getParameters()[i];
//
//      // private int a;
//      // private String b;
//      w.write("private %s %s;", param.getType().getParameterizedQualifiedSourceName(),
//          param.getName());
//    }
//  }
//
//  private void writeRendererDispatcherFire(IndentedWriter w, JMethod sourceMethod) {
//    // public void fire(Foo o, NativeEvent e, Element parent, int a, String b) {
//    w.write("public void fire(");
//    w.indent();
//    JParameter[] sourceParameters = sourceMethod.getParameters();
//    for (int i = 0; i < sourceParameters.length; i++) {
//      JParameter param = sourceParameters[i];
//      w.write(i == 0 ? "%s %s" : ", %s %s", param.getType().getQualifiedSourceName(),
//          param.getName());
//    }
//    w.write(") {");
//    w.indent();
//
//    // this.a = a;
//    for (int i = 3; i < sourceParameters.length; i++) {
//      JParameter sourceParam = sourceParameters[i];
//      w.write("this.%s = %s;", sourceParam.getName(), sourceParam.getName());
//    }
//
//    // fireEvent(o, e, parent);
//    w.write("fireEvent(%s, %s, %s);", sourceParameters[0].getName(), sourceParameters[1].getName(),
//        sourceParameters[2].getName());
//
//    w.outdent();
//    w.write("}");
//    w.newline();
//  }
//
//  private void writeRendererDispatcherTableInit(IndentedWriter w,
//      String rootFieldName, JMethod[] uiHandlerMethods, String dispatcherName)
//      throws UnableToCompleteException {
//    ArrayList<String> keys = new ArrayList<String>();
//    ArrayList<Integer> values = new ArrayList<Integer>();
//
//    // Collect the event types and field names to form the dispatch table
//    for (int i = 0; i < uiHandlerMethods.length; i++) {
//      JMethod jMethod = uiHandlerMethods[i];
//      String eventType = findEventTypeName(jMethod);
//      String[] fieldNames = jMethod.getAnnotation(UiHandler.class).value();
//      for (String fieldName : fieldNames) {
//        if (rootFieldName.equals(fieldName)) {
//          fieldName = AbstractUiRenderer.ROOT_FAKE_NAME;
//        }
//        keys.add(eventType + AbstractUiRenderer.UI_ID_SEPARATOR + fieldName);
//        values.add(i);
//      }
//    }
//
//    // private static String[] somethingUnlikelyToCollideWithParamNames_keys;
//    w.write("private static String[] %s_keys;", SAFE_VAR_PREFIX);
//    // private static Integer[] somethingUnlikelyToCollideWithParamNames_values;
//    w.write("private static Integer[] %s_values;", SAFE_VAR_PREFIX);
//
//    w.write("static {");
//    w.indent();
//    // private static String[] somethingUnlikelyToCollideWithParamNames_keys = new String[] {
//    w.write("%s_keys = new String[] {", SAFE_VAR_PREFIX);
//    w.indent();
//    for (String key : keys) {
//      // "click:aField",
//      w.write("\"%s\",", key);
//    }
//    w.outdent();
//    w.write("};");
//    w.newline();
//
//    // somethingUnlikelyToCollideWithParamNames_values = {0,1};
//    w.write("%s_values = new Integer[] {", SAFE_VAR_PREFIX);
//    w.indent();
//    StringBuffer commaSeparatedValues = new StringBuffer();
//    for (Integer value : values) {
//      commaSeparatedValues.append(value);
//      commaSeparatedValues.append(",");
//    }
//    // "0,0,0,1,1,",
//    w.write("%s", commaSeparatedValues.toString());
//    w.outdent();
//    w.write("};");
//    w.newline();
//
//    w.outdent();
//    w.write("}");
//    w.newline();
//
//    // public Foo() {
//    w.write("public %s() {", dispatcherName);
//    w.indent();
//    // initDispatchTable(keys, values);
//    w.write("initDispatchTable(%s_keys, %s_values);", SAFE_VAR_PREFIX, SAFE_VAR_PREFIX);
//
//    // This ensures the DomEvent#TYPE fields are properly initialized and registered
//    // ClickEvent.getType();
//    HashSet<String> eventTypes = new HashSet<String>();
//    for (JMethod uiMethod : uiHandlerMethods) {
//      eventTypes.add(uiMethod.getParameterTypes()[0].getQualifiedSourceName());
//    }
//    for (String eventType : eventTypes) {
//      w.write("%s.getType();", eventType);
//    }
//
//    w.outdent();
//    w.write("}");
//    w.newline();
//  }
//
//  private void writeRendererEventMethods(IndentedWriter w, JMethod[] eventMethods,
//      String rootField) throws UnableToCompleteException {
//    for (JMethod jMethod : eventMethods) {
//      JClassType eventTargetType = jMethod.getParameterTypes()[0].isClassOrInterface();
//      String eventTargetSimpleName = eventTargetType.getSimpleSourceName();
//      String dispatcherClassName = UI_RENDERER_DISPATCHER_PREFIX + eventTargetSimpleName;
//      JMethod[] uiHandlerMethods = findUiHandlerMethods(eventTargetType);
//
//      // public void onBrowserEvent(Foo f, NativeEvent event, Element parent, A a, B b ...) {
//      w.write("@Override");
//      w.write("public %s {", jMethod.getReadableDeclaration(true, true, true, true, true));
//
//      if (uiHandlerMethods.length != 0) {
//        w.indent();
//        //  if (singletonUiRendererDispatcherForFoo == null) {
//        w.write("if (singleton%s == null) {", dispatcherClassName);
//        w.indent();
//        // singletonUiRendererDispatcherForFoo = new UiRendererDispatcherForFoo();
//        w.write("singleton%s = new %s();", dispatcherClassName, dispatcherClassName);
//
//        w.outdent();
//        w.write("}");
//
//        // singletonUiRendererDispatcherForFoo.fire(o, event, parent, a, b);
//        StringBuffer sb = new StringBuffer();
//        JParameter[] parameters = jMethod.getParameters();
//        for (int i = 0; i < parameters.length; i++) {
//          JParameter callParam = parameters[i];
//          if (i != 0) {
//            sb.append(", ");
//          }
//          sb.append(callParam.getName());
//        }
//        w.write("singleton%s.fire(%s);", dispatcherClassName, sb.toString());
//        w.outdent();
//      }
//
//      w.write("}");
//      w.newline();
//
//      if (uiHandlerMethods.length != 0) {
//        // private static UiRendererDispatcherForFoo singletonUiRendererDispatcherForFoo;
//        w.write("private static %s singleton%s;", dispatcherClassName, dispatcherClassName);
//
//        writeRendererDispatcher(w, dispatcherClassName, eventTargetType, rootField,
//            uiHandlerMethods,
//            jMethod);
//      }
//    }
//  }
//
//  private void writeRendererGetters(IndentedWriter w, JClassType owner, String rootFieldName) {
//    List<JMethod> getters = findGetterNames(owner);
//
//    // For every requested getter
//    for (JMethod getter : getters) {
//      // public ElementSubclass getFoo(Element parent) {
//      w.write("%s {", getter.getReadableDeclaration(false, false, false, false, true));
//      w.indent();
//      String getterFieldName = getterToFieldName(getter.getName());
//      // Is this a CSS resource field?
//      FieldWriter fieldWriter = fieldManager.lookup(getterFieldName);
//      if (FieldWriterType.GENERATED_CSS.equals(fieldWriter.getFieldType())) {
//        // return (CssResourceSubclass) get_styleField;
//        w.write("return (%s) %s;", getter.getReturnType().getErasedType().getQualifiedSourceName(),
//            FieldManager.getFieldGetter(getterFieldName));
//      } else {
//        // Else the non-root elements are found by id
//        String elementParameter = getter.getParameters()[0].getName();
//        if (!getterFieldName.equals(rootFieldName)) {
//          // return (ElementSubclass) findInnerField(parent, "foo", RENDERED_ATTRIBUTE);
//          w.write("return (%s) findInnerField(%s, \"%s\", RENDERED_ATTRIBUTE);",
//              getter.getReturnType().getErasedType().getQualifiedSourceName(), elementParameter,
//              getterFieldName);
//        } else {
//          // return (ElementSubclass) findRootElement(parent);
//          w.write("return (%s) findRootElement(%s, RENDERED_ATTRIBUTE);",
//              getter.getReturnType().getErasedType().getQualifiedSourceName(), elementParameter);
//        }
//      }
//      w.outdent();
//      w.write("}");
//    }
//  }
//
//  private void writeRenderParameterInitializers(IndentedWriter w, JParameter[] renderParameters) {
//    for (int i = 0; i < renderParameters.length; i++) {
//      JParameter parameter = renderParameters[i];
//      if (fieldManager.lookup(parameter.getName()) != null) {
//        w.write("this.%s = %s;", parameter.getName(), parameter.getName());
//        w.newline();
//      }
//    }
//  }

  private void writeStaticMessagesInstance(IndentedWriter niceWriter) {
    if (messages.hasMessages()) {
      niceWriter.write(messages.getDeclaration());
    }
  }

  private void writeStatics(IndentedWriter w) {
    writeStaticMessagesInstance(w);
  }

  /**
   * Write statements created by {@link HtmlTemplatesWriter#addSafeHtmlTemplate} . This code must be
   * placed after all instantiation code.
   */
  private void writeTemplatesInterface(IndentedWriter w) {
    if (!(htmlTemplates.isEmpty())) {
      htmlTemplates.writeInterface(w);
      w.newline();
    }
  }
}
