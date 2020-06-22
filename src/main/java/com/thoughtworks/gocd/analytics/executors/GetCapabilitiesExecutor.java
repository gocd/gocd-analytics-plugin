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

package com.thoughtworks.gocd.analytics.executors;

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.AvailableAnalytics;
import com.thoughtworks.gocd.analytics.models.Capabilities;
import com.thoughtworks.gocd.analytics.models.SupportedAnalytics;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse.SUCCESS_RESPONSE_CODE;

public class GetCapabilitiesExecutor implements RequestExecutor {
    public GoPluginApiResponse execute() {
        Capabilities capabilities = getCapabilities();

        return new DefaultGoPluginApiResponse(SUCCESS_RESPONSE_CODE, capabilities.toJSON());
    }

    Capabilities getCapabilities() {
        return new Capabilities(supportedAnalytics());
    }

    private List<SupportedAnalytics> supportedAnalytics() {
        final AvailableAnalytics[] values = AvailableAnalytics.class.getEnumConstants();

        return Arrays.stream(values).
                map(SupportedAnalytics::new).
                collect(Collectors.toList());
    }
}
