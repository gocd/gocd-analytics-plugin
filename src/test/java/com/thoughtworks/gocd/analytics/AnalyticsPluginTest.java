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
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.gocd.analytics.cleanup.DataPurgeScheduler;
import com.thoughtworks.gocd.analytics.db.DBAccess;
import com.thoughtworks.gocd.analytics.executors.AnalyticsExecutorSelector;
import com.thoughtworks.gocd.analytics.executors.RequestExecutor;
import com.thoughtworks.gocd.analytics.executors.agent.AgentStateTransitionExecutor;
import com.thoughtworks.gocd.analytics.executors.agent.AgentsWithHighestUtilizationExecutor;
import com.thoughtworks.gocd.analytics.executors.job.JobsHighestWaitTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PipelineBuildTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.pipeline.PipelinesHighestWaitTimeExecutor;
import com.thoughtworks.gocd.analytics.executors.vsm.VSMTrendAcrossMultipleRunsExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class AnalyticsPluginTest {
    private AnalyticsPlugin plugin;
    @Mock
    private DBAccess dbAccess;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        CallTrackingExecutor.requests.clear();

        GoApplicationAccessor accessor = mock(GoApplicationAccessor.class);
        when(accessor.submit(any(GoApiRequest.class))).thenReturn(DefaultGoApiResponse.success("ok"));

        plugin = new AnalyticsPlugin();
        plugin.initializeGoApplicationAccessor(accessor);

        Bootstrap.instance().updateOnlyFromTests_DoNotUseThisInProduction(dbAccess, mock(DataPurgeScheduler.class));
    }

    @Test
    public void shouldRegisterAllAnalyticsRequestExecutors() throws Exception {
        assertExecutorType(PipelineBuildTimeExecutor.class, "{\"type\": \"pipeline\", \"id\": \"pipeline_build_time\"}");
        assertExecutorType(JobsHighestWaitTimeExecutor.class, "{\"type\": \"dashboard\", \"id\": \"jobs_with_the_highest_wait_time\", \"params\": {\"metric\": \"Longest Waiting Job\"}}");
        assertExecutorType(PipelinesHighestWaitTimeExecutor.class, "{\"type\": \"dashboard\", \"id\": \"pipelines_with_the_highest_wait_time\", \"params\": {\"metric\": \"Pipeline With Longest Average\"}}");
        assertExecutorType(AgentsWithHighestUtilizationExecutor.class, "{\"type\": \"dashboard\", \"id\": \"agents_with_the_highest_utilization\", \"params\": {\"metric\": \"Agent With Highest Utilization\"}}");
        assertExecutorType(AgentStateTransitionExecutor.class, "{\"type\": \"agent\", \"id\": \"agent_state_transition\", \"params\": {\"metric\": \"Agent State Transition\"}}");
        assertExecutorType(VSMTrendAcrossMultipleRunsExecutor.class, "{\"type\": \"vsm\", \"id\": \"vsm_trend_across_multiple_runs\", \"params\": {\"current\": \"{}\", \"other\": \"{}\"}}");
    }

    private void assertExecutorType(Class<? extends RequestExecutor> expectedExecutorType, String requestBody) throws Exception {
        AnalyticsExecutorSelector selector = AnalyticsExecutorSelector.instance();
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("", "", "");
        request.setRequestBody(requestBody);

        RequestExecutor requestExecutor = selector.executorFor(request, null);
        assertTrue(requestExecutor.getClass().isAssignableFrom(expectedExecutorType));
    }
}
