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
import sparklineChart from "js/lib/chart-utils/sparkline";
import xRangeChart from "js/lib/chart-utils/xrange";
import Formatters from "js/lib/formatters.js";

import library from "js/lib/load-fontawesome";
import {faAngleDown, faQuestionCircle} from "@fortawesome/free-solid-svg-icons";

library.add(faQuestionCircle);
library.add(faAngleDown);

function isLastPipelineInCurrentWorkflow(pipelineName, pipelineNames, pipelineNamesInCurrentWorkflow) {
	const currentPipelineIndex = pipelineNames.indexOf(pipelineName);

	if ((currentPipelineIndex + 1) === pipelineNames.length) {
		return true;
	}

	const pipelinesAfterCurrentPipeline = pipelineNames.slice(currentPipelineIndex + 1);

	return 0 === pipelinesAfterCurrentPipeline.filter(i => -1 !== pipelineNamesInCurrentWorkflow.indexOf(i)).length;
}

function legendAsTitle() {
  return`<div class="td-legend-container">
     <span class="td-zoom-hint">Click and drag in the plot area to zoom in.</span>
     <div class="td-legend">
       <span class="td-stage-passed">Stage Passed</span>
       <span class="td-stage-failed">Stage Failed</span>
       <span class="td-stage-cancelled">Stage Cancelled</span>
       <span class="td-stage-waiting">Waiting Time</span>
     </div>
   </div>`;
}

const DataTransforms = {
	runs: {
		"VSM TRENDS": function (pipelines, pipelineNames) {
			const pipelineNameToPipeline = pipelines.reduce((hash, p) => {
				hash[p.name] = p;
				return hash;
			}, {});

			return pipelineNames.map(pipelineName => {
				let pipeline = pipelineNameToPipeline[pipelineName];
				if (pipeline) {
					return {
						y: (pipeline.total_time_secs) / 60.0,
						pipeline_name: pipeline.name,
						pipeline_counter: pipeline.counter,
						workflow_id: pipeline.workflow_id,
						result: pipeline.result,
						colorIndex: Utils.colorByResult(pipeline.result),
						scheduled_at: pipeline.scheduled_at,
						completed_at: pipeline.last_transition_time,
						total_time_secs: pipeline.total_time_secs
					};
				}
				if (isLastPipelineInCurrentWorkflow(pipelineName, pipelineNames, Object.keys(pipelineNameToPipeline))) {
					return {y: ""};
				} else {
					return {
						y: 0,
						pipeline_name: pipelineName,
						pipeline_counter: 0,
						colorIndex: Utils.colorByResult("info"),
					};
				}
			});
		}
	},
	timeDistribution: {
		"STAGES DATA": function (pipelineStages, pipelinesInWorkflow) {
			return pipelinesInWorkflow.reduce((stageData, pipelineName) => {
        const stages = pipelineStages[pipelineName];

        if(!stages) return stageData;

        stages.forEach((stage) => {
          stageData.push({
            x: moment(stage.scheduled_at).toDate(),
            x2: moment(stage.completed_at).toDate(),
            y: pipelinesInWorkflow.indexOf(pipelineName),
            partialFill: stage.time_waiting_secs / stage.total_time_secs,
            colorIndex: Utils.colorByResult(stage.result),
            stage: stage
          });
        });

        return stageData;
      }, []);
    }
	}
};

function tooltips(datum) {
  return Utils.withTooltipFooter("Click on data point to navigate to VSM", `${[
    Utils.tooltipKeyVal("Pipeline", _.escape(datum.pipeline_name)),
    Utils.tooltipKeyVal("Pipeline Instance", datum.pipeline_counter),
    Utils.tooltipKeyVal("Started At", moment(datum.scheduled_at).format("DD MMM YYYY [at] HH:mm:ss [Local Time]")),
    Utils.tooltipKeyVal("Completed At", moment(datum.completed_at).format("DD MMM YYYY [at] HH:mm:ss [Local Time]")),
    Utils.tooltipKeyVal("Duration", moment.duration(datum.total_time_secs, "seconds").humanizeForGoCD())
  ].join("<br/>")}`);
}

function tooltipForNoRun(datum) {
	return `${[
		Utils.tooltipKeyVal("Pipeline", _.escape(datum.pipeline_name)),
		"<span>There has been no run of this pipeline in this workflow.</span>",
	].join("<br/>")}`;
}

function VSMCharts() {
	function trends(pipelinesInCurrentWorkflow, allPipelineNames) {
		const options = {
			tooltip: {
				formatter: function () {
					const datum = this.points[0].point;
					return (0 === datum.pipeline_counter) ? tooltipForNoRun(datum) : tooltips(datum);
				},

				positioner: function (w, h, point) {
					const rect = this.chart.container.getBoundingClientRect();

					return {
						x: point.plotX + rect.x,

						// without moving up, the tooltip covers the graph and can
						// cause flickering if the mouse is over that part of the graph.
						y: point.plotY + rect.y - this.container.offsetHeight
					};
				}
			},
			series: [{
				colorIndex: Constants.COLORS.building,
				pointPlacement: "on",
				marker: {symbol: "circle"},
				data: DataTransforms.runs["VSM TRENDS"](pipelinesInCurrentWorkflow, allPipelineNames),
			}],
			pointStart: 1
		};

		return sparklineChart(options);
	}

	function details(allStages, pipelinesInWorkflow) {
		const options = {
			title: "Workflow Time Distribution",
			tooltip: {
				formatter: Formatters.makeTimingTooltipFormatter(function(cursor) {
          const point = cursor.point.options;
          const stage = point.stage;

          return [
            Utils.tooltipKeyVal("Pipeline Instance", stage.pipeline_counter),
            Utils.tooltipKeyVal("Stage/Counter", `${_.escape(stage.stage_name)}/${stage.stage_counter}`),
            Utils.tooltipKeyVal("Scheduled at", moment(stage.scheduled_at).format("DD MMM YYYY [at] HH:mm:ss [Local Time]")),
            Utils.tooltipKeyVal("Completed at", moment(stage.completed_at).format("DD MMM YYYY [at] HH:mm:ss [Local Time]")),
            Utils.tooltipKeyVal("Build Time", moment.duration(stage.total_time_secs / 60, "m").humanizeForGoCD()),
            Utils.tooltipKeyVal("Wait Time", moment.duration(stage.time_waiting_secs / 60, "m").humanizeForGoCD())
          ].join("<br/>");
        }, "Click on data point for stage details")
			},
      yAxis: {
        categories: pipelinesInWorkflow,
			},
      xAxis: {
        title: {
          text: legendAsTitle(),
          align: "high",
          useHTML: true,
          y: 10
        },
        className: "pipeline-timeline"
      },
			series: [{
        name: "Workflow Stages",
        pointWidth: 15,
        data: DataTransforms.timeDistribution["STAGES DATA"](allStages, pipelinesInWorkflow),
        showInLegend: false,
        dataLabels: {
          enabled: false
        }
      }]
		};

		return xRangeChart(options);
	}

	Object.assign(this, {
		trends: trends,
		details: details
	});
}

export default new VSMCharts();
export {DataTransforms};
