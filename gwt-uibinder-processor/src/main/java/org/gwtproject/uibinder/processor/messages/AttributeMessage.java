package org.gwtproject.uibinder.processor.messages;

/**
 * Associates an attribute name and a message expression. Can generate code that refers to the
 * message.
 */
public class AttributeMessage {

  private final String attribute;
  private final String message;

  public AttributeMessage(String attribute, String message) {
    super();
    this.attribute = attribute;
    this.message = message;
  }

  public String getAttribute() {
    return attribute;
  }

  public String getMessageUnescaped() {
    return message;
  }
}
