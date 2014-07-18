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

package com.continuuity.api.dataset.lib;

import com.continuuity.api.annotation.Beta;
import com.continuuity.api.dataset.DatasetAdmin;
import com.continuuity.api.dataset.DatasetDefinition;
import com.continuuity.api.dataset.DatasetProperties;
import com.continuuity.api.dataset.table.Table;
import com.continuuity.reactor.metadata.DatasetSpecification;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * {@link DatasetDefinition} for {@link KeyValueTable}.
 */
@Beta
public class KeyValueTableDefinition
  extends AbstractDatasetDefinition<KeyValueTable, DatasetAdmin> {

  private final DatasetDefinition<? extends Table, ?> tableDef;

  public KeyValueTableDefinition(String name, DatasetDefinition<? extends Table, ?> tableDef) {
    super(name);
    Preconditions.checkArgument(tableDef != null, "Table definition is required");
    this.tableDef = tableDef;
  }

  @Override
  public DatasetSpecification configure(String instanceName, DatasetProperties properties) {
    return DatasetSpecification.builder(instanceName, getName())
      .properties(properties.getProperties())
      .datasets(tableDef.configure("kv", properties))
      .build();
  }

  @Override
  public DatasetAdmin getAdmin(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    return tableDef.getAdmin(spec.getSpecification("kv"), classLoader);
  }

  @Override
  public KeyValueTable getDataset(DatasetSpecification spec, ClassLoader classLoader) throws IOException {
    Table table = tableDef.getDataset(spec.getSpecification("kv"), classLoader);
    return new KeyValueTable(spec.getName(), table);
  }
}
