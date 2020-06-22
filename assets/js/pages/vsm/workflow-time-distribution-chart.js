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

import "css/global";

import _ from "lodash";
import H from "js/lib/load-highcharts.js";
import VSMChartFactories from "js/factories/vsm-chart-factories.js";
import WorkflowTrendsChart from "js/pages/vsm/workflow-trends-chart.js";

const groupByPipelines = function (stages) {
	return _.reduce(stages, (pipelines, stage) => {
		pipelines[stage.pipeline_name] = pipelines[stage.pipeline_name] || [];
		pipelines[stage.pipeline_name].push(stage);
		return pipelines;
	}, {});
};

const supportDrillUp = function (chart, drillUpCallback) {
    if (!chart.upstreamButton) {
        chart.upstreamButton = chart.renderer.button(
            "\u3031Back",
            null,
            null,
            drillUpCallback,
            { width: 59, height: 6, r: 3 }
        ).addClass("upstream-button").
        attr({ align: "right", zIndex: 7 }).
        add().
        align({ x: 0, y: 5, align: "right", verticalAlign: "top" }, false, "spacingBox");
    }
};

function renderChart(initialData, trendsChartInitialData, transport) {
	const data = JSON.parse(initialData),
		factory = VSMChartFactories.get("WorkflowDetails"),
		container = document.getElementById("chart-container");

    const drillUpOnClick = function () {
        WorkflowTrendsChart.render(trendsChartInitialData, transport);
    };

	let chart = H.chart(container, factory.config(groupByPipelines(data.stages), data.pipelines_in_workflow, transport));

	supportDrillUp(chart, drillUpOnClick);
}

const WorkflowTimeDistributionChart = function () {
	this.render = renderChart;
};

export default new WorkflowTimeDistributionChart();
