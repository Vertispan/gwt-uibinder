/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.uibinder.processor;

/**
 *
 */
public enum UiBinderApiPackage {
  /**
   * Intended to bridge
   */
  BRIDGE(
      "urn:ui:com.google.gwt.uibinder",
      "org.gwtproject.uibinder.client",
      "com.google.gwt.dom.client",
      "com.google.gwt.i18n.client",
      "com.google.gwt.safehtml"
  ),
  COM_GOOGLE_GWT_UIBINDER(
      "urn:ui:com.google.gwt.uibinder",
      "com.google.gwt.uibinder.client",
      "com.google.gwt.dom.client",
      "com.google.gwt.i18n.client",
      "com.google.gwt.safehtml"
  ),
  ORG_GWTPROJECT_UIBINDER(
      "urn:ui:com.google.gwt.uibinder",
      "org.gwtproject.uibinder.client",
      "org.gwtproject.dom.client",
      "com.google.gwt.i18n.client",
      "org.gwtproject.safehtml"
  );

  /**
   * This is the same irregardless of package source.
   */
  public static final String UITEMPLATE = "org.gwtproject.uibinder.client.UiTemplate";


  /**
   * Determines which API by the old vs new UiBinder interface.
   */
  public static UiBinderApiPackage fromInterfaceName(String interfaceName) {
    if (COM_GOOGLE_GWT_UIBINDER.getUiBinderInterfaceFqn().equals(interfaceName)) {
      return COM_GOOGLE_GWT_UIBINDER;
    }
    if (ORG_GWTPROJECT_UIBINDER.getUiBinderInterfaceFqn().equals(interfaceName)) {
      return ORG_GWTPROJECT_UIBINDER;
    }
    throw new IllegalArgumentException();
  }

  private final String binderUri;
  private final String uiBinderPackageName;
  private final String domPackageName;
  private final String i18nPackageName;
  private final String safeHtmlPackageName;

  UiBinderApiPackage(
      String binderUri,
      String uiBinderPackageName,
      String domPackageName,
      String i18nPackageName,
      String safeHtmlPackageName) {
    this.binderUri = binderUri;
    this.uiBinderPackageName = uiBinderPackageName;
    this.domPackageName = domPackageName;
    this.i18nPackageName = i18nPackageName;
    this.safeHtmlPackageName = safeHtmlPackageName;
  }

  public String getBinderUri() {
    return binderUri;
  }

  public String getElementParserPackageName() {
    if (ORG_GWTPROJECT_UIBINDER.equals(this)) {
      return "org.gwtproject.uibinder.processor.elementparsers";
    }
    return "com.google.gwt.uibinder.elementparsers";
  }

  public String getDomElementFqn() {
    return domPackageName + ".Element";
  }

  public String getI18nMessagesInterfaceFqn() {
    return i18nPackageName + ".Messages";
  }

  public String getLazyDomElementFqn() {
    return uiBinderPackageName + ".LazyDomElement";
  }

  public String getSafeHtmlInterfaceFqn() {
    return safeHtmlPackageName + ".shared.SafeHtml";
  }

  public String getSafeHtmlTemplatesInterfaceFqn() {
    return safeHtmlPackageName + ".client.SafeHtmlTemplates";
  }

  public String getUiBinderInterfaceFqn() {
    return uiBinderPackageName + ".UiBinder";
  }

  public String getUiBinderUtilFqn() {
    return uiBinderPackageName + ".UiBinderUtil";
  }

  public String getUiChildFqn() {
    return uiBinderPackageName + ".UiChild";
  }

  public String getUiConstructorFqn() {
    return uiBinderPackageName + ".UiConstructor";
  }

  public String getUiFactoryFqn() {
    return uiBinderPackageName + ".UiFactory";
  }

  public String getUiFieldFqn() {
    return uiBinderPackageName + ".UiField";
  }

  public String getUiHandlerFqn() {
    return uiBinderPackageName + ".UiHandler";
  }

  public String getUiRendererInterfaceFqn() {
    return uiBinderPackageName + ".UiRenderer";
  }
}
