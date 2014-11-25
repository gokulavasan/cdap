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

package co.cask.cdap.gateway.handlers;

import co.cask.cdap.api.flow.flowlet.StreamEvent;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.stream.StreamEventTypeAdapter;
import co.cask.cdap.gateway.GatewayTestBase;
import co.cask.cdap.proto.StreamProperties;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Test stream handler. This is not part of GatewayFastTestsSuite because it needs to start the gateway multiple times.
 */
public class StreamHandlerTestRun extends GatewayTestBase {
  private static final String API_KEY = GatewayTestBase.getAuthHeader().getValue();
  private static final String HOSTNAME = "127.0.0.1";

  private static final Gson GSON = StreamEventTypeAdapter.register(new GsonBuilder()).create();

  private HttpURLConnection openURL(String location, HttpMethod method) throws IOException {
    URL url = new URL(location);
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    urlConn.setRequestMethod(method.getName());
    urlConn.setRequestProperty(Constants.Gateway.API_KEY, API_KEY);

    return urlConn;
  }

  @Test
  public void testStreamCreate() throws Exception {
    int port = GatewayTestBase.getPort();

    // Try to get info on a non-existant stream
    HttpURLConnection urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream1/info", HOSTNAME, port),
                                        HttpMethod.GET);

    Assert.assertEquals(HttpResponseStatus.NOT_FOUND.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    // Now, create the new stream.
    urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream1", HOSTNAME, port), HttpMethod.PUT);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    // getInfo should now return 200
    urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream1/info", HOSTNAME, port),
                                        HttpMethod.GET);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testSimpleStreamEnqueue() throws Exception {
    int port = GatewayTestBase.getPort();

    // Create new stream.
    HttpURLConnection urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream_enqueue", HOSTNAME, port),
                                        HttpMethod.PUT);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    // Enqueue 10 entries
    for (int i = 0; i < 10; ++i) {
      urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream_enqueue", HOSTNAME, port), HttpMethod.POST);
      urlConn.setDoOutput(true);
      urlConn.addRequestProperty("test_stream_enqueue.header1", Integer.toString(i));
      urlConn.getOutputStream().write(Integer.toString(i).getBytes(Charsets.UTF_8));
      Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
      urlConn.disconnect();
    }

    // Fetch 10 entries
    urlConn = openURL(String.format("http://%s:%d/v2/streams/test_stream_enqueue/events?limit=10", HOSTNAME, port),
                      HttpMethod.GET);
    List<StreamEvent> events = GSON.fromJson(new String(ByteStreams.toByteArray(urlConn.getInputStream()),
                                                        Charsets.UTF_8),
                                             new TypeToken<List<StreamEvent>>() { }.getType());
    for (int i = 0; i < 10; i++) {
      StreamEvent event = events.get(i);
      int actual = Integer.parseInt(Charsets.UTF_8.decode(event.getBody()).toString());
      Assert.assertEquals(i, actual);
      Assert.assertEquals(Integer.toString(i), event.getHeaders().get("header1"));
    }
    urlConn.disconnect();
  }

  @Test
  public void testStreamInfo() throws Exception {
    int port = GatewayTestBase.getPort();

    // Now, create the new stream.
    HttpURLConnection urlConn = openURL(String.format("http://%s:%d/v2/streams/stream_info",
                                                      HOSTNAME, port), HttpMethod.PUT);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    //config ttl for the stream
    urlConn = openURL(String.format("http://%s:%d/v2/streams/stream_info/config",
                                    HOSTNAME, port), HttpMethod.PUT);
    urlConn.setDoOutput(true);
    JsonObject json = new JsonObject();
    json.addProperty("ttl", "2");
    urlConn.getOutputStream().write(json.toString().getBytes(Charsets.UTF_8));
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    urlConn.disconnect();

    // test the config ttl by calling info
    urlConn = openURL(String.format("http://%s:%d/v2/streams/stream_info/info", HOSTNAME, port),
                      HttpMethod.GET);
    Assert.assertEquals(HttpResponseStatus.OK.getCode(), urlConn.getResponseCode());
    StreamProperties properties = GSON.fromJson(new String(ByteStreams.toByteArray(urlConn.getInputStream()),
                                                        Charsets.UTF_8), StreamProperties.class);
    Assert.assertEquals(2, properties.getTTL());
    urlConn.disconnect();
  }
}
