package org.gwtproject.uibinder.client;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method that may be called as an alternative to a GWT.create call in a {@link UiBinder}
 * template. The parameter names of the method are treated as required xml element attribute
 * values.
 *
 * <p>It is an error to apply this annotation to more than one method of a given return type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UiFactory {

}
