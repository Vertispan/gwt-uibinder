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

import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;

import javax.lang.model.type.TypeMirror;

/**
 * Parses a string attribute.
 */
class StringAttributeParser implements AttributeParser {

  /* package private for testing */
  static class FieldReferenceDelegate implements
      FieldReferenceConverter.Delegate {

    private final TypeMirror[] types;

    FieldReferenceDelegate(TypeMirror type) {
      this.types = new TypeMirror[]{type};
    }

    public TypeMirror[] getTypes() {
      return types;
    }

    public String handleFragment(String literal) {
      return "\"" + UiBinderWriter.escapeTextForJavaStringLiteral(literal)
          + "\"";
    }

    public String handleReference(String reference) {
      return String.format(" + %s + ", reference);
    }
  }

  private final FieldReferenceConverter converter;
  private final TypeMirror stringType;

  StringAttributeParser(FieldReferenceConverter converter, TypeMirror stringType) {
    this.converter = converter;
    this.stringType = stringType;
  }

  public String parse(XMLElement source, String value) {
    return converter.convert(source, value, new FieldReferenceDelegate(stringType));
  }
}
