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

public enum AvailableAnalytics {

    PIPELINE_BUILD_TIME(AnalyticTypes.TYPE_PIPELINE,
            "pipeline_build_time",
            "Pipeline Build Time"),

    STAGE_BUILD_TIME(AnalyticTypes.TYPE_STAGE,
            "stage_build_time",
            "Stage Build Time"),

    JOBS_WITH_THE_HIGHEST_WAIT_TIME(AnalyticTypes.TYPE_JOB,
            "jobs_with_the_highest_wait_time",
            "Jobs with the Highest Wait Time"),

    JOB_BUILD_TIME(AnalyticTypes.TYPE_JOB,
            "job_build_time",
            "Job Build Time"),

    PIPELINES_WITH_THE_HIGHEST_WAIT_TIME(AnalyticTypes.TYPE_DASHBOARD,
            "pipelines_with_the_highest_wait_time",
            "Pipelines with the Highest Wait Time"),

    AGENTS_WITH_THE_HIGHEST_UTILIZATION(AnalyticTypes.TYPE_DASHBOARD,
            "agents_with_the_highest_utilization",
            "Agents with the Highest Utilization"),

    // rename this later to a request type
    PIPELINES_RUNTIME_ACROSS_TIMELINE(AnalyticTypes.TYPE_JOB,
        "pipeline_list", "Helper to list pipelines"),

    PIPELINE_TIMELINE(AnalyticTypes.TYPE_DASHBOARD, "pipeline_timeline", "Pipeline timeline"),

    STAGE_TIMELINE(AnalyticTypes.TYPE_DASHBOARD, "stage_timeline", "Stage Timeline"),

    PRIORITY(AnalyticTypes.TYPE_DASHBOARD, "priority", "Priority"),

    PIPELINE_STATE_SUMMARY(AnalyticTypes.TYPE_DASHBOARD, "pipeline_state_summary", "Pipeline "
        + "State Summary"),

    JOBS_WAIT_VS_AGENTS_AVAILABLE(AnalyticTypes.TYPE_DASHBOARD, "worrysome", "Worrysome"),
    HELPER_AGENT_UTILIZATION(AnalyticTypes.TYPE_JOB, "helper_agent_utilization", "?"),

    JOBS_WITH_THE_HIGHEST_WAIT_TIME_ON_AGENT(AnalyticTypes.TYPE_DRILLDOWN,
            "jobs_with_the_highest_wait_time_on_an_agent",
            "Jobs with the Highest Wait Time on an Agent"),

    JOB_BUILD_TIME_ON_AGENT(AnalyticTypes.TYPE_DRILLDOWN,
            "job_build_time_on_an_agent",
            "Job Build Time on an Agent"),

    AGENT_STATE_TRANSITION(AnalyticTypes.TYPE_AGENT,
            "agent_state_transition",
            "Agent State Transition"),

    VSM_TREND_ACROSS_MULTIPLE_RUNS(AnalyticTypes.TYPE_VSM,
            "vsm_trend_across_multiple_runs",
            "VSM Trend Across Multiple Runs"),

    VSM_WORKFLOW_TIME_DISTRIBUTION(AnalyticTypes.TYPE_DRILLDOWN,
            "vsm_workflow_time_distribution",
            "VSM Workflow Time Distribution");

    private final String type;
    private final String id;
    private final String title;

    AvailableAnalytics(String type, String id, String title) {
        this.type = type;
        this.id = id;
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
