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
import com.thoughtworks.gocd.analytics.dao.AgentTransitionDAO;
import com.thoughtworks.gocd.analytics.models.AgentTransition;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.PARAM_AGENT_UUID;
import static com.thoughtworks.gocd.analytics.AvailableAnalytics.AGENT_STATE_TRANSITION;
import static com.thoughtworks.gocd.analytics.executors.notification.AgentStatusRequestExecutorTest.agentTransitionFrom;
import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgentStateTransitionExecutorTest {

    @Test
    public void shouldFetchAgentStateTransitionsMetric() throws Exception {
        String agentUUID = "agent_uuid";

        SessionFactory sessionFactory = mock(SessionFactory.class);
        AgentTransitionDAO agentTransitionDAO = mock(AgentTransitionDAO.class);
        SqlSession session = mock(SqlSession.class);
        when(sessionFactory.openSession()).thenReturn(session);

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_AGENT_UUID, agentUUID);

        ArrayList<AgentTransition> agentTransitions = new ArrayList<>();
        ZonedDateTime transitionTime = ZonedDateTime.parse("2018-03-22T12:34:56Z", DateTimeFormatter.ISO_DATE_TIME).withZoneSameInstant(UTC);
        agentTransitions.add(agentTransitionFrom("agent_uuid", "enabled", "building", "building", transitionTime));

        AnalyticsRequest request = new AnalyticsRequest(AGENT_STATE_TRANSITION.getType(), AGENT_STATE_TRANSITION.getId(), params);
        when(agentTransitionDAO.findByUuid(eq(session), any(ZonedDateTime.class), any(ZonedDateTime.class), eq(agentUUID))).thenReturn(agentTransitions);

        AgentStateTransitionExecutor executor = new AgentStateTransitionExecutor(request, agentTransitionDAO, sessionFactory);
        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        String expectedResponse = "{" +
                "\"data\":\"{\\\"transitions\\\":[{\\\"uuid\\\":\\\"agent_uuid\\\",\\\"agent_config_state\\\":\\\"enabled\\\",\\\"agent_state\\\":\\\"building\\\",\\\"build_state\\\":\\\"building\\\",\\\"transition_time\\\":\\\"2018-03-22T12:34:56.000+0000\\\"}],\\\"uuid\\\":\\\"agent_uuid\\\"}\"," +
                "\"view_path\":\"agent-state-transition-chart.html\"}";

        assertEquals(expectedResponse, response.responseBody());
    }
}
