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
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static com.thoughtworks.gocd.analytics.MaterialRevisionMother.materialRevisionFrom;
import static com.thoughtworks.gocd.analytics.PipelineInstanceMother.pipelineInstanceFrom;
import static com.thoughtworks.gocd.analytics.PipelineWorkflowMother.withWorkflowId;
import static com.thoughtworks.gocd.analytics.StageMother.stageFrom;
import static com.thoughtworks.gocd.analytics.StageMother.stageWithId;
import static org.mockito.Mockito.*;

public class WorkflowAllocatorForSubsequentStagesOfPipelineTest {
    private SqlSession sqlSession;
    private PipelineDAO pipelineDAO;
    private StageDAO stageDAO;
    private MaterialRevisionDAO materialRevisionDAO;
    private WorkflowDAO workflowDAO;
    private PipelineWorkflowDAO pipelineWorkflowDAO;
    private WorkflowAllocator workflowAllocator;

    @Before
    public void setUp() {
        sqlSession = mock(SqlSession.class);
        pipelineDAO = mock(PipelineDAO.class);
        stageDAO = mock(StageDAO.class);
        materialRevisionDAO = mock(MaterialRevisionDAO.class);
        workflowDAO = mock(WorkflowDAO.class);
        pipelineWorkflowDAO = mock(PipelineWorkflowDAO.class);
        workflowAllocator = new WorkflowAllocatorForSubsequentStagesOfPipeline(pipelineDAO, stageDAO, materialRevisionDAO,
                workflowDAO, pipelineWorkflowDAO);
    }

    @Test
    public void currentPipelineStageWorkflowShouldBeSameAsPreviousStagesWorkflow() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", "stage-1", 1, buildScheduleTime);

        when(stageDAO.One(sqlSession, pipeline, stage.getPreviousStageName(), stage.getPreviousStageCounter())).thenReturn(stageWithId(30));
        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 10, 30)).thenReturn(Arrays.asList(withWorkflowId(111), withWorkflowId(222)));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.emptyList());

        verify(pipelineWorkflowDAO).insert(sqlSession, 10, 20, 111);
        verify(pipelineWorkflowDAO).insert(sqlSession, 10, 20, 222);
    }

    @Test
    public void inAbsenceOfARecordForPreviousStage_SkipWorkflowAssignmentForTheCurrentStage() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision materialRevision = materialRevisionFrom(111, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", "stage-1", 1, buildScheduleTime);

        when(stageDAO.One(sqlSession, pipeline, stage.getPreviousStageName(), stage.getPreviousStageCounter())).thenReturn(null);
        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 10, 30)).thenReturn(Collections.emptyList());

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.singletonList(materialRevision));

        verify(pipelineWorkflowDAO, never()).insert(eq(sqlSession), eq(10), eq(20), any(Integer.class));
    }

    @Test
    public void inAbsenceOfWorkflowForThePreviousStage_SkipWorkflowAssignmentForTheCurrentStage() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision materialRevision = materialRevisionFrom(111, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-2", 1, "success",
                "changes", "stage-1", 1, buildScheduleTime);

        when(stageDAO.One(sqlSession, pipeline, stage.getPreviousStageName(), stage.getPreviousStageCounter())).thenReturn(stageWithId(30));
        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 10, 30)).thenReturn(Collections.emptyList());

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.singletonList(materialRevision));

        verify(pipelineWorkflowDAO, never()).insert(eq(sqlSession), eq(10), eq(20), any(Integer.class));
    }
}
