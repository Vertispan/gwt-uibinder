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
   * Completely legacy api.
   *
   * <p>This is really intended for the "fallback" of some of the UiBinder annotations.  Since
   * legacy widgets were most likely coded with the old namespaced @UiChild, etc, we need to be able
   * to fallback to those as necessary.
   */
  LEGACY("com.google.gwt.uibinder.client"),
  /**
   * Use majority of the legacy classes for generation.
   *
   * <p>Note that the uibinder package is the new one.  If a user wanted ALL legacy, they would
   * have used the GWT.create mechanism rather than using the new @UiTemplate annotation.
   */
  COM_GOOGLE_GWT_UIBINDER(
      true,
      "urn:ui:com.google.gwt.uibinder",
      "org.gwtproject.uibinder.client",
      "com.google.gwt.dom.client",
      "com.google.gwt.user.client.ui",
      "com.google.gwt.i18n.client",
      "com.google.gwt.safehtml"
  ),
  ORG_GWTPROJECT_UIBINDER(
      true, //FIXME- update- this will have issues with GSS, Messages, etc.
      "urn:ui:com.google.gwt.uibinder",
      "org.gwtproject.uibinder.client",
      "org.gwtproject.dom.client",
      "org.gwtproject.user.client.ui",
      "com.google.gwt.i18n.client",
      "org.gwtproject.safehtml"
  );

  /**
   * This is the same irregardless of package source.
   */
  public static final String UITEMPLATE = "org.gwtproject.uibinder.client.UiTemplate";

  /**
   * ThreadLocal for processing API.
   *
   * <p>Rather than passing this class around, we'll keep in a thread local to be available where
   * needed.
   */
  private static ThreadLocal<UiBinderApiPackage> uiBinderApiPackageThreadLocal = new ThreadLocal<>();

  public static void setUiBinderApiPackage(UiBinderApiPackage api) {
    if (api == null) {
      uiBinderApiPackageThreadLocal.remove();
    }
    uiBinderApiPackageThreadLocal.set(api);
  }

  public static UiBinderApiPackage current() {
    return uiBinderApiPackageThreadLocal.get();
  }

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

  private final boolean gwtCreateSupported;
  private final String binderUri;
  private final String uiBinderPackageName;
  private final String domPackageName;
  private final String widgetsPackageName;
  private final String i18nPackageName;
  private final String safeHtmlPackageName;

  UiBinderApiPackage(String uiBinderPackageName) {
    this(true,
        null,
        uiBinderPackageName,
        null,
        null,
        null,
        null);
  }

  UiBinderApiPackage(
      boolean isGwtCreateSupported,
      String binderUri,
      String uiBinderPackageName,
      String domPackageName,
      String widgetsPackageName,
      String i18nPackageName,
      String safeHtmlPackageName) {
    this.gwtCreateSupported = isGwtCreateSupported;
    this.binderUri = binderUri;
    this.uiBinderPackageName = uiBinderPackageName;
    this.domPackageName = domPackageName;
    this.widgetsPackageName = widgetsPackageName;
    this.i18nPackageName = i18nPackageName;
    this.safeHtmlPackageName = safeHtmlPackageName;
  }

  public String getBinderUri() {
    return binderUri;
  }

  public boolean isGwtCreateSupported() {
    return gwtCreateSupported;
  }

  /**
   * Gets the GWT fully qualified name.
   *
   * @deprecated shouldn't use GWT.create moving forward.
   */
  @Deprecated
  public String getGWTFqn() {
    return "com.google.gwt.core.client.GWT";
  }

  public String getElementParserPackageName() {
    return "org.gwtproject.uibinder.processor.elementparsers";
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

  public String getRenderablePanelFqn() {
    return widgetsPackageName + ".RenderablePanel";
  }

  public String getSafeHtmlBuilderFqn() {
    return safeHtmlPackageName + ".shared.SafeHtmlBuilder";
  }

  public String getSafeHtmlInterfaceFqn() {
    return safeHtmlPackageName + ".shared.SafeHtml";
  }

  public String getSafeHtmlTemplatesInterfaceFqn() {
    return safeHtmlPackageName + ".client.SafeHtmlTemplates";
  }

  public String getSafeHtmlUtilsFqn() {
    return safeHtmlPackageName + ".shared.SafeHtmlUtils";
  }

  public String getSafeHtmlUriUtilsFqn() {
    return safeHtmlPackageName + ".shared.UriUtils";
  }

  public String getSafeUriInterfaceFqn() {
    return safeHtmlPackageName + ".shared.SafeUri";
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
