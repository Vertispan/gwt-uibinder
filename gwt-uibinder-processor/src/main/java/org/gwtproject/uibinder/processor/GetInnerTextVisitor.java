package org.gwtproject.uibinder.processor;


import org.gwtproject.uibinder.processor.XMLElement.Interpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

class GetInnerTextVisitor implements NodeVisitor {

  /**
   * Gathers a text representation of the children of the given Elem, and stuffs it into the given
   * StringBuffer. Applies the interpreter to each descendant, and uses the writer to report
   * errors.
   */
  public static void getEscapedInnerText(Element elem, StringBuffer buffer,
      Interpreter<String> interpreter, XMLElementProvider writer)
      throws UnableToCompleteException {
    new ChildWalker().accept(elem, new GetInnerTextVisitor(buffer,
        interpreter, writer, false));
  }

  /**
   * Gathers a text representation of the children of the given Elem, and stuffs it into the given
   * StringBuffer. Applies the interpreter to each descendant, and uses the writer to report errors.
   * Escapes HTML Entities.
   */
  public static void getHtmlEscapedInnerText(Element elem, StringBuffer buffer,
      Interpreter<String> interpreter, XMLElementProvider writer)
      throws UnableToCompleteException {
    new ChildWalker().accept(elem, new GetInnerTextVisitor(buffer,
        interpreter, writer, true));
  }

  protected final StringBuffer buffer;
  protected final Interpreter<String> interpreter;
  protected final XMLElementProvider elementProvider;
  protected final boolean escapeHtmlEntities;

  protected GetInnerTextVisitor(StringBuffer buffer,
      Interpreter<String> interpreter, XMLElementProvider elementProvider) {
    this(buffer, interpreter, elementProvider, true);
  }

  protected GetInnerTextVisitor(StringBuffer buffer,
      Interpreter<String> interpreter, XMLElementProvider elementProvider,
      boolean escapeHtmlEntities) {
    this.buffer = buffer;
    this.interpreter = interpreter;
    this.elementProvider = elementProvider;
    this.escapeHtmlEntities = escapeHtmlEntities;
  }

  public void visitCData(CDATASection d) {
    // TODO(jgw): write this back just as it came in.
  }

  public void visitElement(Element e) throws UnableToCompleteException {
    String replacement = interpreter.interpretElement(elementProvider.get(e));

    if (replacement != null) {
      buffer.append(replacement);
    }
  }

  public void visitText(Text t) {
    String escaped;
    if (escapeHtmlEntities) {
      escaped = UiBinderWriter.escapeText(t.getTextContent(),
          preserveWhiteSpace(t));
    } else {
      escaped = t.getTextContent();
      if (!preserveWhiteSpace(t)) {
        escaped = escaped.replaceAll("\\s+", " ");
      }
      escaped = UiBinderWriter.escapeTextForJavaStringLiteral(escaped);
    }

    buffer.append(escaped);
  }

  private boolean preserveWhiteSpace(Text t) {
    Element parent = Node.ELEMENT_NODE == t.getParentNode().getNodeType()
        ? (Element) t.getParentNode() : null;

    boolean preserveWhitespace = parent != null
        && "pre".equals(parent.getTagName());
    // TODO(rjrjr) What about script blocks?
    return preserveWhitespace;
  }
}
