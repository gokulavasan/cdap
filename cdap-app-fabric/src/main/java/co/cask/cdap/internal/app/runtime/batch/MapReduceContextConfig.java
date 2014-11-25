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

package co.cask.cdap.internal.app.runtime.batch;

import co.cask.cdap.api.data.batch.SimpleSplit;
import co.cask.cdap.api.data.batch.Split;
import co.cask.cdap.app.runtime.Arguments;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.internal.app.runtime.BasicArguments;
import co.cask.cdap.internal.app.runtime.batch.dataset.DataSetInputFormat;
import co.cask.cdap.internal.app.runtime.batch.dataset.DataSetOutputFormat;
import co.cask.tephra.Transaction;
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Helper class for getting and setting specific config settings for a job context.
 */
public final class MapReduceContextConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MapReduceContextConfig.class);
  private static final Gson GSON = new Gson();

  private static final String HCONF_ATTR_RUN_ID = "hconf.program.run.id";
  private static final String HCONF_ATTR_LOGICAL_START_TIME = "hconf.program.logical.start.time";
  private static final String HCONF_ATTR_WORKFLOW_BATCH = "hconf.program.workflow.batch";
  private static final String HCONF_ATTR_ARGS = "hconf.program.args";
  private static final String HCONF_ATTR_PROGRAM_JAR_NAME = "hconf.program.jar.name";
  private static final String HCONF_ATTR_CCONF = "hconf.cconf";
  private static final String HCONF_ATTR_INPUT_SPLIT_CLASS = "hconf.program.input.split.class";
  private static final String HCONF_ATTR_INPUT_SPLITS = "hconf.program.input.splits";
  private static final String HCONF_ATTR_NEW_TX = "hconf.program.newtx.tx";

  private final JobContext jobContext;

  public MapReduceContextConfig(JobContext context) {
    this.jobContext = context;
  }

  public void set(BasicMapReduceContext context, CConfiguration conf,
                  Transaction tx, String programJarName) {
    setRunId(context.getRunId().getId());
    setLogicalStartTime(context.getLogicalStartTime());
    setWorkflowBatch(context.getWorkflowBatch());
    setArguments(context.getRuntimeArguments());
    setProgramJarName(programJarName);
    setConf(conf);
    setTx(tx);
    if (context.getInputDataSelection() != null) {
      setInputSelection(context.getInputDataSelection());
    }
  }

  private void setArguments(Map<String, String> arguments) {
    jobContext.getConfiguration().set(HCONF_ATTR_ARGS, new Gson().toJson(arguments));
  }

  public Arguments getArguments() {
    Map<String, String> arguments = new Gson().fromJson(jobContext.getConfiguration().get(HCONF_ATTR_ARGS),
                                                        new TypeToken<Map<String, String>>() { }.getType());
    return new BasicArguments(arguments);
  }

  public URI getProgramLocation() {
    String programJarName = getProgramJarName();
    for (Path file : jobContext.getFileClassPaths()) {
      if (programJarName.equals(file.getName())) {
        return file.toUri();
      }
    }
    throw new IllegalStateException("Program jar " + programJarName + " not found in classpath files.");
  }

  private void setRunId(String runId) {
    jobContext.getConfiguration().set(HCONF_ATTR_RUN_ID, runId);
  }

  public String getRunId() {
    return jobContext.getConfiguration().get(HCONF_ATTR_RUN_ID);
  }

  private void setLogicalStartTime(long startTime) {
    jobContext.getConfiguration().setLong(HCONF_ATTR_LOGICAL_START_TIME, startTime);
  }

  public long getLogicalStartTime() {
    return jobContext.getConfiguration().getLong(HCONF_ATTR_LOGICAL_START_TIME, System.currentTimeMillis());
  }

  private void setWorkflowBatch(String workflowBatch) {
    if (workflowBatch != null) {
      jobContext.getConfiguration().set(HCONF_ATTR_WORKFLOW_BATCH, workflowBatch);
    }
  }

  public String getWorkflowBatch() {
    return jobContext.getConfiguration().get(HCONF_ATTR_WORKFLOW_BATCH);
  }


  private void setProgramJarName(String programJarName) {
    jobContext.getConfiguration().set(HCONF_ATTR_PROGRAM_JAR_NAME, programJarName);
  }

  public String getProgramJarName() {
    return jobContext.getConfiguration().get(HCONF_ATTR_PROGRAM_JAR_NAME);
  }

  public String getInputDataSet() {
    return jobContext.getConfiguration().get(DataSetInputFormat.HCONF_ATTR_INPUT_DATASET);
  }

  private void setInputSelection(List<Split> splits) {
    // todo: this is ugly
    Class<? extends Split> splitClass;
    if (splits.size() > 0) {
      splitClass = splits.get(0).getClass();
    } else {
      // assign any
      splitClass = SimpleSplit.class;
    }
    jobContext.getConfiguration().set(HCONF_ATTR_INPUT_SPLIT_CLASS, splitClass.getName());

    // todo: re-use Gson instance?
    jobContext.getConfiguration().set(HCONF_ATTR_INPUT_SPLITS, new Gson().toJson(splits));
  }

  public List<Split> getInputSelection() {
    String splitClassName = jobContext.getConfiguration().get(HCONF_ATTR_INPUT_SPLIT_CLASS);
    String splitsJson = jobContext.getConfiguration().get(HCONF_ATTR_INPUT_SPLITS);
    if (splitClassName == null || splitsJson == null) {
      return Collections.emptyList();
    }

    try {
      // Yes, we know that it implements Split
      @SuppressWarnings("unchecked")
      Class<? extends Split> splitClass =
        (Class<? extends Split>) jobContext.getConfiguration().getClassLoader().loadClass(splitClassName);
      return new Gson().fromJson(splitsJson, new ListSplitType(splitClass));
    } catch (ClassNotFoundException e) {
      //todo
      throw Throwables.propagate(e);
    }
  }

  // This is needed to deserialize JSON into generified List
  private static final class ListSplitType implements ParameterizedType {
    private final Class<? extends Split> implementationClass;

    private ListSplitType(Class<? extends Split> implementationClass) {
      this.implementationClass = implementationClass;
    }

    @Override
    public Type[] getActualTypeArguments() {
      return new Type[]{implementationClass};
    }

    @Override
    public Type getRawType() {
      return List.class;
    }

    @Override
    public Type getOwnerType() {
      // it is fine, as it is not inner class
      return null;
    }
  }

  public String getOutputDataSet() {
    return jobContext.getConfiguration().get(DataSetOutputFormat.HCONF_ATTR_OUTPUT_DATASET);
  }

  private void setConf(CConfiguration conf) {
    StringWriter stringWriter = new StringWriter();
    try {
      conf.writeXml(stringWriter);
    } catch (IOException e) {
      LOG.error("Unable to serialize CConfiguration into xml");
      throw Throwables.propagate(e);
    }
    jobContext.getConfiguration().set(HCONF_ATTR_CCONF, stringWriter.toString());
  }

  public CConfiguration getConf() {
    CConfiguration conf = CConfiguration.create();
    conf.addResource(new ByteArrayInputStream(jobContext.getConfiguration().get(HCONF_ATTR_CCONF).getBytes()));
    return conf;
  }

  private void setTx(Transaction tx) {
    jobContext.getConfiguration().set(HCONF_ATTR_NEW_TX, GSON.toJson(tx));
  }

  public Transaction getTx() {
    return GSON.fromJson(jobContext.getConfiguration().get(HCONF_ATTR_NEW_TX), Transaction.class);
  }
}
