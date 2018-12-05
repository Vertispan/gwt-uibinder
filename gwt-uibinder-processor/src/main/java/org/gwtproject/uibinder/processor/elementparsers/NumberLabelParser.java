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

import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.UiBinderWriter;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Parses DateLabel widgets.
 */
public class NumberLabelParser implements ElementParser {

  static final String AT_MOST_ONE_SPECIFIED_FORMAT
      = "May have only one of format, predefinedFormat and customFormat.";
  static final String AT_MOST_ONE_SPECIFIED_CURRENCY
      = "May have only one of currencyData and customCode.";
  static final String NO_CURRENCY_WITH_FORMAT
      = "May not specify both a NumberFormat and a currency code.";
  static final String NO_CURRENCY_WITHOUT_SPECIFIED_FORMAT
      = "May not specify a currency code if no format is given.";
  static final String NO_CURRENCY_WITH_PREDEFINED_FORMAT
      = "May not specify a currency code with a predefined format (except the CURRENCY format)";
  static final String UNKNOWN_PREDEFINED_FORMAT = "Unknown predefined format: %s";

  private static final Map<String, String> predefinedFormats;

  static {
    String prefix = UiBinderApiPackage.current().getI18nNumberFormatFqn();
    Map<String, String> formats = new HashMap<String, String>(4);
    formats.put("DECIMAL", prefix + ".getDecimalFormat()");
    formats.put("PERCENT", prefix + ".getPercentFormat()");
    formats.put("SCIENTIFIC", prefix + ".getScientificFormat()");
    // CURRENCY is special-cased in consumeFormat.
    predefinedFormats = Collections.unmodifiableMap(formats);
  }

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    if (hasNumberFormatConstructor(type)) {
      String format = consumeFormat(elem, writer);

      if (format != null) {
        writer.setFieldInitializerAsConstructor(fieldName, format);
      }
    }
  }

  private String consumeCurrency(XMLElement elem, UiBinderWriter writer)
      throws UnableToCompleteException {
    String currencyData = elem.consumeAttribute("currencyData",
        AptUtil.getElementUtils().getTypeElement(
            UiBinderApiPackage.current().getI18nCurrencyDataFqn()).asType());
    String currencyCode = elem.consumeStringAttribute("currencyCode");

    if (currencyData != null && currencyCode != null) {
      writer.die(elem, AT_MOST_ONE_SPECIFIED_CURRENCY);
    }
    return currencyData != null ? currencyData : currencyCode;
  }

  private String consumeFormat(XMLElement elem, UiBinderWriter writer)
      throws UnableToCompleteException {
    String format = elem.consumeAttribute("format",
        AptUtil.getElementUtils().getTypeElement(
            UiBinderApiPackage.current().getI18nNumberFormatFqn()).asType());
    String predefinedFormat = elem.consumeRawAttribute("predefinedFormat");
    String customFormat = elem.consumeStringAttribute("customFormat");

    if (format != null) {
      if (predefinedFormat != null || customFormat != null) {
        writer.die(elem, AT_MOST_ONE_SPECIFIED_FORMAT);
      }
      if (hasCurrency(elem)) {
        writer.die(elem, NO_CURRENCY_WITH_FORMAT);
      }
      return format;
    }
    if (predefinedFormat != null) {
      if (customFormat != null) {
        writer.die(elem, AT_MOST_ONE_SPECIFIED_FORMAT);
      }
      if ("CURRENCY".equals(predefinedFormat)) {
        String currency = consumeCurrency(elem, writer);
        return UiBinderApiPackage.current().getI18nNumberFormatFqn() + ".getCurrencyFormat("
            + (currency != null ? currency : "") + ")";
      }
      if (hasCurrency(elem)) {
        writer.die(elem, NO_CURRENCY_WITH_PREDEFINED_FORMAT);
      }
      String f = predefinedFormats.get(predefinedFormat);
      if (f == null) {
        writer.die(elem, UNKNOWN_PREDEFINED_FORMAT, predefinedFormat);
      }
      return f;
    }
    if (customFormat != null) {
      String currency = consumeCurrency(elem, writer);
      return UiBinderApiPackage.current().getI18nNumberFormatFqn() + ".getFormat(" + customFormat
          + (currency != null ? ", " + currency : "") + ")";
    }
    if (hasCurrency(elem)) {
      writer.die(elem, NO_CURRENCY_WITHOUT_SPECIFIED_FORMAT);
    }
    return null;
  }

  private boolean hasCurrency(XMLElement elem) {
    return elem.hasAttribute("currencyData")
        || elem.hasAttribute("currencyCode");
  }

  private boolean hasNumberFormatConstructor(TypeMirror type) {
    TypeElement numberFormatType = AptUtil.getElementUtils()
        .getTypeElement(UiBinderApiPackage.current().getI18nNumberFormatFqn());
    return AptUtil.findConstructor(type, new TypeMirror[]{numberFormatType.asType()}) != null;
  }
}
