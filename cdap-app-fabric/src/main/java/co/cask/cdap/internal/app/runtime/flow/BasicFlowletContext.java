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

package co.cask.cdap.internal.app.runtime.flow;

import co.cask.cdap.api.flow.flowlet.FlowletContext;
import co.cask.cdap.api.flow.flowlet.FlowletSpecification;
import co.cask.cdap.api.metrics.Metrics;
import co.cask.cdap.app.metrics.FlowletMetrics;
import co.cask.cdap.app.program.Program;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.logging.LoggingContext;
import co.cask.cdap.common.metrics.MetricsCollectionService;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.internal.app.runtime.AbstractContext;
import co.cask.cdap.logging.context.FlowletLoggingContext;
import org.apache.twill.api.RunId;
import org.apache.twill.discovery.DiscoveryServiceClient;

import java.util.Set;

/**
 * Internal implementation of {@link FlowletContext}.
 */
final class BasicFlowletContext extends AbstractContext implements FlowletContext {

  private final String flowId;
  private final String flowletId;
  private final long groupId;
  private final int instanceId;
  private final FlowletSpecification flowletSpec;

  private volatile int instanceCount;
  private final FlowletMetrics flowletMetrics;

  BasicFlowletContext(Program program, String flowletId,
                      int instanceId, RunId runId,
                      int instanceCount, Set<String> datasets,
                      Arguments runtimeArguments, FlowletSpecification flowletSpec,
                      MetricsCollectionService metricsCollectionService,
                      DiscoveryServiceClient discoveryServiceClient,
                      DatasetFramework dsFramework,
                      CConfiguration conf) {
    super(program, runId, runtimeArguments, datasets,
          getMetricContext(program, flowletId, instanceId),
          metricsCollectionService,
          dsFramework, conf, discoveryServiceClient);
    this.accountId = program.getAccountId();
    this.flowId = program.getName();
    this.flowletId = flowletId;
    this.groupId = FlowUtils.generateConsumerGroupId(program, flowletId);
    this.instanceId = instanceId;
    this.instanceCount = instanceCount;
    this.flowletSpec = flowletSpec;
    this.flowletMetrics = new FlowletMetrics(metricsCollectionService, getApplicationId(), flowId, flowletId,
                                             runId.getId());
  }

  @Override
  public String toString() {
    return String.format("flowlet=%s, instance=%d, groupsize=%s, %s",
                         getFlowletId(), getInstanceId(), getInstanceCount(), super.toString());
  }

  @Override
  public int getInstanceCount() {
    return instanceCount;
  }

  @Override
  public String getName() {
    return getFlowletId();
  }

  @Override
  public FlowletSpecification getSpecification() {
    return flowletSpec;
  }

  public void setInstanceCount(int count) {
    instanceCount = count;
  }

  public String getFlowId() {
    return flowId;
  }

  public String getFlowletId() {
    return flowletId;
  }

  @Override
  public int getInstanceId() {
    return instanceId;
  }

  public LoggingContext getLoggingContext() {
    return new FlowletLoggingContext(getAccountId(), getApplicationId(), getFlowId(), getFlowletId());
  }

  @Override
  public Metrics getMetrics() {
    return flowletMetrics;
  }

  public long getGroupId() {
    return groupId;
  }

  private static String getMetricContext(Program program, String flowletId, int instanceId) {
    return String.format("%s.f.%s.%s.%d",
                         program.getApplicationId(),
                         // flow name
                         program.getName(),
                         flowletId,
                         instanceId);
  }
}
