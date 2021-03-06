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

package co.cask.cdap.internal.pipeline;

import co.cask.cdap.pipeline.Pipeline;
import co.cask.cdap.pipeline.PipelineFactory;

/**
 * A factory for providing synchronous pipeline.
 */
public class SynchronousPipelineFactory implements PipelineFactory {

  /**
   * @return A synchronous pipeline.
   */
  @Override
  public <T> Pipeline<T> getPipeline() {
    return new SynchronousPipeline<T>();
  }
}
