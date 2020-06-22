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


import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.dao.AgentTransitionDAO;
import com.thoughtworks.gocd.analytics.dao.AgentUtilizationDAO;
import com.thoughtworks.gocd.analytics.dao.JobDAO;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

public class DataPurgerTest {
    private JobDAO jobDAO;
    private StageDAO stageDAO;
    private AgentUtilizationDAO agentUtilizationDAO;
    private AgentTransitionDAO agentTransitionDAO;
    private SessionFactory sessionFactory;
    private DataPurger dataPurger;

    @Before
    public void setUp() throws Exception {
        jobDAO = mock(JobDAO.class);
        stageDAO = mock(StageDAO.class);
        agentUtilizationDAO = mock(AgentUtilizationDAO.class);
        agentTransitionDAO = mock(AgentTransitionDAO.class);
        sessionFactory = mock(SessionFactory.class);
        dataPurger = new DataPurger(jobDAO, stageDAO, agentUtilizationDAO, agentTransitionDAO, sessionFactory);
    }

    @Test
    public void shouldPurgeJobRunsBeforeAGivenRetentionDate() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        SqlSession sqlSession = mock(SqlSession.class);

        when(sessionFactory.openSession()).thenReturn(sqlSession);

        dataPurger.purge(now);

        verify(jobDAO).deleteJobRunsPriorTo(sqlSession, now);
    }

    @Test
    public void shouldPurgeStageRunsBeforeTAGivenRetentionDate() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        SqlSession sqlSession = mock(SqlSession.class);

        when(sessionFactory.openSession()).thenReturn(sqlSession);

        dataPurger.purge(now);

        verify(stageDAO).deleteStageRunsPriorTo(sqlSession, now);
    }

    @Test
    public void shouldPurgeAgentUtilizationBeforeAGivenRetentionDate() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        SqlSession sqlSession = mock(SqlSession.class);

        when(sessionFactory.openSession()).thenReturn(sqlSession);

        dataPurger.purge(now);

        verify(agentUtilizationDAO).deleteUtilizationPriorTo(sqlSession, now);
    }

    @Test
    public void shouldPurgeAgentTransitionsBeforeAGivenRetentionDate() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        SqlSession sqlSession = mock(SqlSession.class);

        when(sessionFactory.openSession()).thenReturn(sqlSession);

        dataPurger.purge(now);

        verify(agentTransitionDAO).deleteTransitionsPriorTo(sqlSession, now);
    }
}
