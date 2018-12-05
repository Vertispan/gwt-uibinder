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

import org.gwtproject.uibinder.processor.attributeparsers.AttributeParser;
import org.gwtproject.uibinder.processor.attributeparsers.AttributeParsers;
import org.gwtproject.uibinder.processor.elementparsers.SimpleInterpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * A wrapper for {@link Element} that limits the way parsers can interact with the XML document, and
 * provides some convenience methods. <p> The main function of this wrapper is to ensure that
 * parsers can only read elements and attributes by 'consuming' them, which removes the given value.
 * This allows for a natural hierarchy among parsers -- more specific parsers will run first, and if
 * they consume a value, less-specific parsers will not see it.
 */
public class XMLElement {

  /**
   * Callback interface used by {@link #consumeInnerHtml(Interpreter)} and {@link
   * #consumeChildElements(Interpreter)}.
   *
   * @param <T> the interpreter type.
   */
  public interface Interpreter<T> {

    /**
     * Given an XMLElement, return its filtered value.
     *
     * @throws UnableToCompleteException on error
     */
    T interpretElement(XMLElement elem) throws UnableToCompleteException;
  }

  /**
   * Extends {@link Interpreter} with a method to be called after all elements have been processed.
   *
   * @param <T> the interpreter type.
   */
  public interface PostProcessingInterpreter<T> extends Interpreter<T> {

    String postProcess(String consumedText) throws UnableToCompleteException;
  }

  /**
   * Represents the source location where the XMLElement was declared.
   */
  public static class Location {

    private final String systemId;
    private final int lineNumber;

    public Location(String systemId, int lineNumber) {
      this.systemId = systemId;
      this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public String getSystemId() {
      return systemId;
    }

    /**
     * For debugging use only.
     */
    @Override
    public String toString() {
      return systemId + ":" + lineNumber;
    }
  }

  static final String LOCATION_KEY = "gwtLocation";

  private static final Set<String> NO_END_TAG = new HashSet<>();

  private static final String[] EMPTY = new String[]{};

  private static void clearChildren(Element elem) {
    // TODO(rjrjr) I'm nearly positive that anywhere this is called
    // we should instead be calling assertNoBody
    Node child;
    while ((child = elem.getFirstChild()) != null) {
      elem.removeChild(child);
    }
  }

  private final Element elem;
  private final AttributeParsers attributeParsers;

  private final MortalLogger logger;
  private final String debugString;

  private final XMLElementProvider provider;

  private TypeMirror booleanType;
  private TypeMirror imageResourceType;
  private TypeMirror doubleType;
  private TypeMirror intType;
  private TypeMirror safeHtmlType;
  private TypeMirror stringType;

  {
    // from com/google/gxp/compiler/schema/html.xml
    NO_END_TAG.add("area");
    NO_END_TAG.add("base");
    NO_END_TAG.add("basefont");
    NO_END_TAG.add("br");
    NO_END_TAG.add("col");
    NO_END_TAG.add("frame");
    NO_END_TAG.add("hr");
    NO_END_TAG.add("img");
    NO_END_TAG.add("input");
    NO_END_TAG.add("isindex");
    NO_END_TAG.add("link");
    NO_END_TAG.add("meta");
    NO_END_TAG.add("param");
    NO_END_TAG.add("wbr");
  }

  XMLElement(Element elem, AttributeParsers attributeParsers, MortalLogger logger,
      XMLElementProvider provider) {
    this.elem = elem;
    this.attributeParsers = attributeParsers;
    this.logger = logger;
    this.provider = provider;

    this.debugString = getOpeningTag();
  }

  /**
   * Ensure that the receiver has no attributes left.
   *
   * @throws UnableToCompleteException if it does
   */
  public void assertNoAttributes() throws UnableToCompleteException {
    int numAtts = getAttributeCount();
    if (numAtts == 0) {
      return;
    }

    StringBuilder b = new StringBuilder();
    for (int i = 0; i < numAtts; i++) {
      if (i > 0) {
        b.append(", ");
      }
      b.append('"').append(getAttribute(i).getName()).append('"');
    }
    logger.die(this, "Unexpected attributes: %s", b);
  }

  /**
   * Require that the receiver's body is empty of text and has no child nodes.
   *
   * @throws UnableToCompleteException if it isn't
   */
  public void assertNoBody() throws UnableToCompleteException {
    consumeChildElements(new Interpreter<Boolean>() {
      public Boolean interpretElement(XMLElement elem) throws UnableToCompleteException {
        logger.die(elem, "Found unexpected child element");
        return false; // unreachable
      }
    });
    assertNoText();
  }

