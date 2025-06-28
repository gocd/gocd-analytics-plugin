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

package com.thoughtworks.gocd.analytics.db;

import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.TestConnectionResult;
import com.thoughtworks.gocd.analytics.TestDBConnectionManager;
import com.thoughtworks.gocd.analytics.dao.StageDAO;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import com.thoughtworks.gocd.analytics.models.Stage;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostgresqlDatabaseTest {
    private PostgresqlDatabase psql;

    @BeforeEach
    public void before() {
        psql = new PostgresqlDatabase(TestDBConnectionManager.connectionSettings());
    }

    @AfterEach
    public void after() throws SQLException, InterruptedException {
        psql.tryClean();
        psql.close();
    }

    @Test
    public void testConnectionShouldBeSuccessFulWithValidConfig() {
        TestConnectionResult result = psql.testConnection(TestDBConnectionManager.connectionSettings());
        assertEquals(true, result.isSuccessful());
    }

    @Test
    public void testConnectionShouldNotBeSuccessfulWithBadConfig() {
        final PluginSettings badConfig = PluginSettings.fromJSON("{\"host\": \"localhost\", \"port\": \"5432\", \"username\": \"?\", \"password\": \".\"}");
        TestConnectionResult result = psql.testConnection(badConfig);
        assertEquals(false, result.isSuccessful());
    }

    @Test
    public void connectShouldMigrateDB() throws InterruptedException, SQLException {
        psql.connect();
        SessionFactory sessionFactory = new SessionFactory();
        sessionFactory.initialize(psql.dataSource());
        StageDAO dao = new StageDAO();
        SqlSession sqlSession = sessionFactory.openSession();
        Stage stage = new Stage("test", 1);
        stage.setPipelineName("test");
        dao.insert(sqlSession, stage);
        assertEquals(1, dao.all(sqlSession, "test").size());
        sqlSession.close();
    }
}
