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
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.TYPE_VSM;
import static com.thoughtworks.gocd.analytics.StageMother.stageFrom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class VSMWorkflowTimeDistributionExecutorTest {
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private SqlSession sqlSession;
    @Mock
    private StageDAO stageDAO;
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void forTheGivenPipelinesInWorkflowAndWorkflowId_shouldListAllStages() throws JSONException {
        ZonedDateTime scheduleTime = ZonedDateTime.now();
        String scheduleTimeString = scheduleTime.withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern(DATE_PATTERN));

        AnalyticsRequest analyticsRequest = new AnalyticsRequest(TYPE_VSM, "", requestParams());

        Stage p1_s1 = stageFrom(1, "P1", 1, "S1", 1, "success", "changes", null, 0, scheduleTime);
        Stage p1_s2 = stageFrom(1, "P1", 1, "S2", 1, "success", "changes", null, 0, scheduleTime);
        Stage p2_s1 = stageFrom(1, "P2", 1, "S1", 1, "success", "changes", null, 0, scheduleTime);

        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(stageDAO.allStagesWithWorkflowIdInPipelines(sqlSession, 1234L, Arrays.asList("P1", "P2")))
                .thenReturn(Arrays.asList(p1_s1, p1_s2, p2_s1));

        GoPluginApiResponse response = new VSMWorkflowTimeDistributionExecutor(analyticsRequest, sessionFactory, stageDAO).doExecute();

        verify(stageDAO).allStagesWithWorkflowIdInPipelines(sqlSession, 1234L, Arrays.asList("P1", "P2"));
        assertEquals(200, response.responseCode());

        String expectedResponse = "{\"data\":" +
                "\"{\\\"stages\\\":[" +
                "{\\\"pipeline_name\\\":\\\"P1\\\",\\\"pipeline_counter\\\":1,\\\"stage_name\\\":\\\"S1\\\",\\\"stage_counter\\\":1," +
                "\\\"approval_type\\\":\\\"success\\\",\\\"approved_by\\\":\\\"changes\\\",\\\"scheduled_at\\\":\\\"" + scheduleTimeString + "\\\"," +
                "\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0}," +
                "{\\\"pipeline_name\\\":\\\"P1\\\",\\\"pipeline_counter\\\":1,\\\"stage_name\\\":\\\"S2\\\",\\\"stage_counter\\\":1," +
                "\\\"approval_type\\\":\\\"success\\\",\\\"approved_by\\\":\\\"changes\\\",\\\"scheduled_at\\\":\\\"" + scheduleTimeString + "\\\"," +
                "\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0}," +
                "{\\\"pipeline_name\\\":\\\"P2\\\",\\\"pipeline_counter\\\":1,\\\"stage_name\\\":\\\"S1\\\",\\\"stage_counter\\\":1," +
                "\\\"approval_type\\\":\\\"success\\\",\\\"approved_by\\\":\\\"changes\\\",\\\"scheduled_at\\\":\\\"" + scheduleTimeString + "\\\"," +
                "\\\"total_time_secs\\\":0,\\\"time_waiting_secs\\\":0}],\\\"pipelines_in_workflow\\\":[\\\"P1\\\",\\\"P2\\\"]}\"," +
                "\"view_path\":\"workflow-time-distribution-chart.html\"}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
    }

    private Map requestParams() {
        return Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("pipelines_in_workflow", "[\"P1\", \"P2\"]");
                put("workflow_id", "1234");
            }
        });
    }
}
