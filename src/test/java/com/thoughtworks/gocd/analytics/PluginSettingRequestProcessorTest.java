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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.gocd.analytics.exceptions.ServerRequestFailedException;
import com.thoughtworks.gocd.analytics.models.PluginSettings;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginSettingRequestProcessorTest {
    @Test
    public void shouldFetchPluginSettingsFromServer() throws ServerRequestFailedException {
        GoApplicationAccessor accessor = mock(GoApplicationAccessor.class);

        ArgumentCaptor<DefaultGoApiRequest> requestArgumentCaptor = ArgumentCaptor.forClass(DefaultGoApiRequest.class);

        when(accessor.submit(requestArgumentCaptor.capture())).thenReturn(DefaultGoApiResponse.success("{\n" +
                "  \"host\": \"localhost\",\n" +
                "  \"username\": \"postgres\",\n" +
                "  \"name\": \"test\"" +
                "}"));

        PluginSettingRequestProcessor processor = new PluginSettingRequestProcessor(accessor);

        PluginSettings pluginSettings = processor.getPluginSettings();

        DefaultGoApiRequest request = requestArgumentCaptor.getValue();
        assertThat(request.api(), is("go.processor.plugin-settings.get"));
        assertThat(request.apiVersion(), is("1.0"));

        assertThat(pluginSettings.getDbHost(), is("localhost"));
        assertThat(pluginSettings.getDbUsername(), is("postgres"));
        assertThat(pluginSettings.getDbName(), is("test"));
    }
}
