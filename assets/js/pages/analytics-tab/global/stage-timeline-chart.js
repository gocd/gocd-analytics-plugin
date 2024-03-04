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

// import "css/global";

import AnalyticsEndpoint from "gocd-server-comms";
import DrilldownSupport from "js/lib/drilldown-support.js";
import PipelineChartFactories from "js/factories/pipeline-chart-factories.js";
import JobChartFactories from "js/factories/job-chart-factories.js";
import $ from "jquery";
import stageTimeline from "../../../santosh/defination/stage-timeline";
import pipelineTimeline from "../../../santosh/defination/pipeline-timeline";
import { updateChartSize } from "../../../santosh/utils";
import * as echarts from "echarts";

import GraphManager from "../../../santosh/GraphManager";

console.log("stage-timeline-chart.js start");

function addPipelineNamesToSelect(data) {
  const pipelineSelector = document.getElementById("pipeline");

  data.forEach((pipeline) => {
    const selectOption = document.createElement("option");
    selectOption.setAttribute("value", pipeline.name);
    selectOption.text = pipeline.name;

    pipelineSelector.appendChild(selectOption);
  });
}

function requestPipelineList(transport) {
  transport
    .request("fetch-analytics", {
      metric: "pipeline_list",
    })
    .done((data) => {
      console.log("fetch-analytics ", data);
      // this.initSeries(this.child.getNextGraphName(), JSON.parse(data));
      addPipelineNamesToSelect(JSON.parse(data));
      return JSON.parse(data);
    })
    .fail(console.error.toString());
}

AnalyticsEndpoint.onInit(function (initialData, transport) {
  console.log("onInit called with initial data as ", initialData);

  const data = JSON.parse(initialData);

  requestPipelineList(transport);

  // factory = PipelineChartFactories.get("LongestWaitingPipelines"),
  // container = document.getElementById("chart-container"),
  // drilldowns = ["LongestWaitingJobs", "JobBuildTime"];

  // console.log("#1 Santosh data = ", data);

  // const config = DrilldownSupport.withDrilldown(
  //     factory.config(data),
  //     drilldowns,
  //     JobChartFactories,
  //     transport
  // );

  // console.log(config);

  // H.chart(container, config);

  // const point = {name: "up42"}
  // const params = JobChartFactories.get("LongestWaitingJobs").params(point);

  // dataBank.push(data);
  // billboard_bar(transport);

  // new StackedBar("LongestWaitingPipelines", data, drilldowns, transport);

  // const graphManager = new GraphManager('series', transport);
  // graphManager.initSeries('LongestWaitingPipelines', data);

  // $("#chart-container").load("./pipeline-list.html");

  var chartDom = document.getElementById("chart-container");

  var myChart = echarts.init(chartDom);
  updateChartSize(myChart, 1, 0.8);

  var option;

  const chartMeta = document.getElementById("chart-container-meta");
  chartMeta.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Stage timeline across workflow</b></span>

    <input type="checkbox" id="weird" name="weird" value="weird">
<label for="weird"> Weird proof</label>

    <select id="pipeline" style="float:right">
</select>
</div>
<hr>
    `;

  const pipelineSelector = document.getElementById("pipeline");

  let selectedPipeline = pipelineSelector.value;
  pipelineSelector.addEventListener("change", function () {
    selectedPipeline = pipelineSelector.value;

    transport
      .request("fetch-analytics", {
        metric: "stage_timeline",
        pipeline_name: selectedPipeline,
      })
      .done((data) => {
        console.log("fetch-analytics ", data);
        // this.initSeries(this.child.getNextGraphName(), JSON.parse(data));
        option = null;
        option = stageTimeline(JSON.parse(data), myChart);
        option && myChart.setOption(option, true);
      })
      .fail(console.error.toString());
  });

  // option = pipelineTimeline();
  // option && myChart.setOption(option);

  // const graphManager = new GraphManager("standalone", null);
  // graphManager.initStandalone("pipeline-timeline", data);

  console.log("*********** stage-timeline graph loaded");
});

AnalyticsEndpoint.ensure("v1");
