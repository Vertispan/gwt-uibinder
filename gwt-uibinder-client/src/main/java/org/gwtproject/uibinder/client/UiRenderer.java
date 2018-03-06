package org.gwtproject.uibinder.client;

import com.google.gwt.dom.client.Element;

/**
 * Marker interface for classes whose implementation is to be provided via UiBinder code generation
 * for SafeHtml rendering.
 *
 * <p><span style='color: red'>This is experimental code in active development. It is unsupported,
 * and its api is subject to change.</span>
 */
public interface UiRenderer {

  /**
   * Checks whether {@code parent} is a valid element to use as an argument for field getters.
   *
   * @return {@code true} if parent contains or directly points to a previously rendered element. In
   * DevMode it also checks whether the parent is attached to the DOM
   */
  boolean isParentOrRenderer(Element parent);
}
