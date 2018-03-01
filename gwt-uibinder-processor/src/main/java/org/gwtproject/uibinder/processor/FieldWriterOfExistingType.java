package org.gwtproject.uibinder.processor;

import javax.lang.model.type.TypeMirror;

/**
 *
 */
public class FieldWriterOfExistingType extends AbstractFieldWriter {

  final TypeMirror type;
  final MortalLogger logger;

  FieldWriterOfExistingType(FieldManager manager, FieldWriterType fieldType,
      TypeMirror type, String name, MortalLogger logger) {
    super(manager, fieldType, name, logger);
    this.logger = logger;
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null");
    }
    this.type = type;
  }

  public TypeMirror getAssignableType() {
    return type;
  }

  public TypeMirror getInstantiableType() {
    return type;
  }

  public String getQualifiedSourceName() {
    return AptUtil.asQualifiedNameable(type).getQualifiedName().toString();
  }
}
