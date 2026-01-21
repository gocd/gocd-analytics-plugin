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

import areaChart from "js/lib/chart-utils/area";
import moment from "js/lib/moment-humanize-for-gocd.js";
import Formatters from "js/lib/formatters.js";
import Constants from "js/lib/constants.js";
import Utils from "js/lib/utils.js";
import Fragments from "js/lib/chart-utils/fragments";
import BuildMetrics from "js/lib/build-metrics.js";

const DataTransforms = {
  runs: {
    "Mean Build Time": function (data) {
      const stages = data.runs;
      return 0 === stages.length ? [] : [[0, BuildMetrics.mean(stages)], [stages.length - 1, BuildMetrics.mean(stages)]];
    },

    "Build Time": function (data) {
      return data.runs.map(s => {
        return {
          y: (s.total_time_secs - s.time_waiting_secs) / 60.0,
          colorIndex: Utils.colorByResult(s.result),
          result: s.result,
          stage_name: s.stage_name,
          stage_counter: s.stage_counter,
          pipeline_name: s.pipeline_name,
          pipeline_counter: s.pipeline_counter
        };
      });
    },

    "Wait Time": function (data) {
      return data.runs.map(s => {
        return {
          y: s.time_waiting_secs / 60.0,
          result: s.result,
          stage_name: s.stage_name,
          stage_counter: s.stage_counter,
          pipeline_name: s.pipeline_name,
          pipeline_counter: s.pipeline_counter
        };
      });
    }
  }
};

function StageCharts() {
  this.runs = function runs(stage) {
    const stages = stage.runs;

    return areaChart({
      title: "Stage Build Time",
      subtitle: Formatters.breadcrumbFormatter(stage.identifier),
      xAxis: {
        title: Fragments.chartZoomHint(),
        className: "stage-runs",
        formatter: function () {
          if (!stages.length) {
            return;
          }
          return moment(this.value.scheduled_at).format("ddd MMM D");
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
              Utils.resultTooltipEntry(datum.result),
              Utils.tooltipKeyVal("Pipeline Instance", datum.pipeline_counter),
              Utils.tooltipKeyVal("Stage Counter", datum.stage_counter),
              Utils.tooltipKeyVal("Started On", moment(datum.scheduled_at).format("DD MMM YYYY [at] HH:mm:ss [Local Time]"))
            ].join("<br/>")}`;
        }, "Click on data point for stage details")
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
        data: DataTransforms.runs["Mean Build Time"](stage)
      },
      {
        name: "Build Time",
        colorIndex: Constants.COLORS.building,
        pointPlacement: "on",
        marker: { symbol: "circle" },
        data: DataTransforms.runs["Build Time"](stage)
      },
      {
        name: "Wait Time",
        colorIndex: Constants.COLORS.waiting,
        pointPlacement: "on",
        marker: { enabled: false, states: { hover: { enabled: false } } },
        data: DataTransforms.runs["Wait Time"](stage)
      }]
    });
  };
}

export default new StageCharts();
export { DataTransforms };
