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

package com.thoughtworks.gocd.analytics.executors.workflow;

import com.thoughtworks.gocd.analytics.dao.*;
import com.thoughtworks.gocd.analytics.models.MaterialRevision;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.models.Workflow;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class WorkflowAllocatorForManualTriggerOfPipeline extends WorkflowAllocator {
    private final WorkflowAllocatorForFirstStageOfPipeline workflowAllocatorForFirstStageOfPipeline;

    public WorkflowAllocatorForManualTriggerOfPipeline() {
        this(new PipelineDAO(), new StageDAO(), new MaterialRevisionDAO(), new WorkflowDAO(), new PipelineWorkflowDAO(),
                new WorkflowAllocatorForFirstStageOfPipeline());
    }

    protected WorkflowAllocatorForManualTriggerOfPipeline(PipelineDAO pipelineDAO, StageDAO stageDAO, MaterialRevisionDAO materialRevisionDAO,
                                                          WorkflowDAO workflowDAO, PipelineWorkflowDAO pipelineWorkflowDAO,
                                                          WorkflowAllocatorForFirstStageOfPipeline workflowAllocatorForFirstStageOfPipeline) {
        super(pipelineDAO, stageDAO, materialRevisionDAO, workflowDAO, pipelineWorkflowDAO);
        this.workflowAllocatorForFirstStageOfPipeline = workflowAllocatorForFirstStageOfPipeline;
    }

    @Override
    public void allocate(SqlSession sqlSession, PipelineInstance pipelineInstance, Stage stage, List<MaterialRevision> materialRevisions) {
        if (materialRevisions.isEmpty()) {
            Workflow workflow = new Workflow(pipelineInstance.getCreatedAt());
            workflowDAO.insert(sqlSession, workflow);

            LOG.debug("[Workflow-Allocator] Associating stage to new workflow: '{}' in absence of changed materials.", workflow.getId());
            pipelineWorkflowDAO.insert(sqlSession, pipelineInstance.getId(), stage.getId(), workflow.getId());
        } else {
            LOG.debug("[Workflow-Allocator] In presence of changed materials using WorkflowAllocatorForFirstStageOfPipeline to associate workflows to stage: '{}'."
                    , stage.toShortString());
            workflowAllocatorForFirstStageOfPipeline.allocate(sqlSession, pipelineInstance, stage, materialRevisions);
        }
    }
}
