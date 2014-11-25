/*
 * Copyright © 2012-2014 Cask, Inc.
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

package co.cask.cdap.cli;

/**
 * Argument names.
 */
public enum ArgumentName {
  PROGRAM("program-id"),
  STREAM("stream-id"),
  PROCEDURE("procedure-id"),
  METHOD("method-id"),
  FLOW("flow-id"),
  FLOWLET("flowlet-id"),
  WORKFLOW("workflow-id"),
  SERVICE("service-id"),
  RUNNABLE("runnable-id"),
  MAPREDUCE("mapreduce-id"),
  SPARK("spark-id"),

  HOSTNAME("hostname"),
  DATASET_TYPE("dataset-type"),
  DATASET_MODULE("dataset-module"),
  NEW_DATASET_MODULE("new-dataset-module"),
  DATASET("dataset-name"),
  NEW_DATASET("new-dataset-name"),
  STREAM_EVENT("stream-event"),
  NEW_STREAM("new-stream-id"),
  PARAMETER_MAP("parameter-map"),
  TTL_IN_SECONDS("ttl-in-seconds"),
  NUM_INSTANCES("num-instances"),
  START_TIME("start-time"),
  END_TIME("end-time"),
  LIMIT("limit"),
  RUN_STATUS("status"),
  APP_JAR_FILE("app-jar-file"),
  DATASET_MODULE_JAR_FILE("module-jar-file"),
  DATASET_MODULE_JAR_CLASSNAME("module-jar-classname"),
  QUERY("query"),
  APP("app-id"),
  HTTP_METHOD("http-method"),
  ENDPOINT("endpoint"),
  HEADERS("headers"),
  HTTP_BODY("body");

  private final String name;

  ArgumentName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
