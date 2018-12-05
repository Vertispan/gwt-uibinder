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

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.FieldManager;
import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Managers access to all implementations of {@link AttributeParser}.
 */
public class AttributeParsers {

  private static final String INT = "int";
  private static final String STRING = String.class.getCanonicalName();
  private static final String DOUBLE = "double";
  private static final String BOOLEAN = "boolean";

  private final MortalLogger logger;
  private final FieldReferenceConverter converter;

  /**
   * Class names of parsers keyed by method parameter signatures.
   */
  private final Map<String, AttributeParser> parsers = new HashMap<String, AttributeParser>();
  private final SafeUriAttributeParser safeUriInHtmlParser;

  public AttributeParsers(FieldManager fieldManager, MortalLogger logger) {
    this.logger = logger;
    converter = new FieldReferenceConverter(fieldManager);
    Types types = AptUtil.getTypeUtils();
    Elements elements = AptUtil.getElementUtils();

    BooleanAttributeParser boolParser = new BooleanAttributeParser(converter,
        types.getPrimitiveType(TypeKind.BOOLEAN), logger);
    addAttributeParser(BOOLEAN, boolParser);
    addAttributeParser(Boolean.class.getCanonicalName(), boolParser);

    IntAttributeParser intParser = new IntAttributeParser(converter,
        types.getPrimitiveType(TypeKind.BOOLEAN.INT), logger);
    addAttributeParser(INT, intParser);
    addAttributeParser(Integer.class.getCanonicalName(), intParser);

    DoubleAttributeParser doubleParser = new DoubleAttributeParser(converter,
        types.getPrimitiveType(TypeKind.DOUBLE), logger);
    addAttributeParser(DOUBLE, doubleParser);
    addAttributeParser(Double.class.getCanonicalName(), doubleParser);

    addAttributeParser("int,int", new IntPairAttributeParser(intParser,
        logger));

    addAttributeParser(UiBinderApiPackage.current().getHorizontalAlignmentConstantFqn(),
        new HorizontalAlignmentConstantParser(converter, elements
            .getTypeElement(UiBinderApiPackage.current().getHorizontalAlignmentConstantFqn())
            .asType(), logger));
    addAttributeParser(UiBinderApiPackage.current().getVerticalAlignmentConstantFqn(),
        new VerticalAlignmentConstantParser(
            converter, elements
            .getTypeElement(UiBinderApiPackage.current().getVerticalAlignmentConstantFqn())
            .asType(), logger));
    addAttributeParser(UiBinderApiPackage.current().getTextBoxBaseTextAlignConstantFqn(),
        new TextAlignConstantParser(
            converter, elements
            .getTypeElement(UiBinderApiPackage.current().getTextBoxBaseTextAlignConstantFqn())
            .asType(), logger));

    StringAttributeParser stringParser = new StringAttributeParser(converter,
        elements.getTypeElement(STRING).asType());
    addAttributeParser(STRING, stringParser);

    EnumAttributeParser unitParser = new EnumAttributeParser(converter,
        elements.getTypeElement(UiBinderApiPackage.current().getDomStyleUnitFqn()).asType(),
        logger);
    addAttributeParser(DOUBLE + "," + UiBinderApiPackage.current().getDomStyleUnitFqn(),
        new LengthAttributeParser(doubleParser, unitParser, logger));

    SafeUriAttributeParser uriParser = new SafeUriAttributeParser(stringParser,
        converter,
        elements.getTypeElement(UiBinderApiPackage.current().getSafeUriInterfaceFqn()).asType(),
        logger);
    addAttributeParser(UiBinderApiPackage.current().getSafeUriInterfaceFqn(), uriParser);

    safeUriInHtmlParser = new SafeUriAttributeParser(stringParser,
        converter,
        elements.getTypeElement(UiBinderApiPackage.current().getSafeUriInterfaceFqn()).asType(),
        elements.getTypeElement(STRING).asType(), logger);
  }

  /**
   * Returns a parser for the given type(s). Accepts multiple types args to allow requesting parsers
   * for things like for pairs of ints.
   */
  public AttributeParser getParser(TypeMirror... types) {
    if (types.length == 0) {
      throw new RuntimeException("Asked for attribute parser of no type");
    }

    AttributeParser rtn = getForKey(getParametersKey(types));
    if (rtn != null || types.length > 1) {
      return rtn;
    }

    /* Maybe it's an enum */
    if (AptUtil.isEnum(types[0])) {
      return new EnumAttributeParser(converter, types[0], logger);
    }

    /*
     * Dunno what it is, so let a StrictAttributeParser look for a
     * {field.reference}
     */
    return new StrictAttributeParser(converter, logger, types[0]);
  }

  /**
   * Returns a parser specialized for handling URI references in html contexts, like &lt;a
   * href="{foo.bar}">.
   */
  public AttributeParser getSafeUriInHtmlParser() {
    return safeUriInHtmlParser;
  }

  private void addAttributeParser(String signature,
      AttributeParser attributeParser) {
    parsers.put(signature, attributeParser);
  }

  private AttributeParser getForKey(String key) {
    return parsers.get(key);
  }

  /**
   * Given a types array, return a key for the attributeParsers table.
   */
  private String getParametersKey(TypeMirror[] types) {
    StringBuffer b = new StringBuffer();
    for (TypeMirror t : types) {
      if (b.length() > 0) {
        b.append(',');
      }
      if (t.getKind().isPrimitive()) {
        b.append(t.toString());
      } else {
        b.append(AptUtil.getParameterizedQualifiedSourceName(t));
      }
    }
    return b.toString();
  }
}
