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

package co.cask.cdap.client;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.client.app.FakeApp;
import co.cask.cdap.client.app.FakeFlow;
import co.cask.cdap.client.common.ClientTestBase;
import co.cask.cdap.explore.client.ExploreExecutionResult;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.QueryResult;
import co.cask.cdap.test.XSlowTests;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

/**
 * Test for {@link QueryClient}.
 */
@Category(XSlowTests.class)
public class QueryClientTestRun extends ClientTestBase {

  private static final Gson GSON = new Gson();

  private ApplicationClient appClient;
  private QueryClient queryClient;
  private ProgramClient programClient;
  private StreamClient streamClient;

  @Before
  public void setUp() throws Throwable {
    super.setUp();
    appClient = new ApplicationClient(clientConfig);
    queryClient = new QueryClient(clientConfig);
    programClient = new ProgramClient(clientConfig);
    streamClient = new StreamClient(clientConfig);
  }

  @Test
  public void testAll() throws Exception {
    appClient.deploy(createAppJarFile(FakeApp.class));
    programClient.start(FakeApp.NAME, ProgramType.FLOW, FakeFlow.NAME);
    assertProgramRunning(programClient, FakeApp.NAME, ProgramType.FLOW, FakeFlow.NAME);
    streamClient.sendEvent(FakeApp.STREAM_NAME, "bob:123");
    streamClient.sendEvent(FakeApp.STREAM_NAME, "joe:321");

    Thread.sleep(3000);

    ExploreExecutionResult executionResult = queryClient.execute("select * from cdap_user_" + FakeApp.DS_NAME).get();

    Assert.assertNotNull(executionResult.getResultSchema());
    List<QueryResult> results = Lists.newArrayList(executionResult);
    Assert.assertNotNull(results);
    Assert.assertEquals(2, results.size());
    Assert.assertEquals("bob", Bytes.toString(GSON.fromJson(
      results.get(0).getColumns().get(0).toString(), byte[].class)));
    Assert.assertEquals("123", Bytes.toString(GSON.fromJson(
      results.get(0).getColumns().get(1).toString(), byte[].class)));
    Assert.assertEquals("joe", Bytes.toString(GSON.fromJson(
      results.get(1).getColumns().get(0).toString(), byte[].class)));
    Assert.assertEquals("321", Bytes.toString(GSON.fromJson(
      results.get(1).getColumns().get(1).toString(), byte[].class)));
  }
}
