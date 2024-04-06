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
import stageTimeline from "../../../santosh/defination/stage-timeline";
import pipelineTimeline from "../../../santosh/defination/pipeline-timeline";
import {updateChartSize} from "../../../santosh/utils";
import * as echarts from "echarts";
import SlimSelect from 'slim-select'

import GraphManager from "../../../santosh/GraphManager";
import Console from "../../../santosh/Console";

const c = new Console('pipeline-timeline-chart.js', 'prod');

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

    const data = JSON.parse(initialData);

    c.log("data = " + data);

    requestPipelineList(transport);

    var chartDom = document.getElementById("chart-container");

    var myChart = echarts.init(chartDom);
    updateChartSize(myChart, 1, 0.8);

    var option;

    const chartMeta = document.getElementById("chart-container-meta");
    chartMeta.innerHTML = `
    <div style="position:relative;"><span style="font-size:18px"><b>Pipeline timeline across workflow</b></span>

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
                metric: "pipeline_timeline",
                pipeline_name: selectedPipeline,
            })
            .done((data) => {
                console.log("fetch-analytics ", data);
                // this.initSeries(this.child.getNextGraphName(), JSON.parse(data));

                // option = null;
                // option = stageTimeline(JSON.parse(data), myChart);
                // option && myChart.setOption(option);

                graphManager.initStandalone("pipeline-timeline", JSON.parse(data));
            })
            .fail(console.error.toString());
    });

    // option = pipelineTimeline();
    // option && myChart.setOption(option);

    const graphManager = new GraphManager("standalone", null, null, null, c);
    graphManager.initStandalone("pipeline-timeline", data);

    console.log("*********** pipeline-timeline graph loaded");
});

AnalyticsEndpoint.ensure("v1");
