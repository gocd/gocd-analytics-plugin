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

package com.thoughtworks.gocd.analytics.executors.agent;


import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.PARAM_END_DATE;
import static com.thoughtworks.gocd.analytics.AnalyticTypes.PARAM_START_DATE;
import static com.thoughtworks.gocd.analytics.AvailableAnalytics.AGENTS_WITH_THE_HIGHEST_UTILIZATION;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgentsWithHighestUtilizationExecutorTest {
    @Test
    public void shouldFetchAgentsWithHighestUtilization() throws Exception {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        AgentUtilizationDAO agentUtilizationDAO = mock(AgentUtilizationDAO.class);
        SqlSession session = mock(SqlSession.class);
        when(sessionFactory.openSession()).thenReturn(session);

        List<AgentUtilization> utilizations = new ArrayList<>();
        utilizations.add(utilizationFrom("id1", "host1", 100, 200));
        utilizations.add(utilizationFrom("id2", "host2", 100, 200));

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_START_DATE, "2018-03-15");
        params.put(PARAM_END_DATE, "2018-03-22");
        AnalyticsRequest request = new AnalyticsRequest(AGENTS_WITH_THE_HIGHEST_UTILIZATION.getType(), AGENTS_WITH_THE_HIGHEST_UTILIZATION.getId(), params);

        when(agentUtilizationDAO.highestUtilization(eq(session), any(ZonedDateTime.class), any(ZonedDateTime.class), eq(10))).thenReturn(utilizations);

        AgentsWithHighestUtilizationExecutor executor = new AgentsWithHighestUtilizationExecutor(request, agentUtilizationDAO, sessionFactory);
        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        String expectedResponse = "{\n" +
                "\"data\":\"[" +
                "{\\\"agent_host_name\\\":\\\"host1\\\",\\\"uuid\\\":\\\"id1\\\",\\\"idle_duration_secs\\\":200,\\\"building_duration_secs\\\":100,\\\"cancelled_duration_secs\\\":0,\\\"lost_contact_duration_secs\\\":0,\\\"unknown_duration_secs\\\":0}," +
                "{\\\"agent_host_name\\\":\\\"host2\\\",\\\"uuid\\\":\\\"id2\\\",\\\"idle_duration_secs\\\":200,\\\"building_duration_secs\\\":100,\\\"cancelled_duration_secs\\\":0,\\\"lost_contact_duration_secs\\\":0,\\\"unknown_duration_secs\\\":0}" +
                "]\",\n" +
                "    \"view_path\": \"agent-with-highest-utilization-chart.html\"\n" +
                "}";

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
    }

    private AgentUtilization utilizationFrom(String uuid, String hostName, int buildingDuration, int idleDuration) {
        AgentUtilization agentUtilization = new AgentUtilization();
        agentUtilization.setUuid(uuid);
        agentUtilization.setAgentHostName(hostName);
        agentUtilization.setBuildingDurationSecs(buildingDuration);
        agentUtilization.setIdleDurationSecs(idleDuration);

        return agentUtilization;
    }
}
