package org.gwtproject.uibinder.processor.attributeparsers;


import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * Parses an enum attribute.
 */
class EnumAttributeParser extends StrictAttributeParser {

  private final Map<String, Element> values = new HashMap<>();

  EnumAttributeParser(FieldReferenceConverter converter, TypeMirror enumType, MortalLogger logger) {
    super(converter, logger, enumType);

    List<Element> enumValues = AptUtil.getEnumValues(AptUtil.asTypeElement(enumType));
    for (Element c : enumValues) {
      values.put(c.getSimpleName().toString(), c);
    }
  }

  @Override
  public String parse(XMLElement source, String value) throws UnableToCompleteException {
    Element c = values.get(value);
    if (c != null) {
      return String.format("%s.%s",
          AptUtil.asTypeElement(c.getEnclosingElement()).getQualifiedName().toString(), value);
    }
    return super.parse(source, value);
  }
}
