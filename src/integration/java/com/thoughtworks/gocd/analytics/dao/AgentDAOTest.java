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
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static com.thoughtworks.gocd.analytics.AgentMother.agentWith;
import static org.junit.jupiter.api.Assertions.*;

public class AgentDAOTest implements DAOIntegrationTest {
    private SqlSession sqlSession;
    private TestDBConnectionManager manager;
    private AgentDAO agentDAO;

    @BeforeEach
    public void before() throws SQLException, InterruptedException {
        agentDAO = new AgentDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
    }

    @AfterEach
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldInsertAgent() {
        Agent agent = agentWith("uuid", "host_name", true, "127.0.0.1", "rh", "100G", ENABLED);

        agentDAO.updateOrInsert(sqlSession, agent);

        Agent agentFromDb = agentDAO.findByUuid(sqlSession, "uuid");

        assertEquals("host_name", agentFromDb.getHostName());
        assertTrue(agentFromDb.isElastic());
        assertEquals("127.0.0.1", agentFromDb.getIpAddress());
        assertEquals("rh", agentFromDb.getOperatingSystem());
        assertEquals("100G", agentFromDb.getFreeSpace());
        assertEquals(ENABLED, agentFromDb.getConfigState());
    }

    @Test
    public void shouldUpdateAnExistingAgent() {
        Agent agent = agentWith("uuid", "host_name", true, "127.0.0.1", "rh", "100G", ENABLED);

        Agent updatedAgent = agentWith("uuid", "new_host_name", false, "127.1.1.1", "new_rh", "new_100G", DISABLED);

        assertNull(agentDAO.findByUuid(sqlSession, "uuid"));

        agentDAO.updateOrInsert(sqlSession, agent);

        agentDAO.updateOrInsert(sqlSession, updatedAgent);

        Agent agentFromDb = agentDAO.findByUuid(sqlSession, "uuid");
        assertEquals("new_host_name", agentFromDb.getHostName());
        assertFalse(agentFromDb.isElastic());
        assertEquals("127.1.1.1", agentFromDb.getIpAddress());
        assertEquals("new_rh", agentFromDb.getOperatingSystem());
        assertEquals("new_100G", agentFromDb.getFreeSpace());
        assertEquals(DISABLED, agentFromDb.getConfigState());
    }
}
