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

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.analytics.executors.RequestExecutor;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;

import java.util.ArrayList;
import java.util.List;

public class CallTrackingExecutor implements RequestExecutor {
    static List<AnalyticsRequest> requests = new ArrayList<>();

    private final AnalyticsRequest request;

    public CallTrackingExecutor(AnalyticsRequest request, SessionFactory sessionFactory) {
        this.request = request;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        requests.add(request);
        return DefaultGoPluginApiResponse.success("{}");
    }
}
