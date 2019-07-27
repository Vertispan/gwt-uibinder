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

/**
 *
 */
public class ParentClass {
  private String text;
  private Number number;

  public void setText(String text) {
    this.text = text;
  }


  public void setNumber(final Number number) {
    this.number = number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public void setData(String arg1, String arg2) {
  }
}
