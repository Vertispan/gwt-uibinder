package org.gwtproject.uibinder.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks fields in a UiBinder client that must be filled by the binder's {@link
 * UiBinder#createAndBindUi} method. If provided is true the field creation is delegated to the
 * client (owner).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiField {

  /**
   * If true, the field must be filled before {@link UiBinder#createAndBindUi} is called. If false,
   * {@link UiBinder#createAndBindUi} will fill the field, usually by calling {@link
   * com.google.gwt.core.client.GWT#create}.
   */
  boolean provided() default false;
}
