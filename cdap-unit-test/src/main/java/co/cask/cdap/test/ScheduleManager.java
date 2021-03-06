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

package co.cask.cdap.test;

/**
 * This interface is for managing the schedule of a workflow
 */
public interface ScheduleManager {
  /**
   * suspends the workflow schedule
   */
  public void suspend();

  /**
   * Resumes the workflow schedule
   */
  public void resume();

  /**
   * returns the status of the workflow schedule
   */
  public String status();
}
