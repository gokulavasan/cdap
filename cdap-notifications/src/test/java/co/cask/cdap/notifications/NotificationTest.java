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

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.notifications.actors.ActorType;
import co.cask.cdap.notifications.actors.Publisher;
import co.cask.cdap.notifications.actors.Subscriber;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class NotificationTest {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationTest.class);

  protected abstract NotificationPublisher getNotificationPublisher();

  protected abstract NotificationSubscriber getNotificationSubscriber(Subscriber subscriber);

  @Test
  public void onePublisherOneSubscriberTest() throws Exception {
    final int messagesCount = 20;
    final Publisher publisher = new Publisher(ActorType.QUERY, "fake-query-handle");
    Runnable publisherRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          for (int i = 0; i < messagesCount; i++) {
            getNotificationPublisher().publish(publisher,
                                               new Notification(0, String.format("fake-payload-%d", i).getBytes()));
            TimeUnit.MILLISECONDS.sleep(100);
          }
        } catch (Throwable e) {
          Throwables.propagate(e);
        }
      }
    };
    Thread publisherThread = new Thread(publisherRunnable);

    NotificationSubscriber notificationSubscriber =
      getNotificationSubscriber(new Subscriber(ActorType.DATASET, "fake-dataset"));

    final AtomicInteger receiveCount = new AtomicInteger(0);
    final AtomicBoolean assertionOk = new AtomicBoolean(true);
    notificationSubscriber.subscribe(publisher, new NotificationHandler() {
      @Override
      public void handle(Notification notification) {
        String payloadStr = Bytes.toString(notification.getPayload());
        LOG.debug("Received notification payload: {}", payloadStr);
        try {
          Assert.assertEquals("fake-payload-" + receiveCount.get(), payloadStr);
          receiveCount.incrementAndGet();
        } catch (Throwable t) {
          assertionOk.set(false);
//            Throwables.propagate(t);
        }
      }
    });

    // Give time to the subscribers to start listening, otherwise they will miss the first notifications
    TimeUnit.MILLISECONDS.sleep(2000);
    publisherThread.start();
    publisherThread.join();
    TimeUnit.MILLISECONDS.sleep(2000);
    notificationSubscriber.close();

    Assert.assertTrue(assertionOk.get());
    Assert.assertEquals(messagesCount, receiveCount.get());
  }

  @Test
  public void onePublisherMultipleSubscribers() throws Exception {
    final int messagesCount = 5;
    int subscribersCount = 1;
    final Publisher publisher = new Publisher(ActorType.QUERY, "fake-query-handle");
    Runnable publisherRunnable = new Runnable() {
      @Override
      public void run() {
        try {
          for (int i = 0; i < messagesCount; i++) {
            getNotificationPublisher().publish(publisher,
                                               new Notification(0, String.format("fake-payload-%d", i).getBytes()));
            TimeUnit.MILLISECONDS.sleep(100);
          }
        } catch (Throwable e) {
          Throwables.propagate(e);
        }
      }
    };
    Thread publisherThread = new Thread(publisherRunnable);
    publisherThread.start();

    final int[] receiveCounts = new int[subscribersCount];
    final AtomicBoolean assertionOk = new AtomicBoolean(true);
    List<NotificationSubscriber> notificationSubscribers = Lists.newArrayList();
    for (int i = 0; i < subscribersCount; i++) {
      final int j = i;
      final Subscriber subscriber = new Subscriber(ActorType.DATASET, "fake-dataset-" + i);
      NotificationSubscriber notificationSubscriber = getNotificationSubscriber(subscriber);
      notificationSubscribers.add(notificationSubscriber);

      notificationSubscriber.subscribe(publisher, new NotificationHandler() {
        @Override
        public void handle(Notification notification) {
          String payloadStr = Bytes.toString(notification.getPayload());
          LOG.debug("Received notification for subscriber {}, payload: {}", subscriber, payloadStr);
          try {
            Assert.assertEquals(String.format("fake-payload-%d", receiveCounts[j]), payloadStr);
            receiveCounts[j]++;
          } catch (Throwable t) {
            assertionOk.set(false);
//            Throwables.propagate(t);
          }
        }
      });
    }

    publisherThread.join();
    TimeUnit.MILLISECONDS.sleep(2000);
    for (NotificationSubscriber notificationSubscriber : notificationSubscribers) {
      Closeables.closeQuietly(notificationSubscriber);
    }

    Assert.assertTrue(assertionOk.get());
    for (int i : receiveCounts) {
      Assert.assertEquals(messagesCount, i);
    }
  }
}
