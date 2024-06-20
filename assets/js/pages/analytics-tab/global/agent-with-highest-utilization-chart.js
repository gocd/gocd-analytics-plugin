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

let graphManager = null;
let requestMaster = null;

let header = null;

const c = new Console('agents-with-highest-utilization-chart.js', 'dev');

const settings = {"AgentsMostUtilized": null, "LongestWaitingJobsOnAgent": null, "JobBuildTimeOnAgent": null};

AnalyticsEndpoint.onInit(function (initialData, transport) {
    const agents = JSON.parse(initialData);

    c.log("agents = ", agents);

    requestMaster = new RequestMaster(transport);
    header = new Header(requestMaster);

    graphManager = new GraphManager('series', transport, informSeriesMovement, null);

    init(agents);


});

async function init(data) {
    settings["AgentsMostUtilized"] = await header.getLongestWaitingPipelinesHeader(graphOneHeaderChangeHandler);

    graphManager.initSeries('AgentsMostUtilized', data, settings['AgentsMostUtilized']);
}

async function informSeriesMovement(graphName, requestParams) {
    console.log('📞 I am informed of graphName ', graphName, ' changing the header now.');
    console.log('requestParams', requestParams);
    // return header.switchHeader(graphName);
    header.clear();

    let headerSettings = null;

    switch (graphName) {
        case "AgentsMostUtilized":
            headerSettings = await header.getLongestWaitingPipelinesHeader(graphOneHeaderChangeHandler, settings["AgentsMostUtilized"]);
            break;

        case "LongestWaitingJobsOnAgent":
            // if(settings.LongestWaitingJobs === null) {
            //     settings["LongestWaitingJobs"] = await header.getLongestWaitingPipelinesHeader(graphTwoHeaderChangeHandler);
            //     headerSettings = settings["LongestWaitingJobs"];
            // } else {
            headerSettings = await header.getLongestWaitingPipelinesHeader(graphTwoHeaderChangeHandler, settings["LongestWaitingJobsOnAgent"]);
            // }
            break;

        default:
            return null;
    }

    return headerSettings;
}

function graphOneHeaderChangeHandler(changedSettings) {
    c.log("graphOneHeaderChangeHandler", changedSettings);

    settings["AgentsMostUtilized"] = changedSettings;
    graphManager.call_initSeriesWithNewSettings(changedSettings);
}

function graphTwoHeaderChangeHandler(changedSettings) {
    c.log("graphTwoHeaderChangeHandler", changedSettings);

    settings["LongestWaitingJobsOnAgent"] = changedSettings;
    graphManager.call_initSeriesWithNewSettings(changedSettings);
}

AnalyticsEndpoint.ensure("v1");
