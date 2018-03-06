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
