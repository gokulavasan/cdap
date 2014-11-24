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
import co.cask.cdap.notifications.NotificationPublisher;
import co.cask.cdap.notifications.actors.Publisher;
import com.google.inject.Inject;
import org.apache.twill.kafka.client.Compression;
import org.apache.twill.kafka.client.KafkaClient;
import org.apache.twill.kafka.client.KafkaPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
// TODO worry about thread safety
public class KafkaNotificationPublisher implements NotificationPublisher {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaNotificationPublisher.class);

  private final KafkaClient kafkaClient;
  private final KafkaPublisher.Ack ack;

  private KafkaPublisher publisher;

  @Inject
  public KafkaNotificationPublisher(KafkaClient kafkaClient) {
    this.kafkaClient = kafkaClient;

    // TODO thing about that ack
    this.ack = KafkaPublisher.Ack.LEADER_RECEIVED;
  }

  @Override
  public boolean publish(Publisher publisher, Notification notification) throws IOException {
    KafkaPublisher kafkaPublisher = getPublisher();
    if (kafkaPublisher == null) {
      LOG.warn("Unable to get kafka publisher, will not be able to publish notifications.");
      return false;
    }

    // TODO build a cache of serDes
    KafkaMessageSerializer serializer = new KafkaMessageSerializer(publisher);

    LOG.debug("Publishing on notification feed [{}]: {}", publisher, notification);
    ByteBuffer bb = ByteBuffer.wrap(serializer.encode(notification));
    KafkaPublisher.Preparer preparer = kafkaPublisher.prepare(KafkaConstants.KAFKA_TOPIC);
    preparer.add(bb, serializer.buildKafkaMessageKey(publisher));

    preparer.send();
    return true;
  }

  private KafkaPublisher getPublisher() {
    if (publisher != null) {
      return publisher;
    }
    try {
      publisher = kafkaClient.getPublisher(ack, Compression.SNAPPY);
    } catch (IllegalStateException e) {
      // can happen if there are no kafka brokers because the kafka server is down.
      publisher = null;
    }
    return publisher;
  }
}
