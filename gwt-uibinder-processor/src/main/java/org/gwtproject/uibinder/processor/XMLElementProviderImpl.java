package org.gwtproject.uibinder.processor;

import org.gwtproject.uibinder.processor.attributeparsers.AttributeParsers;
import org.w3c.dom.Element;

/**
 *
 */
public class XMLElementProviderImpl implements XMLElementProvider {

  private final AttributeParsers attributeParsers;
  private final MortalLogger logger;

  public XMLElementProviderImpl(AttributeParsers attributeParsers, MortalLogger logger) {
    this.attributeParsers = attributeParsers;
    this.logger = logger;
  }

  @Override
  public XMLElement get(Element e) {
    return new XMLElement(e, attributeParsers, logger, this);
  }
}
