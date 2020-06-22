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

import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.gocd.analytics.CallTrackingExecutor;
import com.thoughtworks.gocd.analytics.exceptions.UnSupportedAnalyticException;
import com.thoughtworks.gocd.analytics.executors.job.JobsHighestWaitTimeExecutor;
import org.junit.Test;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

public class AnalyticsExecutorSelectorTest {
    @Test(expected = UnSupportedAnalyticException.class)
    public void shouldFailIfExecutorIsNotFound() throws Exception {
        AnalyticsExecutorSelector selector = new AnalyticsExecutorSelector();

        selector.registerExecutor("metric-1", CallTrackingExecutor.class);

        selector.executorFor(requestFor("IS-WRONG-METRIC"), null);
    }

    @Test
    public void shouldAllowOverwritingRegisteredExecutor() throws Exception {
        AnalyticsExecutorSelector selector = new AnalyticsExecutorSelector();

        selector.registerExecutor("metric-1", JobsHighestWaitTimeExecutor.class);
        selector.registerExecutor("metric-1", CallTrackingExecutor.class);

        assertTrue(selector.executorFor(requestFor("metric-1"), null).getClass().isAssignableFrom(CallTrackingExecutor.class));
    }

    private GoPluginApiRequest requestFor(String metricID) {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(null, null, null);
        request.setRequestBody(format("{\"id\": \"%s\"}", metricID));
        return request;
    }
}
