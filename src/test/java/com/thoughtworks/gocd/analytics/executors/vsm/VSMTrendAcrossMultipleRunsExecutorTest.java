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

package com.thoughtworks.gocd.analytics.executors.vsm;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.PipelineInstanceMother;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.PipelineDAO;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.PipelineInstance;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.TYPE_VSM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class VSMTrendAcrossMultipleRunsExecutorTest {
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private SqlSession sqlSession;
    @Mock
    private PipelineDAO pipelineDAO;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void forAGivenSourceAndDestinationPipeline_shouldListAllWorkflowsWithCorrespondingPipelinesWithInThem() throws JSONException {
        AnalyticsRequest analyticsRequest = new AnalyticsRequest(TYPE_VSM, "", requestParams("P2", "P4"));

        PipelineInstance p2_1 = PipelineInstanceMother.pipelineInstanceFrom(1, "P2", 1, 1);
        PipelineInstance p3_1 = PipelineInstanceMother.pipelineInstanceFrom(2, "P3", 1, 1);
        PipelineInstance p4_1 = PipelineInstanceMother.pipelineInstanceFrom(3, "P4", 1, 1);
        PipelineInstance p2_2 = PipelineInstanceMother.pipelineInstanceFrom(4, "P2", 2, 2);
        PipelineInstance p3_2 = PipelineInstanceMother.pipelineInstanceFrom(5, "P3", 2, 2);

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(pipelineDAO.allPipelineInstancesWithNameIn(sqlSession, "P2", Arrays.asList("P2", "P3", "P4")))
                .thenReturn(Arrays.asList(p3_2, p2_2, p4_1, p3_1, p2_1));

        GoPluginApiResponse response = new VSMTrendAcrossMultipleRunsExecutor(analyticsRequest, sessionFactory, pipelineDAO).doExecute();

        assertEquals(200, response.responseCode());

        String expectedResponse = "{\"data\":\"{" +
                "\\\"pipelines\\\":[\\\"P2\\\",\\\"P3\\\",\\\"P4\\\"]," +
                "\\\"workflows\\\":{\\\"1\\\":[" +
                "{\\\"counter\\\":1,\\\"name\\\":\\\"P2\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":1}," +
                "{\\\"counter\\\":1,\\\"name\\\":\\\"P3\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":1}," +
                "{\\\"counter\\\":1,\\\"name\\\":\\\"P4\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":1}]," +
                "\\\"2\\\":[" +
                "{\\\"counter\\\":2,\\\"name\\\":\\\"P2\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":2}," +
                "{\\\"counter\\\":2,\\\"name\\\":\\\"P3\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":2}]}}\"," +
                "\"view_path\":\"workflow-trends-chart.html\"}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
    }

    @Test
    public void forAGivenMaterialSourceAndDestinationPipeline_shouldListAllWorkflowsWithCorrespondingPipelinesWithInThem() throws JSONException {
        AnalyticsRequest analyticsRequest = new AnalyticsRequest(TYPE_VSM, "", requestParams("3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629", "P4"));

        PipelineInstance p1_1 = PipelineInstanceMother.pipelineInstanceFrom(1, "P1", 1, 1);
        PipelineInstance p2_1 = PipelineInstanceMother.pipelineInstanceFrom(2, "P2", 1, 1);
        PipelineInstance p3_1 = PipelineInstanceMother.pipelineInstanceFrom(3, "P3", 1, 1);
        PipelineInstance p4_1 = PipelineInstanceMother.pipelineInstanceFrom(4, "P4", 1, 1);
        PipelineInstance p1_2 = PipelineInstanceMother.pipelineInstanceFrom(5, "P1", 2, 2);
        PipelineInstance p2_2 = PipelineInstanceMother.pipelineInstanceFrom(6, "P2", 2, 2);
        PipelineInstance p3_2 = PipelineInstanceMother.pipelineInstanceFrom(7, "P3", 2, 2);

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(pipelineDAO.allPipelineInstancesWithNameIn(sqlSession, "P1", Arrays.asList("P1", "P2", "P3", "P4")))
                .thenReturn(Arrays.asList(p3_2, p2_2, p4_1, p3_1, p2_1, p1_1, p1_2));

        GoPluginApiResponse response = new VSMTrendAcrossMultipleRunsExecutor(analyticsRequest, sessionFactory, pipelineDAO).doExecute();

        assertEquals(200, response.responseCode());

        String expectedResponse = "{\"data\":\"{" +
                "\\\"pipelines\\\":[\\\"P1\\\",\\\"P2\\\",\\\"P3\\\",\\\"P4\\\"]," +
                "\\\"workflows\\\":{\\\"1\\\":[" +
                "{\\\"counter\\\":1,\\\"name\\\":\\\"P1\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":1}," +
                "{\\\"counter\\\":1,\\\"name\\\":\\\"P2\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":1}," +
                "{\\\"counter\\\":1,\\\"name\\\":\\\"P3\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":1}," +
                "{\\\"counter\\\":1,\\\"name\\\":\\\"P4\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":1}]," +
                "\\\"2\\\":[" +
                "{\\\"counter\\\":2,\\\"name\\\":\\\"P1\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":2}," +
                "{\\\"counter\\\":2,\\\"name\\\":\\\"P2\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":2}," +
                "{\\\"counter\\\":2,\\\"name\\\":\\\"P3\\\",\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0,\\\"workflow_id\\\":2}]}}\"," +
                "\"view_path\":\"workflow-trends-chart.html\"}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
    }

    @Test
    public void inAbsenceOfPipelineInstancesForTheGivenWorkflow_shouldRespondWithNoDataInfoMessage() throws JSONException {
        AnalyticsRequest analyticsRequest = new AnalyticsRequest(TYPE_VSM, "", requestParams("P1", "P4"));

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(pipelineDAO.allPipelineInstancesWithNameIn(sqlSession, "P1", Arrays.asList("P1", "P2", "P3", "P4")))
                .thenReturn(Collections.emptyList());

        GoPluginApiResponse response = new VSMTrendAcrossMultipleRunsExecutor(analyticsRequest, sessionFactory, pipelineDAO).doExecute();

        assertEquals(200, response.responseCode());
        JSONAssert.assertEquals("{\"data\":\"{}\",\"view_path\":\"info-message.html\"}", response.responseBody(), true);
    }

    private Map requestParams(String source, String destination) {
        return Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("source", source);
                put("destination", destination);
                put("vsm_graph", graphJSON);
            }
        });
    }

    String graphJSON = "{\n" +
            "    \"current_pipeline\": \"P4\",\n" +
            "    \"levels\": [\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"id\": \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\",\n" +
            "            \"name\": \"material_name\",\n" +
            "            \"parents\": [],\n" +
            "            \"dependents\": [\n" +
            "              \"P1\",\n" +
            "              \"P2\",\n" +
            "              \"P4\"\n" +
            "            ],\n" +
            "            \"type\": \"GIT\",\n" +
            "            \"material_revisions\": [\n" +
            "              {\n" +
            "                \"modifications\": [\n" +
            "                  {\n" +
            "                    \"revision\": \"2a13c2e8cf1661d905099e7297dba3c5b58bce7c\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P3\"\n" +
            "            ],\n" +
            "            \"id\": \"P1\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 1,\n" +
            "                \"label\": \"1\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 30,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P1\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\"\n" +
            "            ]\n" +
            "          },\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P3\"\n" +
            "            ],\n" +
            "            \"id\": \"P2\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 1,\n" +
            "                \"label\": \"1\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 52,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P2\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\"\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P4\"\n" +
            "            ],\n" +
            "            \"id\": \"P3\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 1,\n" +
            "                \"label\": \"1\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 13,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P3\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"P1\",\n" +
            "              \"P2\"\n" +
            "            ]\n" +
            "          },\n" +
            "          {\n" +
            "            \"dependents\": [\n" +
            "              \"P4\"\n" +
            "            ],\n" +
            "            \"id\": \"9e02d1ae843b55f2cf77af4dbaba38e2dfaf8f86d4ca4c890a4ba9396bfc26c8\",\n" +
            "            \"material_revisions\": [\n" +
            "              {\n" +
            "                \"modifications\": [\n" +
            "                  {\n" +
            "                    \"revision\": \"ad67c8a52dd0ed18e722ef526b7818ad0959df19\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"material_name\",\n" +
            "            \"type\": \"GIT\",\n" +
            "            \"parents\": []\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"dependents\": [],\n" +
            "            \"id\": \"P4\",\n" +
            "            \"instances\": [\n" +
            "              {\n" +
            "                \"counter\": 3,\n" +
            "                \"label\": \"3\",\n" +
            "                \"stages\": [\n" +
            "                  {\n" +
            "                    \"duration\": 17,\n" +
            "                    \"name\": \"defaultStage\",\n" +
            "                    \"status\": \"Passed\",\n" +
            "                    \"counter\": 1\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"name\": \"P4\",\n" +
            "            \"type\": \"PIPELINE\",\n" +
            "            \"parents\": [\n" +
            "              \"P3\",\n" +
            "              \"3795dca7e793e62cfde2e8e2898efee05bde08c99700cff0ec96d68ad4522629\",\n" +
            "              \"9e02d1ae843b55f2cf77af4dbaba38e2dfaf8f86d4ca4c890a4ba9396bfc26c8\"\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }";
}
