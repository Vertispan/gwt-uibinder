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
      "com.google.gwt.safehtml",
      "com.google.gwt.resources.client",
      "com.google.gwt.event"
  ),
  ORG_GWTPROJECT_UIBINDER(
      false, //FIXME- update- this have issues with GSS, Messages, etc.
      "urn:ui:org.gwtproject.uibinder",// changing this will require updates to .xsd files
      "org.gwtproject.uibinder.client",
      "org.gwtproject.dom.client",
      "org.gwtproject.user.client.ui",
      "com.google.gwt.i18n.client",
      "org.gwtproject.safehtml",
      "org.gwtproject.resources.client",
      "org.gwtproject.event.legacy"
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
  private final String resourcesPackageName;
  private final String eventsPackageName;


  UiBinderApiPackage(String uiBinderPackageName) {
    this(true,
        null,
        uiBinderPackageName,
        null,
        null,
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
      String safeHtmlPackageName,
      String resourcesPackageName,
      String eventsPackageName) {
    this.gwtCreateSupported = isGwtCreateSupported;
    this.binderUri = binderUri;
    this.uiBinderPackageName = uiBinderPackageName;
    this.domPackageName = domPackageName;
    this.widgetsPackageName = widgetsPackageName;
    this.i18nPackageName = i18nPackageName;
    this.safeHtmlPackageName = safeHtmlPackageName;
    this.resourcesPackageName = resourcesPackageName;
    this.eventsPackageName = eventsPackageName;
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

  public String getAbstractUiRendererFqn() {
    return uiBinderPackageName + ".AbstractUiRenderer";
  }

  public String getClientBundleFqn() {
    return resourcesPackageName + ".ClientBundle";
  }

  public String getCommandFqn() {
    // Command is in parent package of the ui widgets
    String thePackage = widgetsPackageName.replaceAll("\\.ui$", "");
    return thePackage + ".Command";
  }

  public String getCssResourceFqn() {
    return resourcesPackageName + ".CssResource";
  }

  public String getCssResourceImportFqn() {
    return resourcesPackageName + ".CssResource.Import";
  }

  public String getResourceAnnotationImportFqn() {
    return resourcesPackageName + ".Resource";
  }

  public String getDataResourceFqn() {
    return resourcesPackageName + ".DataResource";
  }

  public String getDataResourceDoNotEmbedFqn() {
    return resourcesPackageName + ".DataResource.DoNotEmbed";
  }

  public String getDataResourceMimeTypeFqn() {
    return resourcesPackageName + ".DataResource.MimeType";
  }

  public String getDialogBoxFqn() {
    return widgetsPackageName + ".DialogBox";
  }

  public String getDialogBoxCaptionFqn() {
    return widgetsPackageName + ".DialogBox.Caption";
  }

  public String getDockPanelFqn() {
    return widgetsPackageName + ".DockPanel";
  }

  public String getDomDocumentFqn() {
    return domPackageName + ".Document";
  }

  public String getDomElementFqn() {
    return domPackageName + ".Element";
  }

  public String getDomNativeEventFqn() {
    return domPackageName + ".NativeEvent";
  }

  public String getDomStyleUnitFqn() {
    if (COM_GOOGLE_GWT_UIBINDER.equals(this)) {
      // in org.gwtproject, the Unit inside Style is not an enum.
      return domPackageName + ".Style.Unit";
    }
    return "org.gwtproject.dom.style.shared" + ".Unit";
  }

  public String getElementParserPackageName() {
    return "org.gwtproject.uibinder.processor.elementparsers";
  }

  public String getEventHandlerFqn() {
    return eventsPackageName + ".shared.EventHandler";
  }

  public String getGwtEventFqn() {
    return eventsPackageName + ".shared.GwtEvent";
  }

  public String getHandlerRegistrationFqn() {
    String thePackage = eventsPackageName;
    if (ORG_GWTPROJECT_UIBINDER.equals(this)) {
      // using the 'legacy package' in the other place for ORG
      thePackage = "org.gwtproject.event";
    }
    return thePackage + ".shared.HandlerRegistration";
  }

  public String getHasHorizontalAlignmentFqn() {
    return widgetsPackageName + ".HasHorizontalAlignment";
  }

  public String getHasHTMLFqn() {
    return widgetsPackageName + ".HasHTML";
  }

  public String getHasText() {
    return widgetsPackageName + ".HasText";
  }

  public String getHasVerticalAlignmentFqn() {
    return widgetsPackageName + ".HasVerticalAlignment";
  }

  public String getHorizontalAlignmentConstantFqn() {
    return widgetsPackageName + ".HasHorizontalAlignment.HorizontalAlignmentConstant";
  }

  public String getI18nCurrencyDataFqn() {
    return i18nPackageName + ".CurrencyData";
  }

  public String getI18nDateTimeFormatFqn() {
    return i18nPackageName + ".DateTimeFormat";
  }

  public String getI18nDateTimeFormatPredefinedFormatFqn() {
    return i18nPackageName + ".DateTimeFormat.PredefinedFormat";
  }

  public String getI18nLocalizableResourceFqn() {
    return i18nPackageName + ".LocalizableResource";
  }

  public String getI18nMessagesInterfaceFqn() {
    return i18nPackageName + ".Messages";
  }

  public String getI18nNumberFormatFqn() {
    return i18nPackageName + ".NumberFormat";
  }

  public String getI18nTimeZoneFqn() {
    return i18nPackageName + ".TimeZone";
  }

  public String getImageResourceFqn() {
    return resourcesPackageName + ".ImageResource";
  }

  public String getImageResourceImageOptionsFqn() {
    return resourcesPackageName + ".ImageResource.ImageOptions";
  }

  public String getImageResourceRepeatStyleFqn() {
    return resourcesPackageName + ".ImageResource.RepeatStyle";
  }

  public String getImageFqn() {
    return widgetsPackageName + ".Image";
  }

  public String getIsRenderableFqn() {
    return widgetsPackageName + ".IsRenderable";
  }

  public String getIsWidgetFqn() {
    return widgetsPackageName + ".IsWidget";
  }

  public String getLazyDomElementFqn() {
    String thePackage = uiBinderPackageName;

    if (this == COM_GOOGLE_GWT_UIBINDER) {
      // wrong package for lazy dom
      thePackage = LEGACY.uiBinderPackageName;
    }
      return thePackage + ".LazyDomElement";
  }

  public String getLazyPanelFqn() {
    return widgetsPackageName + ".LazyPanel";
  }

  public String getMenuBarFqn() {
    return widgetsPackageName + ".MenuBar";
  }

  public String getMenuItemFqn() {
    return widgetsPackageName + ".MenuItem";
  }

  public String getMenuItemSeparatorFqn() {
    return widgetsPackageName + ".MenuItemSeparator";
  }

  public String getRenderablePanelFqn() {
    return widgetsPackageName + ".RenderablePanel";
  }

  public String getRenderableStamperFqn() {
    return widgetsPackageName + ".RenderableStamper";
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

  public String getSplitLayoutPanelFqn() {
    return widgetsPackageName + ".SplitLayoutPanel";
  }

  public String getTextBoxBaseFqn() {
    return widgetsPackageName + ".TextBoxBase";
  }

  public String getTextBoxBaseTextAlignConstantFqn() {
    return widgetsPackageName + ".TextBoxBase.TextAlignConstant";
  }

  public String getTreeItemFqn() {
    return widgetsPackageName + ".TreeItem";
  }

  public String getUiBinderInterfaceFqn() {
    return uiBinderPackageName + ".UiBinder";
  }

  public String getUiBinderUtilFqn() {
    // this is one place that we have to use the old vs new appropriately
    // TODO - make sure we synchronize with enum
    if (this == COM_GOOGLE_GWT_UIBINDER) {
      return LEGACY.getUiBinderUtilFqn();
    }
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

  public String getVerticalAlignmentConstantFqn() {
    return widgetsPackageName + ".HasVerticalAlignment.VerticalAlignmentConstant";
  }

  public String getWidgetFqn() {
    return widgetsPackageName + ".Widget";
  }
}
