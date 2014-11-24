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

import co.cask.cdap.notifications.actors.Publisher;
import co.cask.cdap.notifications.actors.Subscriber;

import java.io.Closeable;

/**
 * Notification subscriber that can listen to multiple {@link Publisher}s. Each subscription uses the
 * {@link NotificationSubscriber#subscribe} method, providing a {@link NotificationHandler} that specifies
 * the logic to run when notifications are received.
 *
 * One NotificationSubscriber object can listen to several {@link co.cask.cdap.notifications.actors.Publisher}s.
 */
// TODO should it also be closeable to free consumer resources?
public abstract class NotificationSubscriber implements Closeable {

  protected final Subscriber subscriber;

  public NotificationSubscriber(Subscriber subscriber) {
    this.subscriber = subscriber;
  }

  /**
   * Subscribe to notifications published by a {@code publisher}. This call is asynchronous.
   *
   * @param publisher {@link co.cask.cdap.notifications.actors.Publisher} to subscribe to.
   * @param handler {@link NotificationHandler} that specifies the logic to run when notifications coming from the
   *                {@code publisher} are received.
   */
  public abstract void subscribe(Publisher publisher, NotificationHandler handler);
}
