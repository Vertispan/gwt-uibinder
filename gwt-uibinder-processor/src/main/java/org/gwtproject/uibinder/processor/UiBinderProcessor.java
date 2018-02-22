package org.gwtproject.uibinder.processor;

import java.util.List;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 *
 */
@SupportedAnnotationTypes(UiBinderClasses.UITEMPLATE)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class UiBinderProcessor extends BaseProcessor {

  private Element currentElement;

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    try {
      if (!env.processingOver()) {
        for (TypeElement annotation : annotations) {
          for (Element element : env.getElementsAnnotatedWith(annotation)) {
            currentElement = element;
            processElement((TypeElement) element);
          }
        }
        return true;
      }

    } catch (Exception e) {
      logException(currentElement, e);
      return false;
    }
    return false;
  }

  private boolean processElement(TypeElement element) throws Exception {
    String packageName = processingEnv.getElementUtils()
        .getPackageOf(element).getQualifiedName().toString();

    String simpleSourceName = element.getSimpleName().toString() + "Impl";

    // handle nested classes
    Element e = element;
    while (e.getEnclosingElement() instanceof TypeElement) {
      simpleSourceName = e.getEnclosingElement()
          .getSimpleName().toString() + "_" + simpleSourceName;
      e = e.getEnclosingElement();
    }

    // validate extension of UiBinder interface and get parameterized values
    TypeElement uiBinderType = processingEnv.getElementUtils()
        .getTypeElement(UiBinderClasses.UIBINDER);

    TypeMirror uiBinderInterfaceDeclaration = getImplementedInterface(element,
        uiBinderType.asType());

    List<? extends TypeMirror> typeArguments = getTypeArguments(uiBinderInterfaceDeclaration);
    if (typeArguments == null || typeArguments.isEmpty()) {
      error(currentElement, UiBinderClasses.UIBINDER + " must be parameterized correctly.");
      return false;
    }

    TypeMirror uiObjectType = typeArguments.get(0);
    TypeMirror ownerType = typeArguments.get(1);

    ClassSourceFactory factory = new ClassSourceFactory(packageName, simpleSourceName,
        UiBinderProcessor.class, processingEnv, element);

    factory.addImport(UiBinderClasses.UIBINDER);
    factory.addImport(uiObjectType);
    factory.addImport(ownerType);

    factory.addImplementedInterface(element);


    SourceWriter sw = factory.createSourceWriter();

    sw.println("private %1s owner;", ownerType);

    sw.println("public %1s createAndBindUi(%1s owner) {", uiObjectType, ownerType);
    sw.indent();
    sw.println("this.owner = owner;");
    sw.comment("Needs implementation.");
    sw.println("return null;");
    sw.outdent();
    sw.println("}");

    sw.commit();

    return true;
  }
}
