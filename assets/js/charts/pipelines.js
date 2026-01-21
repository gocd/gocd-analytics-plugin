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

import moment from "js/lib/moment-humanize-for-gocd.js";
import Constants from "js/lib/constants.js";
import BuildMetrics from "js/lib/build-metrics.js";
import Utils from "js/lib/utils.js";
import Formatters from "js/lib/formatters.js";
import barChart from "js/lib/chart-utils/bar";
import areaChart from "js/lib/chart-utils/area";
import Fragments from "js/lib/chart-utils/fragments";

import library from "js/lib/load-fontawesome";
import {faAngleDown, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";

library.add(faQuestionCircle);
library.add(faAngleDown);

const PIPELINE_STATUS = {
  BUILDING: "building",
  WAITING: "waiting"
};

function mttrDisplay(data) {
  return moment.duration(BuildMetrics.mttr(data), "ms").humanizeForGoCD();
}

function mtbfDisplay(data) {
  return moment.duration(BuildMetrics.mtbf(data), "ms").humanizeForGoCD();
}

function auxiliaryMetrics(data) {
  return `
    ${Formatters.auxiliaryMetric("Run Frequency", BuildMetrics.runFrequency(data))}
    ${Formatters.auxiliaryMetric("Mean Time to Recovery", mttrDisplay(data), "mttr", true)}
    ${Formatters.auxiliaryMetric("Mean Time Between Failures", mtbfDisplay(data))}
    ${Formatters.auxiliaryMetric("Failure Rate", BuildMetrics.failureRate(data), "failure-rate")}
  `;
}

const DataTransforms = {
  runs: {
    "Mean Build Time": function (data) {
      const pips = data.instances;
      return 0 === pips.length ? [] : [[0, BuildMetrics.mean(pips)], [pips.length - 1, BuildMetrics.mean(pips)]];
    },
    "Build Time": function (data) {
      return data.instances.map(p => {
        return {
          y: (p.total_time_secs - p.time_waiting_secs) / 60.0,
          scheduled_at: p.scheduled_at,
          colorIndex: Utils.colorByResult(p.result),
          result: p.result,
          pipeline_name: p.name,
          pipeline_counter: p.counter,
          last_transition_time: p.last_transition_time
        };
      });
    },
    "Wait Time": function (data) {
      return data.instances.map(p => {
        return {
          y: p.time_waiting_secs / 60.0,
          result: p.result,
          pipeline_name: p.name,
          pipeline_counter: p.counter
        };
      });
    },
  },

  longestWaiting: {
    "Build Time": function (data) {
      return data.map(p => {
        return {
          name: p.name,
          type: PIPELINE_STATUS.BUILDING,
          y: p.avg_build_time_secs / 60.0
        };
      });
    },
    "Wait Time": function (data) {
      return data.map(p => {
        return {
          name: p.name,
          type: PIPELINE_STATUS.WAITING,
          y: p.avg_wait_time_secs / 60.0
        };
      });
    },
  }
};

function PipelineCharts() {
  const FIELD_NAME = "name";

  function runs(pipeline) {
    const instancesData = pipeline.instances;
    const options = {
      title: "Pipeline Build Time",
      subtitle: `${Fragments.auxiliaryMetricsDropdown(auxiliaryMetrics(instancesData))}`,
      xAxis: {
        title: Fragments.chartZoomHint(),
        className: "pipeline-runs",
        formatter: function () {
          if (!instancesData.length) {
            return;
          }
          const valueWithinRange = Math.max(0, Math.min(Math.floor(this.value), instancesData.length - 1));
          return moment(instancesData[valueWithinRange]["scheduled_at"]).format("ddd MMM D");
        }
      },
      yAxis: {
        title: "Duration",
        formatter: Formatters.durationAxisFormatter
      },
      tooltip: {
        formatter: Formatters.makeTimingTooltipFormatter(function(cursor) {
          const datum = cursor.points[0].point;
            return `${[
              Utils.tooltipKeyVal("Pipeline Instance", datum.pipeline_counter),
              Utils.resultTooltipEntry(datum.result),
              Utils.tooltipKeyVal("Started On", moment(datum.scheduled_at).format("DD MMM YYYY [at] HH:mm:ss [Local Time]"))
            ].join("<br/>")}`;
        }, "Click on data point for pipeline details")
      },
      series: [{
        type: "line",
        name: "Mean Build Time",
        colorIndex: Constants.COLORS.info,
        className: "trend-line",
        connectNulls: true,
        enableMouseTracking: false,
        marker: {
          enabled: false
        },
        width: 2,
        zIndex: 4,
        data: DataTransforms.runs["Mean Build Time"](pipeline)
      },
      {
        name: "Build Time",
        colorIndex: Constants.COLORS.building,
        pointPlacement: "on",
        marker: { symbol: "circle" },
        data: DataTransforms.runs["Build Time"](pipeline),
        events: {
          data: function(e) {
            const auxMetricsEl = this.chart.container.querySelector(".auxiliary-metrics");

            if (auxMetricsEl) {
              auxMetricsEl.innerHTML = auxiliaryMetrics(e.data);
            }
          }
        }
      },
      {
        name: "Wait Time",
        colorIndex: Constants.COLORS.waiting,
        pointPlacement: "on",
        marker: { enabled: false, states: { hover: { enabled: false } } },
        data: DataTransforms.runs["Wait Time"](pipeline)
      }]
    };

    return areaChart(options);
  }

  function longestWaiting(pipelines) {
    const data = {
      title: "Pipelines with the Highest Wait Time",
      addendum: "(Average over the last 7 days)",
      xAxis: {
        categories: pipelines.map(p => p[FIELD_NAME])
      },
      tooltip: {
        formatter: Formatters.makeTimingTooltipFormatter(function(cursor) {
          return Formatters.breadcrumbFormatter(cursor.x);
        }, "Click on bar for more info")
      },
      series: [
        {
          name: "Build Time",
          colorIndex: Constants.COLORS.jobbuild,
          data: DataTransforms.longestWaiting["Build Time"](pipelines)
        },
        {
          name: "Wait Time",
          colorIndex: Constants.COLORS.jobwait,
          data: DataTransforms.longestWaiting["Wait Time"](pipelines)
        }
      ]
    };
    return barChart(data);
  }

  Object.assign(this, {
    runs: runs,
    longestWaiting: longestWaiting
  });
}

export default new PipelineCharts();
export { DataTransforms };
