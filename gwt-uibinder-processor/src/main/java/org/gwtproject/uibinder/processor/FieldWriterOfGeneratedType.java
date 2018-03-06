package org.gwtproject.uibinder.processor;

import javax.lang.model.type.TypeMirror;

/**
 * Implementation of FieldWriter for fields whose type we haven't genereated yet, e.g. locally
 * defined CssResources.
 */
class FieldWriterOfGeneratedType extends AbstractFieldWriter {

  private final String typePackage;
  private final String typeName;
  private final TypeMirror assignableType;

  public FieldWriterOfGeneratedType(FieldManager manager, TypeMirror assignableType,
      String typePackage,
      String typeName, String name, MortalLogger logger) {
    super(manager, FieldWriterType.GENERATED_BUNDLE, name, logger);
    if (assignableType == null) {
      throw new RuntimeException("assignableType must not be null");
    }
    if (typeName == null) {
      throw new RuntimeException("typeName must not be null");
    }
    if (typePackage == null) {
      throw new RuntimeException("typePackage must not be null");
    }

    this.assignableType = assignableType;
    this.typeName = typeName;
    this.typePackage = typePackage;
  }

  public TypeMirror getAssignableType() {
    return assignableType;
  }

  public TypeMirror getInstantiableType() {
    return null;
  }

  public String getQualifiedSourceName() {
    if (typePackage.length() == 0) {
      return typeName;
    }

    return String.format("%s.%s", typePackage, typeName);
  }
}
