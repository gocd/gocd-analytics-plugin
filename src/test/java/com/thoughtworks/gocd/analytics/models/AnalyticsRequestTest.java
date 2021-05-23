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

package com.thoughtworks.gocd.analytics.models;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.thoughtworks.gocd.analytics.AnalyticTypes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AnalyticsRequestTest {
    @Test
    public void shouldDeserializeFromJSON() throws Exception {
        AnalyticsRequest pipelineRequest = AnalyticsRequest.fromJSON("{" +
                "\"type\": \"pipeline\"," +
                "\"id\": \"pipeline_build_time\"," +
                " \"params\": " +
                "{\"pipeline_name\": \"test_pipeline\"}" +
                "}");

        assertEquals(TYPE_PIPELINE, pipelineRequest.getType());
        assertEquals(Collections.singletonMap(PARAM_PIPELINE_NAME, "test_pipeline"), pipelineRequest.getParams());

        AnalyticsRequest dashboardRequest = AnalyticsRequest.fromJSON("{" +
                "\"type\": \"dashboard\"," +
                "\"id\": \"longest_waiting_job\"," +
                " \"params\": " +
                "{\"metric\": \"test_metric\"}" +
                "}");

        assertEquals(TYPE_DASHBOARD, dashboardRequest.getType());
        assertEquals(Collections.singletonMap(PARAM_METRIC, "test_metric"), dashboardRequest.getParams());
    }

    @Test
    public void missingKeysInJsonShouldBeNull() throws Exception {
        assertNull(AnalyticsRequest.fromJSON("{\"foo\": \"pipeline\"}").getType());
        assertNull(AnalyticsRequest.fromJSON("{\"foo\": \"pipeline\"}").getParams());
    }
}
