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

const TODAY        = moment(new Date()).format("YYYY-MM-DD"),
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
      return {
        "job_name":      point.job_name,
        "stage_name":    point.stage_name,
        "pipeline_name": point.pipeline_name,
        "type":          "job",
        "metric":        this.id
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
      return {
        "type":            "drilldown",
        "metric":          this.id,
        "job_name":        point.job_name,
        "stage_name":      point.stage_name,
        "pipeline_name":   point.pipeline_name,
        "agent_uuid":      point.agent_uuid,
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
      return {
        "pipeline_name": point.name,
        "start":         ONE_WEEK_AGO,
        "end":           TODAY,
        "type":          "job",
        "metric":        this.id
      };
    }
  },

  LongestWaitingJobsOnAgent: {
    id: "jobs_with_the_highest_wait_time_on_an_agent",

    config: (data) => {
      return JobCharts.longestWaitingForAnAgent(data);
    },

    params: function (point) {
      return {
        "type":            "drilldown",
        "metric":          this.id,
        "start":           ONE_WEEK_AGO,
        "end":             TODAY,
        "agent_uuid":      point.agent_uuid,
        "agent_host_name": point.agent_host_name
      };
    }
  }
});
