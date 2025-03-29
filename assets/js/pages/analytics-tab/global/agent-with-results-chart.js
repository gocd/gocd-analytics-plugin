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
import {getFirstDayOfTheCurrentMonth, getTodaysDateInDBFormat} from "../../../santosh/utils";
import Footer from "../../../santosh/defination/stage-timeline/footer";
import agentWithResults from "../../../santosh/defination/agent-with-results";

let graphManager = null;
let requestMaster = null;

let header = null;
let footer = null;

const c = new Console('agent-with-results-chart.js', 'dev');

const settings = {
    "AgentsLeastUtilized": {
        truncateOrder: 'Last',
        startDate: getFirstDayOfTheCurrentMonth(),
        endDate: getTodaysDateInDBFormat(),
        limit: 10
    }
    // , "LongestWaitingJobsOnAgent": {
    //     truncateOrder: 'Last',
    //     startDate: getFirstDayOfTheCurrentMonth(),
    //     endDate: getTodaysDateInDBFormat(),
    //     limit: 10
    // }, "JobBuildTimeOnAgent": null
};

AnalyticsEndpoint.onInit(function (initialData, transport) {
    const agentsWithResults = JSON.parse(initialData);

    c.log("agent with results = ", agentsWithResults);

    requestMaster = new RequestMaster(transport);
    header = new Header(requestMaster);
    footer = new Footer();
    //
    graphManager = new GraphManager('series', transport, informSeriesMovement, footer);
    //
    init(agentsWithResults);

});

async function init(data) {
    settings["AgentWithResults"] = await header.getLongestWaitingPipelinesHeader(graphOneHeaderChangeHandler);

    if (data.length === 0) {
        footer.showMessage("No data to display", "INFO", true);
    } else {
        graphManager.initSeries('AgentWithResults', data, settings['AgentWithResults']);
    }
}

async function informSeriesMovement(graphName, requestParams) {
    console.log('📞 I am informed of graphName ', graphName, ' changing the header now.');
    console.log('requestParams', requestParams);
    // return header.switchHeader(graphName);
    header.clear();

    let headerSettings = null;

    switch (graphName) {
        case "AgentsLeastUtilized":
            headerSettings = await header.getLongestWaitingPipelinesHeader(graphOneHeaderChangeHandler, settings["AgentsMostUtilized"]);
            break;

        case "LongestWaitingJobsOnAgent":
            // if(settings.LongestWaitingJobs === null) {
            //     settings["LongestWaitingJobs"] = await header.getLongestWaitingPipelinesHeader(graphTwoHeaderChangeHandler);
            //     headerSettings = settings["LongestWaitingJobs"];
            // } else {
            settings["LongestWaitingJobsOnAgent"]["startDate"] = settings["AgentsMostUtilized"]["startDate"];
            settings["LongestWaitingJobsOnAgent"]["endDate"] = settings["AgentsMostUtilized"]["endDate"];

            headerSettings = await header.getLongestWaitingPipelinesHeader(graphTwoHeaderChangeHandler, settings["LongestWaitingJobsOnAgent"]);
            // }
            break;

        default:
            return null;
    }

    return headerSettings;
}

async function graphOneHeaderChangeHandler(changedSettings) {
    c.log("graphOneHeaderChangeHandler", changedSettings);

    const previousTruncateOrder = settings["AgentsLeastUtilized"].truncateOrder;

    settings["AgentsLeastUtilized"] = changedSettings;

    if (previousTruncateOrder === changedSettings.truncateOrder) {
        const agents = await requestMaster.getAgentLeastUtilized(changedSettings.startDate, changedSettings.endDate, changedSettings.limit);
        graphManager.clear();
        if (agents.length === 0) {
            footer.showMessage("No data to display", "Error", true);
        } else {
            graphManager.call_initSeriesWithNewData(agents);
            footer.clear();
        }
    } else {
        graphManager.call_initSeriesWithNewSettings(changedSettings);
    }
}

function graphTwoHeaderChangeHandler(changedSettings) {
    c.log("graphTwoHeaderChangeHandler", changedSettings);

    settings["LongestWaitingJobsOnAgent"] = changedSettings;
    graphManager.call_initSeriesWithNewSettings(changedSettings);
}

AnalyticsEndpoint.ensure("v1");
