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

public interface AnalyticTypes {
    String PARAM_CONTEXT = "context";
    String PARAM_METRIC = "metric";

    String TYPE_DASHBOARD = "dashboard";
    String TYPE_DRILLDOWN = "drilldown";
    String TYPE_PIPELINE = "pipeline";
    String TYPE_STAGE = "stage";
    String TYPE_JOB = "job";
    String TYPE_VSM = "vsm";

    String PARAM_PIPELINE_NAME = "pipeline_name";
    String PARAM_STAGE_NAME = "stage_name";
    String PARAM_JOB_NAME = "job_name";

    String PARAM_PIPELINE_COUNTER_START = "pipeline_counter_start";
    String PARAM_PIPELINE_COUNTER_END = "pipeline_counter_end";

    String PARAM_RESULT = "result";
    String PARAM_START_DATE = "start";
    String PARAM_END_DATE = "end";

    String PARAM_AGENT_UUID = "agent_uuid";
    String PARAM_AGENT_HOST_NAME = "agent_host_name";

    String TYPE_AGENT = "agent";

    String PARAM_VSM_GRAPH = "vsm_graph";
    String PARAM_VSM_SOURCE = "source";
    String PARAM_VSM_DESTINATION = "destination";
    String PARAM_VSM_WORKFLOW_ID = "workflow_id";
    String PARAM_PIPELINES_IN_WORKFLOW = "pipelines_in_workflow";
}
