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
import PipelineChartFactories from "js/factories/pipeline-chart-factories.js";
import GraphManager from "../../santosh/GraphManager";
import moment from "../../lib/moment-humanize-for-gocd";
import drawChartStats from "./header";
import {auxiliaryMetrics} from "../../charts/pipelines";

console.log("#1 Santosh pipeline-instances-chart.js");

console.log("#1 initialData");

AnalyticsEndpoint.onInit(function (initialData, transport) {
    console.log('initialData = ', initialData);
    const data = JSON.parse(initialData), factory = PipelineChartFactories.get("PipelineBuildTime"),
        config = factory.config(data, transport), container = document.getElementById("chart-container");

    console.log("#1 Santosh AnalyticsEndpoint.onInit");
    console.log("data = ", data);
    console.log("factory = ", factory);
    console.log("config = ", config);
    console.log("container = ", container);

    console.log("#1 Santosh blocking H.chart for now");
    // H.chart(container, config);

    const instances = data.instances;
    const graph_data = [];
    instances.forEach((i) => {
        graph_data.push({
            total_time_secs: i.total_time_secs, total_waiting_time: i.total_waiting_time, scheduled_at: i.scheduled_at,
        });
    });

    console.log("#1 Santosh const instances =", instances);

    console.log("#1 Santosh instances map", instances.map((i) => i.total_waiting_time));

    const scheduled_at = [...instances.map((i) => i.scheduled_at)];
    const data1 = [...instances.map((i) => i.time_waiting_secs)];
    const data2 = [...instances.map((i) => i.total_time_secs)];

    // StackedArea.generate({
    //     text: "Text", subtext: 'Subtext'
    // }, ['Waiting time', 'Building time'], scheduled_at, [getAreaSeries('Waiting time', data1), getAreaSeries('Building time', data2)])


    const DATE_FMT = "YYYY-MM-DD";
    const TODAY = moment().format(DATE_FMT);

    const range = [
        {id: "30", text: "Last 30 Days", start: moment(TODAY).subtract(30, "days").format(DATE_FMT), selected: true},
        {id: "7", text: "Last 7 Days", start: moment(TODAY).subtract(7, "days").format(DATE_FMT), selected: false},
        {id: "1", text: "Last 24 Hours", start: moment(TODAY).subtract(1, "days").format(DATE_FMT), selected: false}
    ];

    const am = auxiliaryMetrics(instances);
    drawChartStats(range, null, am);
    const graphManager = new GraphManager('standalone', transport);
    graphManager.initStandalone('pipeline-instances', data);

});

AnalyticsEndpoint.ensure("v1");
