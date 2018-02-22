package org.gwtproject.uibinder.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class ClassSourceFactory {

  private final Set<String> implementedInterfaces = new LinkedHashSet<>();
  private final Set<String> imports = new LinkedHashSet<>();
  private final Element[] originatingElements;
  private final Class<?> processorClass;
  private final String packageName;
  private final ProcessingEnvironment processingEnv;
  private final String simpleClassname;

  private boolean finalClass;
  private String superClass;

  public ClassSourceFactory(String packageName, String simpleClassname, Class<?> processorClass,
      ProcessingEnvironment processingEnv, Element... originatingElements) {
    this.originatingElements = originatingElements;
    this.packageName = packageName;
    this.processingEnv = processingEnv;
    this.processorClass = processorClass;
    this.simpleClassname = simpleClassname;
  }

  public void addImport(String... imports) {
    for (String anImport : imports) {
      this.imports.add(anImport);
    }
  }

  public void addImport(Class<?>... imports) {
    for (Class<?> anImport : imports) {
      addImport(anImport.getCanonicalName());
    }
  }

  public void addImport(Name... imports) {
    for (Name anImport : imports) {
      this.imports.add(anImport.toString());
    }
  }

  public void addImport(TypeMirror... imports) {
    for (TypeMirror anImport : imports) {
      QualifiedNameable qualifiedNameable = asQualifiedNameable(anImport);
      if (qualifiedNameable != null) {
        addImport(qualifiedNameable.getQualifiedName());
      }
    }
  }

  public void addImports(Collection<TypeElement> imports) {
    for (TypeElement anImport : imports) {
      addImport(anImport.asType());
    }
  }

  public void addImplementedInterface(String... interfaces) {
    for (String anInterface : interfaces) {
      implementedInterfaces.add(anInterface);
    }
  }

  public void addImplementedInterface(TypeElement... interfaces) {
    for (TypeElement anInterface : interfaces) {
      addImport(anInterface.getQualifiedName());
      addImplementedInterface(anInterface.getSimpleName().toString());
    }
  }

  public SourceWriter createSourceWriter() {
    return new SourceWriter(this);
  }

  String getPackageName() {
    return packageName;
  }

  String getSimpleClassname() {
    return simpleClassname;
  }

  String getClassname() {
    return packageName + "." + simpleClassname;
  }

  Element[] getOriginatingElements() {
    return originatingElements;
  }

  Filer getFiler() {
    return processingEnv.getFiler();
  }

  Set<String> getImplementedInterfaces() {
    return Collections.unmodifiableSet(implementedInterfaces);
  }

  Set<String> getImports() {
    return Collections.unmodifiableSet(imports);
  }

  boolean isFinalClass() {
    return finalClass;
  }

  public void setFinalClass(boolean finalClass) {
    this.finalClass = finalClass;
  }

  Class<?> getProcessorClass() {
    return processorClass;
  }

  String getSuperClass() {
    return superClass;
  }

  public void setSuperClass(String superClass) {
    this.superClass = superClass;
  }

  // HELPERS

  private QualifiedNameable asQualifiedNameable(Element element) {
    if (element instanceof QualifiedNameable) {
      return (QualifiedNameable) element;
    }
    return null;
  }

  private QualifiedNameable asQualifiedNameable(TypeMirror typeMirror) {
    DeclaredType declaredType = asDeclaredType(typeMirror);
    if (declaredType != null) {
      return asQualifiedNameable(declaredType.asElement());
    }
    return null;
  }

  private DeclaredType asDeclaredType(TypeMirror typeMirror) {
    if (typeMirror instanceof DeclaredType) {
      return (DeclaredType) typeMirror;
    }

    return null;
  }
}
