/*
 * Copyright 2012-2014 Continuuity, Inc.
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

package com.continuuity.internal.app.store;

import com.continuuity.api.ProgramSpecification;
import com.continuuity.api.data.DataSetSpecification;
import com.continuuity.api.data.stream.StreamSpecification;
import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.table.Table;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.flow.FlowletConnection;
import com.continuuity.api.flow.FlowletDefinition;
import com.continuuity.api.procedure.ProcedureSpecification;
import com.continuuity.api.service.ServiceSpecification;
import com.continuuity.app.ApplicationSpecification;
import com.continuuity.app.program.Program;
import com.continuuity.app.program.Programs;
import com.continuuity.app.store.Store;
import com.continuuity.archive.ArchiveBundler;
import com.continuuity.common.conf.CConfiguration;
import com.continuuity.common.conf.Constants;
import com.continuuity.data.DataSetAccessor;
import com.continuuity.data2.OperationException;
import com.continuuity.data2.datafabric.ReactorDatasetNamespace;
import com.continuuity.data2.datafabric.dataset.DatasetsUtil;
import com.continuuity.data2.dataset2.DatasetFramework;
import com.continuuity.data2.dataset2.NamespacedDatasetFramework;
import com.continuuity.data2.dataset2.tx.Transactional;
import com.continuuity.internal.app.ForwardingApplicationSpecification;
import com.continuuity.internal.app.ForwardingFlowSpecification;
import com.continuuity.internal.app.ForwardingResourceSpecification;
import com.continuuity.internal.app.ForwardingRuntimeSpecification;
import com.continuuity.internal.app.ForwardingTwillSpecification;
import com.continuuity.internal.app.program.ProgramBundle;
import com.continuuity.internal.procedure.DefaultProcedureSpecification;
import com.continuuity.internal.service.DefaultServiceSpecification;
import com.continuuity.proto.Id;
import com.continuuity.proto.ProgramType;
import com.continuuity.proto.RunRecord;
import com.continuuity.tephra.DefaultTransactionExecutor;
import com.continuuity.tephra.TransactionAware;
import com.continuuity.tephra.TransactionExecutor;
import com.continuuity.tephra.TransactionExecutorFactory;
import com.continuuity.tephra.TransactionSystemClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.twill.api.ResourceSpecification;
import org.apache.twill.api.RuntimeSpecification;
import org.apache.twill.api.TwillSpecification;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Implementation of the Store that ultimately places data into MetaDataTable.
 */
public class DefaultStore implements Store {
  public static final String APP_META_TABLE = "app.meta";
  private static final Logger LOG = LoggerFactory.getLogger(DefaultStore.class);

  private final LocationFactory locationFactory;
  private final CConfiguration configuration;
  private final DatasetFramework dsFramework;

  private Transactional<AppMds, AppMetadataStore> txnl;

  @Inject
  public DefaultStore(CConfiguration conf,
                      LocationFactory locationFactory,
                      final TransactionSystemClient txClient,
                      DatasetFramework framework) {

    this.locationFactory = locationFactory;
    this.configuration = conf;
    this.dsFramework =
      new NamespacedDatasetFramework(framework, new ReactorDatasetNamespace(conf, DataSetAccessor.Namespace.SYSTEM));

    txnl =
      Transactional.of(
        new TransactionExecutorFactory() {
          @Override
          public TransactionExecutor createExecutor(Iterable<TransactionAware> transactionAwares) {
            return new DefaultTransactionExecutor(txClient, transactionAwares);
          }},
        new Supplier<AppMds>() {
          @Override
          public AppMds get() {
            try {
              Table mdsTable = DatasetsUtil.getOrCreateDataset(dsFramework, APP_META_TABLE, "table",
                                                               DatasetProperties.EMPTY,
                                                               DatasetDefinition.NO_ARGUMENTS, null);
              return new AppMds(mdsTable);
            } catch (Exception e) {
              LOG.error("Failed to access app.meta table", e);
              throw Throwables.propagate(e);
            }
          }
        });
  }

  public static void upgrade(DatasetFramework framework) throws Exception {
    DatasetAdmin admin = framework.getAdmin(APP_META_TABLE, null);
    if (admin != null) {
      admin.upgrade();
    }
  }

