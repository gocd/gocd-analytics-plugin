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
import GraphManager from "../../../santosh/GraphManager";
import RequestMaster from "../../../RequestMaster";
import Header from "../../../santosh/defination/stage-timeline/header";
import Console from "../../../santosh/Console";
import PipelinePriority from "../../../santosh/defination/pipeline-priority";
import {getPreviousMonthDateInDBFormat, getTodaysDateInDBFormat} from "../../../santosh/utils";

let graphManager = null;
let requestMaster = null;

let header = null;

let currentPrioritySettings;

const c = new Console('priority-view-chart.js');

const settings = {"Priority": {scope: "Pipelines", result: "", alignTicks: true, startDate: getPreviousMonthDateInDBFormat(), endDate: getTodaysDateInDBFormat()}, "PriorityDetails": {alignTicks: true}};

AnalyticsEndpoint.onInit(function (initialData, transport) {

    const data = JSON.parse(initialData);

    requestMaster = new RequestMaster(transport);
    header = new Header(requestMaster);

    graphManager = new GraphManager("series", transport, informSeriesMovement, null);
    graphManager.initSeries("priority", data, settings.Priority);

    console.log("*********** priority graph loaded");
});

async function informSeriesMovement(graphName, requestParams) {
    console.log('📞 I am informed of graphName ', graphName, ' changing the header now.');
    console.log('requestParams', requestParams);
    // return header.switchHeader(graphName);

    header.clear();

    let headerSettings = null;

    switch (graphName) {
        case "PipelinePriority":
        case "StagePriority":
        case "JobPriority":
            headerSettings = await header.getPriorityPipelineHeader(ppChangeHandler, requestParams, settings["Priority"]);
            break;

        case "PipelinePriorityDetails":
        case "StagePriorityDetails":
        case "JobPriorityDetails":
            headerSettings = await header.getPriorityDetailsHeader(pdChangeHandler, currentPrioritySettings, settings["PriorityDetails"]);
            break;
    }

    // if (graphName.includes("Details")) {
    //     return await header.getPriorityDetailsHeader(pdChangeHandler, currentPrioritySettings);
    // }
    // const settings = await header.getPriorityPipelineHeader(ppChangeHandler, requestParams.result);
    // currentPrioritySettings = settings;
    // return settings;

    currentPrioritySettings = headerSettings;
    return headerSettings;
}

function pdChangeHandler(changedSettings) {
    settings["PriorityDetails"] = changedSettings;
    graphManager.call_initSeriesWithNewSettings(changedSettings);
}

function ppChangeHandler(changedSettings) {
    console.log("header changed changedSettings = ", changedSettings);
    currentPrioritySettings = changedSettings;
    settings["Priority"] = changedSettings;
    doJob(changedSettings);
}

async function doJob(settings) {
    console.log('🛜 requesting priority pipeline with the settings', settings);

    switch (settings.scope) {
        case 'Pipelines':
            const pp = await requestMaster.getPriorityPipeline(settings);
            graphManager.initSeries("PipelinePriority", pp, settings);
            break;
        case 'Stages':
            const ps = await requestMaster.getPriorityStage(settings);
            graphManager.initSeries("StagePriority", ps, settings);
            break;
        case 'Jobs':
            const pj = await requestMaster.getPriorityJob(settings);
            graphManager.initSeries("JobPriority", pj, settings);
            break;
    }

    // graphManager.call_initSeriesWithNewSettings(settings);
}

AnalyticsEndpoint.ensure("v1");
