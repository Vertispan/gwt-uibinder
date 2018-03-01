package org.gwtproject.uibinder.processor;


import org.gwtproject.uibinder.processor.XMLElement.Interpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import org.w3c.dom.Element;

class GetInnerHtmlVisitor extends GetInnerTextVisitor {

  /**
   * Recursively gathers an HTML representation of the children of the given Elem, and stuffs it
   * into the given StringBuffer. Applies the interpreter to each descendant, and uses the writer to
   * report errors.
   */
  public static void getEscapedInnerHtml(Element elem, StringBuffer buffer,
      Interpreter<String> interpreter, XMLElementProvider writer)
      throws UnableToCompleteException {
    new ChildWalker().accept(elem, new GetInnerHtmlVisitor(buffer, interpreter,
        writer));
  }

  private GetInnerHtmlVisitor(StringBuffer buffer,
      Interpreter<String> interpreter, XMLElementProvider writer) {
    super(buffer, interpreter, writer);
  }

  @Override
  public void visitElement(Element elem) throws UnableToCompleteException {
    XMLElement xmlElement = elementProvider.get(elem);
    String replacement = interpreter.interpretElement(xmlElement);
    if (replacement != null) {
      buffer.append(replacement);
      return;
    }

    // TODO(jgw): Ditch the closing tag when there are no children.
    buffer.append(xmlElement.consumeOpeningTag());
    getEscapedInnerHtml(elem, buffer, interpreter, elementProvider);
    buffer.append(xmlElement.getClosingTag());
  }
}
