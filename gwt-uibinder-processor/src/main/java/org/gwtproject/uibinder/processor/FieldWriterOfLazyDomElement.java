package org.gwtproject.uibinder.processor;

import static org.gwtproject.uibinder.processor.AptUtil.asQualifiedNameable;

import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.model.OwnerField;

import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * Implementation of FieldWriter for a {@link com.google.gwt.uibinder.client.LazyDomElement}.
 */
public class FieldWriterOfLazyDomElement extends AbstractFieldWriter {

  /**
   * The field type for @UiField LazyDomElement&lt;T&gt;.
   */
  private final TypeMirror ownerFieldType;

  /**
   * The T parameter in LazyDomElement&lt;T&gt;.
   */
  private final TypeMirror parameterType;

  public FieldWriterOfLazyDomElement(FieldManager manager, TypeMirror templateFieldType,
      OwnerField ownerField, MortalLogger logger) throws UnableToCompleteException {
    super(manager, FieldWriterType.DEFAULT, ownerField.getName(), logger);

    List<? extends TypeMirror> ownerFieldTypeArguments = AptUtil
        .getTypeArguments(ownerField.getRawType());

    // ownerFieldType null means LazyDomElement is not parameterized.
    this.ownerFieldType = ownerField.getRawType();
    if (ownerFieldTypeArguments.isEmpty()) {
      logger.die("LazyDomElement must be of type LazyDomElement<? extends Element>.");
    }

    // Parameterized LazyDomElement<T> must match its respective html element.
    // Example:
    //  DivElement -> div
    //  SpanElement -> span

    parameterType = ownerFieldTypeArguments.get(0);
    if (!AptUtil.isAssignableTo(templateFieldType, parameterType)) {
      logger.die("Field %s is %s<%s>, must be %s<%s>.", ownerField.getName(),
          asQualifiedNameable(ownerFieldType).getQualifiedName(), parameterType,
          asQualifiedNameable(ownerFieldType).getQualifiedName(), templateFieldType);
    }
  }

  public TypeMirror getAssignableType() {
    return ownerFieldType;
  }

  public TypeMirror getInstantiableType() {
    return ownerFieldType;
  }

  public String getQualifiedSourceName() {
    return asQualifiedNameable(ownerFieldType).getQualifiedName()
        + "<" + asQualifiedNameable(parameterType).getQualifiedName() + ">";
  }
}
