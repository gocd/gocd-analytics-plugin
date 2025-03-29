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

const c = new Console('dora-metrics-chart.js', 'dev');

const settings = {
    "DoraMetrics": {
        truncateOrder: 'Last',
        startDate: getFirstDayOfTheCurrentMonth(),
        endDate: getTodaysDateInDBFormat(),
        pipeline_name: ''
    }
};

AnalyticsEndpoint.onInit(function (initialData, transport) {

    const doraMetrics = JSON.parse(initialData);

    c.log("dora metrics = ", doraMetrics);

    requestMaster = new RequestMaster(transport);
    header = new Header(requestMaster);
    footer = new Footer();
    //
    graphManager = new GraphManager('series', transport, informSeriesMovement, footer);
    //

    init(doraMetrics);

});

async function init(data) {
    // settings.WaitBuildRatio = await header.getWaitBuildTimeRatioHeader(graphOneHeaderChangeHandler);
    // await header.getAgentMetricsHeader(graphOneHeaderChangeHandler);

    console.log("dora metrics settings = ", settings.DoraMetrics);

    const newData = [{
        "pipeline_name": "DB-Watchers",
        "pipeline_counter": 1,
        "stage_name": "Sprout",
        "stage_counter": 1,
        "job_name": "do",
        "result": "Passed",
        "scheduled_at": "2025-02-28T06:54:18.036+0000",
        "completed_at": "2025-02-28T07:00:18.031+0000",
        "time_waiting_secs": 359,
        "time_building_secs": 0,
        "duration_secs": 359,
        "unique_name": "Sprout-do",
        "agent_uuid": "1"
    }, {
        "pipeline_name": "DB-Watchers",
        "pipeline_counter": 1,
        "stage_name": "Sprout",
        "stage_counter": 1,
        "job_name": "do",
        "result": "Passed",
        "scheduled_at": "2025-02-28T06:54:18.036+0000",
        "completed_at": "2025-02-28T07:00:18.031+0000",
        "time_waiting_secs": 359,
        "time_building_secs": 0,
        "duration_secs": 359,
        "unique_name": "Sprout-do",
        "agent_uuid": "1"
    }, {
        "pipeline_name": "DB-Watchers",
        "pipeline_counter": 1,
        "stage_name": "Sprout",
        "stage_counter": 1,
        "job_name": "do",
        "result": "Failed",
        "scheduled_at": "2025-02-28T06:54:18.036+0000",
        "completed_at": "2025-02-28T07:00:18.031+0000",
        "time_waiting_secs": 359,
        "time_building_secs": 0,
        "duration_secs": 359,
        "unique_name": "Sprout-do",
        "agent_uuid": "1"
    }, {
        "pipeline_name": "DB-Watchers",
        "pipeline_counter": 1,
        "stage_name": "Sprout",
        "stage_counter": 1,
        "job_name": "do",
        "result": "Passed",
        "scheduled_at": "2025-02-28T06:54:18.036+0000",
        "completed_at": "2025-02-28T07:00:18.031+0000",
        "time_waiting_secs": 359,
        "time_building_secs": 0,
        "duration_secs": 359,
        "unique_name": "Sprout-do",
        "agent_uuid": "1"
    }, {
        "pipeline_name": "DB-Watchers",
        "pipeline_counter": 1,
        "stage_name": "Sprout",
        "stage_counter": 1,
        "job_name": "do",
        "result": "Failed",
        "scheduled_at": "2025-02-28T06:54:18.036+0000",
        "completed_at": "2025-02-28T07:00:18.031+0000",
        "time_waiting_secs": 359,
        "time_building_secs": 0,
        "duration_secs": 359,
        "unique_name": "Sprout-do",
        "agent_uuid": "1"
    }, {
        "pipeline_name": "DB-Watchers",
        "pipeline_counter": 1,
        "stage_name": "Sprout",
        "stage_counter": 1,
        "job_name": "do",
        "result": "Failed",
        "scheduled_at": "2025-02-28T06:54:18.036+0000",
        "completed_at": "2025-02-28T07:00:18.031+0000",
        "time_waiting_secs": 359,
        "time_building_secs": 0,
        "duration_secs": 359,
        "unique_name": "Sprout-do",
        "agent_uuid": "1"
    }, {
        "pipeline_name": "DB-Watchers",
        "pipeline_counter": 1,
        "stage_name": "Sprout",
        "stage_counter": 1,
        "job_name": "do",
        "result": "Passed",
        "scheduled_at": "2025-02-28T06:54:18.036+0000",
        "completed_at": "2025-02-28T07:00:18.031+0000",
        "time_waiting_secs": 359,
        "time_building_secs": 0,
        "duration_secs": 359,
        "unique_name": "Sprout-do",
        "agent_uuid": "1"
    }];

    if (data.length === 0) {
        footer.showMessage("No data to display", "INFO", true);
    }
    // else {
    graphManager.initSeries('DoraMetrics', data, settings.DoraMetrics);
    // }

    (async () => {
        const defaultSettings = await header.getDoraMetricsHeader(changeHandler);

        settings.DoraMetrics = defaultSettings;

        await doraMetrics(defaultSettings);
    })();
}

async function doraMetrics(settings) {
    c.log('🛜 requesting dora metrics with the settings', settings);
    console.log('🛜 requesting dora metrics with the settings', settings);
    footer.clear();

    const doraMetrics = await requestMaster.getDoraMetrics(settings.startDate, settings.endDate, settings.selectedPipeline);

    if (doraMetrics.length === 0) {
        footer.showMessage("No data for selected option, can't draw a graph.", "Error", true, 10);
        graphManager.clear();
        return;
    }

    graphManager.initSeries('DoraMetrics', doraMetrics, settings.DoraMetrics);
}

async function informSeriesMovement(graphName) {
    c.logs('📞 I am informed of graphName ', graphName, ' changing the header now.');

    // return header.switchHeader(graphName);

    if (graphName === 'JobsTimeline') {
        return await header.getJobsTimelineHeader(jobChangeHandler);
    } else if (graphName === 'stage-timeline') {
        c.logs("I have to send stage-timeline header");
    }

    footer.clear();

}

async function reloadDoraMetrics(settings) {
    c.log('🛜 requesting stage startup with the settings', settings);
    footer.clear();

    const dora_chart_container = document.getElementById("chart-container");
    dora_chart_container.innerHTML = "";
    dora_chart_container.classList.add("loader");

    const doraMetrics = await requestMaster.getDoraMetrics(settings.startDate, settings.endDate, settings.selectedPipeline);

    if (doraMetrics.length === 0) {
        footer.showMessage("No data for selected option, can't draw a graph.", "Error", true, 10);
        graphManager.clear();
        return;
    }

    graphManager.initSeries("DoraMetrics", doraMetrics, settings);

    dora_chart_container.classList.remove("loader");
}

function changeHandler(settings) {
    c.log('settings', settings);

    console.log("dora metrics settings = ", settings);

    reloadDoraMetrics(settings);
}

function jobChangeHandler(settings) {
    doJob(settings);
}

async function doJob(settings) {
    c.logs('🛜 requesting job timeline with the settings', settings);

    // caching for now
    // const stageTimeline = await requestMaster.getJobTimeline(settings.selectedStage, settings.pipeline_counter_start, settings.pipeline_counter_end);

    // graphManager.initSeries("JobsTimeline", stageTimeline, settings);
    graphManager.call_initSeriesWithNewSettings(settings);
}


AnalyticsEndpoint.ensure("v1");
