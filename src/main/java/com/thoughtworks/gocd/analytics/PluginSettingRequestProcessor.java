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
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.gocd.analytics.exceptions.ServerRequestFailedException;
import com.thoughtworks.gocd.analytics.models.PluginSettings;

import static com.thoughtworks.gocd.analytics.PluginConstants.ANALYTICS_PLUGIN_IDENTIFIER;
import static com.thoughtworks.gocd.analytics.PluginConstants.PLUGIN_SETTINGS_API_VERSION;

public class PluginSettingRequestProcessor {
    private final GoApplicationAccessor accessor;

    public PluginSettingRequestProcessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    public PluginSettings getPluginSettings() throws ServerRequestFailedException {
        DefaultGoApiRequest request = new DefaultGoApiRequest(PluginConstants.REQUEST_SERVER_GET_PLUGIN_SETTINGS, PLUGIN_SETTINGS_API_VERSION, ANALYTICS_PLUGIN_IDENTIFIER);
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.getPluginSettings(response);
        }

        return PluginSettings.fromJSON(response.responseBody());
    }
}