  /**
   * Require that the receiver's body is empty of text.
   *
   * @throws UnableToCompleteException if it isn't
   */
  public void assertNoText() throws UnableToCompleteException {
    SimpleInterpreter<String> nullInterpreter = new SimpleInterpreter<String>(null);
    String s = consumeInnerTextEscapedAsHtmlStringLiteral(nullInterpreter);
    if (!"".equals(s)) {
      logger.die(this, "Unexpected text in element: \"%s\"", s);
    }
  }

  /**
   * Consumes the given attribute as a literal or field reference. The type parameter is required to
   * determine how the value is parsed and validated.
   *
   * @param name the attribute's full name (including prefix)
   * @param type the type this attribute is expected to provide
   * @return the attribute's value as a Java expression, or null if it is not set
   * @throws UnableToCompleteException on parse failure
   */
  public String consumeAttribute(String name, TypeMirror type) throws UnableToCompleteException {
    return consumeAttributeWithDefault(name, null, type);
  }

  /**
   * Consumes the given attribute as a literal or field reference. The type parameter is required to
   * determine how the value is parsed and validated.
   *
   * @param name the attribute's full name (including prefix)
   * @param defaultValue the value to @return if the attribute was unset
   * @param type the type this attribute is expected to provide
   * @return the attribute's value as a Java expression, or the given default if it was unset
   * @throws UnableToCompleteException on parse failure
   */
  public String consumeAttributeWithDefault(String name, String defaultValue, TypeMirror type)
      throws UnableToCompleteException {
    return consumeAttributeWithDefault(name, defaultValue, new TypeMirror[]{type});
  }

  /**
   * Like {@link #consumeAttributeWithDefault(String, String, TypeMirror)}, but accommodates more
   * complex type signatures.
   */
  public String consumeAttributeWithDefault(String name, String defaultValue, TypeMirror... types)
      throws UnableToCompleteException {

    if (!hasAttribute(name)) {
      return defaultValue;
    }

    AttributeParser parser = attributeParsers.getParser(types);
    return consumeAttributeWithParser(name, parser);
  }

  /**
   * Convenience method for parsing the named attribute as a boolean value or reference.
   *
   * @return an expression that will evaluate to a boolean value in the generated code, or null if
   * there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeBooleanAttribute(String name) throws UnableToCompleteException {
    return consumeAttribute(name, getBooleanType());
  }

  /**
   * Convenience method for parsing the named attribute as a boolean value or reference.
   *
   * @param defaultValue value to return if attribute was not set
   * @return an expression that will evaluate to a boolean value in the generated code, or
   * defaultValue if there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeBooleanAttribute(String name, boolean defaultValue)
      throws UnableToCompleteException {
    return consumeAttributeWithDefault(name, Boolean.toString(defaultValue), getBooleanType());
  }

  /**
   * Consumes the named attribute as a boolean expression. This will not accept {field.reference}
   * expressions. Useful for values that must be resolved at compile time, such as generated
   * annotation values.
   *
   * @return {@link Boolean#TRUE}, {@link Boolean#FALSE}, or null if no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public Boolean consumeBooleanConstantAttribute(String name) throws UnableToCompleteException {
    String value = consumeRawAttribute(name);
    if (value == null) {
      return null;
    }
    if (value.equals("true") || value.equals("false")) {
      return Boolean.valueOf(value);
    }
    logger.die(this, "%s must be \"true\" or \"false\"", name);
    return null; // unreachable
  }

  /**
   * Consumes and returns all child elements.
   *
   * @throws UnableToCompleteException if extra text nodes are found
   */
  public Iterable<XMLElement> consumeChildElements() throws UnableToCompleteException {
    Iterable<XMLElement> rtn = consumeChildElementsNoEmptyCheck();
    assertNoText();
    return rtn;
  }

