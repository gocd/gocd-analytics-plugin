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
import $ from "jquery";

import GraphManager from "../../../santosh/GraphManager";
import Header from "../../../santosh/defination/stage-timeline/header";
import RequestMaster from "../../../RequestMaster";
import Footer from "../../../santosh/defination/stage-timeline/footer";
import Console from "../../../santosh/Console";
import {getFirstDayOfTheCurrentMonth, getTodaysDateInDBFormat} from "../../../santosh/utils";

let graphManager = null;
let requestMaster = null;

let header = null;
let footer = null;

const c = new Console('stage-timeline-chart.js', 'prod');

const settings = {
    "stage-timeline": null, "JobsTimeline": null
};

AnalyticsEndpoint.onInit(function (initialData, transport) {

    requestMaster = new RequestMaster(transport);
    header = new Header(requestMaster);
    footer = new Footer();

    graphManager = new GraphManager("series", transport, informSeriesMovement, footer);


    (async () => {
        const defaultSettings = await header.getStageTimelineHeader(changeHandler);

        settings["stage-timeline"] = defaultSettings;

        await doStage(defaultSettings);
    })();

});

async function informSeriesMovement(graphName) {
    c.logs('📞 I am informed of graphName ', graphName, ' changing the header now.');

    // return header.switchHeader(graphName);

    let headerSettings = null;

    if (graphName === 'JobsTimeline') {
        console.log("going to get jobs timeline");
        headerSettings = await header.getJobsTimelineHeader(jobChangeHandler, settings["JobsTimeline"]);
        console.log("settings = ", settings);
    } else if (graphName === 'stage-timeline') {
        console.log("inform series movement, passing stage-timeline settings ", settings["stage-timeline"]);
        headerSettings = await header.getStageTimelineHeader(changeHandler, settings["stage-timeline"]);
    }

    // settings[graphName] = ret;

    footer.clear();

    console.log("returning settings");
    return headerSettings;
}

async function doStage(settings) {
    c.logs('🛜 requesting stage timeline with the settings', settings);
    console.log('🛜 requesting stage timeline with the settings', settings);
    footer.clear();

    const stageTimeline = await requestMaster.getStageTimeline(settings.startDate, settings.endDate, settings.selectedPipeline, settings.requestResult, settings.requestOrder, settings.requestLimit);

    if (stageTimeline.length === 0) {
        footer.showMessage("No data for selected option, can't draw a graph.", "Error", true, 10);
        graphManager.clear();
        return;
    }

    graphManager.initSeries("stage-timeline", stageTimeline, settings);
}

function changeHandler(changedSettings) {
    console.log("changeHandler settings ", changedSettings);
    c.logs('settings', settings);
    settings["stage-timeline"] = changedSettings;
    doStage(changedSettings);
    c.log('stage timeline returned the graph and all set');
}

function jobChangeHandler(changedSettings) {
    settings["JobsTimeline"] = changedSettings;
    doJob(changedSettings);
}

async function doJob(settings) {
    c.logs('🛜 requesting job timeline with the settings', settings);

    // caching for now
    // const stageTimeline = await requestMaster.getJobTimeline(settings.selectedStage, settings.pipeline_counter_start, settings.pipeline_counter_end);

    // graphManager.initSeries("JobsTimeline", stageTimeline, settings);
    graphManager.call_initSeriesWithNewSettings(settings);
}


AnalyticsEndpoint.ensure("v1");
