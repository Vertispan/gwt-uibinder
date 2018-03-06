package org.gwtproject.uibinder.client;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as the appropriate way to add a child widget to the parent class.
 *
 * <p> The limit attribute specifies the number of times the function can be safely called. If no
 * limit is specified, it is assumed to be unlimited. Only one child is permitted under each custom
 * tag specified so the limit represents the number of times the tag can be present in any object.
 *
 * <p> The tagname attribute indicates the name of the tag this method will handle in the {@link
 * UiBinder} template. If none is specified, the method name must begin with "add", and the tag is
 * assumed to be the remaining characters (after the "add" prefix") entirely in lowercase.
 *
 * <p> For example, <code>
 *
 * &#064;UiChild MyWidget#addCustomChild(Widget w) </code> and
 *
 * <pre>
 *   &lt;p:MyWidget>
 *     &lt;p:customchild>
 *       &lt;g:SomeWidget />
 *     &lt;/p:customchild>
 *   &lt;/p:MyWidget>
 * </pre>
 * would invoke the <code>addCustomChild</code> function to add an instance of SomeWidget.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UiChild {

  int limit() default -1;

  String tagname() default "";
}
