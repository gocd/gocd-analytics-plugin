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

package com.thoughtworks.gocd.analytics.executors;

import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.dao.AgentDAO;
import com.thoughtworks.gocd.analytics.dao.AgentTransitionDAO;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.executors.notification.AgentStatusRequestExecutor;
import com.thoughtworks.gocd.analytics.models.Agent;
import com.thoughtworks.gocd.analytics.models.AgentTransition;
import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import com.thoughtworks.gocd.analytics.utils.DateUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import static com.thoughtworks.gocd.analytics.utils.DateUtils.UTC;
import static java.lang.String.format;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class AgentStatusRequestExecutorTest {
    private SqlSession sqlSession;
    private TestDBConnectionManager manager;
    private AgentUtilizationDAO dao;
    private AgentDAO agentDAO;
    private AgentTransitionDAO agentTransitionsDAO;
    private GoPluginApiRequest apiRequest;

    @Before
    public void before() throws SQLException, InterruptedException {
        TimeZone.setDefault(TimeZone.getTimeZone(UTC));
        dao = new AgentUtilizationDAO();
        agentDAO = new AgentDAO();
        agentTransitionsDAO = new AgentTransitionDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
        apiRequest = mock(GoPluginApiRequest.class);
    }

    @After
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void onAgentStatusRequestForNonExistentAgent_shouldInsertAgentRecordAgentTransitionRecordAndCorrespondingUtilizationRecord() throws Exception {
        String agentUUID = "agent_uuid";
        String transitionTime = "2018-04-13T14:25:29.165+0000";

        DefaultGoPluginApiRequest apiRequest = new DefaultGoPluginApiRequest("api", "1.0", "request1");
        apiRequest.setRequestBody(agentJSON("Unknown", transitionTime));
        ZonedDateTime utilizationDate = ZonedDateTime.parse(transitionTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));

        assertNull(agentDAO.findByUuid(sqlSession, agentUUID));
        assertThat(agentTransitionsDAO.findByUuid(sqlSession, utilizationDate.minusDays(1), utilizationDate.plusDays(1), agentUUID).size(), is(0));
        assertThat(dao.all(sqlSession, agentUUID).size(), is(0));

        new AgentStatusRequestExecutor(apiRequest, manager.getSessionFactory(), mock(PluginHealthMessageService.class)).execute();

        try (SqlSession sqlSession = manager.getSessionFactory().openSession()) {
            Agent agent = agentDAO.findByUuid(sqlSession, agentUUID);
            assertThat(agent.getFreeSpace(), is("100"));
            assertThat(agent.getConfigState(), is("enabled"));
            assertThat(agent.getOperatingSystem(), is("rh"));
            assertThat(agent.getIpAddress(), is("127.0.0.1"));
            assertTrue(agent.isElastic());
            assertThat(agent.getHostName(), is("agent_hostname"));

            AgentUtilization latestUtilization = dao.findLatestUtilization(sqlSession, agentUUID);
            assertThat(latestUtilization.getUnknownDurationSecs(), is(0));
            assertThat(latestUtilization.getLastTransitionTime(), is(utilizationDate));
            assertThat(latestUtilization.getLastKnownState(), is("Unknown"));
            assertThat(latestUtilization.getUtilizationDate(), is(utilizationDate));

            List<AgentTransition> agentTransitions = agentTransitionsDAO.findByUuid(sqlSession, utilizationDate.minusDays(1), utilizationDate.plusDays(1), agentUUID);
            assertThat(agentTransitions.size(), is(1));

            assertThat(agentTransitions.get(0).getUuid(), is("agent_uuid"));
            assertThat(agentTransitions.get(0).getAgentConfigState(), is("enabled"));
            assertThat(agentTransitions.get(0).getAgentState(), is("Unknown"));
            assertThat(agentTransitions.get(0).getBuildState(), is("building"));
            ZonedDateTime expectedTransitionTime = ZonedDateTime.parse(transitionTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(UTC);
            assertThat(agentTransitions.get(0).getTransitionTime(), is(expectedTransitionTime));
        }
    }

    @Test
    public void shouldUpdateLastKnownUtilizationAndCreateMultipleNewUtilizationIfAgentStateTransitioningOverMultipleDays() throws Exception {
        String previousTransitionTime = "2018-04-13T14:25:29.165+0000";
        String currentTransitionTime = "2018-04-15T11:35:58.688+0000";

        DefaultGoPluginApiRequest apiRequest1 = new DefaultGoPluginApiRequest("api", "1.0", "request1");
        apiRequest1.setRequestBody(agentJSON("Unknown", previousTransitionTime));

        DefaultGoPluginApiRequest apiRequest2 = new DefaultGoPluginApiRequest("api", "1.0", "request1");
        apiRequest2.setRequestBody(agentJSON("Unknown", currentTransitionTime));

        ZonedDateTime currentTransitionDateTime = ZonedDateTime.parse(currentTransitionTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime previousTransitionDateTime = ZonedDateTime.parse(previousTransitionTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime yesterday = ZonedDateTime.parse("2018-04-14T14:25:29.165+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));

        new AgentStatusRequestExecutor(apiRequest1, manager.getSessionFactory(), mock(PluginHealthMessageService.class)).execute();

        new AgentStatusRequestExecutor(apiRequest2, manager.getSessionFactory(), mock(PluginHealthMessageService.class)).execute();

        List<AgentUtilization> utilizations = dao.all(sqlSession, "agent_uuid");

        assertThat(utilizations.size(), is(3));

        AgentUtilization utilizationForCurrentTime = dao.findUtilization(sqlSession, "agent_uuid", currentTransitionDateTime);
        assertThat(utilizationForCurrentTime.getUtilizationDate().toEpochSecond(), is(currentTransitionDateTime.toEpochSecond()));
        assertThat(utilizationForCurrentTime.getLastKnownState(), is("Unknown"));
        assertThat(utilizationForCurrentTime.getLastTransitionTime().toEpochSecond(), is((currentTransitionDateTime.toEpochSecond())));
        assertThat(utilizationForCurrentTime.getUnknownDurationSecs(), is(DateUtils.durationFromStartOfDayInSeconds(currentTransitionDateTime)));

        AgentUtilization utilizationForYesterday = dao.findUtilization(sqlSession, "agent_uuid", yesterday);
        assertThat(utilizationForYesterday.getUtilizationDate().toEpochSecond(), is(yesterday.toEpochSecond()));
        assertThat(utilizationForYesterday.getLastKnownState(), is("Unknown"));
        assertThat(utilizationForYesterday.getLastTransitionTime().toEpochSecond(), is((yesterday.toEpochSecond())));
        assertThat(utilizationForYesterday.getUnknownDurationSecs(), is(86400));

        AgentUtilization utilizationForTwoDaysAgo = dao.findUtilization(sqlSession, "agent_uuid", previousTransitionDateTime);
        assertThat(utilizationForTwoDaysAgo.getUtilizationDate().toEpochSecond(), is(previousTransitionDateTime.toEpochSecond()));
        assertThat(utilizationForTwoDaysAgo.getLastKnownState(), is("Unknown"));
        assertThat(utilizationForTwoDaysAgo.getLastTransitionTime().toEpochSecond(), is((previousTransitionDateTime.toEpochSecond())));
    }

    private String agentJSON(String agentState, String transitionTime) {
        return format("{\n" +
                "    \"agent_config_state\": \"enabled\",\n" +
                "    \"agent_state\": \"%s\",\n" +
                "    \"build_state\": \"building\",\n" +
                "    \"is_elastic\": true,\n" +
                "    \"free_space\": \"100\",\n" +
                "    \"host_name\": \"agent_hostname\",\n" +
                "    \"ip_address\": \"127.0.0.1\",\n" +
                "    \"operating_system\": \"rh\",\n" +
                "    \"transition_time\": \"%s\",\n" +
                "    \"uuid\": \"agent_uuid\"\n" +
                "}\n", agentState, transitionTime);
    }
}
