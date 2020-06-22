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

package com.thoughtworks.gocd.analytics.models;

import com.google.common.base.Objects;
import com.thoughtworks.gocd.analytics.db.PersistentObject;

public class PipelineWorkflow extends PersistentObject {
    long pipelineId;
    long stageId;
    long materialRevisionId;
    long workflowId;

    public long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public long getStageId() {
        return stageId;
    }

    public void setStageId(long stageId) {
        this.stageId = stageId;
    }

    public long getMaterialRevisionId() {
        return materialRevisionId;
    }

    public void setMaterialRevisionId(long materialRevisionId) {
        this.materialRevisionId = materialRevisionId;
    }

    public long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(long workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineWorkflow that = (PipelineWorkflow) o;
        return pipelineId == that.pipelineId &&
                stageId == that.stageId &&
                materialRevisionId == that.materialRevisionId &&
                workflowId == that.workflowId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pipelineId, stageId, materialRevisionId, workflowId);
    }
}
