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


import com.thoughtworks.gocd.analytics.cleanup.DataPurgeScheduler;
import com.thoughtworks.gocd.analytics.db.DBAccess;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import com.thoughtworks.gocd.analytics.pluginhealth.PluginHealthMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BootstrapTest {

    private DBAccess dbAccess;
    private DataPurgeScheduler dataPurgeScheduler;
    private PluginSettingRequestProcessor pluginSettingRequestProcessor;
    private PluginHealthMessageService pluginHealthMessageService;
    private Initializable plugin;
    private Bootstrap bootstrap;

    @BeforeEach
    public void setUp() {
        dbAccess = mock(DBAccess.class);
        dataPurgeScheduler = mock(DataPurgeScheduler.class);
        pluginSettingRequestProcessor = mock(PluginSettingRequestProcessor.class);
        pluginHealthMessageService = mock(PluginHealthMessageService.class);
        plugin = plugin();
        bootstrap = Bootstrap.instance().updateOnlyFromTests_DoNotUseThisInProduction(dbAccess, dataPurgeScheduler);
    }

    @Test
    public void ensure_shouldInitializeDBAccess() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(pluginSettingRequestProcessor.getPluginSettings()).thenReturn(pluginSettings);
        when(pluginSettings.isConfigured()).thenReturn(true);

        bootstrap.ensure(plugin);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dbAccess).reinitialize(pluginSettings);
    }

    @Test
    public void ensure_shouldInitializeDataPurgeScheduler() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(pluginSettingRequestProcessor.getPluginSettings()).thenReturn(pluginSettings);
        when(pluginSettings.isConfigured()).thenReturn(true);

        bootstrap.ensure(plugin);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dataPurgeScheduler).reschedule(pluginSettings);
    }

    @Test
    public void ensure_shouldIgnoreInitializationRequestsIfPluginSettingsIsLoaded() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(pluginSettingRequestProcessor.getPluginSettings()).thenReturn(pluginSettings);
        when(pluginSettings.isConfigured()).thenReturn(true);

        bootstrap.ensure(plugin);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dbAccess, times(1)).reinitialize(pluginSettings);
        verify(dataPurgeScheduler, times(1)).reschedule(pluginSettings);
    }

    @Test
    public void ensure_shouldRetryInitialzationIfPluginSettingsIsNotLoaded() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(pluginSettingRequestProcessor.getPluginSettings()).thenThrow(new RuntimeException("failed")).thenReturn(pluginSettings);
        when(pluginSettings.isConfigured()).thenReturn(true);

        try {
            bootstrap.ensure(plugin);
        } catch (Exception ignored) {
        }

        bootstrap.ensure(plugin);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dbAccess, times(1)).reinitialize(pluginSettings);
        verify(dataPurgeScheduler, times(1)).reschedule(pluginSettings);
    }

    @Test
    public void ensure_shouldScheduleDataPurgerEvenIfInitializingDBAccessFails() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(pluginSettingRequestProcessor.getPluginSettings()).thenReturn(pluginSettings);
        when(pluginSettings.isConfigured()).thenReturn(true);
        when(dbAccess.initialize(pluginSettings)).thenThrow(new RuntimeException());

        bootstrap.ensure(plugin, pluginSettings);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dbAccess).reinitialize(pluginSettings);
        verify(dataPurgeScheduler).reschedule(pluginSettings);
    }

    @Test
    public void ensure_with_settings_shouldInitializeDBAccess() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(pluginSettingRequestProcessor.getPluginSettings()).thenReturn(pluginSettings);

        bootstrap.ensure(plugin, pluginSettings);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dbAccess).reinitialize(pluginSettings);
    }

    @Test
    public void ensure_with_settings_shouldInitializeDataPurgeScheduler() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(pluginSettings.isConfigured()).thenReturn(true);

        bootstrap.ensure(plugin, pluginSettings);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dataPurgeScheduler).reschedule(pluginSettings);
    }

    @Test
    public void ensure_with_settings_shouldScheduleDataPurgerEvenIfInitializingDBAccessFails() throws Exception {
        PluginSettings pluginSettings = mock(PluginSettings.class);

        when(dbAccess.initialize(pluginSettings)).thenThrow(new RuntimeException());

        bootstrap.ensure(plugin, pluginSettings);

        assertEquals(pluginSettings, bootstrap.settings());
        verify(dbAccess).reinitialize(pluginSettings);
        verify(dataPurgeScheduler).reschedule(pluginSettings);
    }

    private Initializable plugin() {
        return new Initializable() {
            @Override
            public PluginSettingRequestProcessor request() {
                return pluginSettingRequestProcessor;
            }

            @Override
            public PluginHealthMessageService healthService() {
                return pluginHealthMessageService;
            }
        };
    }
}
