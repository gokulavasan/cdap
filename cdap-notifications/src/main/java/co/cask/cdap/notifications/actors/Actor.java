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

import com.google.common.base.Objects;

/**
 * Simple POJO identifying an entity that can publish or subscribe to notifications.
 */
public class Actor {
  private final ActorType actorType;
  private final String actorID;

  public Actor(ActorType actorType, String actorID) {
    this.actorType = actorType;
    this.actorID = actorID;
  }

  public ActorType getActorType() {
    return actorType;
  }

  public String getActorID() {
    return actorID;
  }

  /**
   * @return the actor ID of the form actorType-actorID.
   */
  public String getExtendedID() {
    return String.format("%d-%s", actorType.getIndex(), actorID);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("actorType", actorType)
      .add("actorID", actorID)
      .toString();
  }
}
