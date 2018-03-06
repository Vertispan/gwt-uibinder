package org.gwtproject.uibinder.processor.attributeparsers;

import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.user.client.ui.TextBoxBase;

import java.util.HashMap;
import java.util.Locale;
import javax.lang.model.type.TypeMirror;

/**
 * Parses a {@link TextBoxBase.TextAlignConstant}.
 */
class TextAlignConstantParser extends StrictAttributeParser {

  private static final String PREFIX = TextBoxBase.class.getCanonicalName()
      + ".ALIGN_";
  private static final HashMap<String, String> values = new HashMap<>();

  static {
    values.put("LEFT", PREFIX + "LEFT");
    values.put("CENTER", PREFIX + "CENTER");
    values.put("RIGHT", PREFIX + "RIGHT");
    values.put("JUSTIFY", PREFIX + "JUSTIFY");
    values.put("ALIGN_LEFT", PREFIX + "LEFT");
    values.put("ALIGN_CENTER", PREFIX + "CENTER");
    values.put("ALIGN_RIGHT", PREFIX + "RIGHT");
    values.put("ALIGN_JUSTIFY", PREFIX + "JUSTIFY");
  }

  TextAlignConstantParser(FieldReferenceConverter converter, TypeMirror type,
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
