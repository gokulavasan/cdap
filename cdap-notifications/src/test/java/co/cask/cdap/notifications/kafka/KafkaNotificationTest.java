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

import co.cask.cdap.notifications.NotificationPublisher;
import co.cask.cdap.notifications.NotificationSubscriber;
import co.cask.cdap.notifications.NotificationTest;
import co.cask.cdap.notifications.actors.Subscriber;
import co.cask.cdap.test.internal.TempFolder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.twill.internal.kafka.EmbeddedKafkaServer;
import org.apache.twill.internal.kafka.client.ZKKafkaClientService;
import org.apache.twill.internal.utils.Networks;
import org.apache.twill.internal.zookeeper.InMemoryZKServer;
import org.apache.twill.kafka.client.KafkaClientService;
import org.apache.twill.zookeeper.ZKClientService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class KafkaNotificationTest extends NotificationTest {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaNotificationTest.class);

  private static final TempFolder TEMP_FOLDER = new TempFolder();

  private static InMemoryZKServer zkServer;
  private static ZKClientService zkClient;
  private static KafkaClientService kafkaClient;
  private static EmbeddedKafkaServer kafkaServer;

  private NotificationPublisher notificationPublisher;
  private Map<Subscriber, NotificationSubscriber> notificationSubscribers = Maps.newHashMap();

  @BeforeClass
  public static void startUp() throws Exception {
    zkServer = InMemoryZKServer.builder().build();
    zkServer.startAndWait();

    zkClient = ZKClientService.Builder.of(zkServer.getConnectionStr()).build();
    zkClient.startAndWait();

    kafkaClient = new ZKKafkaClientService(zkClient);
    kafkaClient.startAndWait();

    Properties kafkaConfig = generateKafkaConfig(zkServer, TEMP_FOLDER.newFolder("kafka-notifications-test"));

    kafkaServer = new EmbeddedKafkaServer(kafkaConfig);
    kafkaServer.startAndWait();
  }

  @AfterClass
  public static void shutDown() throws Exception {
    kafkaClient.stopAndWait();
    kafkaServer.stopAndWait();
    zkServer.stopAndWait();
  }

  @Override
  protected NotificationPublisher getNotificationPublisher() {
    if (notificationPublisher == null) {
      notificationPublisher = new KafkaNotificationPublisher(kafkaClient);
    }
    return notificationPublisher;
  }

  @Override
  protected NotificationSubscriber getNotificationSubscriber(Subscriber subscriber) {
    NotificationSubscriber notificationSubscriber = notificationSubscribers.get(subscriber);
    if (notificationSubscriber == null) {
      notificationSubscriber = new KafkaNotificationSubscriber(kafkaClient, subscriber);
      notificationSubscribers.put(subscriber, notificationSubscriber);
    }
    return notificationSubscriber;
  }

  private static Properties generateKafkaConfig(InMemoryZKServer zkServer, File tmpFolder) {
    int port = Networks.getRandomPort();
    Preconditions.checkState(port > 0, "Failed to get random port.");

    Properties prop = new Properties();
    prop.setProperty("broker.id", "1");
    prop.setProperty("port", Integer.toString(port));
    prop.setProperty("num.network.threads", "2");
    prop.setProperty("num.io.threads", "2");
    prop.setProperty("socket.send.buffer.bytes", "1048576");
    prop.setProperty("socket.receive.buffer.bytes", "1048576");
    prop.setProperty("socket.request.max.bytes", "104857600");
    prop.setProperty("log.dir", tmpFolder.getAbsolutePath());
    prop.setProperty("log.flush.interval.messages", "10000");
    prop.setProperty("log.flush.interval.ms", "1000");
    prop.setProperty("log.retention.hours", "1");
    prop.setProperty("log.segment.bytes", "536870912");
    prop.setProperty("log.cleanup.interval.mins", "1");
    prop.setProperty("zookeeper.connect", zkServer.getConnectionStr());
    prop.setProperty("zookeeper.connection.timeout.ms", "1000000");

    return prop;
  }
}
