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

package co.cask.cdap.app.deploy;

import java.io.IOException;
import java.io.Reader;
import javax.annotation.Nullable;

/**
 * Interface defining the response as returned by the execution of configure method.
 */
public interface ConfigResponse {
  /**
   * @return {@link java.io.Reader} of configuration data as generated by invoking configure.
   */
  @Nullable
  public Reader get() throws IOException;

  /**
   * @return true if success; false otherwise.
   */
  public int getExitCode();
}
