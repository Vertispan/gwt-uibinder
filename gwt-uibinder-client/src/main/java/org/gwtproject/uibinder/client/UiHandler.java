package org.gwtproject.uibinder.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be automatically bound as an event handler. See examples in {@code
 * com.google.gwt.uibinder.test.client.HandlerDemo}.
 *
 * <p>The annotation values must be declared in the "ui:field" template attribute.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UiHandler {

  String[] value();
}
