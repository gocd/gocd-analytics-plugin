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
import com.thoughtworks.gocd.analytics.models.Agent;
import com.thoughtworks.gocd.analytics.models.AgentUtilization;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.thoughtworks.gocd.analytics.AgentMother.agentWith;
import static com.thoughtworks.gocd.analytics.AgentUtilizationMother.agentUtilizationWith;
import static com.thoughtworks.gocd.analytics.JobMother.jobWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AgentUtilizationDAOTest implements DAOIntegrationTest {
    private SqlSession sqlSession;
    private TestDBConnectionManager manager;
    private AgentUtilizationDAO dao;
    private JobDAO jobDao;
    private AgentDAO agentDAO;

    @BeforeEach
    public void before() throws SQLException, InterruptedException {
        dao = new AgentUtilizationDAO();
        jobDao = new JobDAO();
        agentDAO = new AgentDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
    }

    @AfterEach
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldInsertAgentUtilization() {
        insertAgent("uuid", "host_name", true, "127.0.0.1", "rh", "100G", ENABLED);

        ZonedDateTime utilizationDate = TEST_TS.withZoneSameInstant(ZoneId.of("UTC"));

        AgentUtilization agentUtilization = agentUtilizationWith("uuid", utilizationDate, "idle", utilizationDate, 100, 0, 0, "");

        dao.insert(sqlSession, agentUtilization);

        AgentUtilization utilization = dao.findUtilization(sqlSession, "uuid", utilizationDate);

        assertEquals("uuid", utilization.getUuid());
        assertEquals("idle", utilization.getLastKnownState());
        assertEquals(utilizationDate, utilization.getUtilizationDate());
        assertEquals(utilizationDate, utilization.getLastTransitionTime());
        assertEquals(100, utilization.getIdleDurationSecs());
        assertEquals(0, utilization.getUnknownDurationSecs());
        assertEquals(0, utilization.getCancelledDurationSecs());
        assertEquals(0, utilization.getBuildingDurationSecs());
    }

    @Test
    public void shouldUpdateAgentUtilizationForAUtilizationDate() {
        insertAgent("uuid", "host_name", true, "127.0.0.1", "rh", "100G", ENABLED);
        ZonedDateTime utilizationDate = TEST_TS.withZoneSameInstant(ZoneId.of("UTC"));

        AgentUtilization agentUtilization = agentUtilizationWith("uuid", utilizationDate, "idle", utilizationDate, 0, 0, 0, "");

        dao.insert(sqlSession, agentUtilization);

        AgentUtilization utilization = dao.findUtilization(sqlSession, "uuid", utilizationDate);

        assertEquals("uuid", utilization.getUuid());
        assertEquals("idle", utilization.getLastKnownState());
        assertEquals(utilizationDate, utilization.getUtilizationDate());
        assertEquals(utilizationDate, utilization.getLastTransitionTime());
        assertEquals(0, utilization.getIdleDurationSecs());

        ZonedDateTime currentTransitionTime = TEST_TS.plusHours(5).withZoneSameInstant(ZoneId.of("UTC"));

        utilization.setLastKnownState("building");
        utilization.setLastTransitionTime(currentTransitionTime);
        utilization.setIdleDurationSecs(500);

        dao.update(sqlSession, utilization);

        AgentUtilization utilizationAfterUpdate = dao.findUtilization(sqlSession, "uuid", utilizationDate);

        assertEquals("uuid", utilizationAfterUpdate.getUuid());
        assertEquals("building", utilizationAfterUpdate.getLastKnownState());
        assertEquals(utilizationDate, utilizationAfterUpdate.getUtilizationDate());
        assertEquals(currentTransitionTime, utilizationAfterUpdate.getLastTransitionTime());
        assertEquals(500, utilizationAfterUpdate.getIdleDurationSecs());
    }

    @Test
    public void shouldFindLatestUtilizationFromAnAgent() {
        insertAgent("uuid", "host_name", true, "127.0.0.1", "rh", "100G", ENABLED);
        ZonedDateTime threeDaysAgo = TEST_TS.minusDays(3).withZoneSameInstant(ZoneId.of("UTC"));
        AgentUtilization utilizationThreeDaysAgo = agentUtilizationWith("uuid", threeDaysAgo, "idle", threeDaysAgo, 0, 0, 0, "");
        dao.insert(sqlSession, utilizationThreeDaysAgo);

        ZonedDateTime twoDaysAgo = TEST_TS.minusDays(2).withZoneSameInstant(ZoneId.of("UTC"));
        AgentUtilization utilizationTwoDaysAgo = agentUtilizationWith("uuid", twoDaysAgo, "idle", twoDaysAgo, 0, 0, 0, "");
        dao.insert(sqlSession, utilizationTwoDaysAgo);

        AgentUtilization utilization = dao.findLatestUtilization(sqlSession, "uuid");

        assertEquals(twoDaysAgo, utilization.getUtilizationDate());
    }

    @Test
    public void shouldErrorOutWhileAddingUtilizationForInvalidAgentUuid() {
        AgentUtilization utilization = agentUtilizationWith("uuid", TEST_TS, "idle", TEST_TS, 0, 0, 0, "");

        assertThrows(PersistenceException.class, () -> dao.insert(sqlSession, utilization));
    }

    @Test
    public void shouldFetchAgentsWithHighestUtlizationInDateRangeWhichHasRanAtLeastThreeJobs() {
        ZonedDateTime now = TEST_TS;
        ZonedDateTime threeDaysAgo = now.minusDays(3);
        ZonedDateTime tenDaysAgo = now.minusDays(10);

        insertAgent("agent-1", "host_name-1", true, "127.0.0.1", "rh", "100G", ENABLED);
        // agent1 has ran at least 3 jobs
        jobDao.insertJob(sqlSession, jobWith("p1", 1, "s1", "j1", "pass", now, "agent-1"));
        jobDao.insertJob(sqlSession, jobWith("p1", 2, "s1", "j1", "pass", now, "agent-1"));
        jobDao.insertJob(sqlSession, jobWith("p1", 3, "s1", "j1", "pass", now, "agent-1"));
        dao.insert(sqlSession, agentUtilizationWith("agent-1", now, "idle", now, 10, 100, 0, ""));
        dao.insert(sqlSession, agentUtilizationWith("agent-1", threeDaysAgo, "idle", threeDaysAgo, 10, 500, 600, ""));

        insertAgent("agent-2", "host_name-2", true, "127.0.0.1", "rh", "100G", ENABLED);
        // agent2 has not ran at least 3 jobs
        jobDao.insertJob(sqlSession, jobWith("p1", 4, "s1", "j1", "pass", now, "agent-2"));
        dao.insert(sqlSession, agentUtilizationWith("agent-2", now, "idle", now, 10, 500, 0, ""));
        dao.insert(sqlSession, agentUtilizationWith("agent-2", threeDaysAgo, "idle", threeDaysAgo, 10, 800, 0, ""));

        insertAgent("agent-3", "host_name-3", true, "127.0.0.1", "rh", "100G", ENABLED);
        // agent3 has ran at least 3 jobs
        jobDao.insertJob(sqlSession, jobWith("p1", 5, "s1", "j1", "pass", now, "agent-3"));
        jobDao.insertJob(sqlSession, jobWith("p1", 6, "s1", "j1", "pass", now, "agent-3"));
        jobDao.insertJob(sqlSession, jobWith("p1", 7, "s1", "j1", "pass", now, "agent-3"));
        dao.insert(sqlSession, agentUtilizationWith("agent-3", now, "idle", now, 10, 100, 0, ""));
        dao.insert(sqlSession, agentUtilizationWith("agent-3", threeDaysAgo, "idle", threeDaysAgo, 100, 100, 0, ""));
        dao.insert(sqlSession, agentUtilizationWith("agent-3", tenDaysAgo, "idle", tenDaysAgo, 10, 1800, 0, ""));

        List<AgentUtilization> agentUtilizations = dao.highestUtilization(sqlSession, now.minusDays(6), now.plusDays(1), 10);

        assertEquals(2, agentUtilizations.size());
        assertEquals(agentUtilizationWith("agent-1", null, null, null,
                10, 600, 0, "host_name-1"), agentUtilizations.get(0));
        assertEquals(agentUtilizationWith("agent-3", null, null, null,
                55, 100, 0, "host_name-3"), agentUtilizations.get(1));
    }

    @Test
    public void shouldDeleteAllUtilizationOnOrBeforeAGivenUtilizationDate() {
        ZonedDateTime dateTime = ZonedDateTime.parse("2018-01-02T00:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")).withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime oneHourAgo = dateTime.minusHours(1);
        ZonedDateTime oneDayAhead = dateTime.plusDays(1);

        insertAgent("agent-1", "host_name-1", true, "127.0.0.1", "rh", "100G", "Enabled");
        dao.insert(sqlSession, agentUtilizationWith("agent-1", dateTime, "idle", dateTime, 10, 100, 0, ""));
        dao.insert(sqlSession, agentUtilizationWith("agent-1", oneHourAgo, "idle", oneHourAgo, 10, 100, 0, ""));
        dao.insert(sqlSession, agentUtilizationWith("agent-1", oneDayAhead, "idle", oneDayAhead, 10, 100, 0, ""));

        assertEquals(3, dao.all(sqlSession, "agent-1").size());

        dao.deleteUtilizationPriorTo(sqlSession, dateTime);

        assertEquals(2, dao.all(sqlSession, "agent-1").size());
        assertEquals(oneDayAhead.toEpochSecond(), dao.findUtilization(sqlSession, "agent-1", oneDayAhead).getUtilizationDate().toEpochSecond());
        assertEquals(dateTime.toEpochSecond(), dao.findUtilization(sqlSession, "agent-1", dateTime).getUtilizationDate().toEpochSecond());
    }

    private void insertAgent(String uuid, String hostName, boolean isElastic, String ipAddress, String os, String freeSpace, String configState) {
        Agent agent = agentWith(uuid, hostName, isElastic, ipAddress, os, freeSpace, configState);
        agentDAO.updateOrInsert(sqlSession, agent);

    }
}
