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
import com.thoughtworks.gocd.analytics.models.PipelineWorkflow;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.stream.Collectors;

public class WorkflowAllocatorForSubsequentStagesOfPipeline extends WorkflowAllocator {

    public WorkflowAllocatorForSubsequentStagesOfPipeline() {
        super(new PipelineDAO(), new StageDAO(), new MaterialRevisionDAO(), new WorkflowDAO(), new PipelineWorkflowDAO());
    }

    protected WorkflowAllocatorForSubsequentStagesOfPipeline(PipelineDAO pipelineDAO, StageDAO stageDAO, MaterialRevisionDAO materialRevisionDAO,
                                                             WorkflowDAO workflowDAO, PipelineWorkflowDAO pipelineWorkflowDAO) {
        super(pipelineDAO, stageDAO, materialRevisionDAO, workflowDAO, pipelineWorkflowDAO);
    }

    @Override
    public void allocate(SqlSession sqlSession, PipelineInstance pipelineInstance, Stage stage, List<MaterialRevision> materialRevisions) {
        Stage previousStage = previousStageFromDB(sqlSession, pipelineInstance, stage.getPreviousStageName(), stage.getPreviousStageCounter());

        if (previousStage == null) {
            LOG.debug("[Workflow-Allocator] Skipping workflow allocation for stage: `{}`, no records in db for previous stage: `{}/{}`.",
                    stage.toShortString(), stage.getPreviousStageName(), stage.getPreviousStageCounter());
            return;
        }

        List<PipelineWorkflow> pipelineWorkflows = pipelineWorkflowDAO.workflowsFor(sqlSession, pipelineInstance.getId(), previousStage.getId());

        LOG.debug("[Workflow-Allocator] Associating stage: '{}' to workflow ids: '{}' from previous stage: '{}'",
                stage.toShortString(), workflowIds(pipelineWorkflows), previousStage.toShortString());

        pipelineWorkflows.stream().forEach(pipelineWorkflow -> {
            pipelineWorkflowDAO.insert(sqlSession, pipelineInstance.getId(), stage.getId(), pipelineWorkflow.getWorkflowId());
        });
    }

    private Stage previousStageFromDB(SqlSession sqlSession, PipelineInstance pipelineInstance, String previousStageName, int previousStageCounter) {
        return stageDAO.One(sqlSession, pipelineInstance, previousStageName, previousStageCounter);
    }

    private String workflowIds(List<PipelineWorkflow> workflows) {
        return workflows.stream().map(w -> Long.toString(w.getWorkflowId())).collect(Collectors.joining(","));
    }
}
