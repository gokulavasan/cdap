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

package co.cask.cdap.notifications;

import com.google.common.base.Objects;

/**
 * Notification POJO, containing an {@code eventID} and {@code payload}.
 */
public class Notification {
  private final int eventID;
  private final byte[] payload;

  public Notification(int eventID, byte[] payload) {
    this.eventID = eventID;
    this.payload = payload;
  }

  public int getEventID() {
    return eventID;
  }

  public byte[] getPayload() {
    return payload;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("eventID", eventID).add("payload", payload)
      .toString();
  }
}
