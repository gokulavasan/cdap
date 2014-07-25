/*
 * Copyright 2014 Continuuity, Inc.
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

package com.continuuity.common.program;

import com.continuuity.api.ProgramSpecification;
import com.continuuity.api.flow.FlowSpecification;
import com.continuuity.api.mapreduce.MapReduceSpecification;
import com.continuuity.api.procedure.ProcedureSpecification;
import com.continuuity.api.service.ServiceSpecification;
import com.continuuity.api.webapp.WebappSpecification;
import com.continuuity.api.workflow.WorkflowSpecification;
import com.continuuity.proto.ProgramType;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Provides mapping from {@link com.continuuity.api.ProgramSpecification} to {@link ProgramType}.
 */
public class ProgramTypes {

  private static final ImmutableMap<Class<? extends ProgramSpecification>, ProgramType> specClassToProgramType =
    ImmutableMap.<Class<? extends ProgramSpecification>, ProgramType>builder()
    .put(FlowSpecification.class, ProgramType.FLOW)
    .put(ProcedureSpecification.class, ProgramType.PROCEDURE)
    .put(MapReduceSpecification.class, ProgramType.MAPREDUCE)
    .put(WorkflowSpecification.class, ProgramType.WORKFLOW)
    .put(WebappSpecification.class, ProgramType.WEBAPP)
    .put(ServiceSpecification.class, ProgramType.SERVICE)
    .build();

  /**
   * Maps from {@link ProgramSpecification} to {@link ProgramType}.
   *
   * @param spec {@link ProgramSpecification} to convert
   * @return {@link ProgramType} of the {@link ProgramSpecification}
   */
  public static ProgramType fromSpecification(ProgramSpecification spec) {
    Class<? extends ProgramSpecification> specClass = spec.getClass();
    for (Map.Entry<Class<? extends ProgramSpecification>, ProgramType> entry : specClassToProgramType.entrySet()) {
      if (entry.getKey().isAssignableFrom(specClass)) {
        return entry.getValue();
      }
    }
    throw new IllegalArgumentException("Unknown specification type: " + specClass);
  }

}