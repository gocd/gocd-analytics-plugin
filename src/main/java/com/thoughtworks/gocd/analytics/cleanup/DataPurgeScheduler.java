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

package com.thoughtworks.gocd.analytics.cleanup;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.gocd.analytics.db.DBAccess;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import com.thoughtworks.gocd.analytics.utils.LocalTimeProvider;
import com.thoughtworks.gocd.analytics.utils.SystemProperties;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.thoughtworks.gocd.analytics.utils.DateUtils.nowInUTC;

public class DataPurgeScheduler implements Runnable {
    private static DataPurgeScheduler instance = new DataPurgeScheduler();
    private final Logger LOG = Logger.getLoggerFor(DataPurgeScheduler.class);
    private final int SECONDS_IN_A_DAY = 86400;
    private final ScheduledExecutorServiceProvider executorServiceProvider;
    private final LocalTimeProvider localTimeProvider;
    private final DBAccess dbAccess;
    private ScheduledExecutorService scheduledExecutorService;

    private DataPurgeScheduler() {
        this(new ScheduledExecutorServiceProvider(), new LocalTimeProvider(), DBAccess.instance());
    }

    protected DataPurgeScheduler(ScheduledExecutorServiceProvider executorServiceProvider, LocalTimeProvider localTimeProvider, DBAccess dbAccess) {
        this.executorServiceProvider = executorServiceProvider;
        this.localTimeProvider = localTimeProvider;
        this.dbAccess = dbAccess;
    }

    public static DataPurgeScheduler instance() {
        return instance;
    }

    public void schedule(PluginSettings pluginSettings) {
        long initialDelay = initialDelay(pluginSettings.getPeriodicCleanupTime());

        LOG.info("[Data-Purge-Scheduler] Scheduling data purge task every day at '{}', with initial delay of '{}' seconds.", pluginSettings.getPeriodicCleanupTime(), initialDelay);
        scheduledExecutorService = executorServiceProvider.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this, initialDelay, SECONDS_IN_A_DAY, TimeUnit.SECONDS);
    }

    public void reschedule(PluginSettings pluginSettings) {
        LOG.info("[Data-Purge-Scheduler] ReScheduling data purge task on plugin settings change.");
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                if (scheduledExecutorService != null) {
                    shutdown();
                    scheduledExecutorService = null;
                }
                schedule(pluginSettings);
            }
        });
    }

    @Override
    public void run() {
        try {
            LOG.info("[Data-Purge-Scheduler] Scheduling data purge at: {}", LocalTime.now());
            dbAccess.connectIfRequired();
            if (dbAccess.canConnectToDB()) {
                int dataPurgeIntervalInDays = SystemProperties.getAnalyticsDbDataPurgeInterval();
//                int dataPurgeIntervalInDays = new PluginSettings().getDataPurgeInterval();
                new DataPurger(dbAccess.sessionFactory()).purge(nowInUTC().minusDays(dataPurgeIntervalInDays).truncatedTo(ChronoUnit.DAYS));
                LOG.info("[Data-Purge-Scheduler] Done purging data.");
            } else {
                LOG.info("[Data-Purge-Scheduler] Skipping data purge in absence of active db connection.");
            }
        } catch (Throwable e) {
            LOG.error("[Data-Purge-Scheduler] Error purging data.", e);
        }
    }

    public void shutdown() {
        if (scheduledExecutorService == null) {
            return;
        }

        LOG.info("[Data-Purge-Scheduler] Attempting shutdown of DataPurgeScheduler task.");
        try {
            scheduledExecutorService.shutdown();
            scheduledExecutorService.awaitTermination(120, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("[Data-Purge-Scheduler] Error shutting down DataPurgeScheduler.", e);
        } finally {
            if (!scheduledExecutorService.isTerminated()) {
                LOG.info("[Data-Purge-Scheduler] Attempting force shutdown of DataPurgeScheduler task.");
                scheduledExecutorService.shutdownNow();
            }
        }
        LOG.info("[Data-Purge-Scheduler] Done shutdown of DataPurgeScheduler task.");
    }

    private long initialDelay(LocalTime periodicCleanupTime) {
        if (periodicCleanupTime.isAfter(localTimeProvider.currentTime())) {
            return localTimeProvider.currentTime().until(periodicCleanupTime, ChronoUnit.SECONDS);
        }

        long noOfSecondsTillEOD = localTimeProvider.currentTime().until(LocalTime.MAX, ChronoUnit.SECONDS);
        long noOfSecondsFromMidnightToPeriodicCleanupTime = LocalTime.MIDNIGHT.until(periodicCleanupTime, ChronoUnit.SECONDS);

        return noOfSecondsTillEOD + noOfSecondsFromMidnightToPeriodicCleanupTime;
    }
}
