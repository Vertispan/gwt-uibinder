package org.gwtproject.uibinder.processor;

import java.util.HashMap;
import java.util.Map;
import javax.lang.model.type.TypeMirror;
import org.gwtproject.uibinder.processor.model.OwnerFieldClass;

/**
 * A shared context cache for UiBinder.
 */
public class UiBinderContext {

  private final Map<TypeMirror, OwnerFieldClass> fieldClassesCache = new HashMap<>();

  public OwnerFieldClass getOwnerFieldClass(TypeMirror type) {
    return fieldClassesCache.get(type);
  }

  public void putOwnerFieldClass(TypeMirror forType, OwnerFieldClass clazz) {
    fieldClassesCache.put(forType, clazz);
  }

}
