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
import com.thoughtworks.gocd.analytics.models.PluginSettings;

import java.sql.SQLException;


public class DBAccess {
    private static DBAccess dbAccess = new DBAccess();
    private PostgresqlDatabase postgresqlDatabase;
    private SessionFactory sessionFactory;
    private PluginSettings settings;

    public DBAccess() {
    }

    public static DBAccess instance() {
        return dbAccess;
    }

    public DBAccess initialize(PluginSettings connectionSettings) throws SQLException, InterruptedException {
        this.settings = connectionSettings;
        this.postgresqlDatabase = new PostgresqlDatabase(this.settings);
        this.postgresqlDatabase.connect();

        this.sessionFactory = new SessionFactory();
        this.sessionFactory.initialize(postgresqlDatabase.dataSource());
        return this;
    }

    public DBAccess reinitialize(PluginSettings connectionSettings) throws SQLException, InterruptedException {
        close();

        return initialize(connectionSettings);
    }

    public void close() throws SQLException {
        if (this.postgresqlDatabase != null) {
            this.postgresqlDatabase.close();
        }

        this.settings = null;
        this.postgresqlDatabase = null;
        this.sessionFactory = null;
    }

    public void connectIfRequired() throws SQLException, InterruptedException {
        if (settings != null && !canConnectToDB()) {
            initialize(settings);
        }
    }

    public Database database() {
        return this.postgresqlDatabase;
    }

    public SessionFactory sessionFactory() {
        return this.sessionFactory;
    }

    public boolean canConnectToDB() {
        return this.postgresqlDatabase != null && this.sessionFactory != null;
    }
}
