/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.uibinder;

import static com.google.common.base.Preconditions.checkState;
import static com.google.testing.compile.Compiler.javac;

import com.google.common.collect.ImmutableSet;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compilation.Status;
import com.google.testing.compile.JavaFileObjects;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

/**
 * Adapts the CompilationRule as a JUnit 5 extension.
 */
public class CompilationExtension
    implements BeforeEachCallback, AfterEachCallback,
    ParameterResolver {
  private static final JavaFileObject DUMMY =
      JavaFileObjects.forSourceLines("Dummy", "final class Dummy {}");

  private Compilation compilation;

  private volatile ProcessingEnvironment processingEnvironment;

  private Thread compileThread;
  private EvaluatingProcessor evaluatingProcessor;

  /**
   * Determines when thread is ready for test to run.
   */
  private CountDownLatch readyLatch;
  /**
   * Determines when the thread can finalize.
   */
  private CountDownLatch afterLatch;


  @Override
  public void beforeEach(ExtensionContext extensionContext) throws Exception {
    readyLatch = new CountDownLatch(1);
    afterLatch = new CountDownLatch(1);

    evaluatingProcessor = new EvaluatingProcessor();
    // run compilation in thread
    compileThread = new Thread(() -> {
      compilation = javac().withProcessors(evaluatingProcessor).compile(DUMMY);
    });
    compileThread.start();

    // wait for processing to be in the right place.
    readyLatch.await();
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) throws Exception {
    // let thread know we're in after method
    afterLatch.countDown();
    // join thread
    compileThread.join();
    // make sure we compiled okay
    checkState(compilation.status().equals(Status.SUCCESS), compilation);
    // validate no exceptions thrown in processing.
    try {
      evaluatingProcessor.throwIfStatementThrew();
    } catch (Exception e) {
      throw e;
    } catch (Throwable t) {
      throw new Exception(t);
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterContext.getParameter().getType()
        .equals(ProcessingEnvironment.class)
        || parameterContext.getParameter().getType()
        .equals(Elements.class)
        || parameterContext.getParameter().getType()
        .equals(Types.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    if (parameterContext.getParameter().getType().equals(ProcessingEnvironment.class)) {
      return processingEnvironment;
    }
    if (parameterContext.getParameter().getType().equals(Elements.class)) {
      return processingEnvironment.getElementUtils();
    }
    if (parameterContext.getParameter().getType().equals(Types.class)) {
      return processingEnvironment.getTypeUtils();
    }
    return null;
  }

  final class EvaluatingProcessor extends AbstractProcessor {
    Throwable thrown;

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
      return ImmutableSet.of("*");
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
      super.init(processingEnv);
      processingEnvironment = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      if (roundEnv.processingOver()) {
        try {
          // let parent know we're ready
          readyLatch.countDown();
          // wait for after method to execute
          afterLatch.await();
        } catch (Throwable e) {
          thrown = e;
        }
      }
      return false;
    }

    void throwIfStatementThrew() throws Throwable {
      if (thrown != null) {
        throw thrown;
      }
    }
  }
}
