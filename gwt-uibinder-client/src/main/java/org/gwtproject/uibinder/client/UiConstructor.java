package org.gwtproject.uibinder.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor that may be used as an alternative to a widget's zero args construtor in a
 * {@link UiBinder} template. The parameter names of the constructor may be filled as xml element
 * attribute values.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface UiConstructor {

}
