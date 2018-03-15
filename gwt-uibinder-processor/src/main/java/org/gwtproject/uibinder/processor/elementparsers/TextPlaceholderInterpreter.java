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
package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.NullInterpreter;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.MessageWriter;
import org.gwtproject.uibinder.processor.messages.PlaceholderInterpreter;

/**
 * Interprets the interior of text message. Any ph elements in the message will be consumed by a
 * call to their {@link XMLElement#consumeInnerText} method. A ph inside a text message may contain
 * no other elements.
 */
public final class TextPlaceholderInterpreter extends PlaceholderInterpreter {

  public TextPlaceholderInterpreter(UiBinderWriter writer,
      MessageWriter message) {
    super(writer, message);
  }

  @Override
  protected String consumePlaceholderInnards(XMLElement elem)
      throws UnableToCompleteException {
    return elem.consumeInnerTextEscapedAsHtmlStringLiteral(new NullInterpreter<>());
  }
}
