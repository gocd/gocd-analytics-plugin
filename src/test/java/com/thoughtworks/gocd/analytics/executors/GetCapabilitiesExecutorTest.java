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

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class GetCapabilitiesExecutorTest {

    @Test
    public void shouldReportCapabilities() throws Exception {
        GoPluginApiResponse response = new GetCapabilitiesExecutor().execute();

        String expectedJSON = "{\n" +
                "    \"supported_analytics\": [\n" +
                "        {\n" +
                "            \"id\": \"pipeline_build_time\",\n" +
                "            \"title\": \"Pipeline Build Time\",\n" +
                "            \"type\": \"pipeline\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"stage_build_time\",\n" +
                "            \"title\": \"Stage Build Time\",\n" +
                "            \"type\": \"stage\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"jobs_with_the_highest_wait_time\",\n" +
                "            \"title\": \"Jobs with the Highest Wait Time\",\n" +
                "            \"type\": \"job\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"job_build_time\",\n" +
                "            \"title\": \"Job Build Time\",\n" +
                "            \"type\": \"job\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"pipelines_with_the_highest_wait_time\",\n" +
                "            \"title\": \"Pipelines with the Highest Wait Time\",\n" +
                "            \"type\": \"dashboard\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"agents_with_the_highest_utilization\",\n" +
                "            \"title\": \"Agents with the Highest Utilization\",\n" +
                "            \"type\": \"dashboard\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"jobs_with_the_highest_wait_time_on_an_agent\",\n" +
                "            \"title\": \"Jobs with the Highest Wait Time on an Agent\",\n" +
                "            \"type\": \"drilldown\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"job_build_time_on_an_agent\",\n" +
                "            \"title\": \"Job Build Time on an Agent\",\n" +
                "            \"type\": \"drilldown\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"agent_state_transition\",\n" +
                "            \"title\": \"Agent State Transition\",\n" +
                "            \"type\": \"agent\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"vsm_trend_across_multiple_runs\",\n" +
                "            \"title\": \"VSM Trend Across Multiple Runs\",\n" +
                "            \"type\": \"vsm\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"vsm_workflow_time_distribution\",\n" +
                "            \"title\": \"VSM Workflow Time Distribution\",\n" +
                "            \"type\": \"drilldown\"\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";

        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