  /**
   * Consumes and returns all child elements selected by the interpreter. Note that text nodes are
   * not elements, and so are not presented for interpretation, and are not consumed.
   *
   * @param interpreter Should return true for any child that should be consumed and returned by the
   * consumeChildElements call
   */
  public Collection<XMLElement> consumeChildElements(Interpreter<Boolean> interpreter)
      throws UnableToCompleteException {
    List<XMLElement> elements = new ArrayList<XMLElement>();
    List<Node> doomed = new ArrayList<Node>();

    NodeList childNodes = elem.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); ++i) {
      Node childNode = childNodes.item(i);
      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        XMLElement childElement = provider.get((Element) childNode);
        if (interpreter.interpretElement(childElement)) {
          elements.add(childElement);
          doomed.add(childNode);
        }
      }
    }

    for (Node n : doomed) {
      elem.removeChild(n);
    }
    return elements;
  }

  /**
   * Convenience method for parsing the named attribute as an ImageResource value or reference.
   *
   * @return an expression that will evaluate to an ImageResource value in the generated code, or
   * null if there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeImageResourceAttribute(String name) throws UnableToCompleteException {
    return consumeAttribute(name, getImageResourceType());
  }

  /**
   * Consumes all child elements, and returns an HTML interpretation of them. Trailing and leading
   * whitespace is trimmed. <p> Each element encountered will be passed to the given Interpreter for
   * possible replacement. Escaping is performed to allow the returned text to serve as a Java
   * string literal used as input to a setInnerHTML call. <p> This call requires an interpreter to
   * make sense of any special children. The odds are you want to use {@link
   * org.gwtproject.uibinder.processor.elementparsers.HtmlInterpreter} for an HTML value, or {@link
   * org.gwtproject.uibinder.processor.elementparsers.TextInterpreter} for text.
   *
   * @param interpreter Called for each element, expected to return a string replacement for it, or
   * null if it should be left as is
   */
  public String consumeInnerHtml(Interpreter<String> interpreter) throws UnableToCompleteException {
    if (interpreter == null) {
      throw new NullPointerException("interpreter must not be null");
    }
    StringBuffer buf = new StringBuffer();
    GetInnerHtmlVisitor.getEscapedInnerHtml(elem, buf, interpreter, provider);

    clearChildren(elem);
    return buf.toString().trim();
  }

  /**
   * Refines {@link #consumeInnerHtml(Interpreter)} to handle PostProcessingInterpreter.
   */
  public String consumeInnerHtml(PostProcessingInterpreter<String> interpreter)
      throws UnableToCompleteException {
    String html = consumeInnerHtml((Interpreter<String>) interpreter);
    return interpreter.postProcess(html);
  }

  /**
   * Refines {@link #consumeInnerTextEscapedAsHtmlStringLiteral(Interpreter)} to handle
   * PostProcessingInterpreter.
   */
  public String consumeInnerText(PostProcessingInterpreter<String> interpreter)
      throws UnableToCompleteException {
    String text = consumeInnerTextEscapedAsHtmlStringLiteral(interpreter);
    return interpreter.postProcess(text);
  }

  /**
   * Consumes all child text nodes, and asserts that this element held only text. Trailing and
   * leading whitespace is trimmed, and escaped for use as a string literal. Notice that HTML
   * entities in the text are also escaped <p> This call requires an interpreter to make sense of
   * any special children. The odds are you want to use
   *
   * {@link org.gwtproject.uibinder.processor.elementparsers.TextInterpreter}
   *
   * @throws UnableToCompleteException If any elements present are not consumed by the interpreter
   */
  public String consumeInnerTextEscapedAsHtmlStringLiteral(Interpreter<String> interpreter)
      throws UnableToCompleteException {
    return consumeInnerTextEscapedAsHtmlStringLiteral(interpreter, true);
  }

  /**
   * Consumes all child text nodes, and asserts that this element held only text. Trailing and
   * leading whitespace is trimmed, and escaped for use as a string literal. Notice that HTML
   * entities in the text are NOT escaped <p> This call requires an interpreter to make sense of any
   * special children. The odds are you want to use
   *
   * {@link org.gwtproject.uibinder.processor.elementparsers.TextInterpreter}
   *
   * @throws UnableToCompleteException If any elements present are not consumed by the interpreter
   */
  public String consumeInnerTextEscapedAsStringLiteral(Interpreter<String> interpreter)
      throws UnableToCompleteException {
    return consumeInnerTextEscapedAsHtmlStringLiteral(interpreter, false);
  }

  /**
   * Convenience method for parsing the named attribute as a CSS length value.
   *
   * @return a (double, Unit) pair literal, an expression that will evaluate to such a pair in the
   * generated code, or null if there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeLengthAttribute(String name) throws UnableToCompleteException {
    return consumeAttributeWithDefault(name, null,
        new TypeMirror[]{getDoubleType(), getUnitType()});
  }

  /**
   * Consumes all attributes, and returns a string representing the entire opening tag. E.g., "<div
   * able='baker'>"
   */
  public String consumeOpeningTag() {
    String rtn = getOpeningTag();

    for (int i = getAttributeCount() - 1; i >= 0; i--) {
      getAttribute(i).consumeRawValue();
    }
    return rtn;
  }

  /**
   * Consumes the named attribute and parses it to an unparsed, unescaped array of Strings. The
   * strings in the attribute may be comma or space separated (or a mix of both).
   *
   * @return array of String, empty if the attribute was not set.
   */
  public String[] consumeRawArrayAttribute(String name) {
    String raw = consumeRawAttribute(name, null);
    if (raw == null) {
      return EMPTY;
    }

    return raw.split("[,\\s]+");
  }

  /**
   * Consumes the given attribute and returns its trimmed value, or null if it was unset. The
   * returned string is not escaped.
   *
   * @param name the attribute's full name (including prefix)
   * @return the attribute's value, or ""
   */
  public String consumeRawAttribute(String name) {
    if (!elem.hasAttribute(name)) {
      return null;
    }
    String value = elem.getAttribute(name);
    elem.removeAttribute(name);
    return value.trim();
  }

  /**
   * Consumes the given attribute and returns its trimmed value, or the given default value if it
   * was unset. The returned string is not escaped.
   *
   * @param name the attribute's full name (including prefix)
   * @param defaultValue the value to return if the attribute was unset
   * @return the attribute's value, or defaultValue
   */
  public String consumeRawAttribute(String name, String defaultValue) {
    String value = consumeRawAttribute(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Consumes the given required attribute as a literal or field reference. The types parameters are
   * required to determine how the value is parsed and validated.
   *
   * @param name the attribute's full name (including prefix)
   * @param types the type(s) this attribute is expected to provide
   * @return the attribute's value as a Java expression
   * @throws UnableToCompleteException on parse failure, or if the attribute is empty or
   * unspecified
   */
  public String consumeRequiredAttribute(String name, TypeMirror... types)
      throws UnableToCompleteException {
    if (!hasAttribute(name)) {
      failRequired(name);
    }

    AttributeParser parser = attributeParsers.getParser(types);

    String value = parser.parse(this, consumeRequiredRawAttribute(name));
    return value;
  }

  /**
   * Convenience method for parsing the named required attribute as a double value or reference.
   *
   * @return a double literal, an expression that will evaluate to a double value in the generated
   * code
   * @throws UnableToCompleteException on unparseable value, or if the attribute is empty or
   * unspecified
   */
  public String consumeRequiredDoubleAttribute(String name) throws UnableToCompleteException {
    return consumeRequiredAttribute(name, getDoubleType());
  }

  /**
   * Convenience method for parsing the named required attribute as a integer value or reference.
   *
   * @return a integer literal, an expression that will evaluate to a integer value in the generated
   * code
   * @throws UnableToCompleteException on unparseable value, or if the attribute is empty or
   * unspecified
   */
  public String consumeRequiredIntAttribute(String name) throws UnableToCompleteException {
    return consumeRequiredAttribute(name, getIntType());
  }

  /**
   * Consumes the named attribute, or dies if it is missing.
   */
  public String consumeRequiredRawAttribute(String name) throws UnableToCompleteException {
    String value = consumeRawAttribute(name);
    if (value == null) {
      failRequired(name);
    }
    return value;
  }

  /**
   * Convenience method for parsing the named attribute as a
   *
   * {@link org.gwtproject.safehtml.shared.SafeHtml SafeHtml} value or reference.
   *
   * @return an expression that will evaluate to a {@link org.gwtproject.safehtml.shared.SafeHtml
   * SafeHtml} value in the generated code, or null if there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeSafeHtmlAttribute(String name) throws UnableToCompleteException {
    return consumeAttribute(name, getSafeHtmlType());
  }

  /**
   * Consumes an attribute as either a SafeUri or a String. Used in HTML contexts.
   *
   * @return an expression that will evaluate to a SafeUri value in the generated code, or null if
   * there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeSafeUriOrStringAttribute(String name) throws UnableToCompleteException {
    return consumeAttributeWithParser(name, attributeParsers.getSafeUriInHtmlParser());
  }

  /**
   * Consumes a single child element, ignoring any text nodes and throwing an exception if no child
   * is found, or more than one child element is found.
   *
   * @throws UnableToCompleteException on no children, or too many
   */
  public XMLElement consumeSingleChildElement() throws UnableToCompleteException {
    XMLElement ret = null;
    for (XMLElement child : consumeChildElements()) {
      if (ret != null) {
        logger.die(this, "Element may only contain a single child element, but "
            + "found %s and %s.", ret, child);
      }

      ret = child;
    }

    if (ret == null) {
      logger.die(this, "Element must have a single child element");
    }

    return ret;
  }

  /**
   * Consumes the named attribute and parses it to an array of String expressions. The strings in
   * the attribute may be comma or space separated (or a mix of both).
   *
   * @return array of String expressions, empty if the attribute was not set.
   * @throws UnableToCompleteException on unparseable value
   */
  public String[] consumeStringArrayAttribute(String name) throws UnableToCompleteException {
    AttributeParser parser = attributeParsers.getParser(getStringType());

    String[] strings = consumeRawArrayAttribute(name);
    for (int i = 0; i < strings.length; i++) {
      strings[i] = parser.parse(this, strings[i]);
    }
    return strings;
  }

  /**
   * Convenience method for parsing the named attribute as a String value or reference.
   *
   * @return an expression that will evaluate to a String value in the generated code, or null if
   * there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeStringAttribute(String name) throws UnableToCompleteException {
    return consumeAttribute(name, getStringType());
  }

  /**
   * Convenience method for parsing the named attribute as a String value or reference.
   *
   * @return an expression that will evaluate to a String value in the generated code, or the given
   * defaultValue if there is no such attribute
   * @throws UnableToCompleteException on unparseable value
   */
  public String consumeStringAttribute(String name, String defaultValue)
      throws UnableToCompleteException {
    return consumeAttributeWithDefault(name, defaultValue, getStringType());
  }

  /**
   * Returns the unprocessed, unescaped, raw inner text of the receiver. Dies if the receiver has
   * non-text children. <p> You probably want to use
   *
   * {@link #consumeInnerTextEscapedAsHtmlStringLiteral} instead.
   *
   * @return the text
   * @throws UnableToCompleteException if it held anything other than text nodes
   */
  public String consumeUnescapedInnerText() throws UnableToCompleteException {
    final NodeList children = elem.getChildNodes();
    if (children.getLength() < 1) {
      return "";
    }
    if (children.getLength() > 1 || Node.TEXT_NODE != children.item(0).getNodeType()) {
      logger.die(this, "Element must contain only text");
    }
    Text t = (Text) children.item(0);
    return t.getTextContent();
  }

  /**
   * Get the attribute at the given index. If you are consuming attributes, remember to traverse
   * them in reverse.
   */
  public XMLAttribute getAttribute(int i) {
    return new XMLAttribute(XMLElement.this, (Attr) elem.getAttributes().item(i));
  }

  /**
   * Get the attribute with the given name.
   *
   * @return the attribute, or null if there is none of that name
   */
  public XMLAttribute getAttribute(String name) {
    Attr attr = elem.getAttributeNode(name);
    if (attr == null) {
      return null;
    }
    return new XMLAttribute(this, attr);
  }

  /**
   * Returns the number of attributes this element has.
   */
  public int getAttributeCount() {
    return elem.getAttributes().getLength();
  }

  public String getClosingTag() {
    if (NO_END_TAG.contains(elem.getTagName())) {
      return "";
    }
    return String.format("</%s>", elem.getTagName());
  }

  /**
   * Gets this element's local name (sans namespace prefix).
   */
  public String getLocalName() {
    return elem.getLocalName();
  }

  public Location getLocation() {
    return (Location) elem.getUserData(LOCATION_KEY);
  }

  /**
   * Gets this element's namespace URI.
   */
  public String getNamespaceUri() {
    return elem.getNamespaceURI();
  }

  /**
   * Returns the parent element, or null if parent is null or a node type other than Element.
   */
  public XMLElement getParent() {
    Node parent = elem.getParentNode();
    if (parent == null || Node.ELEMENT_NODE != parent.getNodeType()) {
      return null;
    }
    return provider.get((Element) parent);
  }

  public String getPrefix() {
    return elem.getPrefix();
  }

  /**
   * Determines whether the element has a given attribute.
   */
  public boolean hasAttribute(String name) {
    return elem.hasAttribute(name);
  }

  public boolean hasChildNodes() {
    return elem.hasChildNodes();
  }

  public String lookupPrefix(String prefix) {
    return elem.lookupPrefix(prefix);
  }

  public void setAttribute(String name, String value) {
    elem.setAttribute(name, value);
  }

  @Override
  public String toString() {
    return debugString;
  }

  private String consumeAttributeWithParser(String name, AttributeParser parser)
      throws UnableToCompleteException {
    String value = parser.parse(this, consumeRawAttribute(name));
    return value;
  }

  private Iterable<XMLElement> consumeChildElementsNoEmptyCheck() {
    try {
      Iterable<XMLElement> rtn = consumeChildElements(new SimpleInterpreter<Boolean>(true));
      return rtn;
    } catch (UnableToCompleteException e) {
      throw new RuntimeException("Impossible exception", e);
    }
  }

  /**
   * Consumes all child text nodes, and asserts that this element held only text. Trailing and
   * leading whitespace is trimmed, and escaped for use as a string literal. If escapeHtmlEntities
   * is true, HTML Entities are also escaped. <p> This call requires an interpreter to make sense of
   * any special children. The odds are you want to use
   *
   * {@link org.gwtproject.uibinder.processor.elementparsers.TextInterpreter}
   *
   * @throws UnableToCompleteException If any elements present are not consumed by the interpreter
   */
  private String consumeInnerTextEscapedAsHtmlStringLiteral(Interpreter<String> interpreter,
      boolean escapeHtmlEntities)
      throws UnableToCompleteException {
    if (interpreter == null) {
      throw new NullPointerException("interpreter must not be null");
    }
    StringBuffer buf = new StringBuffer();

    if (escapeHtmlEntities) {
      GetInnerTextVisitor.getHtmlEscapedInnerText(elem, buf, interpreter, provider);
    } else {
      GetInnerTextVisitor.getEscapedInnerText(elem, buf, interpreter, provider);
    }

    // Make sure there are no children left but empty husks
    for (XMLElement child : consumeChildElementsNoEmptyCheck()) {
      if (child.hasChildNodes() || child.getAttributeCount() > 0) {
        logger.die(this, "Illegal child %s in a text-only context. "
            + "Perhaps you are trying to use unescaped HTML "
            + "where text is required, as in a HasText widget?", child);
      }
    }

    clearChildren(elem);
    return buf.toString().trim();
  }

  private void failRequired(String name) throws UnableToCompleteException {
    logger.die(this, "Missing required attribute \"%s\"", name);
  }

  private TypeMirror getBooleanType() {
    if (booleanType == null) {
      booleanType = AptUtil.getTypeUtils().getPrimitiveType(TypeKind.BOOLEAN);
    }
    return booleanType;
  }

  private TypeMirror getDoubleType() {
    if (doubleType == null) {
      doubleType = AptUtil.getTypeUtils().getPrimitiveType(TypeKind.DOUBLE);
    }
    return doubleType;
  }

  private TypeMirror getImageResourceType() {
    if (imageResourceType == null) {
      TypeElement typeElement = AptUtil.getElementUtils()
          .getTypeElement(UiBinderApiPackage.current().getImageResourceFqn());
      imageResourceType = typeElement.asType();
    }
    return imageResourceType;
  }

  private TypeMirror getIntType() {
    if (intType == null) {
      intType = AptUtil.getTypeUtils().getPrimitiveType(TypeKind.INT);
    }
    return intType;
  }

  private String getOpeningTag() {
    StringBuilder b = new StringBuilder().append("<").append(elem.getTagName());

    NamedNodeMap attrs = elem.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Attr attr = (Attr) attrs.item(i);
      b.append(String.format(" %s='%s'", attr.getName(),
          UiBinderWriter.escapeAttributeText(attr.getValue())));
    }
    b.append(">");
    return b.toString();
  }

  private TypeMirror getSafeHtmlType() {
    if (safeHtmlType == null) {
      safeHtmlType = AptUtil.getElementUtils()
          .getTypeElement(UiBinderApiPackage.current().getSafeHtmlInterfaceFqn()).asType();
    }
    return safeHtmlType;
  }

  private TypeMirror getStringType() {
    if (stringType == null) {
      stringType = AptUtil.getElementUtils().getTypeElement(String.class.getCanonicalName())
          .asType();
    }
    return stringType;
  }

  private TypeMirror getUnitType() {
    return AptUtil.getElementUtils()
        .getTypeElement(UiBinderApiPackage.current().getDomStyleUnitFqn()).asType();
  }
}
