/*
 * Copyright 2020 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.gocd.analytics;

import com.thoughtworks.gocd.analytics.models.PipelineInstance;

import java.time.ZonedDateTime;

public class PipelineInstanceMother {
    public static PipelineInstance pipelineInstanceFrom(long id, String pipelineName, int pipelineCounter, ZonedDateTime pipelineScheduleTime) {
        PipelineInstance pipelineInstance = new PipelineInstance();

        pipelineInstance.setId(id);
        pipelineInstance.setName(pipelineName);
        pipelineInstance.setCounter(Integer.valueOf(pipelineCounter));
        pipelineInstance.setCreatedAt(pipelineScheduleTime);

        return pipelineInstance;
    }

    public static PipelineInstance pipelineInstanceFrom(long id, String pipelineName, int pipelineCounter, long workflowId) {
        PipelineInstance pipelineInstance = new PipelineInstance();

        pipelineInstance.setId(id);
        pipelineInstance.setName(pipelineName);
        pipelineInstance.setCounter(Integer.valueOf(pipelineCounter));
        pipelineInstance.setWorkflowId(workflowId);

        return pipelineInstance;
    }

    public static PipelineInstance pipelineInstanceWithId(long id) {
        PipelineInstance pipelineInstance = new PipelineInstance();
        pipelineInstance.setId(id);

        return pipelineInstance;
    }
}
