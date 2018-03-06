package org.gwtproject.uibinder.processor;

import org.w3c.dom.Element;

/**
 * Implemented by objects that instantiate XMLElement.
 */
public interface XMLElementProvider {

  XMLElement get(Element e);
}
