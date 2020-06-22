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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PostgresqlDatabaseTest {
    private PostgresqlDatabase psql;

    @Before
    public void before() {
        psql = new PostgresqlDatabase(TestDBConnectionManager.connectionSettings());
    }

    @After
    public void after() throws SQLException, InterruptedException {
        psql.clean();
        psql.close();
    }

    @Test
    public void testConnectionShouldBeSuccessFulWithValidConfig() {
        TestConnectionResult result = psql.testConnection(TestDBConnectionManager.connectionSettings());
        assertThat(result.isSuccessful(), is(true));
    }

    @Test
    public void testConnectionShouldNotBeSuccessfulWithBadConfig() {
        final PluginSettings badConfig = PluginSettings.fromJSON("{\"host\": \"localhost\", \"port\": \"5432\", \"username\": \"?\", \"password\": \".\"}");
        TestConnectionResult result = psql.testConnection(badConfig);
        assertThat(result.isSuccessful(), is(false));
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
        assertThat(dao.all(sqlSession, "test").size(), is(1));
        sqlSession.close();
    }
}
