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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Parses DateLabel widgets.
 */
public class DateLabelParser implements ElementParser {

  static final String AT_MOST_ONE_SPECIFIED_FORMAT
      = "May have at most one of format, predefinedFormat and customFormat.";
  static final String AT_MOST_ONE_SPECIFIED_TIME_ZONE
      = "May have at most one of timezone and timezoneOffset.";
  static final String NO_TIMEZONE_WITHOUT_SPECIFIED_FORMAT
      = "May not specify a time zone if no format is given.";

  public void parse(XMLElement elem, String fieldName, TypeMirror type,
      UiBinderWriter writer) throws UnableToCompleteException {
    boolean supportsTimeZone = hasDateTimeFormatAndTimeZoneConstructor(type);
    if (hasDateTimeFormatConstructor(type)
        || supportsTimeZone) {
      String format = consumeFormat(elem, writer);

      if (format != null) {
        String timeZone = (supportsTimeZone ? consumeTimeZone(elem, writer)
            : null);

        writer.setFieldInitializerAsConstructor(fieldName, makeArgs(
            format, timeZone));
      } else if (supportsTimeZone && hasTimeZone(elem)) {
        writer.die(elem, NO_TIMEZONE_WITHOUT_SPECIFIED_FORMAT);
      }
    }
  }

  private String consumeFormat(XMLElement elem, UiBinderWriter writer)
      throws UnableToCompleteException {
    String format = elem.consumeAttribute("format",
        AptUtil.getElementUtils()
            .getTypeElement(UiBinderApiPackage.current().getI18nDateTimeFormatFqn()).asType());
    String predefinedFormat = elem.consumeAttribute("predefinedFormat",
        AptUtil.getElementUtils()
            .getTypeElement(UiBinderApiPackage.current().getI18nDateTimeFormatPredefinedFormatFqn())
            .asType());
    String customFormat = elem.consumeStringAttribute("customFormat");

    if (format != null) {
      if (predefinedFormat != null || customFormat != null) {
        writer.die(elem, AT_MOST_ONE_SPECIFIED_FORMAT);
      }
      return format;
    }
    if (predefinedFormat != null) {
      if (customFormat != null) {
        writer.die(elem, AT_MOST_ONE_SPECIFIED_FORMAT);
      }
      return makeGetFormat(predefinedFormat);
    }
    if (customFormat != null) {
      return makeGetFormat(customFormat);
    }
    return null;
  }

  private String consumeTimeZone(XMLElement elem, UiBinderWriter writer)
      throws UnableToCompleteException {
    String timeZone = elem.consumeAttribute("timezone",
        AptUtil.getElementUtils()
            .getTypeElement(UiBinderApiPackage.current().getI18nTimeZoneFqn()).asType());
    String timeZoneOffset = elem.consumeAttribute("timezoneOffset",
        getIntType());
    if (timeZone != null && timeZoneOffset != null) {
      writer.die(elem, AT_MOST_ONE_SPECIFIED_TIME_ZONE);
    }
    if (timeZone != null) {
      return timeZone;
    }
    if (timeZoneOffset != null) {
      return UiBinderApiPackage.current().getI18nTimeZoneFqn() + ".createTimeZone("
          + timeZoneOffset + ")";
    }
    return null;
  }

  private TypeMirror getIntType() {
    return AptUtil.getTypeUtils().getPrimitiveType(TypeKind.INT);
  }

  private boolean hasDateTimeFormatAndTimeZoneConstructor(TypeMirror type) {
    TypeElement dateTimeFormatType = AptUtil.getElementUtils()
        .getTypeElement(UiBinderApiPackage.current().getI18nDateTimeFormatFqn());
    TypeElement timeZoneType = AptUtil.getElementUtils()
        .getTypeElement(UiBinderApiPackage.current().getI18nTimeZoneFqn());
    return AptUtil
        .hasCompatibleConstructor(type, dateTimeFormatType.asType(), timeZoneType.asType());
  }

  private boolean hasDateTimeFormatConstructor(TypeMirror type) {
    TypeElement dateTimeFormatType = AptUtil.getElementUtils()
        .getTypeElement(UiBinderApiPackage.current().getI18nDateTimeFormatFqn());
    return AptUtil.hasCompatibleConstructor(type, dateTimeFormatType.asType());
  }

  private boolean hasTimeZone(XMLElement elem) {
    return elem.hasAttribute("timezone") || elem.hasAttribute("timezoneOffset");
  }

  private String[] makeArgs(String format, String timeZone) {
    if (timeZone == null) {
      return new String[]{format};
    }
    return new String[]{format, timeZone};
  }

  private String makeGetFormat(String format) {
    return UiBinderApiPackage.current().getI18nDateTimeFormatFqn() + ".getFormat(" + format
        + ")";
  }
}
