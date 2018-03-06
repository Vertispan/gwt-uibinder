package org.gwtproject.uibinder.processor;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

public abstract class BaseProcessor extends AbstractProcessor {

  @Override
  public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    MortalLogger logger = new MortalLogger(processingEnv.getMessager());
    try {
      if (!env.processingOver()) {
        AptUtil.setProcessingEnvironment(processingEnv);
        for (TypeElement annotation : annotations) {
          for (Element element : env.getElementsAnnotatedWith(annotation)) {
            logger.setCurrentElement(element);
            String generatedClassName = processElement((TypeElement) element, logger);
            logger.log(Kind.NOTE, "generated type " + generatedClassName);
          }
        }
        return true;
      }
    } catch (Exception e) {
      logger.log(Kind.ERROR, "Error Processing Annotation", e);
      return false;
    } finally {
      AptUtil.setProcessingEnvironment(null);
    }
    return false;
  }

  /**
   * Process single TypeElement.
   *
   * @param interfaceType the TypeElement to process
   * @param treeLogger the logger
   * @return canonical classname
   */
  protected abstract String processElement(TypeElement interfaceType, MortalLogger treeLogger)
      throws UnableToCompleteException;
}
