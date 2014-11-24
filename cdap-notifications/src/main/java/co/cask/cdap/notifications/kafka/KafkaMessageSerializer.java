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

import co.cask.cdap.common.io.BinaryDecoder;
import co.cask.cdap.common.io.BinaryEncoder;
import co.cask.cdap.notifications.Notification;
import co.cask.cdap.notifications.actors.Publisher;
import co.cask.common.io.ByteBufferInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class KafkaMessageSerializer {

  private final Publisher publisher;

  public KafkaMessageSerializer(Publisher publisher) {
    this.publisher = publisher;
  }

  public byte[] encode(Notification notification) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BinaryEncoder encoder = new BinaryEncoder(outputStream);

    String messageKey = buildKafkaMessageKey(publisher);
    encoder.writeString(messageKey);
    encoder.writeInt(notification.getEventID());
    encoder.writeBytes(notification.getPayload());
    return outputStream.toByteArray();
  }

  public Notification decode(ByteBuffer byteBuffer) throws IOException {
    BinaryDecoder decoder = new BinaryDecoder(new ByteBufferInputStream(byteBuffer));
    String publisherID = decoder.readString();
    if (!publisherID.equals(buildKafkaMessageKey(publisher))) {
      return null;
    }
    return new Notification(decoder.readInt(), decoder.readBytes().array());
  }

  public String buildKafkaMessageKey(Publisher publisher) {
    return String.format("%d-%s", publisher.getActorType().getIndex(), publisher.getActorID());
  }
}
