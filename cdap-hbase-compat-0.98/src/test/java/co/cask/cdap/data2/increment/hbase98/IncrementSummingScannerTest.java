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

package co.cask.cdap.data2.increment.hbase98;

import co.cask.cdap.data2.dataset2.lib.table.hbase.HBaseOrderedTable;
import co.cask.cdap.data2.util.hbase.MockRegionServerServices;
import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.HRegionFileSystem;
import org.apache.hadoop.hbase.regionserver.RegionScanner;
import org.apache.hadoop.hbase.regionserver.wal.HLog;
import org.apache.hadoop.hbase.regionserver.wal.HLogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link IncrementSummingScanner} implementation.
 */
public class IncrementSummingScannerTest {
  private static final byte[] TRUE = Bytes.toBytes(true);
  private static HBaseTestingUtility testUtil;
  private static Configuration conf;

  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    testUtil = new HBaseTestingUtility();
    testUtil.startMiniCluster();
    conf = testUtil.getConfiguration();
  }

  @AfterClass
  public static void shutdownAfterClass() throws Exception {
    testUtil.shutdownMiniCluster();
  }

  @Test
  public void testIncrementScanning() throws Exception {
    TableName tableName = TableName.valueOf("TestIncrementSummingScanner");
    byte[] familyBytes = Bytes.toBytes("f");
    byte[] columnBytes = Bytes.toBytes("c");
    HRegion region = createRegion(tableName, familyBytes);
    try {
      region.initialize();
      
      // test handling of a single increment value alone
      Put p = new Put(Bytes.toBytes("r1"));
      p.add(familyBytes, columnBytes, Bytes.toBytes(3L));
      p.setAttribute(HBaseOrderedTable.DELTA_WRITE, TRUE);
      region.put(p);

      Scan scan = new Scan();
      RegionScanner scanner = new IncrementSummingScanner(region, -1, region.getScanner(scan));
      List<Cell> results = Lists.newArrayList();
      scanner.next(results);

      assertEquals(1, results.size());
      Cell cell = results.get(0);
      assertNotNull(cell);
      assertEquals(3L, Bytes.toLong(cell.getValue()));

      // test handling of a single total sum
      p = new Put(Bytes.toBytes("r2"));
      p.add(familyBytes, columnBytes, Bytes.toBytes(5L));
      region.put(p);

      scan = new Scan(Bytes.toBytes("r2"));

      scanner = new IncrementSummingScanner(region, -1, region.getScanner(scan));
      results = Lists.newArrayList();
      scanner.next(results);

      assertEquals(1, results.size());
      cell = results.get(0);
      assertNotNull(cell);
      assertEquals(5L, Bytes.toLong(cell.getValue()));

      // test handling of multiple increment values
      long now = System.currentTimeMillis();
      p = new Put(Bytes.toBytes("r3"));
      for (int i = 0; i < 5; i++) {
        p.add(familyBytes, columnBytes, now - i, Bytes.toBytes((long) (i + 1)));
      }
      p.setAttribute(HBaseOrderedTable.DELTA_WRITE, TRUE);
      region.put(p);

      scan = new Scan(Bytes.toBytes("r3"));
      scan.setMaxVersions();
      scanner = new IncrementSummingScanner(region, -1, region.getScanner(scan));
      results = Lists.newArrayList();
      scanner.next(results);

      assertEquals(1, results.size());
      cell = results.get(0);
      assertNotNull(cell);
      assertEquals(15L, Bytes.toLong(cell.getValue()));

      // test handling of multiple increment values followed by a total sum, then other increments
      now = System.currentTimeMillis();
      p = new Put(Bytes.toBytes("r4"));
      for (int i = 0; i < 3; i++) {
        p.add(familyBytes, columnBytes, now - i, Bytes.toBytes(1L));
      }
      p.setAttribute(HBaseOrderedTable.DELTA_WRITE, TRUE);
      region.put(p);

      // this put will appear as a "total" sum prior to all the delta puts
      p = new Put(Bytes.toBytes("r4"));
      p.add(familyBytes, columnBytes, now - 5, Bytes.toBytes(5L));
      region.put(p);

      scan = new Scan(Bytes.toBytes("r4"));
      scan.setMaxVersions();
      scanner = new IncrementSummingScanner(region, -1, region.getScanner(scan));
      results = Lists.newArrayList();
      scanner.next(results);

      assertEquals(1, results.size());
      cell = results.get(0);
      assertNotNull(cell);
      assertEquals(8L, Bytes.toLong(cell.getValue()));

      // test handling of an increment column followed by a non-increment column
      p = new Put(Bytes.toBytes("r4"));
      p.add(familyBytes, Bytes.toBytes("c2"), Bytes.toBytes("value"));
      region.put(p);

      scan = new Scan(Bytes.toBytes("r4"));
      scan.setMaxVersions();
      scanner = new IncrementSummingScanner(region, -1, region.getScanner(scan));
      results = Lists.newArrayList();
      scanner.next(results);

      assertEquals(2, results.size());
      cell = results.get(0);
      assertNotNull(cell);
      assertEquals(8L, Bytes.toLong(cell.getValue()));

      cell = results.get(1);
      assertNotNull(cell);
      assertEquals("value", Bytes.toString(cell.getValue()));
    } finally {
      region.close();
    }

  }

  @Test
  public void testFlushAndCompact() throws Exception {
    TableName tableName = TableName.valueOf("TestFlushAndCompact");
    byte[] familyBytes = Bytes.toBytes("f");
    byte[] columnBytes = Bytes.toBytes("c");
    HRegion region = createRegion(tableName, familyBytes);
    try {
      region.initialize();

      // load an initial set of increments
      long ts = System.currentTimeMillis();
      byte[] row1 = Bytes.toBytes("row1");
      for (int i = 0; i < 50; i++) {
        Put p = new Put(row1);
        p.add(familyBytes, columnBytes, ts, Bytes.toBytes(1L));
        p.setAttribute(HBaseOrderedTable.DELTA_WRITE, TRUE);
        ts++;
        region.put(p);
      }

      byte[] row2 = Bytes.toBytes("row2");
      ts = System.currentTimeMillis();
      // start with a full put
      Put row2P = new Put(row2);
      row2P.add(familyBytes, columnBytes, ts++, Bytes.toBytes(10L));
      region.put(row2P);
      for (int i = 0; i < 10; i++) {
        Put p = new Put(row2);
        p.add(familyBytes, columnBytes, ts++, Bytes.toBytes(1L));
        p.setAttribute(HBaseOrderedTable.DELTA_WRITE, TRUE);
        region.put(p);
      }

      // force a region flush
      region.flushcache();
      region.waitForFlushesAndCompactions();

      Result r1 = region.get(new Get(row1));
      assertNotNull(r1);
      assertFalse(r1.isEmpty());
      // row1 should have a full put aggregating all 50 incrments
      Cell r1Cell = r1.getColumnLatestCell(familyBytes, columnBytes);
      assertNotNull(r1Cell);
      assertEquals(50L, Bytes.toLong(r1Cell.getValue()));

      Result r2 = region.get(new Get(row2));
      assertNotNull(r2);
      assertFalse(r2.isEmpty());
      // row2 should have a full put aggregating prior put + 10 increments
      Cell r2Cell = r2.getColumnLatestCell(familyBytes, columnBytes);
      assertNotNull(r2Cell);
      assertEquals(20L, Bytes.toLong(r2Cell.getValue()));
    } finally {
      region.close();
    }
  }

  private HRegion createRegion(TableName tableName, byte[] family) throws Exception {
    HTableDescriptor htd = new HTableDescriptor(tableName);
    HColumnDescriptor cfd = new HColumnDescriptor(family);
    cfd.setMaxVersions(Integer.MAX_VALUE);
    htd.addFamily(cfd);
    htd.addCoprocessor(IncrementHandler.class.getName());
    Path tablePath = new Path("/tmp/" + tableName.getNameAsString());
    Path hlogPath = new Path("/tmp/hlog-" + tableName.getNameAsString());
    Configuration hConf = conf;
    FileSystem fs = FileSystem.get(hConf);
    assertTrue(fs.mkdirs(tablePath));
    HLog hLog = HLogFactory.createHLog(fs, hlogPath, tableName.getNameAsString(), hConf);
    HRegionInfo regionInfo = new HRegionInfo(tableName);
    HRegionFileSystem regionFS = HRegionFileSystem.createRegionOnFileSystem(hConf, fs, tablePath, regionInfo);
    return new HRegion(regionFS, hLog, hConf, htd,
                                 new MockRegionServerServices(hConf, null));
  }
}
