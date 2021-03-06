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
package co.cask.cdap.app.guice;

import co.cask.cdap.app.deploy.Manager;
import co.cask.cdap.app.deploy.ManagerFactory;
import co.cask.cdap.app.store.StoreFactory;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.common.runtime.RuntimeModule;
import co.cask.cdap.common.twill.MasterServiceManager;
import co.cask.cdap.common.utils.Networks;
import co.cask.cdap.data.stream.StreamServiceManager;
import co.cask.cdap.data2.datafabric.dataset.DatasetExecutorServiceManager;
import co.cask.cdap.explore.service.ExploreServiceManager;
import co.cask.cdap.gateway.handlers.AppFabricHttpHandler;
import co.cask.cdap.gateway.handlers.MonitorHandler;
import co.cask.cdap.gateway.handlers.PingHandler;
import co.cask.cdap.gateway.handlers.ServiceHttpHandler;
import co.cask.cdap.internal.app.deploy.LocalManager;
import co.cask.cdap.internal.app.deploy.pipeline.ApplicationWithPrograms;
import co.cask.cdap.internal.app.runtime.batch.InMemoryTransactionServiceManager;
import co.cask.cdap.internal.app.runtime.distributed.TransactionServiceManager;
import co.cask.cdap.internal.app.runtime.schedule.DataSetBasedScheduleStore;
import co.cask.cdap.internal.app.runtime.schedule.DistributedSchedulerService;
import co.cask.cdap.internal.app.runtime.schedule.ExecutorThreadPool;
import co.cask.cdap.internal.app.runtime.schedule.LocalSchedulerService;
import co.cask.cdap.internal.app.runtime.schedule.Scheduler;
import co.cask.cdap.internal.app.runtime.schedule.SchedulerService;
import co.cask.cdap.internal.app.store.DefaultStoreFactory;
import co.cask.cdap.internal.pipeline.SynchronousPipelineFactory;
import co.cask.cdap.logging.run.AppFabricServiceManager;
import co.cask.cdap.logging.run.InMemoryDatasetExecutorServiceManager;
import co.cask.cdap.logging.run.InMemoryExploreServiceManager;
import co.cask.cdap.logging.run.InMemoryLogSaverServiceManager;
import co.cask.cdap.logging.run.InMemoryMetricsProcessorServiceManager;
import co.cask.cdap.logging.run.InMemoryMetricsServiceManager;
import co.cask.cdap.logging.run.InMemoryStreamServiceManager;
import co.cask.cdap.logging.run.LogSaverStatusServiceManager;
import co.cask.cdap.metrics.runtime.MetricsProcessorStatusServiceManager;
import co.cask.cdap.metrics.runtime.MetricsServiceManager;
import co.cask.cdap.pipeline.PipelineFactory;
import co.cask.http.HttpHandler;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.apache.twill.filesystem.Location;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.DefaultThreadExecutor;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.StdJobRunShellFactory;
import org.quartz.impl.StdScheduler;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * AppFabric Service Runtime Module.
 */
public final class AppFabricServiceRuntimeModule extends RuntimeModule {

  @Override
  public Module getInMemoryModules() {
    return Modules.combine(new AppFabricServiceModule(),
                           new AbstractModule() {
                             @Override
                             protected void configure() {
                               bind(SchedulerService.class).to(LocalSchedulerService.class).in(Scopes.SINGLETON);
                               bind(Scheduler.class).to(SchedulerService.class);

                               MapBinder<String, MasterServiceManager> mapBinder = MapBinder.newMapBinder(
                                 binder(), String.class, MasterServiceManager.class);
                               mapBinder.addBinding(Constants.Service.LOGSAVER)
                                        .to(InMemoryLogSaverServiceManager.class);
                               mapBinder.addBinding(Constants.Service.TRANSACTION)
                                        .to(InMemoryTransactionServiceManager.class);
                               mapBinder.addBinding(Constants.Service.METRICS_PROCESSOR)
                                        .to(InMemoryMetricsProcessorServiceManager.class);
                               mapBinder.addBinding(Constants.Service.METRICS)
                                        .to(InMemoryMetricsServiceManager.class);
                               mapBinder.addBinding(Constants.Service.APP_FABRIC_HTTP)
                                        .to(AppFabricServiceManager.class);
                               mapBinder.addBinding(Constants.Service.STREAMS)
                                        .to(InMemoryStreamServiceManager.class);
                               mapBinder.addBinding(Constants.Service.DATASET_EXECUTOR)
                                        .to(InMemoryDatasetExecutorServiceManager.class);
                               mapBinder.addBinding(Constants.Service.EXPLORE_HTTP_USER_SERVICE)
                                        .to(InMemoryExploreServiceManager.class);
                             }
                           });
  }

