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
package org.gwtproject.uibinder.processor.attributeparsers;

import org.gwtproject.uibinder.processor.FieldReference;
import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.attributeparsers.FieldReferenceConverter.Delegate;
import org.gwtproject.uibinder.processor.attributeparsers.FieldReferenceConverter.IllegalFieldReferenceException;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Fall through attribute parser. Accepts a field reference or nothing.
 */
class StrictAttributeParser implements AttributeParser {

  /**
   * Package protected for testing.
   */
  static class FieldReferenceDelegate implements Delegate {

    private boolean sawReference = false;
    private final TypeMirror[] types;

    FieldReferenceDelegate(TypeMirror... types) {
      this.types = types;
    }

    public TypeMirror[] getTypes() {
      return types;
    }

    public String handleFragment(String fragment)
        throws IllegalFieldReferenceException {
      if (fragment.length() > 0) {
        throw new IllegalFieldReferenceException();
      }
      return fragment;
    }

    public String handleReference(String reference)
        throws IllegalFieldReferenceException {
      assertOnly();
      sawReference = true;
      return reference;
    }

    private void assertOnly() {
      if (sawReference) {
        throw new IllegalFieldReferenceException();
      }
    }
  }

  private final FieldReferenceConverter converter;
  protected final MortalLogger logger;
  private final TypeMirror[] types;

  StrictAttributeParser(FieldReferenceConverter converter, MortalLogger logger,
      TypeMirror... types) {
    this.converter = converter;
    this.logger = logger;
    this.types = types;
  }

  /**
   * If the value holds a single field reference "{like.this}", converts it to a Java Expression.
   *
   * <p>In any other case (e.g. more than one field reference), an UnableToCompleteException is
   * thrown.
   */
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    if ("".equals(value.trim())) {
      logger
          .die(source, "Cannot use empty value as type %s", FieldReference.renderTypesList(types));
    }
    try {
      return converter.convert(source, value, new FieldReferenceDelegate(types));
    } catch (IllegalFieldReferenceException e) {
      logger.die(source, "Cannot parse value: \"%s\" as type %s", value,
          FieldReference.renderTypesList(types));
      return null; // Unreachable
    }
  }
}
