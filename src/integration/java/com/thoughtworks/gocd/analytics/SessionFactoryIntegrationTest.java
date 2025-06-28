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

package com.thoughtworks.gocd.analytics;

import com.thoughtworks.gocd.analytics.db.PostgresqlDatabase;
import com.thoughtworks.gocd.analytics.mapper.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static com.thoughtworks.gocd.analytics.TestDBConnectionManager.connectionSettings;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionFactoryIntegrationTest {
    private PostgresqlDatabase postgresqlDatabase;
    private SqlSession sqlSession;

    @BeforeEach
    public void before() throws InterruptedException, SQLException {
        postgresqlDatabase = new PostgresqlDatabase(connectionSettings());
        postgresqlDatabase.connect();

        SessionFactory sessionFactory = new SessionFactory();
        sessionFactory.initialize(postgresqlDatabase.dataSource());
        sqlSession = sessionFactory.openSession();
    }

    @AfterEach
    public void after() throws SQLException, InterruptedException {
        sqlSession.close();
        postgresqlDatabase.tryClean();
        postgresqlDatabase.close();
    }

    @Test
    public void shouldRegisterAllMappers() {
        Collection<Class<?>> expected = new ArrayList<>();
        expected.add(JobMapper.class);
        expected.add(StageMapper.class);
        expected.add(PipelineMapper.class);
        expected.add(AgentMapper.class);
        expected.add(AgentUtilizationMapper.class);
        expected.add(AgentTransitionsMapper.class);
        expected.add(MaterialRevisionMapper.class);
        expected.add(WorkflowMapper.class);
        expected.add(PipelineWorkflowMapper.class);

        Collection<Class<?>> actual = sqlSession.getConfiguration().getMapperRegistry().getMappers();
        assertTrue(expected.containsAll(actual));
    }

    @Test
    public void shouldGiveBackAValidOpenSqlSession() throws SQLException {
        assertTrue(sqlSession.getConnection().isValid(0));
    }
}
