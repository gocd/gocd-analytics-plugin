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

import c from "crel";
import vsmUtils from "js/lib/vsm-utils.js";
import moment from "js/lib/moment-humanize-for-gocd.js";
import AnalyticsEndpoint from "gocd-server-comms";
import VSMChartFactories from "js/factories/vsm-chart-factories.js";
import WorkflowTimeDistributionChart from "js/pages/vsm/workflow-time-distribution-chart.js";
import H from "js/lib/load-highcharts.js";
import Tooltip from "js/lib/tooltip-util.js";
import $ from "jquery";

// For the lazy typist
const Id = (sel) => document.getElementById(sel);
const Qa = (sel) => document.querySelectorAll(sel);

const indicator = spinner();

const drawWorkflowTimeDistributionChart = function (workflowId, pipelines, transport, trendsInitialData) {
	$("#chart-container").empty();

	const params = {
		workflow_id: workflowId,
		source: pipelines[0].name,
		pipelines_in_workflow: JSON.stringify(pipelines.map(p => p.name)),
		destination: pipelines[pipelines.length - 1].name,
		type: "drilldown",
		metric: "vsm_workflow_time_distribution"
	};

	transport.request("fetch-analytics", params).done((data) => {
		WorkflowTimeDistributionChart.render(data, trendsInitialData, transport);
	}).fail(console.error); //eslint-disable-line no-console
};

function render(initialData, transport) {
  $("#chart-container").empty();
  const data = JSON.parse(initialData),
    workflows = data.workflows,
    factory = VSMChartFactories.get("WorkflowSparkline");

  const metricThroughput = vsmUtils.calculateThroughput(data.pipelines, workflows);
  const metricCycleTime = vsmUtils.calculateAverageCycleTime(data.pipelines, workflows);

  Id("chart-container").appendChild(
    chartTable(workflows, metricThroughput, metricCycleTime)
  );

  auxMetrics("Deployment Frequency", `${metricThroughput}%`, "throughput");
  auxMetrics("Average Cycle Time", metricCycleTime, "avgCycleTime");

  addTooltip(transport);

  workflowIds(workflows).forEach((workflowId) => {
    H.chart(Id(`workflow-id-${workflowId}`), factory.config(workflows[workflowId], data.pipelines, transport));
    $("#drilldown-for-" + workflowId).click(function () {
        drawWorkflowTimeDistributionChart(workflowId, workflows[workflowId], transport, initialData);
    });
  });

  // hide spinner
  remove(indicator);
}

function addTooltip(transport) {
  Tooltip.addTooltip(".throughput",
    {of: ".throughput", my: "left center", at: "right-24 center"},
    "Deployment Frequency is a measure of the frequency of reaching the destination pipeline. It is not affected by whether the destination pipeline failed.",
    transport, "https://github.com/gocd/gocd-analytics-plugin/blob/main/docs/VSM_Analytics.md#deployment-frequency");

  Tooltip.addTooltip(".avgCycleTime",
    {of: ".avgCycleTime", my: "left center", at: "right-32 center"},
    "Cycle time is a measure of how long it takes from source to the next successful destination pipeline run.",
    transport, "https://github.com/gocd/gocd-analytics-plugin/blob/main/docs/VSM_Analytics.md#average-cycle-time");
}

function init() {
  const root = Id("chart-container");
  empty(root);
  remove(Qa(".highcharts-tooltip-container")); // if we empty the container, is this even there?

  root.appendChild(indicator);
}

const WorkflowTrendsChart = {init, render};

WorkflowTrendsChart.init();

AnalyticsEndpoint.onInit(function (initialData, transport) {
  WorkflowTrendsChart.render(initialData, transport);
});

AnalyticsEndpoint.ensure("v1");

/** Components */

function chartTable(workflows) {
  return c("div", {class: "vsm-trends-container"},
    c("div", {class: "workflow-metrics"}),
    c("div", {class: "vsm-trends-table-header"}, ["VSM TREND", "STARTED AT", "COMPLETED AT", "TIME TAKEN", ""].map(tableHeader)),
    c("div", {class: "vsm-trends"},
      c("table", {class: "vsm-trends-table"}, c("tbody", workflowIds(workflows).map((id) => tableEntry(id, workflows))))
    )
  );
}

function tableHeader(name) {
  return c("div", {class: `table-cell header-item ${snake(name)}-column`}, name);
}

function tableEntry(id, workflows) {
  return c("tr", {id: `workflow-column-${id}`},
    tableCell("VSM TREND", c("div", {id: `workflow-id-${id}`})),
    tableCell("STARTED AT", moment(workflows[id][0].scheduled_at).format("D MMM HH:mm")),
    tableCell("COMPLETED AT", moment(workflows[id][workflows[id].length - 1].last_transition_time).format("D MMM HH:mm")),
    tableCell("TIME TAKEN", c("span", vsmUtils.workflowDuration(workflows[id]).humanizeForGoCD())),
	tableCell("MORE INFO", c("button", {id: `drilldown-for-${id}`, class: "drilldown-button"}, "More Info"))
  );
}

function tableCell(key, val) {
  return c("td", {class: `table-cell ${snake(key)}-column`}, val);
}

/**Was unable able to get jquery tooltips working on a element added using 'crel', had to fallback on jQuery to append the aux metrics. */
function auxMetrics(key, value, cssClass) {
  $(".workflow-metrics").append(`
      <dl class="workflow-metric-item ${cssClass}">
        <dt class="key">${key}<i class="fas fa-question-circle contextual-help" title=""></i></dt>
        <dd class="val">${value}</dd>
      </dl>
  `);
}

function spinner() {
  return c("div", {class: "spinner-overlay"}, c("div", {class: "spinner"}, "Loading\u2026"));
}

/** Utils */

function workflowIds(workflows) {
  return Object.keys(workflows).sort((a, b) => ((b << 0) - (a << 0)));
}

function snake(text) {
  return text.toLowerCase().split(" ").join("-");
}

function remove(el) {
  "function" === typeof el.forEach ? el.forEach(remove) : (el.parentNode && el.parentNode.removeChild(el));
}

function empty(el) {
  while (el.firstChild) { el.removeChild(el.firstChild); }
}

export default WorkflowTrendsChart;
