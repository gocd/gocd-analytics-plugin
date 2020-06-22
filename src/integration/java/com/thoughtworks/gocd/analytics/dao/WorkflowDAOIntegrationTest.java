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
import com.thoughtworks.gocd.analytics.models.Workflow;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.ZonedDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WorkflowDAOIntegrationTest {
    private SqlSession sqlSession;
    private TestDBConnectionManager manager;
    private WorkflowDAO dao;

    @Before
    public void before() throws SQLException, InterruptedException {
        dao = new WorkflowDAO();
        manager = new TestDBConnectionManager();
        sqlSession = manager.getSqlSession();
    }

    @After
    public void after() throws InterruptedException, SQLException {
        manager.shutdown();
    }

    @Test
    public void shouldInsertAWorkflowAndPopulateTheIdForTheGivenWorkflow() {
        Workflow workflow = new Workflow(ZonedDateTime.now());

        dao.insert(sqlSession, workflow);

        assertThat(workflow.getId(), is(1L));
    }
}
