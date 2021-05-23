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


import com.thoughtworks.gocd.analytics.db.DBAccess;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import com.thoughtworks.gocd.analytics.utils.LocalTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class DataPurgeSchedulerTest {

    private ScheduledExecutorServiceProvider executorServiceFactory;
    private ScheduledExecutorService scheduledExecutorService;
    private LocalTimeProvider localTimeProvider;
    private DBAccess dbAccess;
    private DataPurgeScheduler dataPurgeScheduler;

    @BeforeEach
    public void setUp() throws Exception {
        executorServiceFactory = mock(ScheduledExecutorServiceProvider.class);
        scheduledExecutorService = mock(ScheduledExecutorService.class);
        localTimeProvider = mock(LocalTimeProvider.class);
        dbAccess = mock(DBAccess.class);

        dataPurgeScheduler = new DataPurgeScheduler(executorServiceFactory, localTimeProvider, dbAccess);
    }

    @Test
    public void shouldScheduleASingleThreadForPurgingDataAtAFixedTime() throws Exception {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{\"periodic_cleanup_time\": \"10:00\"}");
        LocalTime localTime = timeAt("09:00");

        when(executorServiceFactory.newSingleThreadScheduledExecutor()).thenReturn(scheduledExecutorService);
        when(localTimeProvider.currentTime()).thenReturn(localTime);
        when(dbAccess.canConnectToDB()).thenReturn(true);

        dataPurgeScheduler.schedule(pluginSettings);

        verify(scheduledExecutorService).scheduleAtFixedRate(dataPurgeScheduler, 3600L, 86400L, TimeUnit.SECONDS);
    }

    @Test
    public void shouldScheduleTheFirstCleanupTaskToTomorrowIfPeriodicCleanupTimeIsBeforeCurrentLocalTime() throws Exception {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{\"periodic_cleanup_time\": \"10:00\"}");
        LocalTime localTime = timeAt("11:00");

        when(executorServiceFactory.newSingleThreadScheduledExecutor()).thenReturn(scheduledExecutorService);
        when(localTimeProvider.currentTime()).thenReturn(localTime);
        when(dbAccess.canConnectToDB()).thenReturn(true);

        dataPurgeScheduler.schedule(pluginSettings);

        verify(scheduledExecutorService).scheduleAtFixedRate(dataPurgeScheduler, 82799L, 86400L, TimeUnit.SECONDS);
    }

    @Test
    public void reschedule_shouldShutdownExistingTasksAndReschedule() throws InterruptedException {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{\"periodic_cleanup_time\": \"10:00\"}");
        LocalTime localTime = timeAt("09:00");

        when(executorServiceFactory.newSingleThreadScheduledExecutor()).thenReturn(scheduledExecutorService);
        when(localTimeProvider.currentTime()).thenReturn(localTime);
        when(dbAccess.canConnectToDB()).thenReturn(true);

        dataPurgeScheduler.schedule(pluginSettings);
        dataPurgeScheduler.reschedule(pluginSettings);
        Thread.sleep(1000);

        verify(scheduledExecutorService).shutdown();
        verify(scheduledExecutorService, times(2)).scheduleAtFixedRate(dataPurgeScheduler, 3600L, 86400L, TimeUnit.SECONDS);
    }

    @Test
    public void shutdown_shouldStopExistingTasks() throws Exception {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{\"periodic_cleanup_time\": \"10:00\"}");
        LocalTime localTime = timeAt("09:00");

        when(executorServiceFactory.newSingleThreadScheduledExecutor()).thenReturn(scheduledExecutorService);
        when(localTimeProvider.currentTime()).thenReturn(localTime);
        when(dbAccess.canConnectToDB()).thenReturn(true);

        dataPurgeScheduler.schedule(pluginSettings);
        dataPurgeScheduler.shutdown();

        Thread.sleep(1000);

        verify(scheduledExecutorService).shutdown();
        verify(scheduledExecutorService).scheduleAtFixedRate(dataPurgeScheduler, 3600L, 86400L, TimeUnit.SECONDS);
    }

    @Test
    public void run_shouldTryToReConnectToDBIfRequired() throws Exception {
        dataPurgeScheduler.run();

        verify(dbAccess).connectIfRequired();
    }

    @Test
    public void run_shouldSkipDataPurgeInAbsenceOfDbConnection() throws Exception {
        when(dbAccess.canConnectToDB()).thenReturn(false);

        dataPurgeScheduler.run();

        verify(dbAccess, times(0)).sessionFactory();
    }

    private LocalTime timeAt(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
