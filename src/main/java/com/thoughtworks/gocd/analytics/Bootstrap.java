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

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.cleanup.DataPurgeScheduler;
import com.thoughtworks.gocd.analytics.db.DBAccess;
import com.thoughtworks.gocd.analytics.exceptions.ServerRequestFailedException;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessage;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthScope;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthState;

import java.sql.SQLException;

import static java.text.MessageFormat.format;

public class Bootstrap {
    private static final Logger LOG = Logger.getLoggerFor(Bootstrap.class);
    private static Bootstrap instance = new Bootstrap();

    private PluginSettings settings = null;
    private DBAccess dbAccess = DBAccess.instance();
    private DataPurgeScheduler scheduler = DataPurgeScheduler.instance();

    private Bootstrap() {
    }

    public static Bootstrap instance() {
        return instance;
    }

    public SessionFactory sessionFactory(Initializable plugin) {
        if (!dbaccess().canConnectToDB()) {
            LOG.info("No DB connection present; bootstrapping...");

            try {
                if (!ensure(plugin)) {
                    return null;
                }
                LOG.info("DB connection established.");
            } catch (Exception e) {
                LOG.error("Failed to establish DB connection.", e);
                return null;
            }
        }

        return dbaccess().sessionFactory();
    }

    boolean ensure(Initializable plugin, PluginSettings newSettings) throws InterruptedException, ServerRequestFailedException, SQLException {
        updateSettings(newSettings);
        return ensure(plugin);
    }

    boolean ensure(Initializable plugin) throws SQLException, InterruptedException, ServerRequestFailedException {
        LOG.info("Bootstrapping...");
        plugin.healthService().removeByScope(PluginHealthScope.forPluginSettings());

        if (null == settings()) {
            LOG.info("Plugin settings not yet loaded.");
            loadSettings(plugin);

            if (null == settings()) {
                LOG.error("Failed to bootstrap because plugin is not configured.");
                return false;
            }
        }

        try {
            LOG.info("Initializing DB connection...");
            dbaccess().reinitialize(settings());
            LOG.info("Done.");

            LOG.info("Starting scheduled services...");
            purgeScheduler().reschedule(settings());
            LOG.info("Done.");

            LOG.info("Bootstrap successful.");
            return true;
        } catch (Exception e) {
            final String errorMessage = format("Error bootstrapping; reason: {0}", e.getMessage());

            LOG.error(errorMessage);
            plugin.healthService().update(new PluginHealthState(PluginHealthMessage.error(errorMessage), PluginHealthScope.forPluginSettings()));

            throw e;
        }
    }

    private void loadSettings(Initializable plugin) throws ServerRequestFailedException {
        try {
            LOG.info("Loading plugin settings...");
            PluginSettings settings = plugin.request().getPluginSettings();

            if (null != settings && settings.isConfigured()) {
                updateSettings(settings);
                LOG.info("Plugin settings loaded.");
                return;
            }

            final String message = "Plugin settings not configured; cannot initialize Analytics plugin. " +
                    "All Stage and Agent Status notifications will be ignored until the plugin is configured.";

            LOG.warn(message);
            plugin.healthService().update(new PluginHealthState(PluginHealthMessage.warn(message), PluginHealthScope.forPluginSettings()));
        } catch (Exception e) {
            final String errorMessage = format("Error retrieving plugin settings; reason: {0}", e.getMessage());

            LOG.error(errorMessage);
            plugin.healthService().update(new PluginHealthState(PluginHealthMessage.error(errorMessage), PluginHealthScope.forPluginSettings()));

            throw e;
        }
    }

    void teardown() throws SQLException {
        LOG.info("Tearing down scheduled services...");
        purgeScheduler().shutdown();
        LOG.info("Done.");

        LOG.info("Tearing down DB connection");
        dbaccess().close();
        LOG.info("Done.");
    }

    public PluginSettings settings() {
        return settings;
    }

    private void updateSettings(PluginSettings settings) {
        this.settings = settings;
    }

    private DBAccess dbaccess() {
        return dbAccess;
    }

    private DataPurgeScheduler purgeScheduler() {
        return scheduler;
    }

    Bootstrap updateOnlyFromTests_DoNotUseThisInProduction(DBAccess dbAccess, DataPurgeScheduler scheduler) {
        this.dbAccess = dbAccess;
        this.scheduler = scheduler;
        updateSettings(null);
        return this;
    }
}
