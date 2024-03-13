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
import {updateChartSize} from "../../../santosh/utils";
import * as echarts from "echarts";

import GraphManager from "../../../santosh/GraphManager";
import stageTimelineHeader from "./stage-timeline-header";

console.log("stage-timeline-chart.js start");

let graphManager = null;

AnalyticsEndpoint.onInit(function (initialData, transport) {
    console.log("onInit called with initial data as ", initialData);

    (async () => {
        const pipelines = await requestMask(transport);

        const pipelineSelector = await stageTimelineHeader(pipelines);

        graphManager = new GraphManager("standalone", transport);

        handleClick(pipelineSelector, transport);

        console.log("*********** stage-timeline graph loaded");
    })();

});

function handleClick(pipelineSelector, transport) {
    let selectedPipeline = pipelineSelector.value;

    pipelineSelector.addEventListener("change", function () {
        selectedPipeline = pipelineSelector.value;

        requestStageTimeline(transport, selectedPipeline);
    });

    requestStageTimeline(transport, selectedPipeline);
}

async function requestMask(transport) {
    return await requestPipelineList(transport);
}

function requestStageTimeline(transport, selectedPipeline) {
    transport
        .request("fetch-analytics", {
            metric: "stage_timeline", pipeline_name: selectedPipeline,
        })
        .done((data) => {
            console.log("fetch-analytics ", data);

            graphManager.initSeries("stage-timeline", JSON.parse(data));
        })
        .fail(console.error.toString());
}

async function requestPipelineList(transport) {
    return new Promise((resolve) => {
        transport
            .request("fetch-analytics", {
                metric: "pipeline_list",
            })
            .done((data) => {
                resolve(JSON.parse(data));
            })
            .fail(console.error.toString());
    });
}


AnalyticsEndpoint.ensure("v1");
