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
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import com.thoughtworks.gocd.analytics.models.Workflow;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static com.thoughtworks.gocd.analytics.StageMother.stageWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StageDAOIntegrationTest implements DAOIntegrationTest {
    private SqlSession sqlSession;
    private TestDBConnectionManager manager;
    private StageDAO stageDAO;
    private WorkflowDAO workflowDAO;
    private PipelineDAO pipelineDAO;
    private PipelineWorkflowDAO pipelineWorkflowDAO;

    @BeforeEach
    public void before() throws SQLException, InterruptedException {
        stageDAO = new StageDAO();
        workflowDAO = new WorkflowDAO();
        pipelineDAO = new PipelineDAO();
        pipelineWorkflowDAO = new PipelineWorkflowDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
    }

    @AfterEach
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldInsertAStageAndSuccessfullyRetrieveIt() {
        PipelineInstance pipelineInstance = new PipelineInstance(1, "pip01", 5, 0, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        Stage expected = stageWith("pip01", 1, "stage", 1, PASSED, PASSED, 5, ZonedDateTime.now().truncatedTo(ChronoUnit.MICROS));

        stageDAO.insert(sqlSession, expected);

        Stage result = stageDAO.One(sqlSession, pipelineInstance, "stage", 1);
        expected.setId(result.getId());
        assertEquals(expected, result);
    }

    @Test
    public void shouldDeleteAllStageRunsScheduledOnOrBeforeAGivenScheduledDate() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-02T00:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));

        stageDAO.insert(sqlSession, stageWith("pipeline_name", 1, "stage_name", 1,
                "passed", "passed", 1, dateTime));
        stageDAO.insert(sqlSession, stageWith("pipeline_name", 1, "stage_name", 1,
                "passed", "passed", 1, dateTime.minusHours(1)));
        stageDAO.insert(sqlSession, stageWith("pipeline_name", 1, "stage_name", 1,
                "passed", "passed", 1, dateTime.plusHours(1)));

        assertEquals(3, stageDAO.all(sqlSession, "pipeline_name").size());

        stageDAO.deleteStageRunsPriorTo(sqlSession, dateTime);

        assertEquals(2, stageDAO.all(sqlSession, "pipeline_name").size());
        assertEquals(dateTime.plusHours(1).toEpochSecond(), stageDAO.all(sqlSession, "pipeline_name").get(0).getScheduledAt().toEpochSecond());
        assertEquals(dateTime.toEpochSecond(), stageDAO.all(sqlSession, "pipeline_name").get(1).getScheduledAt().toEpochSecond());
    }

    @Test
    public void shouldListAllStagesForAGivenWorkflowAndInAGivenListOfPipelines() {
        Workflow workflow1 = new Workflow(ZonedDateTime.now());
        Workflow workflow2 = new Workflow(ZonedDateTime.now());
        insertWorkflow(workflow1);
        insertWorkflow(workflow2);

        PipelineInstance p1_1 = new PipelineInstance(1, "P1", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p1_1);
        Stage p1_1_s1_1 = stageWith("P1", 1, "S1", 1, "passed", "passed", 1, ZonedDateTime.now());
        Stage p1_1_s2_1 = stageWith("P1", 1, "S2", 1, "passed", "passed", 1, ZonedDateTime.now());
        insertStage(p1_1_s1_1);
        insertStage(p1_1_s2_1);
        insertPipelineWorkflow(p1_1.getId(), p1_1_s1_1.getId(), workflow1.getId());
        insertPipelineWorkflow(p1_1.getId(), p1_1_s2_1.getId(), workflow1.getId());

        PipelineInstance p2_1 = new PipelineInstance(1, "P2", 1, 1, PASSED, TEST_TS, TEST_TS.plusMinutes(10));
        insertPipeline(p2_1);
        Stage p2_1_s1_1 = stageWith("P2", 1, "S1", 1, "passed", "passed", 1, ZonedDateTime.now());
        Stage p2_1_s2_1 = stageWith("P2", 1, "S2", 1, "passed", "passed", 1, ZonedDateTime.now());
        insertStage(p2_1_s1_1);
        insertStage(p2_1_s2_1);
        insertPipelineWorkflow(p2_1.getId(), p2_1_s1_1.getId(), workflow2.getId());
        insertPipelineWorkflow(p2_1.getId(), p2_1_s2_1.getId(), workflow2.getId());

        List<Stage> stages = stageDAO.allStagesWithWorkflowIdInPipelines(sqlSession, workflow1.getId(), Collections.singletonList("P1"));

        assertEquals(2, stages.size());
        assertEquals(p1_1_s1_1.getId(), stages.get(0).getId());
        assertEquals(p1_1_s2_1.getId(), stages.get(1).getId());
    }

    private void insertWorkflow(Workflow workflow) {
        workflowDAO.insert(sqlSession, workflow);
    }

    private void insertPipeline(PipelineInstance pipelineInstance) {
        pipelineDAO.updateOrInsert(sqlSession, pipelineInstance);
    }

    private void insertStage(Stage stage) {
        stageDAO.insert(sqlSession, stage);
    }

    private void insertPipelineWorkflow(Long pipelineId, Long stageId, Long workflowId) {
        pipelineWorkflowDAO.insert(sqlSession, pipelineId, stageId, workflowId);
    }
}
