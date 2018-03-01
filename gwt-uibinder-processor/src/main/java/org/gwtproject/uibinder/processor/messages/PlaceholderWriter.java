package org.gwtproject.uibinder.processor.messages;

/**
 * Represents a parameter in a Messages interface method, and can write out its declaration.
 */
class PlaceholderWriter {

  private final String name;
  private final String example;
  private final String value;

  /**
   * @param name Parameter name for this placeholder
   * @param example Contents of the {@literal @}Example annotation
   * @param value The value to provide for this param when writing an invocation of its message
   * method.
   */
  public PlaceholderWriter(String name, String example, String value) {
    this.name = name;
    this.example = inQuotes(example);
    this.value = inQuotes(value);
  }

  public String getDeclaration() {
    return String.format("@Example(%s) String %s", example, name);
  }

  public String getValue() {
    return value;
  }

  private String inQuotes(String s) {
    return String.format("\"%s\"", s);
  }
}
