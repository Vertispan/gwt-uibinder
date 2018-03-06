package org.gwtproject.uibinder.processor;

import java.io.PrintWriter;

/**
 * Pleasant wrapper for PrintWriter, manages indentation levels. Name is a misnomer, as this doesn't
 * implement Writer.
 */
public class IndentedWriter {

  private final PrintWriter pw;

  private int indent;

  public IndentedWriter(PrintWriter pw) {
    super();
    this.pw = pw;
  }

  /**
   * Indents the generated code.
   */
  public void indent() {
    ++indent;
  }

  /**
   * Outputs a new line.
   */
  public void newline() {
    // Unix-style line endings for consistent behavior across platforms.
    pw.print('\n');
  }

  /**
   * Un-indents the generated code.
   */
  public void outdent() {
    if (indent == 0) {
      throw new IllegalStateException("Tried to outdent below zero");
    }
    --indent;
  }

  /**
   * Outputs the given string.
   */
  public void write(String format) {
    printIndent();
    pw.print(format);
    newline();
  }

  /**
   * Outputs the given string with replacements, using the Java message format.
   */
  public void write(String format, Object... args) {
    printIndent();
    pw.printf(format, args);
    newline();
  }

  private void printIndent() {
    for (int i = 0; i < indent; ++i) {
      pw.print("  ");
    }
  }
}
