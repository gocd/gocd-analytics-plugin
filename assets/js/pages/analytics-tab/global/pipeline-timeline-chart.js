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

import AnalyticsEndpoint from "gocd-server-comms";
import {updateChartSize} from "../../../santosh/utils";
import SlimSelect from 'slim-select';
import * as echarts from "echarts";

import GraphManager from "../../../santosh/GraphManager";
import Console from "../../../santosh/Console";

let graphManager = null;
const c = new Console('pipeline-timeline-chart.js', 'prod');

function addPipelineNamesToSelect(data) {
    const pipelineSelector = document.getElementById("pipeline");

    data.forEach((pipeline) => {
        const selectOption = document.createElement("option");
        selectOption.setAttribute("value", pipeline.name);
        selectOption.text = pipeline.name;

        pipelineSelector.appendChild(selectOption);
    });

    console.log("names added to select");
}

function setupSlimSelect(transport) {
    const slimSelect = new SlimSelect({
        select: '#pipeline',
        events: {
            afterChange: (newVal) => {
                console.log("afterChange ", newVal);
                pipelineSelectedEvent(transport, newVal[0].value);
            }
        }
    });
}

function pipelineSelectedEvent(transport, selectedPipeline) {

    transport
        .request("fetch-analytics", {
            metric: "pipeline_timeline",
            pipeline_name: selectedPipeline,
        })
        .done((data) => {
            // console.log("fetch-analytics ", data);
            // this.initSeries(this.child.getNextGraphName(), JSON.parse(data));

            // option = null;
            // option = stageTimeline(JSON.parse(data), myChart);
            // option && myChart.setOption(option);

            graphManager.initStandalone("pipeline-timeline", JSON.parse(data));
        })
        .fail(console.error.toString());
};

function requestPipelineList(transport) {
    transport
        .request("fetch-analytics", {
            metric: "pipeline_list",
        })
        .done((data) => {
            // console.log("fetch-analytics ", data);
            // this.initSeries(this.child.getNextGraphName(), JSON.parse(data));
            addPipelineNamesToSelect(JSON.parse(data));
            setupSlimSelect(transport);
            return JSON.parse(data);
        })
        .fail(console.error.toString());
}

AnalyticsEndpoint.onInit(function (initialData, transport) {

    const data = JSON.parse(initialData);

    c.log("pipeline timeline chart data = " + data);

    requestPipelineList(transport);

    var chartDom = document.getElementById("chart-container");

    var myChart = echarts.init(chartDom);
    updateChartSize(myChart, 1, 0.8);

    var option;

    const chartMeta = document.getElementById("chart-container-meta");
    chartMeta.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Pipeline timings</b></span>

    <select id="pipeline" style="float:right">
</select>
</div>
<hr>
    `;

    // const main = document.getElementById("pipeline-timeline-chart");
    // main.onload = function() {


    // }

    // option = pipelineTimeline();
    // option && myChart.setOption(option);

    graphManager = new GraphManager("standalone", null, null, null);
    graphManager.initStandalone("pipeline-timeline", data);

    c.log("*********** pipeline-timeline graph loaded");
});

AnalyticsEndpoint.ensure("v1");
