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

package com.thoughtworks.gocd.analytics.mapper;

import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StageMapperIntegrationTest {
    private TestDBConnectionManager manager;
    private StageMapper mapper;
    private PipelineWorkflowMapper pipelineWorkflowMapper;

    @BeforeEach
    public void before() throws SQLException, InterruptedException {
        manager = new TestDBConnectionManager();
        mapper = manager.getSqlSession().getMapper(StageMapper.class);
        pipelineWorkflowMapper = manager.getSqlSession().getMapper(PipelineWorkflowMapper.class);
    }

    @AfterEach
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldConsiderPipelineNameAndStageNameToBeCaseInsensitive() {
        PipelineInstance pipelineNamedWithUpperCase = new PipelineInstance(1, "PIPELINE", 1, 1, "Passed", ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(10));
        Stage stageNamedWithUppercase = stageFrom(pipelineNamedWithUpperCase.getName(), 1, "STAGE", 1, "Passed", "Passed", 5);
        mapper.insert(stageNamedWithUppercase);

        assertEquals(1, mapper.allStages("PIPELINE").size());
        long stageId = mapper.allStages("PIPELINE").get(0).getId();

        assertEquals(stageId, mapper.One("PIPELINE", 1, "STAGE", 1).getId());
        assertEquals(stageId, mapper.One("PIPELINE", 1, "stage", 1).getId());
        assertEquals(stageId, mapper.One("pipeline", 1, "STAGE", 1).getId());
        assertEquals(stageId, mapper.One("pipeline", 1, "stage", 1).getId());
    }

    @Test
    public void shouldFetchAllStageInstancesOfGivenWorkflowIDForSpecifiedPipelines() {
        long WORKFLOW_ID = 1L;
        List<String> workflowPipelines = Arrays.asList("build-linux", "build-windows");
    }

    private Stage stageFrom(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                            String result, String state, int duration) {
        Stage stage = new Stage();
        stage.setPipelineName(pipelineName);
        stage.setPipelineCounter(pipelineCounter);
        stage.setStageName(stageName);
        stage.setStageCounter(stageCounter);
        stage.setResult(result);
        stage.setState(state);
        stage.setTotalTimeSecs(duration);

        return stage;
    }

}
