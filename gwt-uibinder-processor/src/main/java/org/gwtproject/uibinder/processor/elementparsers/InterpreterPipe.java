package org.gwtproject.uibinder.processor.elementparsers;

import org.gwtproject.uibinder.processor.XMLElement;
import org.gwtproject.uibinder.processor.XMLElement.Interpreter;
import org.gwtproject.uibinder.processor.XMLElement.PostProcessingInterpreter;
import org.gwtproject.uibinder.processor.ext.UnableToCompleteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Pairs {@link XMLElement.Interpreter} instances.
 *
 * @param <T> The type returned by all members of the pipe
 */
class InterpreterPipe<T> implements PostProcessingInterpreter<T> {

  public static <T> InterpreterPipe<T> newPipe(Interpreter<T>... pipes) {
    InterpreterPipe<T> rtn = new InterpreterPipe<T>();
    for (int i = 0; i < pipes.length; ++i) {
      rtn.add(pipes[i]);
    }
    return rtn;
  }

  private final List<Interpreter<T>> pipe =
      new ArrayList<Interpreter<T>>();

  public void add(Interpreter<T> i) {
    pipe.add(i);
  }

  /**
   * Interpreters are fired in the order they were handed to the constructor. If an interpreter
   * gives a non-null result, downstream interpreters don't fire.
   *
   * @return The T or null returned by the last pipelined interpreter to run
   * @throws UnableToCompleteException on error
   */
  public T interpretElement(XMLElement elem) throws UnableToCompleteException {
    T rtn = null;
    for (XMLElement.Interpreter<T> i : pipe) {
      rtn = i.interpretElement(elem);
      if (null != rtn) {
        break;
      }
    }
    return rtn;
  }

  /**
   * Called by various {@link XMLElement} consumeInner*() methods after all elements have been
   * handed to {@link #interpretElement}. Passes the text to be post processed to each pipe member
   * that is instanceof {@link PostProcessingInterpreter}.
   */
  public String postProcess(String consumedText) throws UnableToCompleteException {
    for (XMLElement.Interpreter<T> i : pipe) {
      if (i instanceof PostProcessingInterpreter<?>) {
        consumedText = ((PostProcessingInterpreter<T>) i).postProcess(consumedText);
      }
    }
    return consumedText;
  }
}
