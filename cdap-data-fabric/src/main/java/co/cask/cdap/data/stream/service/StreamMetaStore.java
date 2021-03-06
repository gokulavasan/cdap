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
package co.cask.cdap.data.stream.service;

/**
 * A temporary place for hosting MDS access logic for streams.
 */
// TODO: The whole access pattern to MDS needs to be rethink, as we are now moving towards SOA and multiple components
// needs to access MDS.
public interface StreamMetaStore {

  /**
   * Adds a stream to the meta store.
   */
  void addStream(String accountId, String streamName) throws Exception;

  /**
   * Removes a stream from the meta store.
   */
  void removeStream(String accountId, String streamName) throws Exception;

  /**
   * Checks if a stream exists in the meta store.
   */
  boolean streamExists(String accountId, String streamName) throws Exception;
}
