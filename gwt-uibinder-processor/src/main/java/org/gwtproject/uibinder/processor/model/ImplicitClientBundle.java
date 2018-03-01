package org.gwtproject.uibinder.processor.model;

import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.gwtproject.uibinder.processor.MortalLogger;

/**
 * Models the ClientBundle to be generated from a ui.xml.
 */
public class ImplicitClientBundle {

  // LinkedHashSets for consistent order across recompiles
  private final LinkedHashSet<ImplicitCssResource> cssMethods = new LinkedHashSet<>();
  private final LinkedHashSet<ImplicitImageResource> imageMethods = new LinkedHashSet<>();
  private final LinkedHashSet<ImplicitDataResource> dataMethods = new LinkedHashSet<>();
  private final String packageName;
  private final String className;
  private final String fieldName;
  private final String cssBaseName;
  private final MortalLogger logger;

  /**
   * @param packageName Where the bundle should live
   * @param uiBinderImplClassName The name of the generated ui binder implementation that owns the
   * bundle
   * @param fieldName The bundle's field name
   */
  public ImplicitClientBundle(String packageName, String uiBinderImplClassName,
      String fieldName, MortalLogger logger) {
    this.packageName = packageName;
    this.className = uiBinderImplClassName + "_GenBundle";
    this.cssBaseName = uiBinderImplClassName + "_GenCss_";
    this.fieldName = fieldName;
    this.logger = logger;
  }

  /**
   * Called to declare a new CssResource accessor on this bundle.
   *
   * @param name the method name and the ui:field name
   * @param source path to the .css file resource
   * @param extendedInterface the public interface implemented by this CssResource, or null
   * @param body the inline css text
   * @param importTypes for the {@literal @}Import annotation, if any. LinkedHashSet to enforce
   * deterministic order across recompiles
   * @param gss indicates that GSS is used or not
   * @param resourceOracle from which to load resources
   * @return the newly-created CssResource
   * TODO createCssResource needs update /
  public ImplicitCssResource createCssResource(String name, String[] source,
      JClassType extendedInterface, String body, LinkedHashSet<JClassType> importTypes,
      Boolean gss, ResourceOracle resourceOracle) {
    ImplicitCssResource css = new ImplicitCssResource(packageName, cssBaseName + name, name, source,
        extendedInterface, body, logger, importTypes, gss, resourceOracle);
    cssMethods.add(css);
    return css;
  }
  */

  /**
   * Called to declare a new DataResource accessor on this bundle. All params must be non-null
   *
   * @param name the method name and the ui:field name
   * @param source path to the resource
   * @param mimeType type of the resource
   * @param doNotEmbed flag to prevent embedding the data resource
   * @return the newly-created DataResource
   */
  public ImplicitDataResource createDataResource(
      String name, String source, String mimeType, Boolean doNotEmbed) {
    ImplicitDataResource data = new ImplicitDataResource(name, source, mimeType, doNotEmbed);
    dataMethods.add(data);
    return data;
  }

  /**
   * Called to declare a new ImageResource accessor on this bundle.
   *
   * @param name the method name and the ui:field name
   * @param source path to the image resource, or null if none was specified
   * @param flipRtl value for the flipRtl ImageOption, or null if none was specified
   * @param repeatStyle value of the RepeatStyle ImageOption, or null if none was specified
   * @return the newly-created ImageResource
   */
  public ImplicitImageResource createImageResource(String name, String source, Boolean flipRtl,
      RepeatStyle repeatStyle) {
    ImplicitImageResource image = new ImplicitImageResource(name, source, flipRtl, repeatStyle);
    imageMethods.add(image);
    return image;
  }

  public String getClassName() {
    return className;
  }

  public Set<ImplicitCssResource> getCssMethods() {
    return Collections.unmodifiableSet(cssMethods);
  }

  public Set<ImplicitDataResource> getDataMethods() {
    return Collections.unmodifiableSet(dataMethods);
  }

  public String getFieldName() {
    return fieldName;
  }

  public Set<ImplicitImageResource> getImageMethods() {
    return Collections.unmodifiableSet(imageMethods);
  }

  public String getPackageName() {
    return packageName;
  }
}