  @Nullable
  @Override
  public Program loadProgram(final Id.Program id, ProgramType type) throws IOException {
    ApplicationMeta appMeta = txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, ApplicationMeta>() {
      @Override
      public ApplicationMeta apply(AppMds mds) throws Exception {
        return mds.apps.getApplication(id.getAccountId(), id.getApplicationId());
      }
    });

    if (appMeta == null) {
      return null;
    }

    Location programLocation = getProgramLocation(id, type);
    // I guess this can happen when app is being deployed at the moment... todo: should be prevented by framework
    Preconditions.checkArgument(appMeta.getLastUpdateTs() >= programLocation.lastModified(),
                                "Newer program update time than the specification update time. " +
                                "Application must be redeployed");

    return Programs.create(programLocation);
  }

  @Override
  public void setStart(final Id.Program id, final String pid, final long startTime) {
    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.recordProgramStart(id.getAccountId(), id.getApplicationId(), id.getId(), pid, startTime);
        return null;
      }
    });
  }

  @Override
  public void setStop(final Id.Program id, final String pid, final long endTime, final String state) {
    Preconditions.checkArgument(state != null, "End state of program run should be defined");

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.recordProgramStop(id.getAccountId(), id.getApplicationId(), id.getId(), pid, endTime, state);
        return null;
      }
    });



    // todo: delete old history data
  }

  @Override
  public List<RunRecord> getRunHistory(final Id.Program id, final long startTime, final long endTime, final int limit) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, List<RunRecord>>() {
      @Override
      public List<RunRecord> apply(AppMds mds) throws Exception {
        return mds.apps.getRunHistory(id.getAccountId(), id.getApplicationId(), id.getId(),
                                      startTime, endTime, limit);
      }
    });
  }

  @Override
  public void addApplication(final Id.Application id,
                             final ApplicationSpecification spec, final Location appArchiveLocation) {

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.writeApplication(id.getAccountId(), id.getId(), spec, appArchiveLocation.toURI().toString());

        for (DataSetSpecification dsSpec : spec.getDataSets().values()) {
          mds.apps.writeDataset(id.getAccountId(), dsSpec);
        }
        for (StreamSpecification stream : spec.getStreams().values()) {
          mds.apps.writeStream(id.getAccountId(), stream);
        }

        return null;
      }
    });

  }

  // todo: this method should be moved into DeletedProgramHandlerState, bad design otherwise
  @Override
  public List<ProgramSpecification> getDeletedProgramSpecifications(final Id.Application id,
                                                                    ApplicationSpecification appSpec) {

    ApplicationMeta existing = txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, ApplicationMeta>() {
      @Override
      public ApplicationMeta apply(AppMds mds) throws Exception {
        return mds.apps.getApplication(id.getAccountId(), id.getId());
      }
    });

    List<ProgramSpecification> deletedProgramSpecs = Lists.newArrayList();

    if (existing != null) {
      ApplicationSpecification existingAppSpec = existing.getSpec();

      ImmutableMap<String, ProgramSpecification> existingSpec = new ImmutableMap.Builder<String, ProgramSpecification>()
                                                                      .putAll(existingAppSpec.getMapReduce())
                                                                      .putAll(existingAppSpec.getWorkflows())
                                                                      .putAll(existingAppSpec.getFlows())
                                                                      .putAll(existingAppSpec.getProcedures())
                                                                      .putAll(existingAppSpec.getServices())
                                                                      .build();

      ImmutableMap<String, ProgramSpecification> newSpec = new ImmutableMap.Builder<String, ProgramSpecification>()
                                                                      .putAll(appSpec.getMapReduce())
                                                                      .putAll(appSpec.getWorkflows())
                                                                      .putAll(appSpec.getFlows())
                                                                      .putAll(appSpec.getProcedures())
                                                                      .putAll(appSpec.getServices())
                                                                      .build();


      MapDifference<String, ProgramSpecification> mapDiff = Maps.difference(existingSpec, newSpec);
      deletedProgramSpecs.addAll(mapDiff.entriesOnlyOnLeft().values());
    }

    return deletedProgramSpecs;
  }

  @Override
  public void addDataset(final Id.Account id, final DataSetSpecification dsSpec) {
    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.writeDataset(id.getId(), dsSpec);
        return null;
      }
    });

  }

  @Override
  public void removeDataSet(final Id.Account id, final String name) {
    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.deleteDataset(id.getId(), name);
        return null;
      }
    });

  }

  @Override
  public DataSetSpecification getDataSet(final Id.Account id, final String name) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, DataSetSpecification>() {
      @Override
      public DataSetSpecification apply(AppMds mds) throws Exception {
        return mds.apps.getDataset(id.getId(), name);
      }
    });

  }

  @Override
  public Collection<DataSetSpecification> getAllDataSets(final Id.Account id) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Collection<DataSetSpecification>>() {
      @Override
      public Collection<DataSetSpecification> apply(AppMds mds) throws Exception {
        return mds.apps.getAllDatasets(id.getId());
      }
    });
  }

  @Override
  public void addStream(final Id.Account id, final StreamSpecification streamSpec) {
    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.writeStream(id.getId(), streamSpec);
        return null;
      }
    });
  }

  @Override
  public void removeStream(final Id.Account id, final String name) {
    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.deleteStream(id.getId(), name);
        return null;
      }
    });
  }

  @Override
  public StreamSpecification getStream(final Id.Account id, final String name) throws OperationException {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, StreamSpecification>() {
      @Override
      public StreamSpecification apply(AppMds mds) throws Exception {
        return mds.apps.getStream(id.getId(), name);
      }
    });
  }

  @Override
  public Collection<StreamSpecification> getAllStreams(final Id.Account id) throws OperationException {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Collection<StreamSpecification>>() {
      @Override
      public Collection<StreamSpecification> apply(AppMds mds) throws Exception {
        return mds.apps.getAllStreams(id.getId());
      }
    });
  }

  @Override
  public void setFlowletInstances(final Id.Program id, final String flowletId, final int count) {
    Preconditions.checkArgument(count > 0, "cannot change number of flowlet instances to negative number: " + count);

    LOG.trace("Setting flowlet instances: account: {}, application: {}, flow: {}, flowlet: {}, new instances count: {}",
              id.getAccountId(), id.getApplicationId(), id.getId(), flowletId, count);

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        ApplicationSpecification appSpec = getAppSpecOrFail(mds, id);
        ApplicationSpecification newAppSpec = updateFlowletInstancesInAppSpec(appSpec, id, flowletId, count);
        replaceAppSpecInProgramJar(id, newAppSpec, ProgramType.FLOW);

        mds.apps.updateAppSpec(id.getAccountId(), id.getApplicationId(), newAppSpec);
        return null;
      }
    });

    LOG.trace("Set flowlet instances: account: {}, application: {}, flow: {}, flowlet: {}, instances now: {}",
              id.getAccountId(), id.getApplicationId(), id.getId(), flowletId, count);
  }

  @Override
  public int getFlowletInstances(final Id.Program id, final String flowletId) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Integer>() {
      @Override
      public Integer apply(AppMds mds) throws Exception {
        ApplicationSpecification appSpec = getAppSpecOrFail(mds, id);
        FlowSpecification flowSpec = getFlowSpecOrFail(id, appSpec);
        FlowletDefinition flowletDef = getFlowletDefinitionOrFail(flowSpec, flowletId, id);
        return flowletDef.getInstances();
      }
    });

  }

  @Override
  public int getProcedureInstances(final Id.Program id) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Integer>() {
      @Override
      public Integer apply(AppMds mds) throws Exception {
        ApplicationSpecification appSpec = getAppSpecOrFail(mds, id);
        ProcedureSpecification specification = getProcedureSpecOrFail(id, appSpec);
        return specification.getInstances();
      }
    });
  }

  @Override
  public void setProcedureInstances(final Id.Program id, final int count) {
    Preconditions.checkArgument(count > 0, "cannot change number of program instances to negative number: " + count);

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        ApplicationSpecification appSpec = getAppSpecOrFail(mds, id);
        ProcedureSpecification specification = getProcedureSpecOrFail(id, appSpec);

        ProcedureSpecification newSpecification =  new DefaultProcedureSpecification(specification.getClassName(),
                                                                                     specification.getName(),
                                                                                     specification.getDescription(),
                                                                                     specification.getDataSets(),
                                                                                     specification.getProperties(),
                                                                                     specification.getResources(),
                                                                                     count);

        ApplicationSpecification newAppSpec = replaceProcedureInAppSpec(appSpec, id, newSpecification);
        replaceAppSpecInProgramJar(id, newAppSpec, ProgramType.PROCEDURE);

        mds.apps.updateAppSpec(id.getAccountId(), id.getApplicationId(), newAppSpec);
        return null;
      }
    });

    LOG.trace("Setting program instances: account: {}, application: {}, procedure: {}, new instances count: {}",
              id.getAccountId(), id.getApplicationId(), id.getId(), count);
  }

  @Override
  public int getServiceRunnableInstances(final Id.Program id, final String runnable) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Integer>() {
      @Override
      public Integer apply(AppMds mds) throws Exception {
        ApplicationSpecification appSpec = getAppSpecOrFail(mds, id);
        ServiceSpecification serviceSpec = getServiceSpecOrFail(id, appSpec);
        RuntimeSpecification runtimeSpec = serviceSpec.getRunnables().get(runnable);
        return runtimeSpec.getResourceSpecification().getInstances();
      }
    });
  }

  @Override
  public void setServiceRunnableInstances(final Id.Program id, final String runnable, final int count) {

    Preconditions.checkArgument(count > 0, "cannot change number of program instances to negative number: " + count);

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        ApplicationSpecification appSpec = getAppSpecOrFail(mds, id);
        ServiceSpecification serviceSpec = getServiceSpecOrFail(id, appSpec);

        RuntimeSpecification runtimeSpec = serviceSpec.getRunnables().get(runnable);
        if (runtimeSpec == null) {
          throw new IllegalArgumentException(String.format("Runnable not found, app: %s, service: %s, runnable %s",
                                                           id.getApplication(), id.getId(), runnable));
        }

        ResourceSpecification resourceSpec = replaceInstanceCount(runtimeSpec.getResourceSpecification(), count);
        RuntimeSpecification newRuntimeSpec = replaceResourceSpec(runtimeSpec, resourceSpec);

        Preconditions.checkNotNull(newRuntimeSpec);

        ApplicationSpecification newAppSpec =
          replaceServiceSpec(appSpec, id.getId(), replaceRuntimeSpec(runnable, serviceSpec, newRuntimeSpec));
        replaceAppSpecInProgramJar(id, newAppSpec, ProgramType.SERVICE);

        mds.apps.updateAppSpec(id.getAccountId(), id.getApplicationId(), newAppSpec);
        return null;
      }
    });

    LOG.trace("Setting program instances: account: {}, application: {}, service: {}, runnable: {}," +
                " new instances count: {}",
              id.getAccountId(), id.getApplicationId(), id.getId(), runnable, count);
  }

  @Override
  public void removeApplication(final Id.Application id) {
    LOG.trace("Removing application: account: {}, application: {}", id.getAccountId(), id.getId());

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.deleteApplication(id.getAccountId(), id.getId());
        mds.apps.deleteProgramArgs(id.getAccountId(), id.getId());
        // todo: delete program history?
        return null;
      }
    });
  }

  @Override
  public void removeAllApplications(final Id.Account id) {
    LOG.trace("Removing all applications of account with id: {}", id.getId());

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.deleteApplications(id.getId());
        mds.apps.deleteProgramArgs(id.getId());
        // todo: delete program history?
        return null;
      }
    });
  }

  @Override
  public void removeAll(final Id.Account id) {
    LOG.trace("Removing all applications of account with id: {}", id.getId());

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.deleteApplications(id.getId());
        mds.apps.deleteProgramArgs(id.getId());
        mds.apps.deleteAllDatasets(id.getId());
        mds.apps.deleteAllStreams(id.getId());
        // todo: delete program history?
        return null;
      }
    });
  }

  @Override
  public void storeRunArguments(final Id.Program id, final Map<String, String> arguments) {
    LOG.trace("Updated program args in mds: id: {}, app: {}, prog: {}, args: {}",
              id.getId(), id.getApplicationId(), id.getId(), Joiner.on(",").withKeyValueSeparator("=").join(arguments));

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        mds.apps.writeProgramArgs(id.getAccountId(), id.getApplicationId(), id.getId(), arguments);
        return null;
      }
    });
  }

  @Override
  public Map<String, String> getRunArguments(final Id.Program id) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Map<String, String>>() {
      @Override
      public Map<String, String> apply(AppMds mds) throws Exception {
        ProgramArgs programArgs = mds.apps.getProgramArgs(id.getAccountId(), id.getApplicationId(), id.getId());
        return programArgs == null ? Maps.<String, String>newHashMap() : programArgs.getArgs();
      }
    });
  }

  @Nullable
  @Override
  public ApplicationSpecification getApplication(final Id.Application id) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, ApplicationSpecification>() {
      @Override
      public ApplicationSpecification apply(AppMds mds) throws Exception {
        return getApplicationSpec(mds, id);
      }
    });
  }

  @Override
  public Collection<ApplicationSpecification> getAllApplications(final Id.Account id) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Collection<ApplicationSpecification>>() {
      @Override
      public Collection<ApplicationSpecification> apply(AppMds mds) throws Exception {
        return Lists.transform(mds.apps.getAllApplications(id.getId()),
                               new Function<ApplicationMeta, ApplicationSpecification>() {
                                 @Override
                                 public ApplicationSpecification apply(ApplicationMeta input) {
                                   return input.getSpec();
                                 }
                               });
      }
    });
  }

  @Nullable
  @Override
  public Location getApplicationArchiveLocation(final Id.Application id) {
    return txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Location>() {
      @Override
      public Location apply(AppMds mds) throws Exception {
        ApplicationMeta meta = mds.apps.getApplication(id.getAccountId(), id.getId());
        return meta == null ? null : locationFactory.create(URI.create(meta.getArchiveLocation()));
      }
    });
  }

  @Override
  public void changeFlowletSteamConnection(final Id.Program flow, final String flowletId,
                                           final String oldValue, final String newValue) {

    Preconditions.checkArgument(flow != null, "flow cannot be null");
    Preconditions.checkArgument(flowletId != null, "flowletId cannot be null");
    Preconditions.checkArgument(oldValue != null, "oldValue cannot be null");
    Preconditions.checkArgument(newValue != null, "newValue cannot be null");

    LOG.trace("Changing flowlet stream connection: account: {}, application: {}, flow: {}, flowlet: {}," +
                " old coonnected stream: {}, new connected stream: {}",
              flow.getAccountId(), flow.getApplicationId(), flow.getId(), flowletId, oldValue, newValue);

    txnl.executeUnchecked(new TransactionExecutor.Function<AppMds, Void>() {
      @Override
      public Void apply(AppMds mds) throws Exception {
        ApplicationSpecification appSpec = getAppSpecOrFail(mds, flow);

        FlowSpecification flowSpec = getFlowSpecOrFail(flow, appSpec);

        boolean adjusted = false;
        List<FlowletConnection> conns = Lists.newArrayList();
        for (FlowletConnection con : flowSpec.getConnections()) {
          if (FlowletConnection.Type.STREAM == con.getSourceType() &&
            flowletId.equals(con.getTargetName()) &&
            oldValue.equals(con.getSourceName())) {

            conns.add(new FlowletConnection(con.getSourceType(), newValue, con.getTargetName()));
            adjusted = true;
          } else {
            conns.add(con);
          }
        }

        if (!adjusted) {
          throw new IllegalArgumentException(
            String.format("Cannot change stream connection to %s, the connection to be changed is not found," +
                            " account: %s, application: %s, flow: %s, flowlet: %s, source stream: %s",
                          newValue, flow.getAccountId(), flow.getApplicationId(), flow.getId(), flowletId, oldValue));
        }

        FlowletDefinition flowletDef = getFlowletDefinitionOrFail(flowSpec, flowletId, flow);
        FlowletDefinition newFlowletDef = new FlowletDefinition(flowletDef, oldValue, newValue);
        ApplicationSpecification newAppSpec = replaceInAppSpec(appSpec, flow, flowSpec, newFlowletDef, conns);

        replaceAppSpecInProgramJar(flow, newAppSpec, ProgramType.FLOW);

        Id.Application app = flow.getApplication();
        mds.apps.updateAppSpec(app.getAccountId(), app.getId(), newAppSpec);
        return null;
      }
    });


    LOG.trace("Changed flowlet stream connection: account: {}, application: {}, flow: {}, flowlet: {}," +
                " old coonnected stream: {}, new connected stream: {}",
              flow.getAccountId(), flow.getApplicationId(), flow.getId(), flowletId, oldValue, newValue);

    // todo: change stream "used by" flow mapping in metadata?
  }

  @VisibleForTesting
  void clear() throws Exception {
    DatasetAdmin admin = dsFramework.getAdmin(APP_META_TABLE, null);
    if (admin != null) {
      admin.truncate();
    }
  }

  /**
   * @return The {@link Location} of the given program.
   * @throws RuntimeException if program can't be found.
   */
  private Location getProgramLocation(Id.Program id, ProgramType type) throws IOException {
    String appFabricOutputDir = configuration.get(Constants.AppFabric.OUTPUT_DIR,
                                                  System.getProperty("java.io.tmpdir"));
    return Programs.programLocation(locationFactory, appFabricOutputDir, id, type);
  }

  private ApplicationSpecification getApplicationSpec(AppMds mds, Id.Application id) {
    ApplicationMeta meta = mds.apps.getApplication(id.getAccountId(), id.getId());
    return meta == null ? null : meta.getSpec();
  }

  private static ResourceSpecification replaceInstanceCount(final ResourceSpecification spec,
                                                     final int instanceCount) {
    return new ResourceSpecificationWithChangedInstances(spec, instanceCount);
  }

  private static class ResourceSpecificationWithChangedInstances extends ForwardingResourceSpecification {
    private final int instanceCount;

    private ResourceSpecificationWithChangedInstances(ResourceSpecification specification, int instanceCount) {
      super(specification);
      this.instanceCount = instanceCount;
    }

    @Override
    public int getInstances() {
      return instanceCount;
    }
  }

  private static RuntimeSpecification replaceResourceSpec(final RuntimeSpecification runtimeSpec,
                                                   final ResourceSpecification resourceSpec) {
    return new RuntimeSpecificationWithChangedResources(runtimeSpec, resourceSpec);
  }

  private static final class RuntimeSpecificationWithChangedResources extends ForwardingRuntimeSpecification {
    private final ResourceSpecification resourceSpec;

    private RuntimeSpecificationWithChangedResources(RuntimeSpecification delegate,
                                                     ResourceSpecification resourceSpec) {
      super(delegate);
      this.resourceSpec = resourceSpec;
    }

    @Override
    public ResourceSpecification getResourceSpecification() {
      return resourceSpec;
    }
  }

  private static ApplicationSpecification replaceServiceSpec(final ApplicationSpecification appSpec,
                                                      final String serviceName,
                                                      final ServiceSpecification serviceSpecification) {
    return new ApplicationSpecificationWithChangedServices(appSpec, serviceName, serviceSpecification);
  }

  private static final class ApplicationSpecificationWithChangedServices extends ForwardingApplicationSpecification {
    private final String serviceName;
    private final ServiceSpecification serviceSpecification;

    private ApplicationSpecificationWithChangedServices(ApplicationSpecification delegate,
                                                        String serviceName, ServiceSpecification serviceSpecification) {
      super(delegate);
      this.serviceName = serviceName;
      this.serviceSpecification = serviceSpecification;
    }

    @Override
    public Map<String, ServiceSpecification> getServices() {
      Map<String, ServiceSpecification> services = Maps.newHashMap(super.getServices());
      services.put(serviceName, serviceSpecification);
      return services;
    }
  }

  private static ServiceSpecification replaceRuntimeSpec(final String runnable, final ServiceSpecification spec,
                                                  final RuntimeSpecification runtimeSpec) {
    return new DefaultServiceSpecification(spec.getName(),
                                           new TwillSpecificationWithChangedRunnable(spec, runnable, runtimeSpec));
  }

  private static final class TwillSpecificationWithChangedRunnable extends ForwardingTwillSpecification {
    private final String runnable;
    private final RuntimeSpecification runtimeSpec;

    private TwillSpecificationWithChangedRunnable(TwillSpecification specification,
                                                  String runnable, RuntimeSpecification runtimeSpec) {
      super(specification);
      this.runnable = runnable;
      this.runtimeSpec = runtimeSpec;
    }

    @Override
    public Map<String, RuntimeSpecification> getRunnables() {
      Map<String, RuntimeSpecification> specs = Maps.newHashMap(
        super.getRunnables());
      specs.put(runnable, runtimeSpec);
      return specs;
    }
  }

  private void replaceAppSpecInProgramJar(Id.Program id, ApplicationSpecification appSpec, ProgramType type) {
    try {
      Location programLocation = getProgramLocation(id, type);
      ArchiveBundler bundler = new ArchiveBundler(programLocation);

      Program program = Programs.create(programLocation);
      String className = program.getMainClassName();

      Location tmpProgramLocation = programLocation.getTempFile("");
      try {
        ProgramBundle.create(id.getApplication(), bundler, tmpProgramLocation, id.getId(), className, type, appSpec);

        Location movedTo = tmpProgramLocation.renameTo(programLocation);
        if (movedTo == null) {
          throw new RuntimeException("Could not replace program jar with the one with updated app spec, " +
                                       "original program file: " + programLocation.toURI() +
                                       ", was trying to replace with file: " + tmpProgramLocation.toURI());
        }
      } finally {
        if (tmpProgramLocation != null && tmpProgramLocation.exists()) {
          tmpProgramLocation.delete();
        }
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private static FlowletDefinition getFlowletDefinitionOrFail(FlowSpecification flowSpec,
                                                              String flowletId, Id.Program id) {
    FlowletDefinition flowletDef = flowSpec.getFlowlets().get(flowletId);
    if (flowletDef == null) {
      throw new IllegalArgumentException("no such flowlet @ account id: " + id.getAccountId() +
                                           ", app id: " + id.getApplication() +
                                           ", flow id: " + id.getId() +
                                           ", flowlet id: " + id.getId());
    }
    return flowletDef;
  }

  private static FlowSpecification getFlowSpecOrFail(Id.Program id, ApplicationSpecification appSpec) {
    FlowSpecification flowSpec = appSpec.getFlows().get(id.getId());
    if (flowSpec == null) {
      throw new IllegalArgumentException("no such flow @ account id: " + id.getAccountId() +
                                           ", app id: " + id.getApplication() +
                                           ", flow id: " + id.getId());
    }
    return flowSpec;
  }

  private static ServiceSpecification getServiceSpecOrFail(Id.Program id, ApplicationSpecification appSpec) {
    ServiceSpecification spec = appSpec.getServices().get(id.getId());
    if (spec == null) {
      throw new IllegalArgumentException("no such service @ account id: " + id.getAccountId() +
                                           ", app id: " + id.getApplication() +
                                           ", service id: " + id.getId());
    }
    return spec;
  }

  private static ProcedureSpecification getProcedureSpecOrFail(Id.Program id, ApplicationSpecification appSpec) {
    ProcedureSpecification procedureSpecification = appSpec.getProcedures().get(id.getId());
    if (procedureSpecification == null) {
      throw new IllegalArgumentException("no such procedure @ account id: " + id.getAccountId() +
                                           ", app id: " + id.getApplication() +
                                           ", procedure id: " + id.getId());
    }
    return procedureSpecification;
  }

  private static ApplicationSpecification updateFlowletInstancesInAppSpec(ApplicationSpecification appSpec,
                                                                          Id.Program id, String flowletId, int count) {

    FlowSpecification flowSpec = getFlowSpecOrFail(id, appSpec);
    FlowletDefinition flowletDef = getFlowletDefinitionOrFail(flowSpec, flowletId, id);

    final FlowletDefinition adjustedFlowletDef = new FlowletDefinition(flowletDef, count);
    return replaceFlowletInAppSpec(appSpec, id, flowSpec, adjustedFlowletDef);
  }

  private ApplicationSpecification getAppSpecOrFail(AppMds mds, Id.Program id) {
    ApplicationSpecification appSpec = getApplicationSpec(mds, id.getApplication());
    if (appSpec == null) {
      throw new IllegalArgumentException("no such application @ account id: " + id.getAccountId() +
                                           ", app id: " + id.getApplication().getId());
    }
    return appSpec;
  }

  private static ApplicationSpecification replaceInAppSpec(final ApplicationSpecification appSpec,
                                                    final Id.Program id,
                                                    final FlowSpecification flowSpec,
                                                    final FlowletDefinition adjustedFlowletDef,
                                                    final List<FlowletConnection> connections) {
    // as app spec is immutable we have to do this trick
    return replaceFlowInAppSpec(appSpec, id,
                                new FlowSpecificationWithChangedFlowletsAndConnections(flowSpec,
                                                                                       adjustedFlowletDef,
                                                                                       connections));
  }

  private static class FlowSpecificationWithChangedFlowlets extends ForwardingFlowSpecification {
    private final FlowletDefinition adjustedFlowletDef;

    private FlowSpecificationWithChangedFlowlets(FlowSpecification delegate,
                                                 FlowletDefinition adjustedFlowletDef) {
      super(delegate);
      this.adjustedFlowletDef = adjustedFlowletDef;
    }

    @Override
    public Map<String, FlowletDefinition> getFlowlets() {
      Map<String, FlowletDefinition> flowlets = Maps.newHashMap(super.getFlowlets());
      flowlets.put(adjustedFlowletDef.getFlowletSpec().getName(), adjustedFlowletDef);
      return flowlets;
    }
  }

  private static final class FlowSpecificationWithChangedFlowletsAndConnections
    extends FlowSpecificationWithChangedFlowlets {

    private final List<FlowletConnection> connections;

    private FlowSpecificationWithChangedFlowletsAndConnections(FlowSpecification delegate,
                                                               FlowletDefinition adjustedFlowletDef,
                                                               List<FlowletConnection> connections) {
      super(delegate, adjustedFlowletDef);
      this.connections = connections;
    }

    @Override
    public List<FlowletConnection> getConnections() {
      return connections;
    }
  }

  private static ApplicationSpecification replaceFlowletInAppSpec(final ApplicationSpecification appSpec,
                                                           final Id.Program id,
                                                           final FlowSpecification flowSpec,
                                                           final FlowletDefinition adjustedFlowletDef) {
    // as app spec is immutable we have to do this trick
    return replaceFlowInAppSpec(appSpec, id, new FlowSpecificationWithChangedFlowlets(flowSpec, adjustedFlowletDef));
  }

  private static ApplicationSpecification replaceFlowInAppSpec(final ApplicationSpecification appSpec,
                                                               final Id.Program id,
                                                               final FlowSpecification newFlowSpec) {
    // as app spec is immutable we have to do this trick
    return new ApplicationSpecificationWithChangedFlows(appSpec, id.getId(), newFlowSpec);
  }

  private static final class ApplicationSpecificationWithChangedFlows extends ForwardingApplicationSpecification {
    private final FlowSpecification newFlowSpec;
    private final String flowId;

    private ApplicationSpecificationWithChangedFlows(ApplicationSpecification delegate,
                                                     String flowId, FlowSpecification newFlowSpec) {
      super(delegate);
      this.newFlowSpec = newFlowSpec;
      this.flowId = flowId;
    }

    @Override
    public Map<String, FlowSpecification> getFlows() {
      Map<String, FlowSpecification> flows = Maps.newHashMap(super.getFlows());
      flows.put(flowId, newFlowSpec);
      return flows;
    }
  }

  private static ApplicationSpecification replaceProcedureInAppSpec(
                                                             final ApplicationSpecification appSpec,
                                                             final Id.Program id,
                                                             final ProcedureSpecification procedureSpecification) {
    // replace the new procedure spec.
    return new ApplicationSpecificationWithChangedProcedure(appSpec, id.getId(), procedureSpecification);
  }

  private static final class ApplicationSpecificationWithChangedProcedure extends ForwardingApplicationSpecification {
    private final String procedureId;
    private final ProcedureSpecification procedureSpecification;

    private ApplicationSpecificationWithChangedProcedure(ApplicationSpecification delegate,
                                                         String procedureId,
                                                         ProcedureSpecification procedureSpecification) {
      super(delegate);
      this.procedureId = procedureId;
      this.procedureSpecification = procedureSpecification;
    }

    @Override
    public Map<String, ProcedureSpecification> getProcedures() {
      Map<String, ProcedureSpecification> procedures = Maps.newHashMap(super.getProcedures());
       procedures.put(procedureId, procedureSpecification);
      return procedures;
    }
  }

  private static final class AppMds implements Iterable<AppMetadataStore> {
    private final AppMetadataStore apps;

    private AppMds(Table mdsTable) {
      this.apps = new AppMetadataStore(mdsTable);
    }

    @Override
    public Iterator<AppMetadataStore> iterator() {
      return Iterators.singletonIterator(apps);
    }
  }
}