package org.gwtproject.uibinder.processor.model;

/**
 * Models a method returning a DataResource on a generated ClientBundle.
 */
public class ImplicitDataResource {

  private final String name;
  private final String mimeType;
  private final String source;
  private final Boolean doNotEmbed;

  ImplicitDataResource(String name, String source, String mimeType, Boolean doNotEmbed) {
    this.name = name;
    this.source = source;
    this.mimeType = mimeType;
    this.doNotEmbed = doNotEmbed;
  }

  public String getName() {
    return name;
  }

  public String getSource() {
    return source;
  }

  public String getMimeType() {
    return mimeType;
  }

  public Boolean getDoNotEmbed() {
    return doNotEmbed;
  }
}
