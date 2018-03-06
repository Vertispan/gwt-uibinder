package org.gwtproject.uibinder.processor;


import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Traverses the children of a {@link Node}.
 */
class ChildWalker {

  /**
   * Take a {@link NodeVisitor} and show it each child of the given {@link Node} that is of a type
   * relevant to our templates.
   *
   * <p>Note that this is not a recursive call, though the visitor itself may choose to recurse
   */
  void accept(Node n, NodeVisitor v) throws UnableToCompleteException {

    NodeList childNodes = n.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); ++i) {
      Node child = childNodes.item(i);

      switch (child.getNodeType()) {
        case Node.ELEMENT_NODE:
          v.visitElement((Element) child);
          break;

        case Node.TEXT_NODE:
          v.visitText((Text) child);
          break;

        case Node.COMMENT_NODE:
          // Ditch comment nodes.
          break;

        case Node.CDATA_SECTION_NODE:
          v.visitCData((CDATASection) child);
          break;

        case Node.ENTITY_NODE:
        case Node.ENTITY_REFERENCE_NODE:
        case Node.ATTRIBUTE_NODE:
        case Node.DOCUMENT_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.NOTATION_NODE:
        case Node.PROCESSING_INSTRUCTION_NODE:
        default: {
          // None of these are expected node types.
          throw new RuntimeException("Unexpected XML node");
        }
      }
    }
  }
}
