package org.gwtproject.uibinder.processor;

import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Implements methods to interpret the {@link org.w3c.dom.Node} types we actually care about.
 */
interface NodeVisitor {

  void visitCData(CDATASection d) throws UnableToCompleteException;

  void visitElement(Element e) throws UnableToCompleteException;

  void visitText(Text t) throws UnableToCompleteException;
}