  @Override
  public Module getStandaloneModules() {

    return Modules.combine(new AppFabricServiceModule(),
                           new AbstractModule() {
                             @Override
                             protected void configure() {
                               bind(SchedulerService.class).to(LocalSchedulerService.class).in(Scopes.SINGLETON);
                               bind(Scheduler.class).to(SchedulerService.class);

                               MapBinder<String, MasterServiceManager> mapBinder = MapBinder.newMapBinder(
                                 binder(), String.class, MasterServiceManager.class);
                               mapBinder.addBinding(Constants.Service.LOGSAVER)
                                        .to(InMemoryLogSaverServiceManager.class);
                               mapBinder.addBinding(Constants.Service.TRANSACTION)
                                        .to(InMemoryTransactionServiceManager.class);
                               mapBinder.addBinding(Constants.Service.METRICS_PROCESSOR)
                                        .to(InMemoryMetricsProcessorServiceManager.class);
                               mapBinder.addBinding(Constants.Service.METRICS)
                                        .to(InMemoryMetricsServiceManager.class);
                               mapBinder.addBinding(Constants.Service.APP_FABRIC_HTTP)
                                        .to(AppFabricServiceManager.class);
                               mapBinder.addBinding(Constants.Service.STREAMS)
                                        .to(InMemoryStreamServiceManager.class);
                               mapBinder.addBinding(Constants.Service.DATASET_EXECUTOR)
                                        .to(InMemoryDatasetExecutorServiceManager.class);
                               mapBinder.addBinding(Constants.Service.EXPLORE_HTTP_USER_SERVICE)
                                        .to(InMemoryExploreServiceManager.class);
                             }
                           });
  }

  @Override
  public Module getDistributedModules() {


    return Modules.combine(new AppFabricServiceModule(),
                           new AbstractModule() {
                             @Override
                             protected void configure() {
                               bind(SchedulerService.class).to(DistributedSchedulerService.class).in(Scopes.SINGLETON);
                               bind(Scheduler.class).to(SchedulerService.class);

                               MapBinder<String, MasterServiceManager> mapBinder = MapBinder.newMapBinder(
                                 binder(), String.class, MasterServiceManager.class);
                               mapBinder.addBinding(Constants.Service.LOGSAVER)
                                        .to(LogSaverStatusServiceManager.class);
                               mapBinder.addBinding(Constants.Service.TRANSACTION)
                                        .to(TransactionServiceManager.class);
                               mapBinder.addBinding(Constants.Service.METRICS_PROCESSOR)
                                        .to(MetricsProcessorStatusServiceManager.class);
                               mapBinder.addBinding(Constants.Service.METRICS)
                                        .to(MetricsServiceManager.class);
                               mapBinder.addBinding(Constants.Service.APP_FABRIC_HTTP)
                                        .to(AppFabricServiceManager.class);
                               mapBinder.addBinding(Constants.Service.STREAMS)
                                        .to(StreamServiceManager.class);
                               mapBinder.addBinding(Constants.Service.DATASET_EXECUTOR)
                                        .to(DatasetExecutorServiceManager.class);
                               mapBinder.addBinding(Constants.Service.EXPLORE_HTTP_USER_SERVICE)
                                        .to(ExploreServiceManager.class);
                             }
                           });
  }

