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

package com.thoughtworks.gocd.analytics.cleanup;


import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.dao.AgentDAO;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.db.DBAccess;
import com.thoughtworks.gocd.analytics.models.*;
import com.thoughtworks.gocd.analytics.utils.LocalTimeProvider;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.thoughtworks.gocd.analytics.AgentMother.agentWith;
import static com.thoughtworks.gocd.analytics.AgentUtilizationMother.agentUtilizationWith;
import static com.thoughtworks.gocd.analytics.JobMother.jobWith;
import static com.thoughtworks.gocd.analytics.StageMother.stageWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataPurgeSchedulerTest {
    private SqlSession sqlSession;
    private TestDBConnectionManager manager;
    private AgentDAO agentDAO;
    private AgentUtilizationDAO agentUtilizationDAO;
    private JobDAO jobDAO;
    private StageDAO stageDAO;
    private DataPurgeScheduler dataPurgeScheduler;
    private LocalTimeProvider localTimeProvider;

    @Before
    public void before() throws SQLException, InterruptedException {
        agentDAO = new AgentDAO();
        agentUtilizationDAO = new AgentUtilizationDAO();
        jobDAO = new JobDAO();
        stageDAO = new StageDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
        localTimeProvider = mock(LocalTimeProvider.class);
        dataPurgeScheduler = new DataPurgeScheduler(new ScheduledExecutorServiceProvider(), localTimeProvider, DBAccess.instance());
    }

    @After
    public void tearDown() throws Exception {
        manager.shutdown();
        dataPurgeScheduler.shutdown();
    }

    @Test
    public void shouldScheduleTaskToDeleteGranularDataOlderThan30Days() throws InterruptedException {
        LocalTime localTime = LocalTime.parse("10:10:59", DateTimeFormatter.ofPattern("HH:mm:ss"));

        insertAgent("agent-1", "host_name", false, "127.0.0.1", "some_os");
        ZonedDateTime twentyNineDaysAgo = ZonedDateTime.of(LocalDate.now().minusDays(29), localTime, ZoneId.of("UTC"));
        ZonedDateTime thirtyOneDaysAgo = ZonedDateTime.of(LocalDate.now().minusDays(31), localTime, ZoneId.of("UTC"));

        insertAgentUtilization("agent-1", twentyNineDaysAgo, "idle", twentyNineDaysAgo, 10,
                100, 0, "");
        insertAgentUtilization("agent-1", thirtyOneDaysAgo, "idle", thirtyOneDaysAgo, 10,
                100, 0, "");

        insertJob("p1", 1, "s1", "j1", "pass", twentyNineDaysAgo);
        insertJob("p1", 2, "s1", "j1", "pass", thirtyOneDaysAgo);

        insertStage("p1", 1, "s1", 1, "pass", "p", 1, twentyNineDaysAgo);
        insertStage("p1", 1, "s1", 1, "pass", "p", 1, thirtyOneDaysAgo);

        sqlSession.commit();

        when(localTimeProvider.currentTime()).thenReturn(localTime);

        PluginSettings pluginSettings = PluginSettings.fromJSON(String.format("{\"periodic_cleanup_time\": \"%s\"}", "10:11"));

        dataPurgeScheduler.schedule(pluginSettings);
        Thread.sleep(1000);
        dataPurgeScheduler.shutdown();

        List<AgentUtilization> agentUtilizations = agentUtilizationDAO.all(sqlSession, "agent-1");
        List<Job> jobs = jobDAO.all(sqlSession, "p1");
        List<Stage> stages = stageDAO.all(sqlSession, "p1");

        assertThat(agentUtilizations.size(), is(1));
        assertThat(agentUtilizations.get(0).getUtilizationDate(), is(twentyNineDaysAgo));

        assertThat(jobs.size(), is(1));
        assertThat(jobs.get(0).getScheduledAt(), is(twentyNineDaysAgo));

        assertThat(stages.size(), is(1));
        assertThat(stages.get(0).getScheduledAt(), is(twentyNineDaysAgo));
    }

    private void insertAgent(String uuid, String hostName, boolean isElastic, String ipAddress, String os) {
        Agent agent = agentWith(uuid, hostName, isElastic, ipAddress, os, "10G", "enabled");
        agentDAO.updateOrInsert(sqlSession, agent);
    }

    private void insertAgentUtilization(String uuid, ZonedDateTime utilizationDate, String lastKnownState,
                                        ZonedDateTime transitionTime, int idleDuration, int buildingDuration,
                                        int cancelledDuration, String hostName) {
        AgentUtilization utilization = agentUtilizationWith(uuid, utilizationDate, lastKnownState, transitionTime,
                idleDuration, buildingDuration, cancelledDuration, hostName);
        agentUtilizationDAO.insert(sqlSession, utilization);
    }

    private void insertJob(String pipelineName, int counter, String stageName, String jobName, String result, ZonedDateTime scheduledAt) {
        Job job = jobWith(pipelineName, counter, stageName, jobName, result, scheduledAt);

        jobDAO.insertJob(sqlSession, job);
    }

    private void insertStage(String pipelineName, int pipelineCounter, String stageName, int stageCounter,
                             String result, String state, int duration, ZonedDateTime scheduledAt) {
        Stage stage = stageWith(pipelineName, pipelineCounter, stageName, stageCounter, result, state, duration, scheduledAt);

        stageDAO.insert(sqlSession, stage);
    }
}
