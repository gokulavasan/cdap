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
package co.cask.cdap.examples.purchase;

import co.cask.cdap.api.ProgramLifecycle;
import co.cask.cdap.api.Resources;
import co.cask.cdap.api.annotation.UseDataSet;
import co.cask.cdap.api.dataset.lib.KeyValueTable;
import co.cask.cdap.api.mapreduce.AbstractMapReduce;
import co.cask.cdap.api.mapreduce.MapReduceContext;
import co.cask.cdap.api.metrics.Metrics;
import com.google.gson.Gson;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * MapReduce job that reads purchases from the purchases DataSet and creates a purchase history for every user.
 */
public class PurchaseHistoryBuilder extends AbstractMapReduce {


  @Override
  public void configure() {
    setDescription("Purchase History Builder MapReduce job");
    useDatasets("frequentCustomers");
    setInputDataset("purchases");
    setOutputDataset("history");
    setMapperResources(new Resources(1024));
    setReducerResources(new Resources(1024));
  }

  @Override
  public void beforeSubmit(MapReduceContext context) throws Exception {
    Job job = context.getHadoopJob();
    job.setMapperClass(PurchaseMapper.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setReducerClass(PerUserReducer.class);
  }

  /**
   * Mapper class.
   */
  public static class PurchaseMapper extends Mapper<byte[], Purchase, Text, Text> {

    private Metrics mapMetrics;

    @Override
    public void map(byte[] key, Purchase purchase, Context context)
      throws IOException, InterruptedException {
      String user = purchase.getCustomer();
      if (purchase.getPrice() > 100000) {
        mapMetrics.count("purchases.large", 1);
      }
      context.write(new Text(user), new Text(new Gson().toJson(purchase)));
    }
  }

  /**
   * Reducer class.
   */
  public static class PerUserReducer extends Reducer<Text, Text, String, PurchaseHistory>
    implements ProgramLifecycle<MapReduceContext> {

    @UseDataSet("frequentCustomers")
    private KeyValueTable frequentCustomers;

    private Metrics reduceMetrics;

    private URL userProfileServiceURL;

    @Override
    public void initialize(MapReduceContext context) throws Exception {
      userProfileServiceURL = context.getServiceURL(UserProfileService.SERVICE_NAME);
    }

    public void reduce(Text customer, Iterable<Text> values, Context context)
      throws IOException, InterruptedException {

      URL getUserProfileURL = new URL(userProfileServiceURL, "user");
      UserProfile userProfile;
      try {
        HttpURLConnection urlConnection = (HttpURLConnection) getUserProfileURL.openConnection();
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
          userProfile = null;
        } else {
          // parse UserProfile
        }
      } catch (Exception e) {
        userProfile = null;
      }

      PurchaseHistory purchases = new PurchaseHistory(customer.toString(), userProfile);
      int numPurchases = 0;
      for (Text val : values) {
        purchases.add(new Gson().fromJson(val.toString(), Purchase.class));
        numPurchases++;
      }
      if (numPurchases == 1) {
        reduceMetrics.count("customers.rare", 1);
      } else if (numPurchases > 10) {
        reduceMetrics.count("customers.frequent", 1);
        frequentCustomers.write(customer.toString(), String.valueOf(numPurchases));
      }

      context.write(customer.toString(), purchases);
    }

    @Override
    public void destroy() {
      // no-op
    }
  }
}
