package org.gwtproject.uibinder.processor;

import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.messages.MessagesWriter;

import org.w3c.dom.Document;

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
import javax.tools.StandardLocation;
import org.xml.sax.SAXParseException;

/**
 *
 */
@SupportedAnnotationTypes(UiBinderClasses.UITEMPLATE)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class UiBinderProcessor extends BaseProcessor {

  private static final String BINDER_URI = "urn:ui:com.google.gwt.uibinder";
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
        .getAnnotation(interfaceType, UiBinderClasses.UITEMPLATE);

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

  private static String slashify(String s) {
    return s.replace(".", "/").replace("$", ".");
  }

  private final UiBinderContext uiBinderCtx = new UiBinderContext();

  @Override
  protected String processElement(TypeElement interfaceType, MortalLogger logger)
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
      PrintWriter binderPrintWriter, MortalLogger logger, PrintWriterManager writerManager)
      throws UnableToCompleteException {

    String templatePath = deduceTemplateFile(logger, interfaceType);
    MessagesWriter messages = new MessagesWriter(BINDER_URI, logger, templatePath,
        AptUtil.getPackageElement(interfaceType).getQualifiedName().toString(), implName);
    FieldManager fieldManager = new FieldManager(logger, true);

    UiBinderWriter uiBinderWriter = new UiBinderWriter(interfaceType.asType(), implName,
        templatePath,
        logger, fieldManager, messages, uiBinderCtx, BINDER_URI);

    FileObject resource = getTemplateResource(logger, templatePath);

    // Ensure that generated uibinder source is modified at least as often as synthesized .cssmap
    // resources, otherwise it would be possible to synthesize a modified .cssmap resource but fail
    // to retrigger the InlineClientBundleGenerator that processes it.
    binderPrintWriter.println("// .ui.xml template last modified: " + resource.getLastModified());
    Document doc = getW3cDoc(logger, resource);

    uiBinderWriter.parseDocument(doc, binderPrintWriter);

    if (messages.hasMessages()) {
      messages.write(writerManager.makePrintWriterFor(messages.getMessagesClassName()));
    }

    writerManager.commit();
  }


  private Document getW3cDoc(MortalLogger logger, FileObject resource)
      throws UnableToCompleteException {
    Document doc = null;
    try {
      CharSequence charContent = resource.getCharContent(false);

      if (charContent == null) {
        logger.die("Error opening resource: " + resource.getName());
      }
      String content = charContent.toString();

      doc = new W3cDomHelper(logger, processingEnv).documentFor(content, resource.getName());
    } catch (IOException iex) {
      logger.die("Error opening resource: " + resource.getName(), iex);
    } catch (SAXParseException e) {
      logger.die("Error parsing XML (line " + e.getLineNumber() + "): " + e.getMessage(), e);
    }

    return doc;
  }

  private FileObject getTemplateResource(MortalLogger logger, String templatePath)
      throws UnableToCompleteException {
    FileObject resource = null;

    String packageName = "";
    String relativeName = templatePath;

    int index = templatePath.lastIndexOf('/');
    if (index >= 0) {
      packageName = templatePath.substring(0, index)
          .replace('/', '.');
      relativeName = templatePath.substring(index + 1);
    }
    try {
      resource = processingEnv.getFiler()
          .getResource(StandardLocation.CLASS_OUTPUT, packageName, relativeName);
    } catch (IOException e) {
      // empty catch
    }
    if (null == resource) {
      logger.die("Unable to find resource: " + templatePath);
    }
    return resource;
  }

}
