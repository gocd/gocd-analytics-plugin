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

package com.thoughtworks.gocd.analytics.dao;

import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.models.Pipeline;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.models.Workflow;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.thoughtworks.gocd.analytics.StageMother.stageWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PipelineDAOIntegrationTest implements DAOIntegrationTest {
    private SqlSession sqlSession;
    private PipelineDAO pipelineDAO;
    private TestDBConnectionManager manager;
    private WorkflowDAO workflowDAO;
    private PipelineWorkflowDAO pipelineWorkflowDAO;
    private StageDAO stageDAO;

    @Before
    public void before() throws SQLException, InterruptedException {
        pipelineDAO = new PipelineDAO();
        stageDAO = new StageDAO();
        workflowDAO = new WorkflowDAO();
        pipelineWorkflowDAO = new PipelineWorkflowDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
    }

    @After
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldSelectPipelineByGroupInstances() {
        List<PipelineInstance> instances = new ArrayList<>();
        instances.add(new PipelineInstance(1, "test", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(2, "test", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));

        pipelineDAO.insertEach(sqlSession, instances);

        List<Pipeline> expected = Collections.singletonList(new Pipeline("test", null));
        List<Pipeline> pipelines = pipelineDAO.allPipelines(sqlSession);

        assertThat(pipelines, is(expected));
    }

    @Test
    public void shouldSelectAllInstancesForPipeline() {
        List<PipelineInstance> instances = new ArrayList<>();
        instances.add(new PipelineInstance(1, "pip1", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(2, "pip1", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));

        pipelineDAO.insertEach(sqlSession, instances);

        List<PipelineInstance> pipelineInstances = pipelineDAO.instancesForPipeline(sqlSession, "pip1", null, ZonedDateTime.now());

        assertThat(pipelineInstances.size(), is(2));
        assertThat(pipelineInstances.get(0).getName(), is("pip1"));
        assertThat(pipelineInstances.get(1).getName(), is("pip1"));
    }

    @Test
    public void shouldSelectAllInstancesForPipelineWithinDateRange() {
        List<PipelineInstance> instances = new ArrayList<>();
        instances.add(new PipelineInstance(1, "pip1", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(2, "pip1", 1, 1, PASSED, TEST_TS.plusDays(10), TEST_TS.plusDays(10).plusMinutes(10)));

        pipelineDAO.insertEach(sqlSession, instances);

        List<PipelineInstance> pipelineInstances = pipelineDAO.instancesForPipeline(sqlSession, "pip1", TEST_TS.plusDays(5), TEST_TS.plusDays(15));

        assertThat(pipelineInstances.size(), is(1));
        assertThat(pipelineInstances.get(0).getName(), is("pip1"));
        assertThat(pipelineInstances.get(0).getCounter(), is(2));
    }

    @Test
    public void shouldFetchLongestWaitingPipelines() {
        List<PipelineInstance> instances = new ArrayList<>();
        instances.add(new PipelineInstance(1, "pip1", 3, 2, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(2, "pip1", 5, 4, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(1, "pip2", 2, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(2, "pip2", 2, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(1, "pip3", 100, 99, PASSED, TEST_TS, TEST_TS.plusMinutes(10)));
        instances.add(new PipelineInstance(2, "pip3", 100, 99999999, PASSED, TEST_TS.minusWeeks(7), TEST_TS.minusWeeks(7).plusMinutes(10)));

        pipelineDAO.insertEach(sqlSession, instances);

        List<Pipeline> pipelines = pipelineDAO.longestWaiting(sqlSession, TEST_TS.minusDays(1), TEST_TS.plusDays(1), 10);

        assertThat(pipelines.size(), is(3));
        assertThat(pipelines.get(0).getName(), is("pip3"));
        assertThat(pipelines.get(0).getAvgWaitTimeSecs(), is(99));
        assertThat(pipelines.get(0).getAvgBuildTimeSecs(), is(1));
        assertThat(pipelines.get(1).getName(), is("pip1"));
        assertThat(pipelines.get(1).getAvgWaitTimeSecs(), is(3));
        assertThat(pipelines.get(1).getAvgBuildTimeSecs(), is(1));
        assertThat(pipelines.get(2).getName(), is("pip2"));
        assertThat(pipelines.get(2).getAvgWaitTimeSecs(), is(1));
        assertThat(pipelines.get(2).getAvgBuildTimeSecs(), is(1));
    }

    @Test
    public void updateOrInsertShouldUpdateExistingPipelineInstances() {
        PipelineInstance instance = new PipelineInstance(1, "pip", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));

        List<PipelineInstance> instances = pipelineDAO.instancesForPipeline(sqlSession, "pip", null, ZonedDateTime.now());
        assertThat(instances.size(), is(0));

        pipelineDAO.updateOrInsert(sqlSession, instance);
        instances = pipelineDAO.instancesForPipeline(sqlSession, "pip", null, ZonedDateTime.now());
        assertThat(instances.size(), is(1));

        instance.setResult("Failed");
        instance.setTimeWaitingSecs(15);
        instance.setTotalTimeSecs(20);
        pipelineDAO.updateOrInsert(sqlSession, instance);

        instances = pipelineDAO.instancesForPipeline(sqlSession, "pip", null, ZonedDateTime.now());
        assertThat(instances.size(), is(1));
        assertThat(instances.get(0).getResult(), is("Failed"));
        assertThat(instances.get(0).getTimeWaitingSecs(), is(16));
        assertThat(instances.get(0).getTotalTimeSecs(), is(21));
    }

    @Test
    public void allPipelineInstancesWithNameIn_shouldFetchAllPipelinesInWorkflowsWhichHasTheSourcePipeline() {
        Workflow workflow1 = new Workflow(ZonedDateTime.now());
        Workflow workflow2 = new Workflow(ZonedDateTime.now());
        insertWorkflow(workflow1);
        insertWorkflow(workflow2);

        Stage dummyStage = stageWith("P1", 1, "S1", 1, "passed", "passed", 1, ZonedDateTime.now());
        insertStage(dummyStage);

        PipelineInstance p1_1 = new PipelineInstance(1, "P1", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p1_1);
        insertPipelineWorkflow(p1_1.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p2_1 = new PipelineInstance(1, "P2", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p2_1);
        insertPipelineWorkflow(p2_1.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p3_1 = new PipelineInstance(1, "P3", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p3_1);
        insertPipelineWorkflow(p3_1.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p2_2 = new PipelineInstance(2, "P2", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p2_2);
        insertPipelineWorkflow(p2_2.getId(), dummyStage.getId(), workflow2.getId());

        List<PipelineInstance> pipelineInstances = pipelineDAO.allPipelineInstancesWithNameIn(sqlSession, "P1", Arrays.asList("P1", "P2"));

        assertThat(pipelineInstances.size(), is(2));
        assertThat(pipelineInstances.get(0).getWorkflowId(), is(workflow1.getId()));
        assertThat(pipelineInstances.get(0).getId(), is(p1_1.getId()));
        assertThat(pipelineInstances.get(1).getWorkflowId(), is(workflow1.getId()));
        assertThat(pipelineInstances.get(1).getId(), is(p2_1.getId()));
    }

    @Test
    public void allPipelineInstancesWithNameIn_shouldEnsureEachWorklowHasASingleAndTheLatestInstanceOfPipeline() {
        Workflow workflow1 = new Workflow(ZonedDateTime.now());
        Workflow workflow2 = new Workflow(ZonedDateTime.now());
        insertWorkflow(workflow1);
        insertWorkflow(workflow2);

        Stage dummyStage = stageWith("P1", 1, "S1", 1, "passed", "passed", 1, ZonedDateTime.now());
        insertStage(dummyStage);

        PipelineInstance p1_1 = new PipelineInstance(1, "P1", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p1_1);
        insertPipelineWorkflow(p1_1.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p1_2 = new PipelineInstance(2, "P1", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p1_2);
        insertPipelineWorkflow(p1_2.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p2_1 = new PipelineInstance(1, "P2", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p2_1);
        insertPipelineWorkflow(p2_1.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p2_2 = new PipelineInstance(2, "P2", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p2_2);
        insertPipelineWorkflow(p2_2.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p3_1 = new PipelineInstance(1, "P3", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p3_1);
        insertPipelineWorkflow(p3_1.getId(), dummyStage.getId(), workflow1.getId());

        PipelineInstance p2_3 = new PipelineInstance(3, "P2", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p2_3);
        insertPipelineWorkflow(p2_3.getId(), dummyStage.getId(), workflow2.getId());

        List<PipelineInstance> pipelineInstances = pipelineDAO.allPipelineInstancesWithNameIn(sqlSession, "P1", Arrays.asList("P1", "P2"));

        assertThat(pipelineInstances.size(), is(2));
        assertThat(pipelineInstances.get(0).getWorkflowId(), is(workflow1.getId()));
        assertThat(pipelineInstances.get(0).getId(), is(p1_2.getId()));
        assertThat(pipelineInstances.get(1).getWorkflowId(), is(workflow1.getId()));
        assertThat(pipelineInstances.get(1).getId(), is(p2_2.getId()));
    }

    private void insertWorkflow(Workflow workflow) {
        workflowDAO.insert(sqlSession, workflow);
    }

    private void insertPipeline(PipelineInstance pipelineInstance) {
        pipelineDAO.updateOrInsert(sqlSession, pipelineInstance);
    }

    private void insertPipelineWorkflow(Long pipelineId, Long stageId, Long workflowId) {
        pipelineWorkflowDAO.insert(sqlSession, pipelineId, stageId, workflowId);
    }

    private void insertStage(Stage stage) {
        stageDAO.insert(sqlSession, stage);
    }
}
