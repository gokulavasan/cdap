/*
 * Copyright © 2014 Cask Data, Inc.
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

package co.cask.cdap.notifications.actors;

/**
 * Type of a notification publisher.
 */
public enum ActorType {
  DATASET(0),
  STREAM(1),
  PROGRAM(2),
  QUERY(3);

  private final int index;

  ActorType(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
}
