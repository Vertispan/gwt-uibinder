package org.gwtproject.uibinder.processor.model;

import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Models a method returning an ImageResource on a generated ClientBundle.
 */
public class ImplicitImageResource {

  private final String name;
  private final String source;
  private final Boolean flipRtl;
  private final RepeatStyle repeatStyle;

  ImplicitImageResource(
      String name, String source, Boolean flipRtl, RepeatStyle repeatStyle) {
    this.name = name;
    this.source = source;
    this.flipRtl = flipRtl;
    this.repeatStyle = repeatStyle;
  }

  public Boolean getFlipRtl() {
    return flipRtl;
  }

  public String getName() {
    return name;
  }

  public RepeatStyle getRepeatStyle() {
    return repeatStyle;
  }

  public String getSource() {
    return source;
  }
}
