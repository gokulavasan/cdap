/*
 * Copyright 2014 Cask, Inc.
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

package co.cask.cdap.batch.stream;

import co.cask.cdap.test.ApplicationManager;
import co.cask.cdap.test.MapReduceManager;
import co.cask.cdap.test.ReactorTestBase;
import co.cask.cdap.test.StreamWriter;
import co.cask.cdap.test.XSlowTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.TimeUnit;

/**
 *
 */
@Category(XSlowTests.class)
public class TestBatchStreamIntegration extends ReactorTestBase {
  @Test
  public void testStreamBatch() throws Exception {
    ApplicationManager applicationManager = deployApplication(TestBatchStreamIntegrationApp.class);
    try {
      StreamWriter s1 = applicationManager.getStreamWriter("s1");
      for (int i = 0; i < 50; i++) {
        s1.send(String.valueOf(i));
      }

      MapReduceManager mapReduceManager = applicationManager.startMapReduce("StreamTestBatch");
      mapReduceManager.waitForFinish(60, TimeUnit.SECONDS);

      // TODO: verify results

    } finally {
      applicationManager.stopAll();
      TimeUnit.SECONDS.sleep(1);
      clear();
    }
  }
}