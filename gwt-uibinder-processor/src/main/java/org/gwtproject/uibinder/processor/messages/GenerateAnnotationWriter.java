package org.gwtproject.uibinder.processor.messages;

import org.gwtproject.uibinder.processor.IndentedWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@literal @}Generate annotation in a Messages interface, and can write it out at
 * code gen time.
 */
class GenerateAnnotationWriter {

  private static String toArgsList(List<String> strings) {
    StringBuilder b = new StringBuilder();
    boolean first = true;
    for (String s : strings) {
      if (first) {
        first = false;
      } else {
        b.append(",\n  ");
      }
      b.append(s);
    }
    return b.toString();
  }

  private static String toArrayLiteral(String[] strings) {
    StringBuilder b = new StringBuilder("{");
    for (String s : strings) {
      b.append(String.format("\"%s\", ", s));
    }
    b.append('}');
    return b.toString();
  }

  private final String[] formats;
  private final String fileName;
  private final String[] locales;

  public GenerateAnnotationWriter(String[] formats, String fileName,
      String[] locales) {
    this.formats = formats;
    this.fileName = fileName;
    this.locales = locales;
  }

  public void write(IndentedWriter w) {
    boolean hasFormats = formats.length > 0;
    boolean hasFileName = fileName.length() > 0;
    boolean hasLocales = locales.length > 0;

    if (hasFormats || hasFileName || hasLocales) {
      List<String> args = new ArrayList<String>();
      if (hasFormats) {
        args.add(String.format("format = %s", toArrayLiteral(formats)));
      }
      if (hasFileName) {
        args.add(String.format("fileName = \"%s\"", fileName));
      }
      if (hasLocales) {
        args.add(String.format("locales = %s", toArrayLiteral(locales)));
      }

      w.write("@Generate(");
      w.indent();
      w.write(toArgsList(args));
      w.outdent();
      w.write(")");
    }
  }
}
