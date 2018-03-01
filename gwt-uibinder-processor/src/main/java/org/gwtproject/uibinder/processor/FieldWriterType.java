package org.gwtproject.uibinder.processor;

/**
 * A simple enum holding all FieldWriter types.
 */
public enum FieldWriterType {
  GENERATED_BUNDLE(6),
  GENERATED_CSS(5),
  IMPORTED(4),  // ui:with clauses.
  DOM_ID_HOLDER(3),
  RENDERABLE_STAMPER(2),
  DEFAULT(1);

  /**
   * Holds the build precedence for this type. This is used when sorting the field builders in the
   * Widgets constructor. {@see com.google.gwt.uibinder.rebind.initializeWidgetsInnerClass}
   */
  private int buildPrecedence;

  private FieldWriterType(int precedence) {
    this.buildPrecedence = precedence;
  }

  public int getBuildPrecedence() {
    return buildPrecedence;
  }
}
