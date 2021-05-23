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
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static com.thoughtworks.gocd.analytics.PipelineInstanceMother.pipelineInstanceFrom;
import static com.thoughtworks.gocd.analytics.PipelineWorkflowMother.withWorkflowId;
import static com.thoughtworks.gocd.analytics.StageMother.stageFrom;
import static com.thoughtworks.gocd.analytics.StageMother.stageWithId;
import static org.mockito.Mockito.*;

public class WorkflowAllocatorForStageReRunsTest {
    private SqlSession sqlSession;
    private PipelineDAO pipelineDAO;
    private StageDAO stageDAO;
    private MaterialRevisionDAO materialRevisionDAO;
    private WorkflowDAO workflowDAO;
    private PipelineWorkflowDAO pipelineWorkflowDAO;
    private WorkflowAllocator workflowAllocator;

    @BeforeEach
    public void setUp() {
        sqlSession = mock(SqlSession.class);
        pipelineDAO = mock(PipelineDAO.class);
        stageDAO = mock(StageDAO.class);
        materialRevisionDAO = mock(MaterialRevisionDAO.class);
        workflowDAO = mock(WorkflowDAO.class);
        pipelineWorkflowDAO = mock(PipelineWorkflowDAO.class);
        workflowAllocator = new WorkflowAllocatorForStageReRuns(pipelineDAO, stageDAO, materialRevisionDAO,
                workflowDAO, pipelineWorkflowDAO);
    }

    @Test
    public void currentPipelineStageWorkflowShouldBeSameAsPreviousStagesWorkflow() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(30, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", null, 2, buildScheduleTime);

        when(stageDAO.One(sqlSession, pipeline, stage.getStageName(), stage.getStageCounter() - 1))
                .thenReturn(stageWithId(20));
        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 10, 20)).thenReturn(Arrays.asList(withWorkflowId(111), withWorkflowId(222)));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.emptyList());

        verify(pipelineWorkflowDAO).insert(sqlSession, 10, 30, 111);
        verify(pipelineWorkflowDAO).insert(sqlSession, 10, 30, 222);
    }

    @Test
    public void inAbsenceOfARecordForThePreviousRunOfTheStage_SkipWorkflowAssignmentForTheCurrentReRunOfStage() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(30, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", null, 2, buildScheduleTime);

        when(stageDAO.One(sqlSession, pipeline, stage.getStageName(), stage.getStageCounter() - 1))
                .thenReturn(null);
        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 10, 20)).thenReturn(Collections.emptyList());

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.emptyList());

        verify(pipelineWorkflowDAO, never()).insert(eq(sqlSession), eq(10), eq(30), any(Integer.class));
    }

    @Test
    public void inAbsenceOfWorkflowForThePreviousRunOfTheStage_SkipWorkflowAssignmentForTheCurrentReRunOfStage() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(30, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", null, 2, buildScheduleTime);

        when(stageDAO.One(sqlSession, pipeline, stage.getStageName(), stage.getStageCounter() - 1))
                .thenReturn(stageWithId(20));
        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 10, 20)).thenReturn(Collections.emptyList());

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.emptyList());

        verify(pipelineWorkflowDAO, never()).insert(eq(sqlSession), eq(10), eq(30), any(Integer.class));
    }
}
