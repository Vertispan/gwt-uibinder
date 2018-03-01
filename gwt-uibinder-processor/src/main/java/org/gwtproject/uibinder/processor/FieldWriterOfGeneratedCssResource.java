package org.gwtproject.uibinder.processor;

import org.gwtproject.uibinder.processor.attributeparsers.CssNameConverter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;
import org.gwtproject.uibinder.processor.model.ImplicitCssResource;
import org.gwtproject.uibinder.processor.model.OwnerField;

import java.util.Set;
import javax.lang.model.type.TypeMirror;

/**
 * Implementation of FieldWriter for an {@link ImplicitCssResource}.
 */
class FieldWriterOfGeneratedCssResource extends AbstractFieldWriter {

  private static final CssNameConverter nameConverter = new CssNameConverter();

  private final ImplicitCssResource css;
  private final TypeMirror stringType;

  public FieldWriterOfGeneratedCssResource(FieldManager manager, TypeMirror stringType,
      ImplicitCssResource css, MortalLogger logger) {
    super(manager, FieldWriterType.GENERATED_CSS, css.getName(), logger);
    this.stringType = stringType;
    this.css = css;
  }

  public TypeMirror getAssignableType() {
    return css.getExtendedInterface();
  }

  public TypeMirror getInstantiableType() {
    return null;
  }

  public String getQualifiedSourceName() {
    return css.getQualifiedSourceName();
  }

  @Override
  public TypeMirror getReturnType(String[] path, MonitoredLogger logger) {
    if (path.length == 2) {
      String maybeCssClass = path[1];
      Set<String> cssClassNames = null;
      try {
        cssClassNames = css.getCssClassNames();
        if (cssClassNames.contains(maybeCssClass)
            || cssClassNames.contains(nameConverter.convertName(maybeCssClass))
            || css.getNormalizedCssClassNames().contains(maybeCssClass)) {
          return stringType;
        }
      } catch (UnableToCompleteException e) {
        logger.error("Can't interpret CSS");
      }
    }
    return super.getReturnType(path, logger);
  }

  @Override
  public void writeFieldBuilder(IndentedWriter w,
      int getterCount, OwnerField ownerField) {
    w.write("%s;  // generated css resource must be always created. Type: %s. Precedence: %s",
        FieldManager.getFieldBuilder(getName()), getFieldType(), getBuildPrecedence());
  }
}
