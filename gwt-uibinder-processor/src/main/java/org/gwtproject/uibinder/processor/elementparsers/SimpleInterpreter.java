package org.gwtproject.uibinder.processor.elementparsers;


import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.XMLElement.Interpreter;

/**
 * A simple {@link Interpreter} that returns a given value to every request.
 */
public class SimpleInterpreter<T> implements Interpreter<T> {

  private final T rtn;

  public SimpleInterpreter(T rtn) {
    this.rtn = rtn;
  }

  public T interpretElement(XMLElement elem) {
    return rtn;
  }
}

