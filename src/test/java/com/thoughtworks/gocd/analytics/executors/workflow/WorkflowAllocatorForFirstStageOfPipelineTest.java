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
import com.thoughtworks.gocd.analytics.models.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static com.thoughtworks.gocd.analytics.MaterialRevisionMother.materialRevisionFrom;
import static com.thoughtworks.gocd.analytics.PipelineInstanceMother.pipelineInstanceFrom;
import static com.thoughtworks.gocd.analytics.PipelineInstanceMother.pipelineInstanceWithId;
import static com.thoughtworks.gocd.analytics.PipelineWorkflowMother.withWorkflowId;
import static com.thoughtworks.gocd.analytics.StageMother.stageFrom;
import static com.thoughtworks.gocd.analytics.StageMother.stageWithId;
import static org.mockito.Mockito.*;

public class WorkflowAllocatorForFirstStageOfPipelineTest {
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
        workflowAllocator = new WorkflowAllocatorForFirstStageOfPipeline(pipelineDAO, stageDAO, materialRevisionDAO, workflowDAO, pipelineWorkflowDAO);
    }

    @Test
    public void shouldCreateANewWorkflow_AssignTheWorkflowToPipelineStageAndMaterialRevision() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision material = materialRevisionFrom(30, "fingerprint", "revision", "git", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Workflow)args[1]).setId(40);
            return (null);
        }).when(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Collections.singletonList(material));

        verify(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 40);
        verify(pipelineWorkflowDAO).insert(sqlSession, material.getId(), 40);
    }

    @Test
    public void multipleBuildCause_shouldCreateANewWorkflowForEachMaterialRevision_AndAssignToPipelineStageAndMaterialRevision() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision material1 = materialRevisionFrom(30, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        MaterialRevision material2 = materialRevisionFrom(40, "fingerprint-2", "revision-2", "git", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);

        doAnswer(new Answer() {
            private int id = 50;
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ((Workflow) args[1]).setId(id);
                id++;
                return (null);
            }
        }).when(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Arrays.asList(material1, material2));

        verify(pipelineWorkflowDAO).insert(sqlSession, material1.getId(), 50);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 50);

        verify(pipelineWorkflowDAO).insert(sqlSession, material2.getId(), 51);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 51);
    }

    @Test
    public void shouldNotCreateANewWorkflowForSCMMaterialIfMaterialIsAlreadyAssignedToAWorkflow() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision material1 = materialRevisionFrom(30, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        MaterialRevision material2 = materialRevisionFrom(40, "fingerprint-2", "revision-2", "git", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);


        when(pipelineWorkflowDAO.workflowFor(sqlSession, 30)).thenReturn(withWorkflowId(111));
        when(pipelineWorkflowDAO.workflowFor(sqlSession, 40)).thenReturn(null);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Workflow) args[1]).setId(222);
            return (null);
        }).when(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Arrays.asList(material1, material2));

        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 111);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 222);
        verify(pipelineWorkflowDAO).insert(sqlSession, material2.getId(), 222);
        verify(pipelineWorkflowDAO, never()).insert(sqlSession, material1.getId(), 111);
    }

    @Test
    public void upstreamPipeline_shouldAssignUpstreamPipelineWorkflowsToCurrentPipelineStageInstance() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision material1 = materialRevisionFrom(30, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        MaterialRevision material2 = materialRevisionFrom(-1, "fingerprint-2", "upstream/1/stage/1", "pipeline", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);


        when(pipelineDAO.find(sqlSession, "upstream", 1))
                .thenReturn(pipelineInstanceWithId(11));
        when(stageDAO.find(sqlSession, "upstream", 1, "stage", 1))
                .thenReturn(stageWithId(21));
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Workflow) args[1]).setId(50);
            return (null);
        }).when(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));

        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 11, 21))
                .thenReturn(Arrays.asList(withWorkflowId(60), withWorkflowId(70)));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Arrays.asList(material1, material2));

        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 50);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 60);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 70);
        verify(pipelineWorkflowDAO).insert(sqlSession, material1.getId(), 50);
    }

    @Test
    public void multipleBuildCause_shouldAssignPipelineStageToUniqueWorflowsAcrossAllBuildCause() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision gitMaterialAssignedToWorkflow = materialRevisionFrom(30, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        MaterialRevision gitMaterialNotAssignedToWorkflow = materialRevisionFrom(40, "fingerprint-1", "revision-1", "git", buildScheduleTime);
        MaterialRevision dependencyMaterial1 = materialRevisionFrom(-1, "fingerprint-2", "upstream-1/1/stage-1/1", "pipeline", buildScheduleTime);
        MaterialRevision dependencyMaterial2 = materialRevisionFrom(-1, "fingerprint-3", "upstream-2/1/stage-2/1", "pipeline", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);

        PipelineWorkflow workflow60 = withWorkflowId(60);
        PipelineWorkflow workflow70 = withWorkflowId(70);

        when(pipelineDAO.find(sqlSession, "upstream-1", 1))
                .thenReturn(pipelineInstanceWithId(11));
        when(pipelineDAO.find(sqlSession, "upstream-2", 1))
                .thenReturn(pipelineInstanceWithId(12));
        when(stageDAO.find(sqlSession, "upstream-1", 1, "stage-1", 1))
                .thenReturn(stageWithId(21));
        when(stageDAO.find(sqlSession, "upstream-2", 1, "stage-2", 1))
                .thenReturn(stageWithId(22));

        when(pipelineWorkflowDAO.workflowFor(sqlSession, gitMaterialAssignedToWorkflow.getId())).thenReturn(workflow60);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Workflow) args[1]).setId(80);
            return (null);
        }).when(workflowDAO).insert(sqlSession, new Workflow(pipeline.getCreatedAt()));

        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 11, 21))
                .thenReturn(Arrays.asList(workflow60, workflow70));
        when(pipelineWorkflowDAO.workflowsFor(sqlSession, 12, 22))
                .thenReturn(Arrays.asList(workflow60, withWorkflowId(71)));

        workflowAllocator.allocate(sqlSession, pipeline, stage, Arrays.asList(gitMaterialAssignedToWorkflow, gitMaterialNotAssignedToWorkflow, dependencyMaterial1, dependencyMaterial2));

        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 60);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 70);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 71);
        verify(pipelineWorkflowDAO).insert(sqlSession, pipeline.getId(), stage.getId(), 80);
        verify(pipelineWorkflowDAO).insert(sqlSession, gitMaterialNotAssignedToWorkflow.getId(), 80);
    }

    @Test
    public void shouldDoNothingInAbsenceOfARecordForDependencyMaterialPipeline() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);
        MaterialRevision material = materialRevisionFrom(-1, "fingerprint-1", "upstream/1/stage/1", "pipeline", buildScheduleTime);

        when(pipelineDAO.find(sqlSession, "upstream", 1)).thenReturn(null);

        workflowAllocator.allocate(sqlSession, pipeline, stage, Arrays.asList(material));

        verifyZeroInteractions(pipelineWorkflowDAO);
        verifyZeroInteractions(workflowDAO);
    }

    @Test
    public void shouldDoNothingInAbsenceOfARecordForDependencyMaterialStage() {
        ZonedDateTime buildScheduleTime = ZonedDateTime.now();
        MaterialRevision material = materialRevisionFrom(-1, "fingerprint-2", "upstream/1/stage/1", "pipeline", buildScheduleTime);
        PipelineInstance pipeline = pipelineInstanceFrom(10, "pipeline-name", 1, buildScheduleTime);
        Stage stage = stageFrom(20, "pipeline-name", 1, "stage-name", 1, "success",
                "changes", null, 0, buildScheduleTime);


        when(pipelineDAO.find(sqlSession, "upstream", 1))
                .thenReturn(pipelineInstanceWithId(11));
        when(stageDAO.find(sqlSession, "upstream", 1, "stage", 1))
                .thenReturn(null);

        workflowAllocator.allocate(sqlSession, pipeline, stage, Arrays.asList(material));

        verifyZeroInteractions(pipelineWorkflowDAO);
        verifyZeroInteractions(workflowDAO);
    }
}