  /**
   * Guice module for AppFabricServer. Requires data-fabric related bindings being available.
   */
  private static final class AppFabricServiceModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(PipelineFactory.class).to(SynchronousPipelineFactory.class);

      install(
        new FactoryModuleBuilder()
          .implement(new TypeLiteral<Manager<Location, ApplicationWithPrograms>>() { },
                     new TypeLiteral<LocalManager<Location, ApplicationWithPrograms>>() { })
          .build(new TypeLiteral<ManagerFactory<Location, ApplicationWithPrograms>>() { })
      );

      bind(StoreFactory.class).to(DefaultStoreFactory.class);

      Multibinder<HttpHandler> handlerBinder = Multibinder.newSetBinder(binder(), HttpHandler.class,
                                                                        Names.named("appfabric.http.handler"));
      handlerBinder.addBinding().to(AppFabricHttpHandler.class);
      handlerBinder.addBinding().to(PingHandler.class);
      handlerBinder.addBinding().to(MonitorHandler.class);
      handlerBinder.addBinding().to(ServiceHttpHandler.class);
    }

    @Provides
    @Named(Constants.AppFabric.SERVER_ADDRESS)
    public final InetAddress providesHostname(CConfiguration cConf) {
      return Networks.resolve(cConf.get(Constants.AppFabric.SERVER_ADDRESS),
                              new InetSocketAddress("localhost", 0).getAddress());
    }

    /**
     * Provides a supplier of quartz scheduler so that initialization of the scheduler can be done after guice
     * injection. It returns a singleton of Scheduler.
     */
    @Provides
    public Supplier<org.quartz.Scheduler> providesSchedulerSupplier(final DataSetBasedScheduleStore scheduleStore,
                                                                    final CConfiguration cConf) {
      return new Supplier<org.quartz.Scheduler>() {
        private org.quartz.Scheduler scheduler;

        @Override
        public synchronized org.quartz.Scheduler get() {
          try {
            if (scheduler == null) {
              scheduler = getScheduler(scheduleStore, cConf);
            }
            return scheduler;
          } catch (Exception e) {
            throw Throwables.propagate(e);
          }
        }
      };
    }

    /**
     * Create a quartz scheduler. Quartz factory method is not used, because inflexible in allowing custom jobstore
     * and turning off check for new versions.
     * @param store JobStore.
     * @param cConf CConfiguration.
     * @return an instance of {@link org.quartz.Scheduler}
     * @throws SchedulerException
     */
    private org.quartz.Scheduler getScheduler(JobStore store,
                                              CConfiguration cConf) throws SchedulerException {

      int threadPoolSize = cConf.getInt(Constants.Scheduler.CFG_SCHEDULER_MAX_THREAD_POOL_SIZE,
                                        Constants.Scheduler.DEFAULT_THREAD_POOL_SIZE);
      ExecutorThreadPool threadPool = new ExecutorThreadPool(threadPoolSize);
      threadPool.initialize();
      String schedulerName = DirectSchedulerFactory.DEFAULT_SCHEDULER_NAME;
      String schedulerInstanceId = DirectSchedulerFactory.DEFAULT_INSTANCE_ID;

      QuartzSchedulerResources qrs = new QuartzSchedulerResources();
      JobRunShellFactory jrsf = new StdJobRunShellFactory();

      qrs.setName(schedulerName);
      qrs.setInstanceId(schedulerInstanceId);
      qrs.setJobRunShellFactory(jrsf);
      qrs.setThreadPool(threadPool);
      qrs.setThreadExecutor(new DefaultThreadExecutor());
      qrs.setJobStore(store);
      qrs.setRunUpdateCheck(false);
      QuartzScheduler qs = new QuartzScheduler(qrs, -1, -1);

      ClassLoadHelper cch = new CascadingClassLoadHelper();
      cch.initialize();

      store.initialize(cch, qs.getSchedulerSignaler());
      org.quartz.Scheduler scheduler = new StdScheduler(qs);

      jrsf.initialize(scheduler);
      qs.initialize();

      return scheduler;
    }
  }
}
