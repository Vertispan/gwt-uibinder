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

import org.gwtproject.uibinder.processor.ext.MyTreeLogger;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.MessagesWriter;
import org.gwtproject.uibinder.processor.model.ImplicitClientBundle;

import com.google.gwt.dev.util.Util;
import com.google.gwt.resources.rg.GssResourceGenerator.AutoConversionMode;
import com.google.gwt.resources.rg.GssResourceGenerator.GssOptions;

import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

/**
 *
 */
@SupportedAnnotationTypes(UiBinderApiPackage.UITEMPLATE)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class UiBinderProcessor extends BaseProcessor {

  private static final String TEMPLATE_SUFFIX = ".ui.xml";

  // TODO - naming strategy
  private static String deduceImplName(Element e) {
    String simpleSourceName = e.getSimpleName().toString() + "Impl";

    while (e.getEnclosingElement() instanceof TypeElement) {
      simpleSourceName = e.getEnclosingElement()
          .getSimpleName().toString() + "_" + simpleSourceName;
      e = e.getEnclosingElement();
    }

    return simpleSourceName;
  }

  /**
   * Given a UiBinder interface, return the path to its ui.xml file, suitable for any classloader to
   * find it as a resource.
   */
  private static String deduceTemplateFile(MortalLogger logger, TypeElement interfaceType)
      throws UnableToCompleteException {
    String templateName = "";
    AnnotationMirror uiTemplate = AptUtil
        .getAnnotation(interfaceType, UiBinderApiPackage.UITEMPLATE);

    Map<String, ? extends AnnotationValue> annotationValues = AptUtil
        .getAnnotationValues(uiTemplate);

    if (annotationValues.containsKey("value")) {
      templateName = (String) annotationValues.get("value").getValue();
    }

    if ("".equals(templateName)) {
      // value is empty, so use the name of the type
      if (interfaceType.getEnclosingElement() != null) {
        interfaceType = AptUtil.asTypeElement(interfaceType.getEnclosingElement());
      }
      return slashify(interfaceType.getQualifiedName().toString()) + TEMPLATE_SUFFIX;
    } else {
      if (!templateName.endsWith(TEMPLATE_SUFFIX)) {
        logger.die("Template file name must end with " + TEMPLATE_SUFFIX);
      }

      /*
       * If the template file name (minus suffix) has no dots, make it relative to the binder's
       * package, otherwise slashify the dots
       */
      String unsuffixed = templateName.substring(0, templateName.lastIndexOf(TEMPLATE_SUFFIX));
      if (!unsuffixed.contains(".")) {
        templateName =
            slashify(AptUtil.getPackageElement(interfaceType).getQualifiedName().toString()) + "/"
                + templateName;
      } else {
        templateName = slashify(unsuffixed) + TEMPLATE_SUFFIX;
      }
    }

    return templateName;
  }

  /**
   * Determine the api to use based on interface extension.
   */
  private static UiBinderApiPackage deduceApi(MortalLogger logger, TypeElement interfaceType)
      throws UnableToCompleteException {

    AnnotationMirror uiTemplate = AptUtil
        .getAnnotation(interfaceType, UiBinderApiPackage.UITEMPLATE);

    AnnotationValue legacyWidgets = AptUtil.getAnnotationValues(uiTemplate).get("legacyWidgets");

    if (legacyWidgets != null && Boolean.TRUE.equals(legacyWidgets.getValue())) {
      return UiBinderApiPackage.COM_GOOGLE_GWT_UIBINDER;
    }
    return UiBinderApiPackage.ORG_GWTPROJECT_UIBINDER;
  }

  private static String slashify(String s) {
    return s.replace(".", "/").replace("$", ".");
  }

  private final UiBinderContext uiBinderCtx = new UiBinderContext();

  @Override
  protected String processElement(TypeElement interfaceType, MyTreeLogger logger)
      throws UnableToCompleteException {
    String implName = deduceImplName(interfaceType);
    String packageName = processingEnv.getElementUtils().getPackageOf(interfaceType)
        .getQualifiedName().toString();
    PrintWriterManager writers = new PrintWriterManager(processingEnv, logger, packageName);
    PrintWriter printWriter = writers.tryToMakePrintWriterFor(implName);

    if (printWriter != null) {
      generateOnce(interfaceType, implName, printWriter, logger, writers);
    }

    return packageName + "." + implName;
  }

  private void generateOnce(TypeElement interfaceType, String implName,
      PrintWriter binderPrintWriter, MyTreeLogger treeLogger, PrintWriterManager writerManager)
      throws UnableToCompleteException {
    MortalLogger logger = new MortalLogger(treeLogger);
    UiBinderApiPackage api = deduceApi(logger, interfaceType);
    UiBinderApiPackage.setUiBinderApiPackage(api);
    String templatePath = deduceTemplateFile(logger, interfaceType);
    MessagesWriter messages = new MessagesWriter(api.getBinderUri(), logger, templatePath,
        AptUtil.getPackageElement(interfaceType).getQualifiedName().toString(), implName);
    FieldManager fieldManager = new FieldManager(logger, true);

    // TODO hardcoded gss options
    GssOptions gssOptions = new GssOptions(true, AutoConversionMode.STRICT, true);

    UiBinderWriter uiBinderWriter = new UiBinderWriter(interfaceType.asType(), implName,
        templatePath, logger, fieldManager, messages, uiBinderCtx, api.getBinderUri(), gssOptions);

    FileObject resource = getTemplateResource(logger, templatePath);

    // Ensure that generated uibinder source is modified at least as often as synthesized .cssmap
    // resources, otherwise it would be possible to synthesize a modified .cssmap resource but fail
    // to retrigger the InlineClientBundleGenerator that processes it.
    binderPrintWriter.println("// Template file: " + templatePath);
    binderPrintWriter.println("// .ui.xml template last modified: " + resource.getLastModified());
    Document doc = getW3cDoc(logger, resource);

    uiBinderWriter.parseDocument(doc, binderPrintWriter);

    if (messages.hasMessages()) {
      messages.write(writerManager.makePrintWriterFor(messages.getMessagesClassName()));
    }

    ImplicitClientBundle bundleClass = uiBinderWriter.getBundleClass();
    new BundleWriter(bundleClass, writerManager, logger).write();

    writerManager.commit();
  }

  private Document getW3cDoc(MortalLogger logger, FileObject resource)
      throws UnableToCompleteException {
    Document doc = null;
    try {
      CharSequence charContent = resource.getCharContent(false);

      if (charContent == null) {
        charContent = Util.readStreamAsString(resource.openInputStream());
      }
      String content = charContent.toString();

      doc = new W3cDomHelper(logger.getTreeLogger(), processingEnv)
          .documentFor(content, resource.getName());
    } catch (IOException iex) {
      logger.die("Error opening resource: " + resource.getName(), iex);
    } catch (SAXParseException e) {
      logger.die("Error parsing XML (line " + e.getLineNumber() + "): " + e.getMessage(), e);
    }

    return doc;
  }

  private FileObject getTemplateResource(MortalLogger logger, String templatePath)
      throws UnableToCompleteException {
    FileObject resource = AptUtil.findResource(templatePath);
    if (null == resource) {
      logger.die("Unable to find resource: " + templatePath);
    }
    return resource;
  }

}
