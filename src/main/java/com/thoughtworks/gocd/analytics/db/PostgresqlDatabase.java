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

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.TestConnectionResult;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

public class PostgresqlDatabase implements Database {
    private static final Logger LOG = Logger.getLoggerFor(PostgresqlDatabase.class);
    private BasicDataSource basicDataSource;
    private PluginSettings pluginSettings;

    public PostgresqlDatabase() {
    }

    public PostgresqlDatabase(PluginSettings pluginSettings) {
        this.pluginSettings = pluginSettings;
    }

    @Override
    public void connect() throws InterruptedException, SQLException {
        Connection connection = null;
        this.basicDataSource = dataSource();

        try {
            connection = this.basicDataSource.getConnection();
            migrate();
        } catch (SQLException e) {
            LOG.error("Error establishing connection with the database", e);
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.error("Error closing connection", e);
                }
            }
        }
        LOG.info("Database connection successful established.");
    }

    @Override
    public void close() throws SQLException {
        LOG.info("Closing connection pool.");
        try {
            basicDataSource.close();
            LOG.info("Done Closing connection pool.");
        } catch (SQLException e) {
            LOG.error("Error closing connection pool", e);
        }
    }

    @Override
    public void migrate() throws InterruptedException {
        // We're creating a new thread here because Flyway's gets its ClassLoader from the current thread
        // which will end up being the go server's class loader. So, we're encapsulating Flyway in a new thread
        // and setting the class loader to the plugin's class loader
        // This is important because it allows Flyway to see the migrations within the plugin. Without the classloader
        // being properly set, it'll look in go's configuration directory.
        Thread thread = new Thread(() -> {
            LOG.info("Applying migrations ...");
            Flyway flyway = new Flyway();
            flyway.setDataSource(basicDataSource);
            flyway.migrate();
            LOG.info("Done Applying migrations ...");
        });
        final Throwable[] migrationExceptions = {null};
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                migrationExceptions[0] = ex;
            }
        });
        thread.setContextClassLoader(this.getClass().getClassLoader());
        thread.start();
        thread.join();
        if (migrationExceptions[0] != null) {
            throw new RuntimeException(migrationExceptions[0]);
        }
    }

    @Override
    public void clean() throws InterruptedException {
        // We're creating a new thread here because Flyway's gets its ClassLoader from the current thread
        // which will end up being the go server's class loader. So, we're encapsulating Flyway in a new thread
        // and setting the class loader to the plugin's class loader
        // This is important because it allows Flyway to see the migrations within the plugin. Without the classloader
        // being properly set, it'll look in go's configuration directory.
        Thread thread = new Thread(() -> {
            LOG.info("Applying migrations ...");
            Flyway flyway = new Flyway();
            flyway.setDataSource(basicDataSource);
            flyway.clean();
            LOG.info("Done Applying migrations ...");
        });
        thread.setContextClassLoader(this.getClass().getClassLoader());
        thread.start();
        thread.join();
    }

    @Override
    public TestConnectionResult testConnection(PluginSettings pluginSettings) {
        try {
            this.pluginSettings = pluginSettings;
            connect();
        } catch (Exception e) {
            String errorMessage = String.format("Error connecting to database, reason - %s", e.getCause().getMessage());
            return new TestConnectionResult(false, errorMessage);
        }

        return TestConnectionResult.SUCCESS;
    }

    @Override
    public BasicDataSource dataSource() {
        if (this.basicDataSource == null) {
            basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName("org.postgresql.Driver");
            String databaseUrl = String.format("jdbc:postgresql://%s:%s/%s", pluginSettings.getDbHost(), pluginSettings.getDbPort(), pluginSettings.getDbName());
            basicDataSource.setUrl(databaseUrl);
            basicDataSource.setUsername(pluginSettings.getDbUsername());
            basicDataSource.setPassword(pluginSettings.getDbPassword());
            basicDataSource.setMaxTotal(pluginSettings.getMaxConnectionsActive());
            basicDataSource.setMaxIdle(pluginSettings.getMaxConnectionsIdle());
            basicDataSource.setMaxWait(Duration.ofMillis(pluginSettings.getMaxConnectionWaitTime()));
            basicDataSource.setDefaultAutoCommit(false);
            basicDataSource.setConnectionProperties(String.format("stringtype=unspecified;%s", sslConfig()));
        }

        return basicDataSource;
    }

    public String sslConfig() {
        return pluginSettings.useSsl() ? String.format("ssl=true;sslmode=%s;sslrootcert=%s;sslcert=%s;sslkey=%s;",
                pluginSettings.getSslMode(), pluginSettings.getRootCert(), pluginSettings.getClientCert(), pluginSettings.getClientPKCS8Key()) : "";
    }

    @Override
    public void backupDb() {
//TBD
    }
}
