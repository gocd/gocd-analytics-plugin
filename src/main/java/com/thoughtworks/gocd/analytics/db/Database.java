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

import com.thoughtworks.gocd.analytics.TestConnectionResult;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLException;

public interface Database {
    void connect() throws InterruptedException, SQLException;

    void migrate() throws SQLException, InterruptedException;

    void close() throws SQLException;

    void backupDb();

    BasicDataSource dataSource();

    void tryClean() throws InterruptedException;

    TestConnectionResult testConnection(PluginSettings pluginSettings);
}
