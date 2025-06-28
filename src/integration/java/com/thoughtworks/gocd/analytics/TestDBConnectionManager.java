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

import com.thoughtworks.gocd.analytics.db.DBAccess;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;

public class TestDBConnectionManager {
    private static final String DB_HOST = System.getProperty("db.host", "localhost");
    private static final String DB_USER = System.getProperty("db.user", System.getenv("USER"));
    private static final String DB_PASSWORD = System.getProperty("db.password", "");
    private static final String DB_PORT = System.getProperty("db.port", "5432");
    private static final String DB_NAME = System.getProperty("db.name", "gocd_test");
    private static final PluginSettings pluginSettings = new PluginSettings(
            DB_HOST,
            DB_PORT,
            DB_USER,
            DB_PASSWORD,
            DB_NAME);
    private final SqlSession sqlSession;
    private final DBAccess db;
    private final SessionFactory sessionFactory;

    public TestDBConnectionManager() throws SQLException, InterruptedException {
        db = DBAccess.instance().initialize(pluginSettings);
        db.database().migrate();
        sessionFactory = db.sessionFactory();
        sqlSession = sessionFactory.openSession();
    }

    public static PluginSettings connectionSettings() {
        return pluginSettings;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public SqlSession getSqlSession() {
        return sqlSession;
    }

    public void shutdown() throws SQLException, InterruptedException {
        sqlSession.close();
        db.database().tryClean();
        db.database().close();
    }
}
