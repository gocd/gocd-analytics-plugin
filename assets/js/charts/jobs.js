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

import _ from "lodash";

import moment from "js/lib/moment-humanize-for-gocd.js";
import Constants from "js/lib/constants.js";
import Utils from "js/lib/utils.js";
import Formatters from "js/lib/formatters.js";
import barChart from "js/lib/chart-utils/bar";
import areaChart from "js/lib/chart-utils/area";
import Fragments from "js/lib/chart-utils/fragments";

function JobCharts() {
  const FIELD_BUILDING       = "time_building_secs",
        FIELD_WAITING        = "time_waiting_secs",
        FIELD_SCHEDULED      = "scheduled_at",
        FIELD_IDENT          = "identifier";

  function addResultData(job, key, point) {
    point.colorIndex  = key === FIELD_BUILDING ? (Utils.colorByResult(job.result)) : null;
    point.name   = moment(job.scheduled_at).format("LLL");
    point.result = job.result;

    // need these to allow linking to pages in GoCD
    point.pipeline_counter = job.pipeline_counter;
    point.stage_counter    = job.stage_counter;

    return point;
  }

  function durationPt(jobs, key, formatter) {
    return _.map(jobs, function (job) {
      const point = _.merge({
        name: identifier(job),
        y:    job[key] / 60.0
      }, _.pick(job, ["pipeline_name", "stage_name", "job_name"]));
      return "function" === typeof formatter ? formatter(job, key, point) : point;
    });
  }

  /** @private builds a full job identifier from pipeline, stage, and job names */
  function identifier(j) {
    return `${j.pipeline_name}/${j.stage_name}/${j.job_name}`;
  }

  /** @private label formatter to humanize a timestamp */
  function timeAxisFormatter() {
    return moment(this.value).format("ddd MMM D");
  }

  /** @private tooltip formatter to show duration information from series and job result */
  const durationsWithStatusFormatter = Formatters.makeTimingTooltipFormatter(function(cursor) {
    const first  = cursor.points[0].point;
    return [
      Utils.resultTooltipEntry(first.result),
      Utils.tooltipKeyVal("Pipeline Instance", first.pipeline_counter),
      Utils.tooltipKeyVal("Stage Counter", first.stage_counter)
    ].join("<br/>");
  }, "Click on data point for job details");

  _.assign(this, {
    runs: function jobRuns(data) {
      const jobs = data.jobs;

      return areaChart({
        title: "Job Build Time",
        subtitle: Formatters.breadcrumbFormatter(data[FIELD_IDENT]),
        xAxis: {
          title: Fragments.chartZoomHint({y: 24}),
          className: "job-runs",
          categories: _.map(jobs, _.property(FIELD_SCHEDULED)),
          formatter: timeAxisFormatter
        },
        yAxis: {
          title: "Duration",
          formatter: Formatters.durationAxisFormatter
        },
        tooltip: { formatter: durationsWithStatusFormatter },
        series: [{
          name:  "Build Time",
          colorIndex: Constants.COLORS.jobbuild,
          marker: { symbol: "circle" },
          data:  durationPt(jobs, FIELD_BUILDING, addResultData)
        },
        {
          name:  "Wait Time",
          colorIndex: Constants.COLORS.jobwait,
          marker: { enabled: false, states: { hover: { enabled: false } } },
          data:  durationPt(jobs, FIELD_WAITING, addResultData)
        }],
        legend: { margin: 24 }
      });
    },

    runsOnAnAgent: function (data) {
      const jobs          = data.jobs,
            agentUUID     = data.agent_uuid,
            agentHostname = data.agent_host_name,
            agentId       = Formatters.agentIdentifier({
              agent_host_name: agentHostname, uuid: agentUUID
            });

      return areaChart({
        title: "Job Build Time on an Agent",
        addendum: Formatters.agentBreadcrumbFormatter(agentId),
        subtitle: Formatters.breadcrumbFormatter(data[FIELD_IDENT]),
        xAxis: {
          title: Fragments.chartZoomHint({y: 24}),
          className: "job-runs",
          categories: _.map(jobs, _.property(FIELD_SCHEDULED)),
          formatter: timeAxisFormatter
        },
        yAxis: {
          title: "Duration",
          formatter: Formatters.durationAxisFormatter
        },
        tooltip: { formatter: durationsWithStatusFormatter },
        series: [{
          name: "Build Time",
          colorIndex: Constants.COLORS.building,
          marker: { symbol: "circle" },
          data: durationPt(jobs, FIELD_BUILDING, addResultData)
        },
        {
          name: "Wait Time",
          colorIndex: Constants.COLORS.waiting,
          marker: { enabled: false, states: { hover: { enabled: false } } },
          data: durationPt(jobs, FIELD_WAITING, addResultData)
        }],
        legend: { margin: 24 }
      });
    },

    longestWaiting: function longestWaiting(data) {
      const pipeline = data.pipeline_name,
                jobs = data.jobs;

      function noKeys() { return Formatters.breadcrumbFormatter(this.value, {startAt: 1, keys: []}); }
      function withKeys() { return Formatters.breadcrumbFormatter(this.value, {startAt: 1}); }
      function shortKeys() { return Formatters.breadcrumbFormatter(this.value, {startAt: 1, keys: ["P", "S", "J"]}); }
      function jobOnly() { return Formatters.breadcrumbFormatter(this.value, {startAt: 2, keys: []}); }

      const variants = [{
        name: "default labels",
        config: {xAxis: {labels: {formatter: noKeys}}}
      }, {
        name: "label keys",
        config: {xAxis: {labels: {formatter: withKeys}}}
      }, {
        name: "short label keys",
        config: {xAxis: {labels: {formatter: shortKeys}}}
      }, {
        name: "only job name",
        config: {xAxis: {labels: {formatter: jobOnly}}}
      }, {
        name: "no format",
        config: {xAxis: {labels: {formatter: null}}}
      }];

      const options = {
        variants: variants,
        title: "Jobs with the Highest Wait Time",
        addendum: "(Average over the last 7 days)",
        subtitle: Formatters.breadcrumbFormatter(pipeline),
        xAxis: {
          categories: _.map(jobs, identifier),
          formatter: noKeys
        },
        tooltip: {
          formatter: Formatters.makeTimingTooltipFormatter(function(cursor) {
            return Formatters.breadcrumbFormatter(cursor.x, {sep: "<br/>", reverse: true});
          }, "Click on bar for more info")
        },
        series: [{
          name:  "Build Time",
          colorIndex: Constants.COLORS.jobbuild,
          data:  durationPt(jobs, FIELD_BUILDING)
        },
        {
          name:  "Wait Time",
          colorIndex: Constants.COLORS.jobwait,
          data:  durationPt(jobs, FIELD_WAITING)
        }]
      };

      return barChart(options);
    },

    longestWaitingForAnAgent: function (data) {
      const jobs          = data.jobs,
            agentUUID     = data.agent_uuid,
            agentHostname = data.agent_host_name,
            agentId       = Formatters.agentIdentifier({
              agent_host_name: agentHostname, uuid: agentUUID
            });

      function addAgentData(_j, _k, point) {
        return _.assign(point, {
          agent_uuid:      agentUUID,
          agent_host_name: agentHostname
        });
      }

      function noKeys() { return Formatters.breadcrumbFormatter(this.value, {keys: []}); }
      function withKeys() { return Formatters.breadcrumbFormatter(this.value); }
      function shortKeys() { return Formatters.breadcrumbFormatter(this.value, {keys: ["P", "S", "J"]}); }
      function jobOnly() { return Formatters.breadcrumbFormatter(this.value, {startAt: 2, keys: []}); }

      const variants = [{
        name: "default labels",
        config: {xAxis: {labels: {formatter: noKeys}}}
      }, {
        name: "label keys",
        config: {xAxis: {labels: {formatter: withKeys}}}
      }, {
        name: "short label keys",
        config: {xAxis: {labels: {formatter: shortKeys}}}
      }, {
        name: "only job name",
        config: {xAxis: {labels: {formatter: jobOnly}}}
      }, {
        name: "no format",
        config: {xAxis: {labels: {formatter: null}}}
      }];

      const options = {
        variants: variants,
        title: "Jobs with the Highest Wait Time on an Agent",
        addendum: "(Average over the last 7 days)",
        subtitle: Formatters.agentBreadcrumbFormatter(agentId),
        xAxis: {
          categories: _.map(jobs, identifier),
          formatter: noKeys
        },
        tooltip: {
          formatter: Formatters.makeTimingTooltipFormatter(function(cursor) {
            return Formatters.breadcrumbFormatter(cursor.x, {sep: "<br/>", reverse: true});
          }, "Click on bar for more info")
        },
        series: [{
          name: "Build Time",
          colorIndex: Constants.COLORS.building,
          data: durationPt(jobs, FIELD_BUILDING, addAgentData)
        }, {
          name: "Wait Time",
          colorIndex: Constants.COLORS.waiting,
          data: durationPt(jobs, FIELD_WAITING, addAgentData)
        }]
      };

      return barChart(options);
    }
  });
}

export default new JobCharts();
