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

package co.cask.cdap.notifications.kafka;

import co.cask.cdap.notifications.Notification;
import co.cask.cdap.notifications.NotificationHandler;
import co.cask.cdap.notifications.NotificationSubscriber;
import co.cask.cdap.notifications.actors.Publisher;
import co.cask.cdap.notifications.actors.Subscriber;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.twill.common.Cancellable;
import org.apache.twill.kafka.client.FetchedMessage;
import org.apache.twill.kafka.client.KafkaClient;
import org.apache.twill.kafka.client.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
// TODO we will start with having one topic partition for all the notifications, then later, we can have say,
// 10 topics - notifications-1 .. notifications-10 and still affect notifications based on the publisher ID
// to a certain topic
public class KafkaNotificationSubscriber extends NotificationSubscriber {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaNotificationSubscriber.class);

  private final KafkaClient kafkaClient;
  private final List<Cancellable> cancels;
  private final Map<Publisher, Long> offsetsMap;

  @Inject
  public KafkaNotificationSubscriber(KafkaClient kafkaClient, Subscriber subscriber) {
    super(subscriber);
    this.kafkaClient = kafkaClient;
    this.cancels = Lists.newArrayList();
    this.offsetsMap = Maps.newHashMap();
  }

  @Override
  public void subscribe(final Publisher publisher, final NotificationHandler handler) {
    KafkaConsumer.Preparer preparer = kafkaClient.getConsumer().prepare();

    String topic = KafkaConstants.KAFKA_TOPIC;
    long offset = getOffset(publisher);
    if (offset >= 0) {
      preparer.add(topic, 0, offset);
    } else {
      // When subscribing, we start reading only the last message - avoiding to receive
      // all the past notifications
      // TODO does it work if we subscribe before we published on a given notification feed? it will, because the
      // topics already exist - so should do that.
      preparer.addLatest(topic, 0);
    }

    cancels.add(preparer.consume(new KafkaConsumer.MessageCallback() {
      @Override
      public void onReceived(Iterator<FetchedMessage> messages) {
        int count = 0;
        while (messages.hasNext()) {
          FetchedMessage message = messages.next();
          ByteBuffer payload = message.getPayload();

          // TODO have a map of serDes
          KafkaMessageSerializer serializer = new KafkaMessageSerializer(publisher);

          try {
            Notification notification = serializer.decode(payload);
            if (notification == null) {
              continue;
            }
            // TODO think about the logic, if things fail, etc
            handler.handle(notification);
            count++;
          } catch (IOException e) {
            // TODO What should be done here?...
            LOG.error("Could not decode Kafka message {}", message);
            Throwables.propagate(e);
          }
        }
        LOG.debug("Got {} messages from kafka", count);
      }

      @Override
      public void finished() {
        LOG.info("Subscription from {} to publisher {} finished.", subscriber, publisher);
      }
    }));
    LOG.info("Consumer created for publisher {}", publisher);
  }

  private long getOffset(Publisher publisher) {
    Long offset = offsetsMap.get(publisher);
    return offset == null ? -1L : offset.longValue();
  }

  @Override
  public void close() throws IOException {
    for (Cancellable cancellable : cancels) {
      cancellable.cancel();
    }
  }
}
