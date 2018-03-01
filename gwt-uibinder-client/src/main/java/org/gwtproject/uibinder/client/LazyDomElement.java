package org.gwtproject.uibinder.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

/**
 * Wraps a call to a DOM element. LazyDomElement can boost performance of html
 * elements and delay calls to getElementById() to when the element is actually
 * used. But note that it will throw a RuntimeException in case the element is
 * accessed but not yet attached in the DOM tree.
 * <p>
 * Usage example:
 * <p>
 * <b>Template:</b>
 * <pre>
 *   &lt;gwt:HTMLPanel&gt;
 *      &lt;div ui:field="myDiv" /&gt;
 *   &lt;/gwt:HTMLPanel&gt;
 * </pre>
 * <p>
 * <b>Class:</b>
 * <pre>
 *   {@literal @}UiField LazyDomElement&lt;DivElement&gt; myDiv;
 *
 *   public setText(String text) {
 *     myDiv.get().setInnerHtml(text);
 *   }
 * </pre>
 *
 * @param <T> the Element type associated
 */
public class LazyDomElement<T extends Element> {

  private T element;
  private final String domId;

 /**
  * Creates an instance to fetch the element with the given id.
  */
  public LazyDomElement(String domId) {
    this.domId = domId;
  }

 /**
  * Returns the dom element.
  *
  * @return the dom element
  * @throws RuntimeException if the element cannot be found
  */
  public T get() {
    if (element == null) {
      element = Document.get().getElementById(domId).<T>cast();
      if (element == null) {
        throw new RuntimeException("Cannot find element with id \"" + domId
            + "\". Perhaps it is not attached to the document body.");
      }
      element.removeAttribute("id");
    }
    return element;
  }
}
