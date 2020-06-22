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

package com.thoughtworks.gocd.analytics.executors.notification;

import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.AgentDAO;
import com.thoughtworks.gocd.analytics.dao.AgentTransitionDAO;
import com.thoughtworks.gocd.analytics.models.Agent;
import com.thoughtworks.gocd.analytics.models.AgentStatusRequest;
import com.thoughtworks.gocd.analytics.models.AgentTransition;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthState;
import com.thoughtworks.gocd.analytics.serialization.adapters.DefaultZonedDateTimeTypeAdapter;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;
import static com.thoughtworks.gocd.analytics.utils.Util.GSON;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AgentStatusRequestExecutorTest {

    private GoPluginApiRequest apiRequest;
    private SqlSession sqlSession;
    private SessionFactory sessionFactory;
    private AgentDAO agentDAO;
    private AgentTransitionDAO agentTransitionDAO;
    private AgentUtilizationUpdater agentUtilizationUpdater;
    private PluginHealthMessageService pluginHealthMessageService;

    public static AgentTransition agentTransitionFrom(String uuid, String agentConfigState, String agentState, String buildState, ZonedDateTime transitionTime) {
        AgentTransition agentTransition = new AgentTransition();

        agentTransition.setUuid(uuid);
        agentTransition.setAgentConfigState(agentConfigState);
        agentTransition.setAgentState(agentState);
        agentTransition.setBuildState(buildState);
        agentTransition.setTransitionTime(transitionTime);

        return agentTransition;
    }

    @Before
    public void setUp() throws Exception {
        apiRequest = mock(GoPluginApiRequest.class);
        sqlSession = mock(SqlSession.class);
        sessionFactory = mock(SessionFactory.class);
        agentDAO = mock(AgentDAO.class);
        agentTransitionDAO = mock(AgentTransitionDAO.class);
        agentUtilizationUpdater = mock(AgentUtilizationUpdater.class);
        pluginHealthMessageService = mock(PluginHealthMessageService.class);
    }

    @Test
    public void shouldInsert_Agent_AgentUtilization_And_AgentTransition_Data() throws Exception {
        when(sessionFactory.openSession()).thenReturn(sqlSession);
        when(apiRequest.requestBody()).thenReturn(agentJSON());

        AgentStatusRequestExecutor executor = new AgentStatusRequestExecutor(apiRequest, sessionFactory, agentDAO, agentTransitionDAO, agentUtilizationUpdater, pluginHealthMessageService);

        GoPluginApiResponse response = executor.execute();
        Map<String, String> responseJson = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());

        assertThat(response.responseCode(), is(200));
        assertThat(responseJson.get("status"), is("success"));
        verify(agentDAO).updateOrInsert(sqlSession, agentFrom("agent_uuid", true, "100", "agent_hostname", "127.0.0.1", "rh", "enabled"));
        ZonedDateTime transitionTime = ZonedDateTime.parse("2018-02-15T06:31:28.998+0000", DateTimeFormatter.ofPattern(DefaultZonedDateTimeTypeAdapter.DATE_PATTERN).withZone(UTC));
        verify(agentTransitionDAO).insertTransition(sqlSession, agentTransitionFrom("agent_uuid", "enabled", "building", "building", transitionTime));
        verify(agentUtilizationUpdater).update(sqlSession, AgentStatusRequest.fromJSON(agentJSON()));
    }

    @Test
    public void shouldNotifyGoCDWithWarningMessageInAbsenceOfDBConnection() throws Exception {
        AgentStatusRequestExecutor executor = new AgentStatusRequestExecutor(apiRequest, null, agentDAO, agentTransitionDAO, agentUtilizationUpdater, pluginHealthMessageService);

        GoPluginApiResponse response = executor.execute();
        Map<String, String> responseJson = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());

        assertThat(response.responseCode(), is(200));
        assertThat(responseJson.get("status"), is("success"));
        verify(pluginHealthMessageService).update(any(PluginHealthState.class));
        verifyZeroInteractions(agentDAO);
        verifyZeroInteractions(agentTransitionDAO);
        verifyZeroInteractions(agentUtilizationUpdater);
    }

    private String agentJSON() {
        return "{\n" +
                "    \"agent_config_state\": \"enabled\",\n" +
                "    \"agent_state\": \"building\",\n" +
                "    \"build_state\": \"building\",\n" +
                "    \"is_elastic\": true,\n" +
                "    \"free_space\": \"100\",\n" +
                "    \"host_name\": \"agent_hostname\",\n" +
                "    \"ip_address\": \"127.0.0.1\",\n" +
                "    \"operating_system\": \"rh\",\n" +
                "    \"transition_time\": \"2018-02-15T06:31:28.998+0000\",\n" +
                "    \"uuid\": \"agent_uuid\"\n" +
                "}\n";
    }

    private Agent agentFrom(String uuid, boolean isElastic, String freeSpace, String hostName, String ipAddress, String os, String configState) {
        Agent agent = new Agent();
        agent.setUuid(uuid);
        agent.setIsElastic(isElastic);
        agent.setFreeSpace(freeSpace);
        agent.setHostName(hostName);
        agent.setIpAddress(ipAddress);
        agent.setOperatingSystem(os);
        agent.setConfigState(configState);

        return agent;
    }
}
