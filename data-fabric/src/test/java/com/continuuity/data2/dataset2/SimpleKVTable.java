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

package com.continuuity.data2.dataset2;

import com.continuuity.api.dataset.lib.AbstractDataset;
import com.continuuity.api.dataset.module.EmbeddedDataset;
import com.continuuity.api.dataset.table.Table;
import com.continuuity.reactor.metadata.DatasetSpecification;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 */
public class SimpleKVTable extends AbstractDataset implements KeyValueTable {
  private static final byte[] COL = new byte[0];

  private final Table table;

  public SimpleKVTable(DatasetSpecification spec,
                       @EmbeddedDataset("data") Table table) {
    super(spec.getName(), table);
    this.table = table;
  }

  public void put(String key, String value) throws Exception {
    table.put(Bytes.toBytes(key), COL, Bytes.toBytes(value));
  }

  public String get(String key) throws Exception {
    byte[] value = table.get(Bytes.toBytes(key), COL);
    return value == null ? null : Bytes.toString(value);
  }
}
