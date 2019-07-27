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
public class ChildClass extends ParentClass implements HasData<DetailData> {

  private DetailData data;

  @Override
  public void setText(String text) {
    super.setText("Child: " + text);
  }

  /**
   * This causes the ambiguity.  Parent has setNumber(Integer) which is the same rank as Long
   */
  public void setNumber(Long number) {
    super.setNumber(number);
  }

  @Override
  public void setData(DetailData data) {
    this.data = data;
  }

  @Override
  public DetailData getData() {
    return data;
  }
}
