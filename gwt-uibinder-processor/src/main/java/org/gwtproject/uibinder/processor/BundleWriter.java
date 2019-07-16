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

import static org.gwtproject.uibinder.processor.AptUtil.asQualifiedNameable;

import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.model.ImplicitClientBundle;
import org.gwtproject.uibinder.processor.model.ImplicitCssResource;
import org.gwtproject.uibinder.processor.model.ImplicitDataResource;
import org.gwtproject.uibinder.processor.model.ImplicitImageResource;

import java.util.Collection;
import java.util.Set;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Writes source implementing an {@link ImplicitClientBundle}.
 */
public class BundleWriter {

  private final ImplicitClientBundle bundleClass;
  private final IndentedWriter writer;
  private final PrintWriterManager writerManager;

  private final MortalLogger logger;

  private final TypeMirror clientBundleType;
  private final TypeMirror dataResourceType;
  private final TypeMirror doNotEmbedType;
  private final TypeMirror dataMimeTypeType;
  private final TypeMirror imageOptionType;
  private final TypeMirror imageResourceType;
  private final TypeMirror repeatStyleType;
  private final TypeMirror importAnnotationType;
  private TypeMirror resourceAnnotationType;

  public BundleWriter(ImplicitClientBundle bundleClass, PrintWriterManager writerManager,
      MortalLogger logger) {
    this.bundleClass = bundleClass;
    this.writer = new IndentedWriter(
        writerManager.makePrintWriterFor(bundleClass.getClassName()));
    this.writerManager = writerManager;
    this.logger = logger;
    Elements elementUtils = AptUtil.getElementUtils();

    UiBinderApiPackage api = UiBinderApiPackage.current();
    clientBundleType = elementUtils.getTypeElement(api.getClientBundleFqn()).asType();
    dataResourceType = elementUtils.getTypeElement(api.getDataResourceFqn()).asType();
    doNotEmbedType = elementUtils.getTypeElement(api.getDataResourceDoNotEmbedFqn()).asType();
    dataMimeTypeType = elementUtils.getTypeElement(api.getDataResourceMimeTypeFqn()).asType();
    imageOptionType = elementUtils.getTypeElement(api.getImageResourceImageOptionsFqn()).asType();
    imageResourceType = elementUtils.getTypeElement(api.getImageResourceFqn()).asType();
    repeatStyleType = elementUtils.getTypeElement(api.getImageResourceRepeatStyleFqn()).asType();
    importAnnotationType = elementUtils.getTypeElement(api.getCssResourceImportFqn()).asType();
    if (!api.isGwtCreateSupported()) {
      resourceAnnotationType = elementUtils.getTypeElement(api.getResourceAnnotationImportFqn()).asType();
    }
  }

  public void write() throws UnableToCompleteException {
    writeBundleClass();
    for (ImplicitCssResource css : bundleClass.getCssMethods()) {
      new CssResourceWriter(css, writerManager.makePrintWriterFor(css.getClassName()), logger)
          .write();
    }
  }

  private void writeBundleClass() {
    // Package declaration
    String packageName = bundleClass.getPackageName();
    if (packageName.length() > 0) {
      writer.write("package %1$s;", packageName);
      writer.newline();
    }

    // Imports
    writer.write("import %s;", asQualifiedNameable(clientBundleType).getQualifiedName());
    writer.write("import %s;", asQualifiedNameable(dataResourceType).getQualifiedName());
    writer.write("import %s;", asQualifiedNameable(doNotEmbedType).getQualifiedName());
    writer.write("import %s;", asQualifiedNameable(dataMimeTypeType).getQualifiedName());
    writer.write("import %s;", asQualifiedNameable(imageResourceType).getQualifiedName());
    writer.write("import %s;", asQualifiedNameable(imageOptionType).getQualifiedName());
    writer.write("import %s;", asQualifiedNameable(importAnnotationType).getQualifiedName());
    if (UiBinderApiPackage.current()
            .equals(UiBinderApiPackage.ORG_GWTPROJECT_UIBINDER)) {
      writer.write("import %s;", asQualifiedNameable(resourceAnnotationType).getQualifiedName());
    }
    writer.newline();


    if (UiBinderApiPackage.current()
            .equals(UiBinderApiPackage.ORG_GWTPROJECT_UIBINDER)) {
      // Add @Resource annotation,
      writer.write("@Resource");
    }
    // Open interface
    writer.write("public interface %s extends ClientBundle {",
        bundleClass.getClassName());
    writer.indent();

    // Write css methods
    for (ImplicitCssResource css : bundleClass.getCssMethods()) {
      writeCssSource(css);
      writeCssImports(css);
      writer.write("%s %s();", css.getClassName(), css.getName());
      writer.newline();
    }

    // Write data methods
    for (ImplicitDataResource data : bundleClass.getDataMethods()) {
      writer.write("@Source(\"%s\")", data.getSource());
      writer.newline();
      if (data.getDoNotEmbed() == Boolean.TRUE) {
        writer.write("@DoNotEmbed");
        writer.newline();
      }
      if (data.getMimeType() != null) {
        writer.write("@MimeType(\"%s\")", data.getMimeType());
        writer.newline();
      }
      writer
          .write("%s %s();", asQualifiedNameable(dataResourceType).getSimpleName(), data.getName());
      writer.newline();
    }

    writeImageMethods();

    // Close interface.
    writer.outdent();
    writer.write("}");
  }

  private void writeCssImports(ImplicitCssResource css) {
    Set<TypeMirror> importTypes = css.getImports();
    int numImports = importTypes.size();
    if (numImports > 0) {
      if (numImports == 1) {
        writer.write("@Import(%s.class)",
            asQualifiedNameable(importTypes.iterator().next()).getQualifiedName());
      } else {
        StringBuffer b = new StringBuffer();
        for (TypeMirror importType : importTypes) {
          if (b.length() > 0) {
            b.append(", ");
          }
          b.append(asQualifiedNameable(importType).getQualifiedName()).append(".class");
        }
        writer.write("@Import({%s})", b);
      }
    }
  }

  private void writeCssSource(ImplicitCssResource css) {
    Collection<String> sources = css.getSource();
    if (sources.size() == 1) {
      writer.write("@Source(\"%s\")", sources.iterator().next());
    } else {
      StringBuffer b = new StringBuffer();
      for (String s : sources) {
        if (b.length() > 0) {
          b.append(", ");
        }
        b.append('"').append(s).append('"');
      }
      writer.write("@Source({%s})", b);
    }
  }

  private void writeImageMethods() {
    for (ImplicitImageResource image : bundleClass.getImageMethods()) {
      if (null != image.getSource()) {
        writer.write("@Source(\"%s\")", image.getSource());
      }

      writeImageOptionsAnnotation(image.getFlipRtl(), image.getRepeatStyle());
      writer.write("%s %s();", asQualifiedNameable(imageResourceType).getSimpleName(),
          image.getName());
    }
  }

  private void writeImageOptionsAnnotation(Boolean flipRtl, /*RepeatStyle*/ Object repeatStyle) {
    if (flipRtl != null || repeatStyle != null) {
      StringBuilder b = new StringBuilder("@ImageOptions(");
      if (null != flipRtl) {
        b.append("flipRtl=").append(flipRtl);
        if (repeatStyle != null) {
          b.append(", ");
        }
      }
      if (repeatStyle != null) {
        b.append(
            String.format("repeatStyle=%s.%s", asQualifiedNameable(repeatStyleType).getSimpleName(),
                repeatStyle.toString()));
      }
      b.append(")");
      writer.write(b.toString());
    }
  }
}
