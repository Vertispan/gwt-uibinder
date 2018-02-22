package org.gwtproject.uibinder.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import javax.annotation.Generated;
import javax.tools.JavaFileObject;

public class SourceWriter {

  private static final String STAR_COMMENT_LINE = " * ";

  private final ClassSourceFactory factory;
  private PrintWriter pw;
  private int indentLevel = 0;
  private boolean atStart = false;
  private boolean inComment;
  private String commentIndicator;
  private JavaFileObject file;

  /**
   * Constructs a SourceWriter.
   *
   * <p>Note that all imports, implemented interfaces, etc should be added to the factory prior to
   * constructing.
   */
  public SourceWriter(ClassSourceFactory factory) {
    this.factory = factory;

    initializePrintWriter();

    factory.addImport(Generated.class);

    println("package %1$s;", factory.getPackageName());
    pw.println();
    for (String anImport : factory.getImports()) {
      println("import %1$s;", anImport);
    }
    pw.println();

    String currentDateTime = DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now());
    println("@Generated(value = \"%1$s\",", factory.getProcessorClass().getCanonicalName());
    indent(6);
    println("date = \"%1$s\")", currentDateTime);
    outdent(6);

    print("public%1$s class %2$s ",
        factory.isFinalClass() ? " final" : "",
        factory.getSimpleClassname());

    // class extension
    if (factory.getSuperClass() != null) {
      print("extends %1$s ", factory.getSuperClass());
    }

    // interfaces
    if (!factory.getImplementedInterfaces().isEmpty()) {
      print("implements ");
      for (Iterator<String> iterator = factory.getImplementedInterfaces().iterator();
          iterator.hasNext(); ) {
        String implementedInterface = iterator.next();
        print(implementedInterface);
        if (iterator.hasNext()) {
          print(", ");
        }
      }
    }
    println(" {");

    indent();
  }

  public void beginJavaDocComment() {
    println("\n/**");
    inComment = true;
    commentIndicator = STAR_COMMENT_LINE;
  }

  public void endJavaDocComment() {
    inComment = false;
    String end = " */";
    if (!atStart) {
      end = "\n" + end;
    }
    println(end);
  }

  public void print(String s) {
    int pos = 0;
    for (; ; ) {
      if (atStart) {
        for (int j = 0; j < indentLevel; ++j) {
          pw.print("  ");
        }
        if (inComment) {
          pw.print(commentIndicator);
        }
        atStart = false;
      }

      int nl = s.indexOf('\n', pos);

      if (nl == -1 || nl == s.length() - 1) {
        pw.write(s, pos, s.length() - pos);
        return;
      }

      pw.write(s, pos, nl + 1 - pos);
      atStart = true;
      pos = nl + 1;
    }
  }

  public void print(String s, Object... args) {
    print(String.format(s, args));
  }

  public void println(String s) {
    print(s + "\n");
    atStart = true;
  }

  public void println() {
    println("");
  }

  public void println(String s, Object... args) {
    println(String.format(s, args));
  }

  public void comment(String s, Object... args) {
    print("// ");
    println(s, args);
  }

  public void indent() {
    indentLevel++;
  }

  public void indent(int level) {
    indentLevel += level;
  }

  public void outdent() {
    indentLevel--;
  }

  public void outdent(int level) {
    indentLevel -= level;
  }

  public void commit() {
    outdent();
    pw.println("}");
    pw.close();
  }

  public void rollback() {
    if (file != null) {
      file.delete();
    }
  }

  private void initializePrintWriter() {
    try {
      file = factory.getFiler()
          .createSourceFile(factory.getClassname(), factory.getOriginatingElements());
      pw = new PrintWriter(file.openWriter());
    } catch (IOException e) {
      rollback();
      throw new RuntimeException("Cannot create File " + factory.getClassname(), e);
    }
  }
}
