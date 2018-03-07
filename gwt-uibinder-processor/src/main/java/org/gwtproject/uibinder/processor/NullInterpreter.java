package org.gwtproject.uibinder.processor;

/**
 * An no-op interpreter.
 *
 * @param <T> The type of null to return
 */
public final class NullInterpreter<T> implements XMLElement.Interpreter<T> {

  public T interpretElement(XMLElement elem) {
    return null;
  }
}
