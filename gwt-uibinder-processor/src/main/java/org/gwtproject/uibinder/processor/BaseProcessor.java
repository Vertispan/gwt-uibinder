package org.gwtproject.uibinder.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.processing.AbstractProcessor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public abstract class BaseProcessor extends AbstractProcessor {

  // utility methods

  /**
   * Gets the implemented interface from provided element.
   */
  public TypeMirror getImplementedInterface(TypeElement element, TypeMirror interfaceType) {
    List<? extends TypeMirror> interfaces = element.getInterfaces();
    Types typeUtils = processingEnv.getTypeUtils();
    interfaceType = typeUtils.erasure(interfaceType);
    for (TypeMirror anInterface : interfaces) {
      if (typeUtils.isSameType(typeUtils.erasure(anInterface), interfaceType)) {
        return anInterface;
      }
    }
    return null;
  }

  public List<? extends TypeMirror> getTypeArguments(TypeMirror declaration) {
    if (declaration instanceof DeclaredType) {
      DeclaredType declaredType = (DeclaredType) declaration;
      return declaredType.getTypeArguments();
    }
    return null;
  }

  protected void error(Element element, Object... message) {
    log(Diagnostic.Kind.ERROR, element, message);
  }

  protected void log(Diagnostic.Kind logLevel, Element element, Object... message) {
    StringBuilder sb = new StringBuilder();
    for (Object o : message) {
      sb.append(o);
    }
    if (element == null) {
      processingEnv.getMessager().printMessage(logLevel, sb.toString());
    } else {
      processingEnv.getMessager().printMessage(logLevel, sb.toString(), element);
    }
  }

  protected void logException(Element element, Exception e) {
    StringWriter stringWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stringWriter));
    error(element, "Error Processing Annotation:\n" + stringWriter.getBuffer().toString());
  }


  protected void note(Element element, Object... message) {
    log(Diagnostic.Kind.NOTE, element, message);
  }

  protected void warn(Element element, Object... message) {
    log(Diagnostic.Kind.MANDATORY_WARNING, element, message);
  }
}
