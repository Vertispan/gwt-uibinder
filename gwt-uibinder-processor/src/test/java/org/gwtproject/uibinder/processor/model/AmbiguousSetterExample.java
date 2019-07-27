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
public class AmbiguousSetterExample {
  private String text;
  private Number number;

  public void setText(String text) {
    this.text = text;
  }

  public void setNumber(Number number) {
    this.number = number;
  }

  /*
   * only these 2 are ambiguous.  The code can handle narrowing,
   * so one of these would be fine and it would have a better rank than the setNumber(Number), but
   * since Long and Integer are same rank, it causes the setter to be ambiguous.
   */
  public void setNumber(Long number) {
    this.number = number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }
}
