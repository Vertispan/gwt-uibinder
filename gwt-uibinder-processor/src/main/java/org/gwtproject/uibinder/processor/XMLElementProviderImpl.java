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
