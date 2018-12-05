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

import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import javax.lang.model.type.TypeMirror;

/**
 * Parses SafeUri literals or references.
 *
 * <p>Simple String literals are passed through UriUtils.fromConstantString(String)
 *
 * <p>Accepts concatenated string expressions, mainly for compatibility with legacy
 * <code>&lt;a href="{foo.bar}{baz.bang}"></code> abuses. Passes such nonsense
 * through UriUtils.fromString(String)
 */
public class SafeUriAttributeParser extends StrictAttributeParser {
  public static String wrapUnsafeStringAndWarn(MortalLogger logger, XMLElement source,
      String expression) {
    logger.warn(source, "Escaping unsafe runtime String expression "
        + "used for URI with UriUtils.fromString(). Use SafeUri instead");
    return UiBinderApiPackage.current().getSafeHtmlUriUtilsFqn()
        + ".fromString(" + expression + ")";
  }

  private final boolean runtimeStringsAllowed;
  private final StringAttributeParser stringParser;

  /**
   * Constructs an instance for particular use in html contexts, where {string.references} are
   * acceptible.
   */
  SafeUriAttributeParser(StringAttributeParser stringParser, FieldReferenceConverter converter,
      TypeMirror safeUriType, TypeMirror stringType, MortalLogger logger) {
    super(converter, logger, safeUriType, stringType);
    this.stringParser = stringParser;
    runtimeStringsAllowed = true;
  }

  /**
   * Constructs an instance for normal use, where String literals are okay but {string.references}
   * are not.
   */
  SafeUriAttributeParser(StringAttributeParser stringParser, FieldReferenceConverter converter,
      TypeMirror safeUriType, MortalLogger logger) {
    super(converter, logger, safeUriType);
    this.stringParser = stringParser;
    runtimeStringsAllowed = false;
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    int howManyFieldRefs = FieldReferenceConverter.countFieldReferences(value);

    /*
     * No field refs, just a string literal.
     *
     * Isn't this a lot of pointless codegen for <a href="#whatever"/>? No. In
     * such an html context this parser is called only for computed attributes,
     * where howManyFieldRefs is > 0. See
     * HtmlInterpreter#ComputedAttributeTypist
     */
    if (howManyFieldRefs == 0) {
      // String literal, convert it for the nice people
      return UiBinderApiPackage.current().getSafeHtmlUriUtilsFqn()
          + ".fromSafeConstant(" + stringParser.parse(source, value) + ")";
    }

    /*
     * No runtime string expressions are allowed, or they are but it
     * "{looks.like.this}". Just do the usual parsing.
     */
    if (!runtimeStringsAllowed || howManyFieldRefs == 1 && value.substring(0, 1).equals("{")
        && value.substring(value.length() - 1, value.length()).equals("}")) {
      return super.parse(source, value);
    }

    return wrapUnsafeStringAndWarn(logger, source, stringParser.parse(source, value));
  }
}
