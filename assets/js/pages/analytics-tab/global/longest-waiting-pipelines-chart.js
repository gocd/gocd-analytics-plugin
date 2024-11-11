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
import Console from "../../../santosh/Console";
import RequestMaster from "../../../RequestMaster";
import Header from "../../../santosh/defination/stage-timeline/header";
import {
    getFirstDayOfTheCurrentMonth,
    getPreviousMonthDateInDBFormat,
    getTodaysDateInDBFormat
} from "../../../santosh/utils";
import Footer from "../../../santosh/defination/stage-timeline/footer";

let graphManager = null;
let requestMaster = null;

let header = null;
let footer = null;

const c = new Console('longest-waiting-pipelines-chart.js', 'prod');

const settings = {
    "LongestWaitingPipelines": {
        truncateOrder: 'Last',
        startDate: getFirstDayOfTheCurrentMonth(),
        endDate: getTodaysDateInDBFormat(),
        limit: 10
    }, "LongestWaitingJobs": {
        truncateOrder: 'Last',
        startDate: getFirstDayOfTheCurrentMonth(),
        endDate: getTodaysDateInDBFormat(),
        limit: 10
    }, "JobBuildTime": null
};

AnalyticsEndpoint.onInit(function (initialData, transport) {
    const data = JSON.parse(initialData);

    c.log("data = " + data);

    requestMaster = new RequestMaster(transport);
    header = new Header(requestMaster);
    footer = new Footer();

    graphManager = new GraphManager('series', transport, informSeriesMovement, footer);

    init(data);

    // c.log("*********** graph loaded");

});

async function init(data) {
    settings["LongestWaitingPipelines"] = await header.getLongestWaitingPipelinesHeader(graphOneHeaderChangeHandler);
    // settings["LongestWaitingJobs"] = await header.getLongestWaitingPipelinesHeader(graphTwoHeaderChangeHandler);

    if (data.length === 0) {
        footer.showMessage("No data to display", "INFO", true);
    } else {
        graphManager.initSeries('LongestWaitingPipelines', data, settings["LongestWaitingPipelines"]);
    }

}

async function informSeriesMovement(graphName, requestParams) {
    console.log('📞 Santosh I am informed of graphName ', graphName, ' changing the header now.');
    console.log('Santosh requestParams', requestParams);
    // return header.switchHeader(graphName);
    header.clear();

    let headerSettings = null;

    switch (graphName) {
        case "LongestWaitingPipelines":
            headerSettings = await header.getLongestWaitingPipelinesHeader(graphOneHeaderChangeHandler, settings["LongestWaitingPipelines"]);
            break;

        case "LongestWaitingJobs":
            // if(settings.LongestWaitingJobs === null) {
            //     settings["LongestWaitingJobs"] = await header.getLongestWaitingPipelinesHeader(graphTwoHeaderChangeHandler);
            //     headerSettings = settings["LongestWaitingJobs"];
            // } else {
            console.log("longest waiting pipelines settings ", settings["LongestWaitingPipelines"]);
            console.log("longest waiting jobs settings before ", settings["LongestWaitingJobs"]);

            settings["LongestWaitingJobs"]["startDate"] = settings["LongestWaitingPipelines"]["startDate"];
            settings["LongestWaitingJobs"]["endDate"] = settings["LongestWaitingPipelines"]["endDate"];

            console.log("longest waiting jobs settings after ", settings["LongestWaitingJobs"]);

            headerSettings = await header.getLongestWaitingPipelinesHeader(graphTwoHeaderChangeHandler, settings["LongestWaitingJobs"]);
            // }
            break;

        default:
            return null;
    }

    return headerSettings;
}

async function graphOneHeaderChangeHandler(changedSettings) {
    c.log("graphOneHeaderChangeHandler", changedSettings);

    const previousTruncateOrder = settings["LongestWaitingPipelines"].truncateOrder;

    settings["LongestWaitingPipelines"] = changedSettings;

    if (previousTruncateOrder === changedSettings.truncateOrder) {
        const pipelines = await requestMaster.getLongestWaitingPipelines(changedSettings.startDate, changedSettings.endDate, changedSettings.limit);
        graphManager.clear();
        if (pipelines.length === 0) {
            footer.showMessage("No data to display", "Error", true);
        } else {
            graphManager.call_initSeriesWithNewData(pipelines);
            footer.clear()
        }
    } else {
        graphManager.call_initSeriesWithNewSettings(changedSettings);
    }
}

function graphTwoHeaderChangeHandler(changedSettings) {
    c.log("graphTwoHeaderChangeHandler", changedSettings);

    settings["LongestWaitingJobs"] = changedSettings;
    graphManager.call_initSeriesWithNewSettings(changedSettings);
}

AnalyticsEndpoint.ensure("v1");
