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

package com.thoughtworks.gocd.analytics.pluginhealth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;

import java.util.Collection;
import java.util.Collections;

public class PluginHealthMessageNotifier {
    public static final Logger LOG = Logger.getLoggerFor(PluginHealthMessageNotifier.class);
    private final GoApplicationAccessor accessor;
    private final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public PluginHealthMessageNotifier(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    public void notify(Collection<PluginHealthMessage> messages) {
        DefaultGoApiRequest request = new DefaultGoApiRequest("go.processor.server-health.add-messages", "1.0",
                new GoPluginIdentifier("analytics", Collections.singletonList("1.0")));
        request.setRequestBody(GSON.toJson(messages));
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            LOG.error("Error notifying plugin health message to GoCD reason: {}", response.responseBody());
        }
    }
}
