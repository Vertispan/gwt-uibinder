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
package org.gwtproject.uibinder.processor.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.gwtproject.uibinder.CompilationExtension;
import org.gwtproject.uibinder.processor.AptUtil;
import org.gwtproject.uibinder.processor.MortalLogger;
import org.gwtproject.uibinder.processor.UiBinderApiPackage;
import org.gwtproject.uibinder.processor.UiBinderContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 *
 */
@ExtendWith(CompilationExtension.class)
@ExtendWith(MockitoExtension.class)
class OwnerFieldClassTest {

  @Mock
  private MortalLogger mockLogger;
  @Mock
  private UiBinderContext mockContext;

  @BeforeEach
  public void setup(ProcessingEnvironment processingEnvironment) throws Exception {
    AptUtil.setProcessingEnvironment(processingEnvironment);
    UiBinderApiPackage.setUiBinderApiPackage(UiBinderApiPackage.COM_GOOGLE_GWT_UIBINDER);
  }

  @Test
  public void nonAmbiguousSetter(Elements elements) throws Exception {
    TypeElement typeElement = elements.getTypeElement(AmbiguousSetterExample.class.getName());
    OwnerFieldClass cut = new OwnerFieldClass(typeElement.asType(), mockLogger, mockContext);

    // not ambiguous
    ExecutableElement setter = cut.getSetter("text");
    assertNotNull(setter);

    verifyNoMoreInteractions(mockLogger, mockContext);
  }

  @Test
  public void ambiguousSetter(Elements elements) throws Exception {
    TypeElement typeElement = elements.getTypeElement(AmbiguousSetterExample.class.getName());
    OwnerFieldClass cut = new OwnerFieldClass(typeElement.asType(), mockLogger, mockContext);

    // this is ambiguous only because setNumber(Number) is the same rank as setNumber(Integer)
    ExecutableElement setter = cut.getSetter("number");
    assertNull(setter);
    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockLogger)
        .die(messageCaptor.capture());
    assertTrue(messageCaptor.getValue().startsWith("Ambiguous setter"));

    verifyNoMoreInteractions(mockLogger, mockContext);
  }

  @Test
  public void nonAmbiguousSetterInParentChild(Elements elements) throws Exception {
    TypeElement typeElement = elements.getTypeElement(ChildClass.class.getName());
    OwnerFieldClass cut = new OwnerFieldClass(typeElement.asType(), mockLogger, mockContext);

    ExecutableElement setter = cut.getSetter("text");
    assertNotNull(setter);

    verifyNoMoreInteractions(mockLogger, mockContext);
  }

  @Test
  public void ambiguousSetterInParentChild(Elements elements) throws Exception {
    TypeElement typeElement = elements.getTypeElement(ChildClass.class.getName());
    OwnerFieldClass cut = new OwnerFieldClass(typeElement.asType(), mockLogger, mockContext);

    ExecutableElement setter = cut.getSetter("number");
    assertNull(setter);

    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockLogger)
        .die(messageCaptor.capture());
    assertTrue(messageCaptor.getValue().startsWith("Ambiguous setter"));

    verifyNoMoreInteractions(mockLogger, mockContext);
  }

  @Test
  public void nonAmbiguousGenericsSetter(Elements elements) throws Exception {
    TypeElement typeElement = elements.getTypeElement(ChildClass.class.getName());
    OwnerFieldClass cut = new OwnerFieldClass(typeElement.asType(), mockLogger, mockContext);

    ExecutableElement setter = cut.getSetter("data");
    assertNotNull(setter);

    verifyNoMoreInteractions(mockLogger, mockContext);
  }
}