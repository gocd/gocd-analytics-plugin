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
import DrilldownSupport from "js/lib/drilldown-support.js";
import AgentChartFactories from "js/factories/agent-chart-factories.js";
import JobChartFactories from "js/factories/job-chart-factories.js";
import H from "js/lib/load-highcharts.js";
import StackedBar from "../../../santosh/stacked-bar";
import GraphManager from "../../../santosh/GraphManager";

AnalyticsEndpoint.onInit(function (initialData, transport) {
    const agents = JSON.parse(initialData),
        factory = AgentChartFactories.get("AgentsMostUtilized"),
        drilldowns = ["LongestWaitingJobsOnAgent", "JobBuildTimeOnAgent"],
        container = document.getElementById("chart-container");

    const config = DrilldownSupport.withDrilldown(
        factory.config(agents),
        drilldowns,
        JobChartFactories,
        transport
    );

    // console.log("agents = ", agents);
    // H.chart(container, config);

    // const name = ['x', ...agents.map(a => a.agent_host_name)];
    // const data1 = ['Idle Duration Secs', ...agents.map(a => a.idle_duration_secs)];
    // new StackedBar(transport, drilldowns, name, data1, undefined, agents);

    // new StackedBar("AgentsMostUtilized", agents, drilldowns, transport);

    const graphManager = new GraphManager('series', transport);
    graphManager.initSeries('AgentsMostUtilized', agents);

});

AnalyticsEndpoint.ensure("v1");
