package org.gwtproject.uibinder.processor.attributeparsers;


import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import com.google.gwt.dom.client.Style.Unit;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a CSS length value (e.g., "2em", "50%"), returning a comma-separated (double, Unit) pair.
 */
public class LengthAttributeParser implements AttributeParser {

  static final String UNIT = Unit.class.getCanonicalName();

  // This regular expression matches CSS length patterns of the form
  // (value)(unit), where the two may be separated by whitespace. Either part
  // can be a {class.method} expression.
  private static final Pattern pattern = Pattern
      .compile("((?:\\{[\\w\\.]+\\})|[\\+\\-]?[\\d\\.]+)\\s*(\\{?[\\w\\.\\%]*\\}?)?");

  private final MortalLogger logger;
  private final DoubleAttributeParser doubleParser;
  private final EnumAttributeParser enumParser;

  LengthAttributeParser(DoubleAttributeParser doubleParser, EnumAttributeParser enumParser,
      MortalLogger logger) {
    this.doubleParser = doubleParser;
    this.enumParser = enumParser;
    this.logger = logger;
  }

  public String parse(XMLElement source, String lengthStr) throws UnableToCompleteException {
    Matcher matcher = pattern.matcher(lengthStr);
    if (!matcher.matches()) {
      logger.die(source, "Unable to parse %s as length", lengthStr);
    }

    String valueStr = matcher.group(1);
    String value = doubleParser.parse(source, valueStr);

    String unit = null;
    String unitStr = matcher.group(2);
    if (unitStr.length() > 0) {
      if (!unitStr.startsWith("{")) {
        // For non-refs, convert % => PCT, px => PX, etc.
        if ("%".equals(unitStr)) {
          unitStr = "PCT";
        }
        unitStr = unitStr.toUpperCase(Locale.ROOT);
      }

      // Now let the default enum parser handle it.
      unit = enumParser.parse(source, unitStr);
    } else {
      // Use PX by default.
      unit = UNIT + ".PX";
    }

    return value + ", " + unit;
  }
}
