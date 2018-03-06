package org.gwtproject.uibinder.processor.attributeparsers;

import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import java.util.HashMap;
import java.util.Locale;
import javax.lang.model.type.TypeMirror;

/**
 * Parses a {@link HasHorizontalAlignment.HorizontalAlignmentConstant} .
 */
class HorizontalAlignmentConstantParser extends StrictAttributeParser {

  private static final String PREFIX = HasHorizontalAlignment.class.getCanonicalName() + ".ALIGN_";
  private static final HashMap<String, String> values = new HashMap<>();

  static {
    values.put("LEFT", PREFIX + "LEFT");
    values.put("CENTER", PREFIX + "CENTER");
    values.put("RIGHT", PREFIX + "RIGHT");
    values.put("JUSTIFY", PREFIX + "JUSTIFY");
    values.put("DEFAULT", PREFIX + "DEFAULT");
    values.put("LOCALE_START", PREFIX + "LOCALE_START");
    values.put("LOCALE_END", PREFIX + "LOCALE_END");
    values.put("ALIGN_LEFT", PREFIX + "LEFT");
    values.put("ALIGN_CENTER", PREFIX + "CENTER");
    values.put("ALIGN_RIGHT", PREFIX + "RIGHT");
    values.put("ALIGN_JUSTIFY", PREFIX + "JUSTIFY");
    values.put("ALIGN_DEFAULT", PREFIX + "DEFAULT");
    values.put("ALIGN_LOCALE_START", PREFIX + "LOCALE_START");
    values.put("ALIGN_LOCALE_END", PREFIX + "LOCALE_END");
  }

  HorizontalAlignmentConstantParser(FieldReferenceConverter converter, TypeMirror type,
      MortalLogger logger) {
    super(converter, logger, type);
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    String translated = values.get(value.toUpperCase(Locale.ROOT));
    if (translated != null) {
      return translated;
    }
    return super.parse(source, value);
  }
}
