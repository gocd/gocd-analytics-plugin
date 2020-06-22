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

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.gocd.analytics.AnalyticTypes;
import com.thoughtworks.gocd.analytics.SessionFactory;
import com.thoughtworks.gocd.analytics.exceptions.UnSupportedAnalyticException;
import com.thoughtworks.gocd.analytics.models.AnalyticsRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

public class AnalyticsExecutorSelector implements AnalyticTypes {
    private static final AnalyticsExecutorSelector analyticsExecutorSelector = new AnalyticsExecutorSelector();

    private Map<String, Class<? extends RequestExecutor>> executors = new HashMap<>();

    public static AnalyticsExecutorSelector instance() {
        return analyticsExecutorSelector;
    }

    public RequestExecutor executorFor(GoPluginApiRequest request, SessionFactory sessionFactory) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        AnalyticsRequest analyticsRequest = AnalyticsRequest.fromJSON(request.requestBody());

        return resolveExecutor(analyticsRequest).getConstructor(AnalyticsRequest.class, SessionFactory.class).newInstance(analyticsRequest, sessionFactory);
    }

    public AnalyticsExecutorSelector registerExecutor(String metricId, Class<? extends RequestExecutor> executor) {
        executors.put(metricId, executor);
        return this;
    }

    private Class<? extends RequestExecutor> resolveExecutor(AnalyticsRequest analyticsRequest) {
        String metricId = analyticsRequest.getId();

        if (!executors.containsKey(metricId)) {
            throw new UnSupportedAnalyticException(format("No available analytics for metric id: %s", metricId));
        }

        return Objects.requireNonNull(executors.get(metricId), format("No executor found for metric id: %s", metricId));
    }
}
