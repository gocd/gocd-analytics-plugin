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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Collections;

import static com.thoughtworks.gocd.analytics.MaterialRevisionMother.materialRevisionFrom;
import static com.thoughtworks.gocd.analytics.PipelineInstanceMother.pipelineInstanceFrom;
import static com.thoughtworks.gocd.analytics.StageMother.stageFrom;
import static org.mockito.Mockito.*;

public class WorkflowAllocatorForManualTriggerOfPipelineTest {
    private SqlSession sqlSession;
    private PipelineDAO pipelineDAO;
    private StageDAO stageDAO;
    private MaterialRevisionDAO materialRevisionDAO;
    private WorkflowDAO workflowDAO;
    private PipelineWorkflowDAO pipelineWorkflowDAO;
    private WorkflowAllocator workflowAllocator;
    private WorkflowAllocatorForFirstStageOfPipeline workflowAllocatorForFirstStageOfPipeline;

    @BeforeEach
    public void setUp() {
        sqlSession = mock(SqlSession.class);
        pipelineDAO = mock(PipelineDAO.class);
        stageDAO = mock(StageDAO.class);
        materialRevisionDAO = mock(MaterialRevisionDAO.class);
        workflowDAO = mock(WorkflowDAO.class);
        pipelineWorkflowDAO = mock(PipelineWorkflowDAO.class);
        workflowAllocatorForFirstStageOfPipeline = mock(WorkflowAllocatorForFirstStageOfPipeline.class);
        workflowAllocator = new WorkflowAllocatorForManualTriggerOfPipeline(pipelineDAO, stageDAO, materialRevisionDAO,
                workflowDAO, pipelineWorkflowDAO, workflowAllocatorForFirstStageOfPipeline);
    }

    @Test
    public void inAbsenceOfAnyChangedMaterials_shouldAssignPipelineStageToNewWorkflow() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Workflow) args[1]).setId(30);
            return (null);
        }).when(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.emptyList());

        verify(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));
        verify(pipelineWorkflowDAO).insert(sqlSession, 10, 20, 30);
    }

    @Test
    public void inPresenceOfChangedMaterials_TreatTheStageAsFirstStageOfPipeline() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision materialRevision = materialRevisionFrom(30, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", null, 0, buildScheduleTime);

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.singletonList(materialRevision));

        verify(workflowAllocatorForFirstStageOfPipeline).allocate(sqlSession, pipeline, stage, Collections.singletonList(materialRevision));
    }
}
