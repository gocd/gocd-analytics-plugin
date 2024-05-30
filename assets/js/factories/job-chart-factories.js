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

import ChartFactories from "./chart-factories";
import JobCharts from "js/charts/jobs";
import moment from "js/lib/moment-humanize-for-gocd.js";
import GoCDLinkSupport from "js/lib/gocd-link-support";

const TODAY = moment(new Date()).format("YYYY-MM-DD"),
    ONE_WEEK_AGO = moment(TODAY).add(-1, "week").format("YYYY-MM-DD");

export default new ChartFactories({
    JobBuildTime: {
        id: "job_build_time",

        config: (data, transport) => {
            const config = JobCharts.runs(data);
            GoCDLinkSupport.linkToJobDetailsPage(config, transport);
            return config;
        },

        params: function (point) {
            console.log('point is ', point);
            return {
                "job_name": point.job_name,
                "stage_name": point.stage_name,
                "pipeline_name": point.pipeline_name,
                "type": "job",
                "metric": this.id
            };
        }
    },

    JobBuildTimeOnAgent: {
        id: "job_build_time_on_an_agent",

        config: (data, transport) => {
            const config = JobCharts.runsOnAnAgent(data);
            GoCDLinkSupport.linkToJobDetailsPage(config, transport);
            return config;
        },

        params: function (point) {
            console.log('point is ', point);
            return {
                "type": "drilldown",
                "metric": this.id,
                "job_name": point.job_name,
                "stage_name": point.stage_name,
                "pipeline_name": point.pipeline_name,
                "agent_uuid": point.agent_uuid,
                "agent_host_name": point.agent_host_name
            };
        }
    },

    LongestWaitingJobs: {
        id: "jobs_with_the_highest_wait_time",

        config: (data) => {
            return JobCharts.longestWaiting(data);
        },

        params: function (point) {
            console.log('point is ', point);
            return {
                "pipeline_name": point.name,
                "start": ONE_WEEK_AGO,
                "end": TODAY,
                "type": "job",
                "metric": this.id
            };
        }
    },

    LongestWaitingJobsOnAgent: {
        id: "jobs_with_the_highest_wait_time_on_an_agent",

        config: (data) => {
            return JobCharts.longestWaitingForAnAgent(data);
        },

        params: function (point) {
            console.log('point is ', point);
            return {
                "type": "drilldown",
                "metric": this.id,
                "start": ONE_WEEK_AGO,
                "end": TODAY,
                "agent_uuid": point.agent_uuid,
                "agent_host_name": point.agent_host_name
            };
        }
    },

    JobsTimeline: {
        id: "job_timeline",

        params: function (point) {
            return {
                "type": "drilldown",
                "metric": this.id,
                "stage_name": point.stage_name,
                "pipeline_counter_start": point.pipeline_counter_start,
                "pipeline_counter_end": point.pipeline_counter_end,
            }
        },
    },

    PipelinePriority: {
        id: "priority_pipeline",

        params: function (point) {
            return {
                "type": "drilldown",
                "metric": this.id,
                "result": point.result,
            }
        },
    },

    PipelinePriorityDetails: {
        id: "priority_pipeline_details",

        params: function (point) {
            return {
                "type": "drilldown",
                "metric": this.id,
                "pipeline_name": point.pipeline_name,
                "result": point.result
            }
        },
    },

    StagePriorityDetails: {
        id: "priority_stage_details",

        params: function (point) {
            return {
                "type": "drilldown",
                "metric": this.id,
                "stage_name": point.stage_name,
                "result": point.result
            }
        },
    },

    JobPriorityDetails: {
        id: "priority_job_details",

        params: function (point) {
            return {
                "type": "drilldown",
                "metric": this.id,
                "job_name": point.job_name,
                "result": point.result
            }
        },
    },

    StageRerunsInstances: {
        id: "stage_reruns",

        params: function (point) {
            let ret = {
                "type": "drilldown",
                "metric": this.id,
                "pipeline_name": point.pipeline_name
            }

            if (point.hasOwnProperty("pipeline_counter")) {
                ret.stage_name = point.stage_name;
                ret.pipeline_counter = point.pipeline_counter;
            }

            return ret;
        },
    },

    StageStartupTimeCompare: {
        id: "stage_startup_time",

        params: function (point) {
            return {
                "type": "drilldown",
                "metric": this.id,
                "pipeline_name": point.pipeline_name,
                "pipeline_counter": point.pipeline_counter
            }
        },
    }

});
