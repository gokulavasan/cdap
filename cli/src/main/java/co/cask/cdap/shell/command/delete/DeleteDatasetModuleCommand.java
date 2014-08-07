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

package co.cask.cdap.shell.command.delete;

import co.cask.cdap.client.DatasetModuleClient;
import co.cask.cdap.shell.ElementType;
import co.cask.cdap.shell.command.AbstractCommand;
import co.cask.cdap.shell.completer.Completable;
import co.cask.cdap.shell.completer.element.DatasetModuleNameCompleter;
import com.google.common.collect.Lists;
import jline.console.completer.Completer;

import java.io.PrintStream;
import java.util.List;
import javax.inject.Inject;

/**
 * Deletes a dataset module.
 */
public class DeleteDatasetModuleCommand extends AbstractCommand implements Completable {

  private final DatasetModuleClient datasetClient;
  private final DatasetModuleNameCompleter completer;

  @Inject
  public DeleteDatasetModuleCommand(DatasetModuleNameCompleter completer,
                                    DatasetModuleClient datasetClient) {
    super("module", "<module-name>", "Deletes a " + ElementType.DATASET_MODULE.getPrettyName());
    this.completer = completer;
    this.datasetClient = datasetClient;
  }

  @Override
  public void process(String[] args, PrintStream output) throws Exception {
    super.process(args, output);

    String datasetModuleName = args[0];

    datasetClient.delete(datasetModuleName);
    output.printf("Successfully deleted dataset module '%s'\n", datasetModuleName);
  }

  @Override
  public List<? extends Completer> getCompleters(String prefix) {
    return Lists.newArrayList(prefixCompleter(prefix, completer));
  }
}